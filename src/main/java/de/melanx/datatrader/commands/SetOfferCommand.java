package de.melanx.datatrader.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.trader.Trader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;

public class SetOfferCommand {

    public static final SuggestionProvider<CommandSourceStack> OFFER_IDS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(DataTrader.getInstance().getOffers().getIds().stream().map(ResourceLocation::toString), builder);
    };

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(DataTrader.getInstance().modid)
                .then(SetOfferCommand.register())
        );
    }

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("setOffer").requires(source -> source.hasPermission(2))
                .then(Commands.argument("entity", EntityArgument.entity())
                        .then(Commands.argument("offerId", ResourceLocationArgument.id()).suggests(OFFER_IDS)
                                .executes(SetOfferCommand::setOffer)));
    }

    private static int setOffer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "entity");
        if (!(entity instanceof Trader trader)) {
            context.getSource().sendFailure(Component.translatable("command.datatrader.setoffer.wrong_entity"));
            return 0;
        }

        ResourceLocation offerId = ResourceLocationArgument.getId(context, "offerId");
        if (!DataTrader.getInstance().getOffers().getIds().contains(offerId)) {
            context.getSource().sendFailure(Component.translatable("command.datatrader.setoffer.wrong_id"));
            return 0;
        }

        trader.setOfferId(offerId);
        context.getSource().sendSuccess(Component.translatable("command.datatrader.setoffer.success", offerId), true);
        return 1;
    }
}
