package de.melanx.datatrader;

import com.google.common.collect.ImmutableSet;
import de.melanx.datatrader.trader.Trader;
import io.github.noeppi_noeppi.libx.annotation.registration.RegisterClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;

import javax.annotation.Nonnull;
import java.util.Optional;

@RegisterClass
public class ModRegistration {

    public static final EntityType<Trader> dataTrader = EntityType.Builder.of(Trader::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10).build(DataTrader.getInstance().modid + "_data_trader");
    public static final VillagerProfession trader = new VillagerProfession("trader", PoiType.UNEMPLOYED, ImmutableSet.of(), ImmutableSet.of(), null);
    public static final Item traderSpawnEgg = new ForgeSpawnEggItem(() -> ModRegistration.dataTrader, 0xEF6231, 0xBD8B72, new Item.Properties().tab(CreativeModeTab.TAB_MISC)) {

        @Nonnull
        @Override
        public Optional<Mob> spawnOffspringFromSpawnEgg(@Nonnull Player player, @Nonnull Mob parent, @Nonnull EntityType<? extends Mob> entityType, @Nonnull ServerLevel level, @Nonnull Vec3 pos, @Nonnull ItemStack stack) {
            if (!this.spawnsEntity(stack.getTag(), entityType)) {
                return Optional.empty();
            } else {
                Mob mob = entityType.create(level);
                if (mob == null) {
                    return Optional.empty();
                }
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(parent.blockPosition()), MobSpawnType.BREEDING, null, null);
                mob.moveTo(pos.x, pos.y, pos.z, 0, 0);
                level.addFreshEntityWithPassengers(mob);
                if (stack.hasCustomHoverName()) {
                    mob.setCustomName(stack.getHoverName());
                }

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                return Optional.of(mob);
            }
        }
    };
    public static final EntityDataSerializer<ResourceLocation> resourceLocation = new EntityDataSerializer<>() {
        @Override
        public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull ResourceLocation offerId) {
            buffer.writeResourceLocation(offerId);
        }

        @Nonnull
        @Override
        public ResourceLocation read(@Nonnull FriendlyByteBuf buffer) {
            return buffer.readResourceLocation();
        }

        @Nonnull
        @Override
        public ResourceLocation copy(ResourceLocation location) {
            return new ResourceLocation(location.getNamespace(), location.getPath());
        }
    };
}
