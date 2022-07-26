package de.melanx.datatrader;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import org.moddingx.libx.annotation.registration.RegisterClass;

import javax.annotation.Nonnull;

@RegisterClass(registry = "ENTITY_DATA_SERIALIZERS")
public class ModEntityDataSerializers {

    public static final EntityDataSerializer<ResourceLocation> resourceLocation = new EntityDataSerializer.ForValueType<>() {
        @Override
        public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull ResourceLocation offerId) {
            buffer.writeResourceLocation(offerId);
        }

        @Nonnull
        @Override
        public ResourceLocation read(@Nonnull FriendlyByteBuf buffer) {
            return buffer.readResourceLocation();
        }
    };
}
