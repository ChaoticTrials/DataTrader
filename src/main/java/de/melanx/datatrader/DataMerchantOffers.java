package de.melanx.datatrader;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.loot.Deserializers;
import org.moddingx.libx.codec.MoreCodecs;
import org.moddingx.libx.datapack.DataLoader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class DataMerchantOffers extends SimpleJsonResourceReloadListener {

    public static final Codec<MerchantOffer> MERCHANT_OFFER_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MoreCodecs.SAFE_ITEM_STACK.fieldOf("buy").forGetter(MerchantOffer::getBaseCostA),
                    MoreCodecs.SAFE_ITEM_STACK.fieldOf("buyB").orElse(ItemStack.EMPTY).forGetter(MerchantOffer::getCostB),
                    MoreCodecs.SAFE_ITEM_STACK.fieldOf("sell").forGetter(MerchantOffer::getResult),
                    Codec.INT.fieldOf("uses").orElse(0).forGetter(MerchantOffer::getUses),
                    Codec.INT.fieldOf("maxUses").orElse(4).forGetter(MerchantOffer::getMaxUses),
                    Codec.BOOL.fieldOf("rewardExp").orElse(false).forGetter(MerchantOffer::shouldRewardExp),
                    Codec.INT.fieldOf("xp").orElse(0).forGetter(MerchantOffer::getXp),
                    Codec.FLOAT.fieldOf("priceMultiplier").orElse(0.0f).forGetter(MerchantOffer::getPriceMultiplier),
                    Codec.INT.fieldOf("specialPrice").orElse(0).forGetter(MerchantOffer::getSpecialPriceDiff),
                    Codec.INT.fieldOf("demand").orElse(0).forGetter(MerchantOffer::getDemand)
            ).apply(instance, (baseCostA, costB, result, uses, maxUses, rewardExp, xp, priceMultiplier, specialPrice, demand) -> {
                MerchantOffer offer = new MerchantOffer(baseCostA, costB, result, uses, maxUses, xp, priceMultiplier, demand);
                offer.rewardExp = rewardExp;
                offer.specialPriceDiff = specialPrice;
                return offer;
            }));
    public static final Codec<MerchantOffers> MERCHANT_OFFERS_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    MERCHANT_OFFER_CODEC.listOf().fieldOf("Recipes").forGetter(offers -> offers.stream().toList())
            ).apply(instance, recipes -> {
                MerchantOffers offers = new MerchantOffers();
                offers.addAll(recipes);
                return offers;
            })
    );
    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private Map<ResourceLocation, MerchantOffers> offers = ImmutableMap.of();

    public DataMerchantOffers() {
        super(GSON, "merchant_offers");
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler) {
        try {
            this.offers = DataLoader.loadJson(resourceManager, "merchant_offers", MERCHANT_OFFERS_CODEC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MerchantOffers getForId(ResourceLocation location) {
        return this.offers.get(location);
    }

    public Set<ResourceLocation> getIds() {
        return this.offers.keySet();
    }
}
