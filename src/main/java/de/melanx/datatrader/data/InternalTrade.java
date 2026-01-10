package de.melanx.datatrader.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.trader.TraderOffer;
import de.melanx.datatrader.trader.TraderOffers;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.Tags;
import org.moddingx.libx.datagen.DatagenContext;
import org.moddingx.libx.datagen.PackTarget;
import org.moddingx.libx.datagen.RegistrySet;
import org.moddingx.libx.mod.ModX;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class InternalTrade implements DataProvider {

    protected final ModX mod;
    protected final PackTarget packTarget;
    private final RegistrySet registries;
    private final Map<ResourceLocation, TraderOffers> traderOffers = new HashMap<>();

    public InternalTrade(DatagenContext context) {
        this.mod = context.mod();
        this.packTarget = context.target();
        this.registries = context.registries();
    }

    public void tradeOffers(ResourceLocation id, TraderOffers traderOffers) {
        this.traderOffers.put(id, traderOffers);
    }

    public void tradeOffers(String path, TraderOffers traderOffers) {
        this.traderOffers.put(this.mod.resource(path), traderOffers);
    }

    public void setup() {
        ResourceLocation internalId = DataTrader.getInstance().resource("internal");
        ItemStack diamondPickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        this.enchant(diamondPickaxe, Enchantments.EFFICIENCY, 2);
        this.enchant(diamondPickaxe, Enchantments.UNBREAKING, 10);
        TraderOffers traderOffers = TraderOffers.of(
                new TraderOffer(
                        new TraderOffer.ItemStackCollection(Ingredient.of(Items.WOODEN_PICKAXE), 1),
                        new TraderOffer.ItemStackCollection(Ingredient.of(Tags.Items.GEMS_DIAMOND), 3),
                        diamondPickaxe,
                        false,
                        0
                )
        );

        this.tradeOffers(internalId, traderOffers);
    }

    @Nonnull
    @Override
    public CompletableFuture<?> run(@Nonnull CachedOutput output) {
        this.setup();

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, this.registries.registryAccess());


        return CompletableFuture.allOf(this.traderOffers.entrySet().stream().map(entry -> {
            Optional<JsonElement> result = TraderOffers.CODEC.encodeStart(ops, entry.getValue()).result();
            if (result.isEmpty()) {
                throw new IllegalStateException("Failed to encode trader offers");
            }

            JsonObject json = new JsonObject();
            json.add("Offers", result.get());

            ResourceLocation id = entry.getKey();
            Path path = this.packTarget.path(PackType.SERVER_DATA).resolve(id.getNamespace()).resolve("trader_offers").resolve(id.getPath() + ".json");

            return DataProvider.saveStable(output, json, path);
        }).toArray(CompletableFuture[]::new));
    }

    public void enchant(ItemStack stack, ResourceKey<Enchantment> enchantment, int level) {
        Registry<Enchantment> enchantmentRegistry = this.registries.registry(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> enchantmentReference = enchantmentRegistry.getHolderOrThrow(enchantment);

        stack.enchant(enchantmentReference, level);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Internal Trader Offers";
    }
}
