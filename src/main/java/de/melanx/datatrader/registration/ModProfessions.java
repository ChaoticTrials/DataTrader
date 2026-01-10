package de.melanx.datatrader.registration;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "VILLAGER_PROFESSION")
public class ModProfessions {

    public static final VillagerProfession trader = new VillagerProfession("trader", PoiType.NONE, PoiType.NONE, ImmutableSet.of(), ImmutableSet.of(), SoundEvents.BEEHIVE_WORK);
}
