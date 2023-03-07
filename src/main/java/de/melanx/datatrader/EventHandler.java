package de.melanx.datatrader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHandler {

    @SubscribeEvent
    public void addToTab(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.SPAWN_EGGS) {
            ItemStack egg = new ItemStack(ModItems.traderSpawnEgg);
            ItemStack noAi = egg.copy();
            CompoundTag entityTag = noAi.getOrCreateTagElement("EntityTag");
            entityTag.putBoolean("NoAI", true);
            event.accept(egg);
            event.accept(noAi);
        }
    }

    @SubscribeEvent
    public void resourcesReload(AddReloadListenerEvent event) {
        event.addListener(DataTrader.getInstance().getOffers());
    }
}
