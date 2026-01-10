package de.melanx.datatrader;

import de.melanx.datatrader.client.TraderScreen;
import de.melanx.datatrader.registration.ModEntities;
import de.melanx.datatrader.registration.ModItems;
import de.melanx.datatrader.trader.TraderMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.List;

@EventBusSubscriber(modid = "datatrader")
public class EventHandler {

    @SubscribeEvent
    public static void addToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            for (Item item : List.of(ModItems.traderSpawnEgg)) {
                ItemStack egg = new ItemStack(item);
                ItemStack noAi = egg.copy();
                CustomData.update(DataComponents.ENTITY_DATA, noAi, tag -> {
                    tag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.dataTrader).toString());
                    tag.putBoolean("NoAI", true);
                });
                event.accept(egg);
                event.accept(noAi);
            }
        }
    }

    @SubscribeEvent
    public static void resourcesReload(AddReloadListenerEvent event) {
        event.addListener(DataTrader.getInstance().getOffers());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TraderMenu.TYPE, TraderScreen::new);
    }
}
