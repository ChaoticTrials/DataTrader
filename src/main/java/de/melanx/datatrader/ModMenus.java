package de.melanx.datatrader;

import de.melanx.datatrader.trader.TraderMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "MENU")
public class ModMenus {

    public static final MenuType<TraderMenu> traderMenu = IForgeMenuType.create(TraderMenu::new);
}
