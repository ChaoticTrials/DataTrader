package de.melanx.datatrader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class EventHandler {

    @SubscribeEvent
    public static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            for (Item item : List.of(ModItems.traderSpawnEgg, ModItems.newTraderSpawnEgg)) {
                ItemStack egg = new ItemStack(item);
                ItemStack noAi = egg.copy();
                CompoundTag entityTag = noAi.getOrCreateTagElement("EntityTag");
                entityTag.putBoolean("NoAI", true);
                event.accept(egg);
                event.accept(noAi);
            }
        }
    }

    @SubscribeEvent
    public void resourcesReload(AddReloadListenerEvent event) {
        event.addListener(DataTrader.getInstance().getLegacyOffers());
        event.addListener(DataTrader.getInstance().getOffers());
    }
}
