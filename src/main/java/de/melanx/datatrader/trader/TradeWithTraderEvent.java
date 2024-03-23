package de.melanx.datatrader.trader;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * This event is fired when a player trades with a {@link Trader}.
 *
 * <p>This event is not {@linkplain net.minecraftforge.eventbus.api.Cancelable cancellable}, and does not {@linkplain net.minecraftforge.eventbus.api.Event.HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain net.minecraftforge.common.MinecraftForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain net.minecraftforge.fml.LogicalSide#SERVER logical server}.</p>
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