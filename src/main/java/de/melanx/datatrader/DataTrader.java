package de.melanx.datatrader;

import de.melanx.datatrader.commands.DataTraderCommands;
import de.melanx.datatrader.data.ItemModels;
import de.melanx.datatrader.network.TraderNetwork;
import de.melanx.datatrader.trader.DataTraderOffers;
import de.melanx.datatrader.trader.Trader;
import de.melanx.datatrader.trader.TraderRenderer;
import de.melanx.datatrader.trader.TraderScreen;
import de.melanx.datatrader.trader.legacy.LegacyDataMerchantOffers;
import de.melanx.datatrader.trader.legacy.LegacyTrader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("datatrader")
public final class DataTrader extends ModXRegistration {

    private static DataTrader instance;
    private final TraderNetwork network;
    private final LegacyDataMerchantOffers legacyOffers;
    private final DataTraderOffers offers;
    public final Logger logger = LoggerFactory.getLogger(DataTrader.class);

    public DataTrader() {
        instance = this;
        this.network = new TraderNetwork(this);
        this.legacyOffers = new LegacyDataMerchantOffers();
        this.offers = new DataTraderOffers();
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.addListener(DataTraderCommands::onRegisterCommands);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventHandler::addToTab);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(LegacyTrader::registerAttributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Trader::registerAttributes);

        DatagenSystem.create(this, system -> {
            system.addDataProvider(ItemModels::new);
        });
    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {
        // NO-OP
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        // NO-OP
    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.dataTrader, VillagerRenderer::new);
        EntityRenderers.register(ModEntities.newDataTrader, TraderRenderer::new);
        MenuScreens.register(ModMenus.traderMenu, TraderScreen::new);
    }

    public LegacyDataMerchantOffers getLegacyOffers() {
        return this.legacyOffers;
    }

    public DataTraderOffers getOffers() {
        return this.offers;
    }

    public static DataTrader getInstance() {
        return instance;
    }

    public static TraderNetwork getNetwork() {
        return instance.network;
    }
}
