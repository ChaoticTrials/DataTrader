package de.melanx.datatrader.trader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TraderOffers extends ArrayList<TraderOffer> {

    public TraderOffers() {}

    private TraderOffers(int size) {
        super(size);
    }

    public TraderOffers(JsonArray json) {
        for (JsonElement element : json) {
            this.add(new TraderOffer(element.getAsJsonObject()));
        }
    }

    public TraderOffers(CompoundTag tag) {
        ListTag offers = tag.getList("Offers", Tag.TAG_COMPOUND);

        for (int i = 0; i < offers.size(); i++) {
            this.add(new TraderOffer(offers.getCompound(i)));
        }
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

    public void writeToStream(FriendlyByteBuf buffer) {
        buffer.writeCollection(this, (buf, offer) -> {
            offer.writeToStream(buf);
        });
    }

    public static TraderOffers createFromStream(FriendlyByteBuf buffer) {
        return buffer.readCollection(TraderOffers::new, TraderOffer::createFromStream);
    }

    public CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (int i = 0; i < this.size(); i++) {
            TraderOffer offer = this.get(i);
            list.add(offer.createTag());
        }

        tag.put("Offers", list);
        return tag;
    }
}
