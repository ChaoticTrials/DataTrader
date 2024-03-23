package de.melanx.datatrader.trader;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.moddingx.libx.codec.MoreCodecs;
import org.moddingx.libx.util.lazy.LazyValue;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TraderOffer {

    public static final Codec<TraderOffer> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    TraderOffer.ItemStackCollection.CODEC.fieldOf("buy").forGetter(TraderOffer::getCostA),
                    TraderOffer.ItemStackCollection.CODEC.fieldOf("buyB").orElse(TraderOffer.ItemStackCollection.EMPTY).forGetter(TraderOffer::getCostB),
                    MoreCodecs.SAFE_ITEM_STACK.fieldOf("sell").forGetter(TraderOffer::getResult),
                    Codec.BOOL.fieldOf("rewardExp").orElse(false).forGetter(TraderOffer::rewardsExp),
                    Codec.INT.fieldOf("xp").orElse(0).forGetter(TraderOffer::getXp)
            ).apply(instance, TraderOffer::new));

    private final LazyValue<ItemStackCollection> costA;
    private final LazyValue<ItemStackCollection> costB;
    private final ItemStack result;
    private boolean rewardExp = true;
    private int xp = 1;

    public TraderOffer(CompoundTag compoundTag) {
        this.costA = new LazyValue<>(() -> TraderOffer.getItemStackCollection("buy", compoundTag));
        this.costB = new LazyValue<>(() -> TraderOffer.getItemStackCollection("buyB", compoundTag));
        this.result = TraderOffer.getItemStack(compoundTag);
        if (compoundTag.contains("rewardExp", Tag.TAG_BYTE)) {
            this.rewardExp = compoundTag.getBoolean("rewardExp");
        }
        if (compoundTag.contains("xp", Tag.TAG_INT)) {
            this.xp = compoundTag.getInt("xp");
        }
    }

    public TraderOffer(JsonObject json) {
        this.costA = new LazyValue<>(() -> TraderOffer.getItemStackCollection("buy", json));
        this.costB = new LazyValue<>(() -> json.has("buyB") ? TraderOffer.getItemStackCollection("buyB", json) : ItemStackCollection.EMPTY);
        this.result = CraftingHelper.getItemStack(json.getAsJsonObject("sell"), true);
        if (json.has("rewardExp")) {
            this.rewardExp = GsonHelper.getAsBoolean(json, "rewardExp", true);
        }
        if (json.has("xp")) {
            this.xp = GsonHelper.getAsInt(json, "xp", 1);
        }
    }

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

    public void writeToStream(FriendlyByteBuf buf) {
        if (this.getCostA().isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeInt(this.getCostA().items.size());
            for (ItemStack item : this.getCostA().items) {
                //noinspection deprecation
                buf.writeId(BuiltInRegistries.ITEM, item.getItem());
            }
            buf.writeInt(this.getCostA().count);
            buf.writeNbt(this.getCostA().tag);
        }

        if (this.getCostB().isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeInt(this.getCostB().items.size());
            for (ItemStack item : this.getCostB().items) {
                //noinspection deprecation
                buf.writeId(BuiltInRegistries.ITEM, item.getItem());
            }
            buf.writeInt(this.getCostB().count);
            buf.writeNbt(this.getCostB().tag);
        }

        buf.writeItem(this.result);
        buf.writeBoolean(this.rewardExp);
        buf.writeInt(this.xp);
    }

    public static TraderOffer createFromStream(FriendlyByteBuf buf) {
        ItemStackCollection costA;
        if (buf.readBoolean()) {
            int size = buf.readInt();
            Set<ItemStack> items = new HashSet<>();
            for (int i = 0; i < size; i++) {
                //noinspection deprecation,DataFlowIssue
                items.add(new ItemStack(buf.readById(BuiltInRegistries.ITEM)));
            }

            costA = new ItemStackCollection(items, buf.readInt(), buf.readNbt());
        } else {
            costA = ItemStackCollection.EMPTY;
        }

        ItemStackCollection costB;
        if (buf.readBoolean()) {
            int size = buf.readInt();
            Set<ItemStack> items = new HashSet<>();
            for (int i = 0; i < size; i++) {
                //noinspection deprecation,DataFlowIssue
                items.add(new ItemStack(buf.readById(BuiltInRegistries.ITEM)));
            }

            costB = new ItemStackCollection(items, buf.readInt(), buf.readNbt());
        } else {
            costB = ItemStackCollection.EMPTY;
        }

        ItemStack result = buf.readItem();
        boolean rewardExp = buf.readBoolean();
        int xp = buf.readInt();

        return new TraderOffer(costA, costB, result, rewardExp, xp);
    }

    public CompoundTag createTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("buy", this.getCostA().save(new CompoundTag()));
        tag.put("buyB", this.getCostB().save(new CompoundTag()));
        tag.put("sell", this.result.save(new CompoundTag()));
        tag.putBoolean("rewardExp", this.rewardExp);
        tag.putInt("xp", this.xp);
        return tag;
    }

    public boolean satisfiedBy(ItemStack playerOfferA, ItemStack playerOfferB) {
        return this.isRequiredItem(playerOfferA, this.getCostA()) && playerOfferA.getCount() >= this.getCostA().count && this.isRequiredItem(playerOfferB, this.getCostB()) && playerOfferB.getCount() >= this.getCostB().count;
    }

    public boolean isRequiredItem(ItemStack offer, ItemStackCollection cost) {
        if (cost.isEmpty() && offer.isEmpty()) {
            return true;
        } else {
            ItemStack stack = offer.copy();
            if (stack.getItem().isDamageable(stack)) {
                stack.setDamageValue(stack.getDamageValue());
            }

            return cost.has(offer) && (!cost.hasTag() || stack.hasTag() && NbtUtils.compareNbt(cost.getTag(), stack.getTag(), false));
        }
    }

    private static ItemStack getItemStack(CompoundTag tag) {
        if (!tag.contains("sell")) {
            throw new IllegalStateException("Missing result item");
        }

        if (tag.contains("sell", Tag.TAG_STRING)) {
            Holder.Reference<Item> item = ForgeRegistries.ITEMS.getDelegateOrThrow(new ResourceLocation(tag.getString("sell")));
            return new ItemStack(item);
        }

        if (tag.contains("sell", Tag.TAG_COMPOUND)) {
            CompoundTag sell = tag.getCompound("sell");
            Holder.Reference<Item> item = ForgeRegistries.ITEMS.getDelegateOrThrow(new ResourceLocation(sell.getString("id")));
            int count = 1;
            if (sell.contains("Count", Tag.TAG_BYTE)) {
                count = sell.getInt("Count");
            }

            CompoundTag nbt = new CompoundTag();
            if (sell.contains("tag", Tag.TAG_COMPOUND)) {
                nbt = sell.getCompound("tag");
            }

            ItemStack stack = new ItemStack(item, count);
            stack.setTag(nbt);
            return stack;
        }

        throw new IllegalStateException("Item definition must be a string or a compound.");
    }

    private static ItemStackCollection getItemStackCollection(String key, JsonObject json) {
        if (!json.has(key) || !json.get(key).isJsonObject()) {
            throw new IllegalStateException("Item definition needs to be an object");
        }

        JsonObject itemObj = json.getAsJsonObject(key);
        Set<ItemStack> items = new HashSet<>();

        if (itemObj.has("item") && itemObj.get("item").isJsonPrimitive()) {
            Holder.Reference<Item> item = ForgeRegistries.ITEMS.getDelegateOrThrow(new ResourceLocation(GsonHelper.getAsString(itemObj, "item")));
            items.add(new ItemStack(item));
        } else if (itemObj.has("tag") && itemObj.get("tag").isJsonPrimitive()) {
            //noinspection deprecation
            for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, new ResourceLocation(itemObj.get("tag").getAsString())))) {
                items.add(new ItemStack(item));
            }

            if (items.isEmpty()) {
                throw new IllegalStateException("Empty tag: " + itemObj.get("tag").getAsString());
            }
        } else {
            throw new IllegalStateException("I need \"item\" or \"tag\" in json object in: " + itemObj);
        }

        int count = itemObj.has("count") && itemObj.get("count").isJsonPrimitive() ? itemObj.get("count").getAsInt() : 1;
        CompoundTag nbt = null;
        if (itemObj.has("nbt") && itemObj.get("nbt").isJsonPrimitive()) {
            try {
                nbt = TagParser.parseTag(GsonHelper.getAsString(itemObj, "nbt"));
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException("Invalid nbt data: " + GsonHelper.getAsString(itemObj, "nbt"), e);
            }
        }

        return new ItemStackCollection(items, count, nbt);
    }

    private static ItemStackCollection getItemStackCollection(String key, CompoundTag tag) {
        if (!tag.contains(key, Tag.TAG_COMPOUND)) {
            throw new IllegalStateException("Item definition needs to be an object");
        }

        CompoundTag compound = tag.getCompound(key);
        Set<ItemStack> items = new HashSet<>();
        if (compound.contains("Items", Tag.TAG_LIST)) {
            ListTag list = (ListTag) compound.get("Items");
            //noinspection DataFlowIssue
            for (Tag value : list) {
                StringTag id = (StringTag) value;
                Holder.Reference<Item> item = ForgeRegistries.ITEMS.getDelegateOrThrow(new ResourceLocation(id.getAsString()));
                items.add(new ItemStack(item));
            }
        } else {
            return ItemStackCollection.EMPTY;
        }

        int count = compound.contains("Count", Tag.TAG_INT) ? compound.getInt("Count") : 1;
        CompoundTag nbt = null;
        if (compound.contains("Tag", Tag.TAG_COMPOUND)) {
            nbt = compound.getCompound("Tag");
        }

        return new ItemStackCollection(items, count, nbt);
    }

    public static class ItemStackCollection {

        public static final Codec<ItemStackCollection> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.list(MoreCodecs.SAFE_ITEM_STACK).fieldOf("items").forGetter(collection -> collection.getItems().stream().toList()),
                        Codec.INT.fieldOf("count").forGetter(ItemStackCollection::getCount),
                        CompoundTag.CODEC.fieldOf("tag").forGetter(ItemStackCollection::getTag)
                ).apply(instance, ItemStackCollection::new));

        private final List<ItemStack> items;
        private final int count;
        private final CompoundTag tag;

        ItemStackCollection(Collection<ItemStack> items, int count, @Nullable CompoundTag tag) {
            this.items = items.stream().peek(item -> {
                item.setTag(tag);
                item.setCount(count);
            }).toList();
            this.count = count;
            this.tag = tag;
        }

        public ItemStackCollection(List<ItemStack> items, int count) {
            this(items, count, null);
        }

        public static final ItemStackCollection EMPTY = new ItemStackCollection(List.of(), 0);

        public List<ItemStack> getItems() {
            return this.items;
        }

        public int getCount() {
            return this.count;
        }

        public boolean isEmpty() {
            return this == EMPTY || this.items.isEmpty() || this.count == 0 || this.items.stream().allMatch(ItemStack::isEmpty);
        }

        public boolean has(ItemStack stack) {
            return this.items.stream().anyMatch(item -> ItemStack.isSameItem(item, stack) && stack.getCount() >= this.count);
        }

        public boolean hasTag() {
            return this.tag != null && !this.tag.isEmpty();
        }

        public CompoundTag getTag() {
            return this.tag;
        }

        public CompoundTag save(CompoundTag tag) {
            ListTag items = new ListTag();
            for (ItemStack item : this.items) {
                //noinspection DataFlowIssue
                items.add(StringTag.valueOf(ForgeRegistries.ITEMS.getKey(item.getItem()).toString()));
            }

            tag.put("Items", items);
            tag.putInt("Count", this.count);
            if (this.hasTag()) {
                tag.put("Tag", this.tag);
            }

            return tag;
        }
    }
}
