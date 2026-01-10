package de.melanx.datatrader.registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.registration.RegisterClass;

@RegisterClass(registry = "ENTITY_DATA_SERIALIZERS")
public class ModEntityDataSerializers {

    public static final EntityDataSerializer<ResourceLocation> resourceLocation = (EntityDataSerializer.ForValueType<ResourceLocation>) () -> StreamCodec.of(FriendlyByteBuf::writeResourceLocation, RegistryFriendlyByteBuf::readResourceLocation);
}
