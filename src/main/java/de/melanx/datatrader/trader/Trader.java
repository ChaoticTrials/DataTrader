package de.melanx.datatrader.trader;

import com.google.common.collect.ImmutableList;
import de.melanx.datatrader.*;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class Trader extends PathfinderMob implements Npc, Trade {

    private static final ResourceLocation INTERNAL_OFFER = DataTrader.getInstance().resource("internal");
    private static final EntityDataAccessor<ResourceLocation> DATA_TRADER_OFFERS_ID = SynchedEntityData.defineId(Trader.class, ModEntityDataSerializers.resourceLocation);
    private ResourceLocation offerId;
    private TraderOffers offers;

    public Trader(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.newDataTrader, Villager.createAttributes().build());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SetTargetLookingAtPlayerGoal(this, Player.class, 8, 1));
    }

    @Override
    public boolean isEffectiveAi() {
        return !this.level().isClientSide;
    }

    @Nonnull
    @Override
    public Brain<Trader> getBrain() {
        //noinspection unchecked
        return (Brain<Trader>) super.getBrain();
    }

    @Nonnull
    @Override
    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(
                ImmutableList.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                ImmutableList.of(SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS)
        );
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("traderBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();

        this.level().getProfiler().push("traderShowingItem");
        if (this.getTarget() instanceof Player player) {
            Optional<TraderOffer> offerOptional = this.getMatchingOffer(player.getMainHandItem(), ItemStack.EMPTY);
            if (offerOptional.isPresent()) {
                TraderOffer offer = offerOptional.get();
                this.setItemInHand(InteractionHand.MAIN_HAND, offer.assemble());
            } else {
                this.removeHoldItem();
            }
        } else {
            this.removeHoldItem();
        }
        this.level().getProfiler().pop();
    }

    private void removeHoldItem() {
        if (!this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return TraderConfig.pickupItems;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRADER_OFFERS_ID, INTERNAL_OFFER);
    }

    public void setOfferId(ResourceLocation location) {
        if (this.offerId != location) {
            this.offerId = location;
        }

        this.entityData.set(DATA_TRADER_OFFERS_ID, location);
        this.updateTrades();
    }

    public ResourceLocation getOfferId() {
        return this.entityData.get(DATA_TRADER_OFFERS_ID);
    }

    @Override
    public TraderOffers getOffers() {
        if (this.offers == null) {
            if (this.offerId == null) {
                return new TraderOffers();
            }

            this.updateTrades();
        }

        return this.offers == null ? new TraderOffers() : this.offers;
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    public void updateTrades() {
        this.offers = DataTrader.getInstance().getOffers().getForId(this.offerId);
    }

    @Override
    public void notifyTrade(Player player, TraderOffer offer) {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.rewardTradeXp(offer);
        if (player instanceof ServerPlayer serverPlayer) {
//            ModCriteriaTriggers.TRADE.trigger(serverPlayer, this, offer.getResult()); // todo own advancement trigger
        }

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new TradeWithTraderEvent(player, offer, this));
    }

    private void rewardTradeXp(TraderOffer offer) {
        if (!this.level().isClientSide) {
            ExperienceOrb orb = EntityType.EXPERIENCE_ORB.create(this.level());
            if (orb == null) {
                return;
            }

            orb.value = offer.getXp();
            this.level().addFreshEntity(orb);
        }
    }

    @Nonnull
    @Override
    protected InteractionResult mobInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModItems.traderSpawnEgg) || !this.isAlive() || player.isSecondaryUseActive()) {
            return super.mobInteract(player, hand);
        }

        //noinspection DataFlowIssue
        OptionalInt optInt = player.openMenu(new SimpleMenuProvider((id, inv, player1) -> new TraderMenu(id, inv, this), this.hasCustomName() ? this.getCustomName() : this.getDisplayName()));
        if (optInt.isPresent()) {
            TraderOffers offers = this.getOffers();
            if (!offers.isEmpty()) {
                TraderMenu menu = (TraderMenu) player.containerMenu;
                menu.setOffers(DataTrader.getInstance().getOffers().getForId(this.getOfferId()));
                DataTrader.getNetwork().syncTrades(player, menu.containerId, offers);
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@Nonnull ServerLevelAccessor level, @Nonnull DifficultyInstance difficulty, @Nonnull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        this.setOfferId(INTERNAL_OFFER);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void pickUpItem(@Nonnull ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        Optional<TraderOffer> offerOptional = this.getMatchingOffer(stack, ItemStack.EMPTY);
        if (offerOptional.isPresent()) {
            TraderOffer offer = offerOptional.get();
            int count = offer.getCostA().getCount();
            stack.shrink(count);
            if (stack.isEmpty()) {
                itemEntity.discard();
            }

            Trader.throwItems(this, List.of(offer.assemble()));
        }
    }

    public Optional<TraderOffer> getMatchingOffer(ItemStack stackA, ItemStack stackB) {
        return Optional.ofNullable(this.getOffers().getTradeFor(stackA, stackB, -1));
    }

    private static void throwItems(Trader trader, List<ItemStack> stacks) {
        Optional<Player> player = trader.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        if (player.isPresent()) {
            Trader.throwItemsTowardPlayer(trader, player.get(), stacks);
        } else {
            Trader.throwItemsTowardRandomPos(trader, stacks);
        }
    }

    private static void throwItemsTowardRandomPos(Trader trader, List<ItemStack> stacks) {
        Trader.throwItemsTowardPos(trader, stacks, Trader.getRandomNearbyPos(trader));
    }

    private static void throwItemsTowardPlayer(Trader trader, Player player, List<ItemStack> stacks) {
        Trader.throwItemsTowardPos(trader, stacks, player.position());
    }

    private static void throwItemsTowardPos(Trader trader, List<ItemStack> stacks, Vec3 pos) {
        if (!stacks.isEmpty()) {
            trader.swing(InteractionHand.OFF_HAND);

            for (ItemStack stack : stacks) {
                BehaviorUtils.throwItem(trader, stack, pos.add(0.0D, 1.0D, 0.0D));
            }
        }

    }

    private static Vec3 getRandomNearbyPos(Trader trader) {
        Vec3 pos = LandRandomPos.getPos(trader, 4, 2);
        return pos == null ? trader.position() : pos;
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.offerId != null) {
            tag.putString("OfferId", this.offerId.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        try {
            this.offerId = new ResourceLocation(tag.getString("OfferId"));
        } catch (ResourceLocationException e) {
            this.offerId = null;
        }
    }
}
