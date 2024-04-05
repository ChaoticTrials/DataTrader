package de.melanx.datatrader;

import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

@RegisterConfig
public class TraderConfig {

    @Config({"If this is true, traders try to pickup an item to give the nearby player the first matching trade result.",
            "Only works for trades with just one cost."})
    public static boolean pickupItems = true;
}
