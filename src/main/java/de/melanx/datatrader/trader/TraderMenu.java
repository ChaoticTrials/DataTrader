package de.melanx.datatrader.trader;

import de.melanx.datatrader.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.moddingx.libx.menu.MenuBase;

import javax.annotation.Nonnull;

public class TraderMenu extends MenuBase {

    protected static final int PAYMENT1_SLOT = 0;
    protected static final int PAYMENT2_SLOT = 1;
    protected static final int RESULT_SLOT = 2;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private static final int SELLSLOT1_X = 136;
    private static final int SELLSLOT2_X = 162;
    private static final int BUYSLOT_X = 220;
    private static final int ROW_Y = 37;
    private final Trade trader;
    private final TraderContainer tradeContainer;

    public TraderMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory);
//        this.setOffers(new TraderOffers(buf.readNbt()));
    }

    public TraderMenu(int containderId, Inventory inventory) {
        this(containderId, inventory, new ClientSideTrader(inventory.player));
    }

    public TraderMenu(int containerId, Inventory inventory, Trade trader) {
        super(ModMenus.traderMenu, containerId, inventory);
        this.trader = trader;
        this.tradeContainer = new TraderContainer(trader);
        this.addSlot(new Slot(this.tradeContainer, PAYMENT1_SLOT, SELLSLOT1_X, ROW_Y));
        this.addSlot(new Slot(this.tradeContainer, PAYMENT2_SLOT, SELLSLOT2_X, ROW_Y));
        this.addSlot(new TraderResultSlot(inventory.player, trader, this.tradeContainer, RESULT_SLOT, BUYSLOT_X, ROW_Y));
        this.setOffers(trader.getOffers());

        this.layoutPlayerInventorySlots(108, 84);
    }

    @Override
    public void slotsChanged(@Nonnull Container container) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(container);
    }

    public void setOffers(TraderOffers offers) {
        this.trader.overrideOffers(offers);
    }

    public TraderOffers getOffers() {
        return this.trader.getOffers();
    }

    public void setSelectionHint(int index) {
        this.tradeContainer.setSelectionHint(index);
    }

    public void tryMoveItems(int index) {
        if (index < 0 || this.getOffers().size() <= index) {
            return;
        }

        ItemStack playerOfferA = this.tradeContainer.getItem(PAYMENT1_SLOT);
        if (!playerOfferA.isEmpty()) {
            if (!this.moveItemStackTo(playerOfferA, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                return;
            }

            this.tradeContainer.setItem(PAYMENT1_SLOT, playerOfferA);
        }

        ItemStack playerOfferB = this.tradeContainer.getItem(PAYMENT2_SLOT);
        if (!playerOfferB.isEmpty()) {
            if (!this.moveItemStackTo(playerOfferB, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                return;
            }

            this.tradeContainer.setItem(PAYMENT2_SLOT, playerOfferB);
        }

        if (!this.tradeContainer.getItem(PAYMENT1_SLOT).isEmpty() || !this.tradeContainer.getItem(PAYMENT2_SLOT).isEmpty()) {
            return;
        }

        TraderOffer offer = this.getOffers().get(index);
        TraderOffer.ItemStackCollection costA = offer.getCostA();
        TraderOffer.ItemStackCollection costB = offer.getCostB();
        ItemStack mayOfferA = this.firstMatching(offer, costA);
        this.moveFromInventoryToPaymentSlot(PAYMENT1_SLOT, mayOfferA);
        if (!costB.isEmpty()) {
            ItemStack mayOfferB = this.firstMatching(offer, costB);
            this.moveFromInventoryToPaymentSlot(PAYMENT2_SLOT, mayOfferB);
        }
    }

    private void moveFromInventoryToPaymentSlot(int paymentSlotIndex, ItemStack paymentSlot) {
        if (!paymentSlot.isEmpty()) {
            for (int i = INV_SLOT_START; i < USE_ROW_SLOT_END; i++) {
                ItemStack item = this.slots.get(i).getItem();
                if (!item.isEmpty() && ItemStack.isSameItemSameTags(paymentSlot, item)) {
                    ItemStack paymentItem = this.tradeContainer.getItem(paymentSlotIndex);
                    int paymentItemCount = paymentItem.isEmpty() ? 0 : paymentItem.getCount();
                    int transferCount = Math.min(paymentSlot.getMaxStackSize() - paymentItemCount, item.getCount());
                    ItemStack itemCopy = item.copy();
                    int combinedCount = paymentItemCount + transferCount;
                    item.shrink(transferCount);
                    itemCopy.setCount(combinedCount);
                    this.tradeContainer.setItem(paymentSlotIndex, itemCopy);
                    if (combinedCount >= paymentSlot.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }
    }

    private ItemStack firstMatching(TraderOffer offer, TraderOffer.ItemStackCollection collection) {
        for (int startIndex = INV_SLOT_START; startIndex < USE_ROW_SLOT_END; startIndex++) {
            ItemStack item = this.slots.get(startIndex).getItem();
            if (offer.isRequiredItem(item, collection) && item.getCount() >= collection.getCount()) {
                return item;
            }
        }

        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemStack = stack.copy();
            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(stack, itemStack);
                this.playTradeSound();
            } else if (index != PAYMENT1_SLOT && index != PAYMENT2_SLOT) {
                if (index >= INV_SLOT_START && index < USE_ROW_SLOT_END) {
                    if (!this.moveItemStackTo(stack, PAYMENT1_SLOT, RESULT_SLOT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(stack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemStack;
    }

    private void playTradeSound() {
        if (!this.trader.isClientSide()) {
            Entity entity = (Entity) this.trader;
            entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return this.tradeContainer.stillValid(player);
    }
}
