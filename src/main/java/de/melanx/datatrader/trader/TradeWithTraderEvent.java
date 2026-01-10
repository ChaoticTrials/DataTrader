package de.melanx.datatrader.trader;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * This event is fired when a player trades with a {@link Trader}.
 *
 * <p>This event is fired on the {@linkplain net.neoforged.neoforge.common.NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain net.neoforged.fml.LogicalSide#SERVER logical server}.</p>
 */
public class TradeWithTraderEvent extends PlayerEvent {

    private final TraderOffer offer;
    private final Trader trader;

    /**
     * Constructs a new TradeWithTraderEvent.
     *
     * @param player The player initiating the trade.
     * @param offer  The offer being traded.
     * @param trader The trader being interacted with.
     */
    public TradeWithTraderEvent(Player player, TraderOffer offer, Trader trader) {
        super(player);
        this.offer = offer;
        this.trader = trader;
    }

    /**
     * Gets the trader involved in the trade.
     *
     * @return The trader being interacted with.
     */
    public Trader getTrader() {
        return this.trader;
    }

    /**
     * Gets the offer being traded.
     *
     * @return The offer being traded.
     */
    public TraderOffer getOffer() {
        return this.offer;
    }
}