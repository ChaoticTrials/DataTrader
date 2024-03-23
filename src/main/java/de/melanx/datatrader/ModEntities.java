package de.melanx.datatrader;

import de.melanx.datatrader.trader.Trader;
import de.melanx.datatrader.trader.legacy.LegacyTrader;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ENTITY_TYPES")
public class ModEntities {

    public static final EntityType<LegacyTrader> dataTrader = EntityType.Builder.of(LegacyTrader::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10).build(DataTrader.getInstance().modid + "_data_trader");
    public static final EntityType<Trader> newDataTrader = EntityType.Builder.of(Trader::new, MobCategory.MISC).sized(0.6F, 1.95F).clientTrackingRange(10).build(DataTrader.getInstance().modid + "new__data_trader");
}
