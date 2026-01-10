package de.melanx.datatrader.registration;

import de.melanx.datatrader.trader.TraderMenu;
import org.moddingx.libx.annotation.registration.RegisterClass;
import org.moddingx.libx.menu.type.AdvancedMenuType;

@RegisterClass(registry = "MENU")
public class ModMenus {

    public static final AdvancedMenuType<TraderMenu, Integer> traderMenu = TraderMenu.TYPE;
}
