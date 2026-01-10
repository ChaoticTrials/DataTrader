package de.melanx.datatrader.registration;

import de.melanx.datatrader.DataTrader;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "CUSTOM_STAT")
public class ModStats {

    public static final ResourceLocation tradedWithTrader = DataTrader.getInstance().resource("traded_with_trader");
}
