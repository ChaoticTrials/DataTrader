package de.melanx.datatrader.trader;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TraderContainer implements Container {

    private final Trade trader;
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
    @Nullable
    private TraderOffer activeOffer;
    private int selectionHint;
    private int futureXp;

    public TraderContainer(Trade trader) {
        this.trader = trader;
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.itemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Nonnull
    @Override
    public ItemStack getItem(int index) {
        return this.itemStacks.get(index);
    }

    @Nonnull
    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack itemstack = this.itemStacks.get(index);
        if (index == 2 && !itemstack.isEmpty()) {
            return ContainerHelper.removeItem(this.itemStacks, index, itemstack.getCount());
        } else {
            ItemStack itemstack1 = ContainerHelper.removeItem(this.itemStacks, index, count);
            if (!itemstack1.isEmpty() && this.isPaymentSlot(index)) {
                this.updateSellItem();
            }

            return itemstack1;
        }
    }

    private boolean isPaymentSlot(int slot) {
        return slot == 0 || slot == 1;
    }

    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.itemStacks, index);
    }

    @Override
    public void setItem(int index, @Nonnull ItemStack stack) {
        this.itemStacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (this.isPaymentSlot(index)) {
            this.updateSellItem();
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.distanceTo((Entity) this.trader) <= player.getEntityReach();
    }

    @Override
    public void setChanged() {
        this.updateSellItem();
    }

    public void updateSellItem() {
        this.activeOffer = null;
        ItemStack itemstack;
        ItemStack itemstack1;
        if (this.itemStacks.get(0).isEmpty()) {
            itemstack = this.itemStacks.get(1);
            itemstack1 = ItemStack.EMPTY;
        } else {
            itemstack = this.itemStacks.get(0);
            itemstack1 = this.itemStacks.get(1);
        }

        if (itemstack.isEmpty()) {
            this.setItem(2, ItemStack.EMPTY);
            this.futureXp = 0;
        } else {
            TraderOffers merchantoffers = this.trader.getOffers();
            if (!merchantoffers.isEmpty()) {
                TraderOffer traderOffer = merchantoffers.getTradeFor(itemstack, itemstack1, this.selectionHint);
                if (traderOffer == null) {
                    this.activeOffer = traderOffer;
                    traderOffer = merchantoffers.getTradeFor(itemstack1, itemstack, this.selectionHint);
                }

                if (traderOffer != null) {
                    this.activeOffer = traderOffer;
                    this.setItem(2, traderOffer.assemble());
                    this.futureXp = traderOffer.getXp();
                } else {
                    this.setItem(2, ItemStack.EMPTY);
                    this.futureXp = 0;
                }
            }
        }
    }

    @Nullable
    public TraderOffer getActiveOffer() {
        return this.activeOffer;
    }

    public void setSelectionHint(int currentRecipeIndex) {
        this.selectionHint = currentRecipeIndex;
        this.updateSellItem();
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    public int getFutureXp() {
        return this.futureXp;
    }
}
