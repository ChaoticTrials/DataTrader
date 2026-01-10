package de.melanx.datatrader.ingredients;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.melanx.datatrader.registration.ModIngredientTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TaggedDataComponentIngredient implements ICustomIngredient {

    public static final MapCodec<TaggedDataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TaggedDataComponentIngredient::getTag),
                            DataComponentPredicate.CODEC.optionalFieldOf("components", DataComponentPredicate.EMPTY).forGetter(TaggedDataComponentIngredient::getComponents)
                    )
                    .apply(instance, TaggedDataComponentIngredient::new));

    private final TagKey<Item> tag;
    private final DataComponentPredicate components;

    private TaggedDataComponentIngredient(TagKey<Item> tag, DataComponentPredicate components) {
        this.tag = tag;
        this.components = components;
    }

    public TagKey<Item> getTag() {
        return this.tag;
    }

    public DataComponentPredicate getComponents() {
        return this.components;
    }

    @Override
    public boolean test(@Nonnull ItemStack stack) {
        return stack.is(this.tag) && this.components.test(stack);
    }

    @Nonnull
    @Override
    public Stream<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();

        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
            ItemStack stack = new ItemStack(item);
            if (!this.components.alwaysMatches()) {
                stack.applyComponents(this.components.asPatch());
            }

            items.add(stack);
        }

        if (items.isEmpty()) {
            throw new IllegalStateException("No items found for tag " + this.tag);
        }

        return items.stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Nonnull
    @Override
    public IngredientType<?> getType() {
        return ModIngredientTypes.tagWithComponents;
    }

    public static TaggedDataComponentIngredient of(TagKey<Item> tag) {
        return new TaggedDataComponentIngredient(tag, DataComponentPredicate.EMPTY);
    }

    public static TaggedDataComponentIngredient of(TagKey<Item> tag, DataComponentPredicate components) {
        return new TaggedDataComponentIngredient(tag, components);
    }
}
