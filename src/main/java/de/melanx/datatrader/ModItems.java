package de.melanx.datatrader;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.moddingx.libx.annotation.registration.RegisterClass;

import javax.annotation.Nonnull;
import java.util.Optional;

@RegisterClass(registry = "ITEMS")
public class ModItems {

    public static final Item traderSpawnEgg = new ForgeSpawnEggItem(() -> ModEntities.dataTrader, 0xEF6231, 0xBD8B72, new Item.Properties().tab(CreativeModeTab.TAB_MISC)) {

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
}
