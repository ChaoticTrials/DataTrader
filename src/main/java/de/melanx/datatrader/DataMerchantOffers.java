package de.melanx.datatrader;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public class DataMerchantOffers extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = Deserializers.createLootTableSerializer().create();
    private Map<ResourceLocation, MerchantOffers> offers = ImmutableMap.of();

    public DataMerchantOffers() {
        super(GSON, "merchant_offers");
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> object, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, MerchantOffers> builder = ImmutableMap.builder();

        object.forEach((location, json) -> {
            MerchantOffers offers = this.loadMerchantOffers(json);
            builder.put(location, offers);
        });

        this.offers = builder.build();
    }

    public MerchantOffers getForId(ResourceLocation location) {
        return this.offers.get(location);
    }

    public Set<ResourceLocation> getIds() {
        return this.offers.keySet();
    }

    public static JsonObject serialize(MerchantOffers offers) {
        JsonArray recipes = new JsonArray();
        for (MerchantOffer offer : offers) {
            JsonObject offerObj = new JsonObject();
            offerObj.add("buy", serializeItem(offer.getBaseCostA()));
            offerObj.add("buyB", serializeItem(offer.getCostB()));
            offerObj.add("sell", serializeItem(offer.getResult()));
            offerObj.addProperty("uses", offer.getUses());
            offerObj.addProperty("maxUses", offer.getMaxUses());
            offerObj.addProperty("rewardExp", offer.shouldRewardExp());
            offerObj.addProperty("xp", offer.getXp());
            offerObj.addProperty("priceMultiplier", offer.getPriceMultiplier());
            offerObj.addProperty("specialPrice", offer.getSpecialPriceDiff());
            offerObj.addProperty("demand", offer.getDemand());
            recipes.add(offerObj);
        }

        JsonObject json = new JsonObject();
        json.add("Recipes", recipes);

        return json;
    }

    private MerchantOffers loadMerchantOffers(JsonElement json) {
        MerchantOffers offers = new MerchantOffers();
        JsonArray recipes = json.getAsJsonObject().getAsJsonArray("Recipes");
        for (JsonElement r : recipes) {
            JsonObject recipe = r.getAsJsonObject();
            ItemStack baseCostA = CraftingHelper.getItemStack(recipe.get("buy").getAsJsonObject(), true);
            ItemStack costB;
            if (recipe.has("buyB")) {
                costB = CraftingHelper.getItemStack(recipe.get("buyB").getAsJsonObject(), true);
            } else {
                costB = ItemStack.EMPTY;
            }
            ItemStack result = CraftingHelper.getItemStack(recipe.get("sell").getAsJsonObject(), true);
            int uses = 0;
            if (recipe.has("uses")) {
                uses = recipe.get("uses").getAsInt();
            }

            int maxUses = 4;
            if (recipe.has("maxUses")) {
                maxUses = recipe.get("maxUses").getAsInt();
            }

            boolean rewardExp = false;
            if (recipe.has("rewardExp")) {
                rewardExp = recipe.get("rewardExp").getAsBoolean();
            }

            int xp = 0;
            if (recipe.has("xp")) {
                xp = recipe.get("xp").getAsInt();
            }

            float priceMultiplier = 0.0f;
            if (recipe.has("priceMultiplier")) {
                priceMultiplier = recipe.get("priceMultiplier").getAsFloat();
            }

            int specialPrice = 0;
            if (recipe.has("specialPrice")) {
                specialPrice = recipe.get("specialPrice").getAsInt();
            }

            int demand = 0;
            if (recipe.has("demand")) {
                demand = recipe.get("demand").getAsInt();
            }

            MerchantOffer offer = new MerchantOffer(baseCostA, costB, result, uses, maxUses, xp, priceMultiplier, demand);
            offer.rewardExp = rewardExp;
            offer.specialPriceDiff = specialPrice;
            offers.add(offer);
        }

        return offers;
    }

    public static JsonObject serializeItem(ItemStack stack) {
        JsonObject json = new JsonObject();
        CompoundTag tag = stack.serializeNBT();
        json.addProperty("item", tag.getString("id"));

        int count = tag.getInt("Count");
        if (count > 1) {
            json.addProperty("count", count);
        }

        if (tag.contains("tag")) {
            //noinspection ConstantConditions
            json.addProperty("nbt", tag.get("tag").toString());
        }

        if (tag.contains("ForgeCaps")) {
            //noinspection ConstantConditions
            json.addProperty("ForgeCaps", tag.get("ForgeCaps").toString());
        }

        return json;
    }
}
