package de.melanx.datatrader;

import de.melanx.datatrader.commands.SetOfferCommand;
import de.melanx.datatrader.trader.Trader;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.moddingx.libx.mod.ModXRegistration;
import org.moddingx.libx.registration.RegistrationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("datatrader")
public final class DataTrader extends ModXRegistration {

    private static DataTrader instance;
    private final DataMerchantOffers offers;
    public final Logger logger = LoggerFactory.getLogger(DataTrader.class);

    public DataTrader() {
        instance = this;
        this.offers = new DataMerchantOffers();
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.addListener(SetOfferCommand::onRegisterCommands);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Trader::registerAttributes);
    }

    @Override
    protected void initRegistration(RegistrationBuilder builder) {

    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {

    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.dataTrader, VillagerRenderer::new);
    }

    public DataMerchantOffers getOffers() {
        return this.offers;
    }

    public static DataTrader getInstance() {
        return instance;
    }
}
