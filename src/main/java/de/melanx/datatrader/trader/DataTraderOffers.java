package de.melanx.datatrader.trader;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.moddingx.libx.datapack.DataLoader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class DataTraderOffers extends SimpleJsonResourceReloadListener {

    public static final Codec<TraderOffers> TRADER_OFFERS_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    TraderOffer.CODEC.listOf().fieldOf("Recipes").forGetter(offers -> offers.stream().toList())
            ).apply(instance, recipes -> {
                TraderOffers offers = new TraderOffers();
                offers.addAll(recipes);
                return offers;
            })
    );
    private static final Gson GSON = new GsonBuilder().create();
    private Map<ResourceLocation, TraderOffers> offers = ImmutableMap.of();

    public DataTraderOffers() {
        super(GSON, "trader_offers");
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler) {
        try {
            this.offers = DataLoader.loadJson(resourceManager, "trader_offers", (id, json) -> TraderOffers.CODEC.decode(RegistryOps.create(JsonOps.INSTANCE, this.getRegistryLookup()), json.getAsJsonObject().getAsJsonArray("Offers")).getOrThrow().getFirst());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TraderOffers getForId(ResourceLocation location) {
        return this.offers.get(location);
    }

    public Set<ResourceLocation> getIds() {
        return this.offers.keySet();
    }
}
