package de.melanx.datatrader.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.ModEntities;
import de.melanx.datatrader.trader.Trader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("summon").requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", Vec3Argument.vec3())
                        .then(Commands.argument("offerId", ResourceLocationArgument.id()).suggests(DataTraderCommands.OFFER_IDS)
                                .executes(context -> SummonCommand.summon(context, false))
                                .then(Commands.argument("NoAI", BoolArgumentType.bool())
                                        .executes(context -> SummonCommand.summon(context, BoolArgumentType.getBool(context, "NoAI"))))));
    }

    private static int summon(CommandContext<CommandSourceStack> context, boolean noai) {
        Vec3 pos = Vec3Argument.getVec3(context, "pos");
        ResourceLocation offerId = ResourceLocationArgument.getId(context, "offerId");

        if (!DataTrader.getInstance().getOffers().getIds().contains(offerId)) {
            context.getSource().sendFailure(Component.translatable("command.datatrader.setoffer.wrong_id"));
            return 0;
        }

        ServerLevel level = context.getSource().getLevel();
        Trader trader = ModEntities.dataTrader.create(level);
        //noinspection DataFlowIssue
        trader.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(pos.x, pos.y, pos.z)), MobSpawnType.BREEDING, null, null);
        trader.moveTo(pos.x, pos.y, pos.z, 330, 0);
        trader.setNoAi(noai);
        level.addFreshEntity(trader);
        trader.setOfferId(offerId);

        context.getSource().sendSuccess(() -> Component.translatable("command.datatrader.setoffer.success", offerId), true);
        return 1;
    }
}
