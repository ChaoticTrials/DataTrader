package de.melanx.datatrader.client;

import de.melanx.datatrader.trader.Trade;
import de.melanx.datatrader.trader.TraderOffers;
import net.minecraft.world.entity.player.Player;

public class ClientSideTrader implements Trade {

    private final Player source;
    private TraderOffers offers = new TraderOffers();

    public ClientSideTrader(Player player) {
        this.source = player;
    }

    @Override
    public TraderOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(TraderOffers offers) {
        this.offers = offers;
    }

    @Override
    public boolean isClientSide() {
        return this.source.level().isClientSide;
    }
}
