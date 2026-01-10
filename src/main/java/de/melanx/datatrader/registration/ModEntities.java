package de.melanx.datatrader.registration;

import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.trader.Trader;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ENTITY_TYPE")
public class ModEntities {

    public static final EntityType<Trader> dataTrader = EntityType.Builder.of(Trader::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10).build(DataTrader.getInstance().modid + "data_trader");
}
