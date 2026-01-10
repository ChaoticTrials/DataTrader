package de.melanx.datatrader.registration;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.EventHooks;
import org.moddingx.libx.annotation.registration.RegisterClass;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@RegisterClass(registry = "ITEM")
public class ModItems {

    public static final Item traderSpawnEgg = new DeferredSpawnEggItem(() -> ModEntities.dataTrader, 0xEF6231, 0xBD8B72, new Item.Properties()) {

        @Override
        public void appendHoverText(ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
            if (stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY).copyTag().getBoolean("NoAI")) {
                tooltipComponents.add(Component.translatable("tooltip.datatrader.no_ai").withStyle(ChatFormatting.RED));
            }
        }

        @Nonnull
        @Override
        public Optional<Mob> spawnOffspringFromSpawnEgg(@Nonnull Player player, @Nonnull Mob parent, @Nonnull EntityType<? extends Mob> entityType, @Nonnull ServerLevel level, @Nonnull Vec3 pos, @Nonnull ItemStack stack) {
            if (!this.spawnsEntity(stack, entityType)) {
                return Optional.empty();
            }

            Mob mob = entityType.create(level);
            if (mob == null) {
                return Optional.empty();
            }
            EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(parent.blockPosition()), MobSpawnType.BREEDING, null);
            mob.moveTo(pos.x, pos.y, pos.z, 0, 0);
            level.addFreshEntityWithPassengers(mob);
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                mob.setCustomName(stack.getHoverName());
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            return Optional.of(mob);
        }
    };
}
