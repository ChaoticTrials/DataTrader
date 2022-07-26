package de.melanx.datatrader;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public void resourcesReload(AddReloadListenerEvent event) {
        event.addListener(DataTrader.getInstance().getOffers());
    }
}
