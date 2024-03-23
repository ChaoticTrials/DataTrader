package de.melanx.datatrader.trader;

import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.ModEntities;
import de.melanx.datatrader.ModEntityDataSerializers;
import de.melanx.datatrader.ModItems;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRADER_OFFERS_ID, INTERNAL_OFFER);
    }

    public void setOfferId(ResourceLocation location) {
        if (this.offerId != location) {
            this.offerId = location;
        }

        this.entityData.set(DATA_TRADER_OFFERS_ID, location);
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

        OptionalInt optInt = player.openMenu(new SimpleMenuProvider((id, inv, player1) -> new TraderMenu(id, inv, this), Component.literal("Test")));
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

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@Nonnull ServerLevelAccessor level, @Nonnull DifficultyInstance difficulty, @Nonnull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        this.setOfferId(INTERNAL_OFFER);
        //noinspection OverrideOnly
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void pickUpItem(@Nonnull ItemEntity itemEntity) {
        // todo handle first matching trade
        super.pickUpItem(itemEntity);
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
