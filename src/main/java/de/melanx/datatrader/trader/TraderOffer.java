package de.melanx.datatrader.trader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nonnull;
import java.util.List;

public class TraderOffer {

    public static final Codec<TraderOffer> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    TraderOffer.ItemStackCollection.CODEC.fieldOf("buy").forGetter(TraderOffer::getCostA),
                    TraderOffer.ItemStackCollection.CODEC.optionalFieldOf("buyB", TraderOffer.ItemStackCollection.EMPTY).forGetter(TraderOffer::getCostB),
                    ItemStack.CODEC.fieldOf("sell").forGetter(TraderOffer::getResult),
                    Codec.BOOL.optionalFieldOf("rewardExp", false).forGetter(TraderOffer::rewardsExp),
                    Codec.INT.optionalFieldOf("xp", 0).forGetter(TraderOffer::getXp)
            ).apply(instance, TraderOffer::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TraderOffer> STREAM_CODEC = StreamCodec.of(
            (buffer, msg) -> {
                ItemStackCollection.STREAM_CODEC.encode(buffer, msg.getCostA());
                ItemStackCollection.STREAM_CODEC.encode(buffer, msg.getCostB());
                ItemStack.STREAM_CODEC.encode(buffer, msg.getResult());
                buffer.writeBoolean(msg.rewardsExp());
                buffer.writeInt(msg.getXp());
            }, buffer -> {
                ItemStackCollection costA = ItemStackCollection.STREAM_CODEC.decode(buffer);
                ItemStackCollection costB = ItemStackCollection.STREAM_CODEC.decode(buffer);
                ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
                boolean rewardExp = buffer.readBoolean();
                int xp = buffer.readInt();

                return new TraderOffer(costA, costB, result, rewardExp, xp);
            }
    );

    private final LazyValue<ItemStackCollection> costA;
    private final LazyValue<ItemStackCollection> costB;
    private final ItemStack result;
    private final boolean rewardExp;
    private final int xp;

    public TraderOffer(ItemStackCollection costA, ItemStackCollection costB, ItemStack result, boolean rewardExp, int xp) {
        this.costA = new LazyValue<>(() -> costA);
        this.costB = new LazyValue<>(() -> costB);
        this.result = result;
        this.rewardExp = rewardExp;
        this.xp = xp;
    }

    public ItemStackCollection getCostA() {
        return this.costA.get();
    }

    public ItemStackCollection getCostB() {
        return this.costB.get();
    }

    public ItemStack getResult() {
        return this.result.copy();
    }

    public boolean rewardsExp() {
        return this.rewardExp;
    }

    public int getXp() {
        return this.xp;
    }

    public ItemStack assemble() {
        return this.result.copy();
    }

    public boolean take(ItemStack playerOfferA, ItemStack playerOfferB) {
        if (!this.satisfiedBy(playerOfferA, playerOfferB)) {
            return false;
        }

        playerOfferA.shrink(this.getCostA().count);
        if (!this.getCostB().isEmpty()) {
            playerOfferB.shrink(this.getCostB().count);
        }

        return true;
    }

    public boolean satisfiedBy(ItemStack playerOfferA, ItemStack playerOfferB) {
        return this.isRequiredItem(playerOfferA, this.getCostA()) && playerOfferA.getCount() >= this.getCostA().count && this.isRequiredItem(playerOfferB, this.getCostB()) && playerOfferB.getCount() >= this.getCostB().count;
    }

    public boolean isRequiredItem(ItemStack offer, ItemStackCollection cost) {
        if (cost.isEmpty() && offer.isEmpty()) {
            return true;
        }

        ItemStack stack = offer.copy();
        if (stack.getItem().isDamageable(stack)) {
            stack.setDamageValue(stack.getDamageValue());
        }

        return cost.has(offer) && (!cost.hasComponents() || !stack.getComponents().isEmpty() && cost.components().test(stack));
    }

    public static class ItemStackCollection {

        public static final Codec<ItemStackCollection> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Ingredient.CODEC.fieldOf("items").forGetter(ItemStackCollection::getIngredient),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").forGetter(ItemStackCollection::getCount),
                        DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(ItemStackCollection::components)
                ).apply(instance, ItemStackCollection::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackCollection> STREAM_CODEC = StreamCodec.of(
                (buffer, msg) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, msg.ingredient);
                    buffer.writeInt(msg.count);
                    buffer.writeBoolean(msg.dataComponents != null);
                    if (msg.dataComponents != null) {
                        DataComponentPredicate.STREAM_CODEC.encode(buffer, msg.dataComponents);
                    }
                }, buffer -> new ItemStackCollection(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                        buffer.readInt(),
                        buffer.readBoolean() ? DataComponentPredicate.STREAM_CODEC.decode(buffer) : DataComponentPredicate.EMPTY
                )
        );

        private final Ingredient ingredient;
        private final int count;
        private final DataComponentPredicate dataComponents;

        ItemStackCollection(Ingredient ingredient, int count, @Nonnull DataComponentPredicate dataComponents) {
            this.ingredient = ingredient;
            this.count = count;
            this.dataComponents = dataComponents;
        }

        public ItemStackCollection(Ingredient ingredient, int count) {
            this(ingredient, count, DataComponentPredicate.EMPTY);
        }

        public static final ItemStackCollection EMPTY = new ItemStackCollection(Ingredient.EMPTY, 0);

        public Ingredient getIngredient() {
            return this.ingredient;
        }

        public List<ItemStack> getItems() {
            return List.of(this.ingredient.getItems());
        }

        public int getCount() {
            return this.count;
        }

        public boolean isEmpty() {
            return this == EMPTY || this.getItems().isEmpty() || this.count == 0 || this.getItems().stream().allMatch(ItemStack::isEmpty);
        }

        public boolean has(ItemStack stack) {
            return this.ingredient.test(stack) && stack.getCount() >= this.count;
        }

        public boolean hasComponents() {
            return !this.dataComponents.alwaysMatches();
        }

        public DataComponentPredicate components() {
            return this.dataComponents;
        }
    }
}
