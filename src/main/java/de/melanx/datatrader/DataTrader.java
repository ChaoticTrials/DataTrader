package de.melanx.datatrader;

import de.melanx.datatrader.client.TraderRenderer;
import de.melanx.datatrader.commands.DataTraderCommands;
import de.melanx.datatrader.data.InternalTrade;
import de.melanx.datatrader.data.ItemModels;
import de.melanx.datatrader.network.TraderNetwork;
import de.melanx.datatrader.registration.ModEntities;
import de.melanx.datatrader.trader.DataTraderOffers;
import de.melanx.datatrader.trader.Trader;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.moddingx.libx.datagen.DatagenSystem;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("datatrader")
public final class DataTrader extends ModXRegistration {

    private static DataTrader instance;
    private final TraderNetwork network;
    private final DataTraderOffers offers;
    public final Logger logger = LoggerFactory.getLogger(DataTrader.class);

    public DataTrader(IEventBus modBus) {
        instance = this;
        this.network = new TraderNetwork(this);
        this.offers = new DataTraderOffers();
        NeoForge.EVENT_BUS.addListener(DataTraderCommands::onRegisterCommands);

        modBus.addListener(Trader::registerAttributes);

        DatagenSystem.create(this, system -> {
            system.addDataProvider(ItemModels::new);
            system.addDataProvider(InternalTrade::new);
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
        EntityRenderers.register(ModEntities.dataTrader, TraderRenderer::new);
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