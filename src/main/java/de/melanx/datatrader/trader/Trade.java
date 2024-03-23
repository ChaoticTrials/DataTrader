package de.melanx.datatrader.trader;

import net.minecraft.world.entity.player.Player;

public interface Trade {

    TraderOffers getOffers();

    boolean isClientSide();

    default void overrideOffers(TraderOffers offers) {}

    default void notifyTrade(Player player, TraderOffer offer) {}
}
