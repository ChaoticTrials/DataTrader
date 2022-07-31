package de.melanx.datatrader.trader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.ModRegistration;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class Trader extends Villager {

    private static final EntityDataAccessor<ResourceLocation> DATA_MERCHANT_OFFERS_ID = SynchedEntityData.defineId(Trader.class, ModRegistration.resourceLocation);
    private ResourceLocation offerId;

    public Trader(EntityType<? extends Villager> entityType, Level level) {
        super(entityType, level);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModRegistration.dataTrader, Villager.createAttributes().build());
    }

    public static ImmutableList<Pair<Integer, ? extends Behavior<? super Villager>>> getCorePackage(VillagerProfession profession, float speedModifier) {
        //noinspection rawtypes
        return ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                Pair.of(0, new InteractWithDoor()),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new VillagerPanicTrigger()),
                Pair.of(0, new WakeUp()),
                Pair.of(0, new ReactToBell()),
                Pair.of(0, new SetRaidStatus()),
                Pair.of(0, new ValidateNearbyPoi(profession.getJobPoiType(), MemoryModuleType.JOB_SITE)),
                Pair.of(0, new ValidateNearbyPoi(profession.getJobPoiType(), MemoryModuleType.POTENTIAL_JOB_SITE)),
                Pair.of(1, new MoveToTargetSink()),
                Pair.of(2, new PoiCompetitorScan(profession)),
                Pair.of(3, new LookAndFollowTradingPlayerSink(speedModifier)),
                Pair.of(5, new GoToWantedItem(speedModifier, false, 4)),
                Pair.of(6, new AcquirePoi(profession.getJobPoiType(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())),
                Pair.of(7, new GoToPotentialJobSite(speedModifier)),
                Pair.of(8, new YieldJobSite(speedModifier)),
                Pair.of(10, new AcquirePoi(PoiType.HOME, MemoryModuleType.HOME, false, Optional.of((byte) 14))),
                Pair.of(10, new AcquirePoi(PoiType.MEETING, MemoryModuleType.MEETING_POINT, true, Optional.of((byte) 14))),
                Pair.of(10, new AssignProfessionFromJobSite())
        );
    }

    @Nonnull
    @Override
    public MerchantOffers getOffers() {
        if (this.offers == null) {
            if (this.offerId == null) {
                return new MerchantOffers();
            }

            this.updateTrades();
        }

        return this.offers == null ? new MerchantOffers() : this.offers;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public void setBaby(boolean p_146756_) {
        this.setAge(0);
    }

    @Override
    protected void updateTrades() {
        this.offers = DataTrader.getInstance().getOffers().getForId(this.offerId);
        if (this.offers == null) {
            DataTrader.getInstance().logger.warn("Location for trades is wrong for trader {} with trade id {}", this.uuid, this.offerId);
        }
    }

    @Override
    protected void customServerAiStep() {
        this.level.getProfiler().push("villagerBrain");
        this.getBrain().tick((ServerLevel) this.level, this);
        this.level.getProfiler().pop();

        if (!this.isTrading() && this.updateMerchantTimer > 0) {
            --this.updateMerchantTimer;
            if (this.updateMerchantTimer <= 0) {
                if (this.increaseProfessionLevelOnUpdate) {
                    this.increaseMerchantCareer();
                    this.increaseProfessionLevelOnUpdate = false;
                }

                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            }
        }

        if (this.lastTradedPlayer != null && this.level instanceof ServerLevel) {
            ((ServerLevel) this.level).onReputationEvent(ReputationEventType.TRADE, this.lastTradedPlayer, this);
            this.level.broadcastEntityEvent(this, EntityEvent.VILLAGER_HAPPY);
            this.lastTradedPlayer = null;
        }

        if (!this.isNoAi() && this.random.nextInt(100) == 0) {
            Raid raid = ((ServerLevel) this.level).getRaidAt(this.blockPosition());
            if (raid != null && raid.isActive() && !raid.isOver()) {
                this.level.broadcastEntityEvent(this, EntityEvent.VILLAGER_SWEAT);
            }
        }
    }

    // [Vanilla copy]
    @Override
    protected void registerBrainGoals(@Nonnull Brain<Villager> brain) {
        VillagerProfession profession = this.getVillagerData().getProfession();
        brain.setSchedule(Schedule.VILLAGER_DEFAULT);
        brain.addActivityWithConditions(Activity.WORK, VillagerGoalPackages.getWorkPackage(profession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT)));

        brain.addActivity(Activity.CORE, Trader.getCorePackage(profession, 0.5F));
        brain.addActivityWithConditions(Activity.MEET, VillagerGoalPackages.getMeetPackage(profession, 0.5F), ImmutableSet.of(Pair.of(MemoryModuleType.MEETING_POINT, MemoryStatus.VALUE_PRESENT)));
        brain.addActivity(Activity.REST, VillagerGoalPackages.getRestPackage(profession, 0.5F));
        brain.addActivity(Activity.IDLE, VillagerGoalPackages.getIdlePackage(profession, 0.5F));
        brain.addActivity(Activity.PANIC, VillagerGoalPackages.getPanicPackage(profession, 0.5F));
        brain.addActivity(Activity.PRE_RAID, VillagerGoalPackages.getPreRaidPackage(profession, 0.5F));
        brain.addActivity(Activity.RAID, VillagerGoalPackages.getRaidPackage(profession, 0.5F));
        brain.addActivity(Activity.HIDE, VillagerGoalPackages.getHidePackage(profession, 0.5F));
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
        brain.updateActivityFromSchedule(this.level.getDayTime(), this.level.getGameTime());
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@Nonnull ServerLevelAccessor level, @Nonnull DifficultyInstance difficulty, @Nonnull MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
        this.setVillagerData(this.getVillagerData().setProfession(ModRegistration.trader).setLevel(10));
        return spawnGroupData;
    }


    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_LIVING_ENTITY_FLAGS, (byte) 0);
        this.entityData.define(DATA_EFFECT_COLOR_ID, 0);
        this.entityData.define(DATA_EFFECT_AMBIENCE_ID, false);
        this.entityData.define(DATA_ARROW_COUNT_ID, 0);
        this.entityData.define(DATA_STINGER_COUNT_ID, 0);
        this.entityData.define(DATA_HEALTH_ID, 1.0F);
        this.entityData.define(SLEEPING_POS_ID, Optional.empty());
        this.entityData.define(DATA_MOB_FLAGS_ID, (byte) 0);
        this.entityData.define(DATA_BABY_ID, false);
        this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
        this.entityData.define(DATA_VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, ModRegistration.trader, 10));
        this.entityData.define(DATA_MERCHANT_OFFERS_ID, DataTrader.getInstance().resource("internal"));
    }

    public void setOfferId(ResourceLocation location) {
        if (this.offerId != location) {
            this.offerId = location;
        }

        this.entityData.set(DATA_MERCHANT_OFFERS_ID, location);
    }

    public ResourceLocation getOfferId() {
        return this.entityData.get(DATA_MERCHANT_OFFERS_ID);
    }

    @Override
    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (tag.contains("Offers")) {
            tag.remove("Offers");
        }

        if (this.offerId != null) {
            tag.putString("OfferId", this.offerId.toString());
        }
    }

    @Override
    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        if (tag.contains("Offers")) { // TODO remove in next major update
            tag.remove("Offers");
        }

        super.readAdditionalSaveData(tag);
        try {
            this.offerId = new ResourceLocation(tag.getString("OfferId"));
        } catch (ResourceLocationException e) {
            this.offerId = null;
        }
    }
}
