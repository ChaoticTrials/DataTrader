package de.melanx.datatrader.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.melanx.datatrader.DataTrader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;

public class DataTraderCommands {

    public static final SuggestionProvider<CommandSourceStack> OFFER_IDS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(DataTrader.getInstance().getOffers().getIds().stream().map(ResourceLocation::toString), builder);
    };

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(DataTrader.getInstance().modid)
                .then(SummonCommand.register())
                .then(SetOfferCommand.register())
        );
    }
}
