package de.melanx.datatrader.trader;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TraderOffers extends ArrayList<TraderOffer> {

    public static final Codec<TraderOffers> CODEC = new Codec<>() {

        @Override
        public <T> DataResult<T> encode(TraderOffers input, DynamicOps<T> ops, T prefix) {
            return TraderOffer.CODEC.listOf().encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<TraderOffers, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Pair<List<TraderOffer>, T>> decode = TraderOffer.CODEC.listOf().decode(ops, input);

            return decode.map(pair -> Pair.of(TraderOffers.of(pair.getFirst()), pair.getSecond()));
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, TraderOffers> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeVarInt(value.size());
                for (TraderOffer offer : value) {
                    TraderOffer.STREAM_CODEC.encode(buffer, offer);
                }
            }, buffer -> {
                int size = buffer.readVarInt();
                TraderOffers offers = new TraderOffers(size);

                for (int i = 0; i < size; i++) {
                    offers.add(TraderOffer.STREAM_CODEC.decode(buffer));
                }

                return offers;
            }
    );

    public TraderOffers() {}

    private TraderOffers(int size) {
        super(size);
    }

    @Nullable
    public TraderOffer getTradeFor(ItemStack stackA, ItemStack stackB, int index) {
        if (index > 0 && index < this.size()) {
            TraderOffer offer = this.get(index);
            return offer.satisfiedBy(stackA, stackB) ? offer : null;
        }

        for (TraderOffer offer : this) {
            if (offer.satisfiedBy(stackA, stackB)) {
                return offer;
            }
        }

        return null;
    }

    public static TraderOffers of(TraderOffer offer) {
        return TraderOffers.of(List.of(offer));
    }

    public static TraderOffers of(Collection<TraderOffer> offers) {
        TraderOffers traderOffers = new TraderOffers(offers.size());

        traderOffers.addAll(offers);

        return traderOffers;
    }
}
