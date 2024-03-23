package de.melanx.datatrader.trader;

import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class TraderResultSlot extends Slot {

    private final TraderContainer container;
    private final Player player;
    private int removeCount;
    private final Trade trader;

    public TraderResultSlot(Player player, Trade trader, TraderContainer container, int slot, int xPosition, int yPosition) {
        super(container, slot, xPosition, yPosition);
        this.container = container;
        this.player = player;
        this.trader = trader;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }

        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(@Nonnull ItemStack stack, int amount) {
        this.removeCount += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(this.player.level(), this.player, this.removeCount);
        this.removeCount = 0;
    }

    @Override
    public void onTake(@Nonnull Player player, @Nonnull ItemStack stack) {
        this.checkTakeAchievements(stack);
        TraderOffer activeOffer = this.container.getActiveOffer();
        if (activeOffer != null) {
            ItemStack playerOfferA = this.container.getItem(0);
            ItemStack playerOfferB = this.container.getItem(1);
            if (activeOffer.take(playerOfferA, playerOfferB) || activeOffer.take(playerOfferB, playerOfferA)) {
                this.trader.notifyTrade(player, activeOffer);
                player.awardStat(Stats.TRADED_WITH_VILLAGER); // todo custom for trader
                this.container.setItem(0, playerOfferA);
                this.container.setItem(1, playerOfferB);
                player.giveExperiencePoints(activeOffer.getXp());
            }
        }
    }
}
