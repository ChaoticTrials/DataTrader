package de.melanx.datatrader.trader;

import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.util.ListEntryGetter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class TraderScreen extends AbstractContainerScreen<TraderMenu> {

    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int MERCHANT_MENU_PART_X = 99;
    private static final int PROGRESS_BAR_X = 136;
    private static final int PROGRESS_BAR_Y = 16;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_OFFER_BUTTONS = 7;
    private static final int TRADE_BUTTON_X = 5;
    private static final int TRADE_BUTTON_HEIGHT = 20;
    private static final int TRADE_BUTTON_WIDTH = 88;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = 139;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 94;
    private static final Component TRADES_LABEL = Component.translatable("merchant.trades");
    /**
     * The integer value corresponding to the currently selected merchant recipe.
     */
    private int shopItem;
    private final TraderScreen.TradeOfferButton[] tradeOfferButtons = new TraderScreen.TradeOfferButton[NUMBER_OF_OFFER_BUTTONS];
    int scrollOff;
    private boolean isDragging;
    private final Map<TraderOffer, EntryCycler> entryCyclers = new HashMap<>();

    public TraderScreen(TraderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
    }

    private void postButtonClick() {
        this.menu.setSelectionHint(this.shopItem);
        this.menu.tryMoveItems(this.shopItem);
        DataTrader.getNetwork().selectTrade(this.shopItem);
    }

    @Override
    protected void containerTick() {
        this.entryCyclers.values().forEach(EntryCycler::tick);
    }

    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int buttonY = y + 18;

        for (int i = 0; i < NUMBER_OF_OFFER_BUTTONS; i++) {
            this.tradeOfferButtons[i] = this.addRenderableWidget(new TraderScreen.TradeOfferButton(x + 5, buttonY, i, button -> {
                if (button instanceof TraderScreen.TradeOfferButton offerButton) {
                    this.shopItem = offerButton.getIndex() + this.scrollOff;
                    this.postButtonClick();
                }
            }));

            buttonY += 20;
        }

        this.entryCyclers.clear();
        for (TraderOffer offer : this.menu.getOffers()) {
            this.entryCyclers.put(offer, new EntryCycler(offer.getCostA(), offer.getCostB()));
        }
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 49 + this.imageWidth / 2 - this.font.width(this.title) / 2, 6, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, Color.DARK_GRAY.getRGB(), false);
        int l = this.font.width(TRADES_LABEL);
        guiGraphics.drawString(this.font, TRADES_LABEL, 5 - l / 2 + 48, 6, Color.DARK_GRAY.getRGB(), false);
    }

    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(VILLAGER_LOCATION, x, y, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private void renderScroller(GuiGraphics guiGraphics, int posX, int posY, TraderOffers offers) {
        int i = offers.size() + 1 - 7;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int l = 113;
            int i1 = Math.min(l, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = l;
            }

            guiGraphics.blit(VILLAGER_LOCATION, posX + 94, posY + 18 + i1, 0, 0.0F, 199.0F, 6, 27, 512, 256);
        } else {
            guiGraphics.blit(VILLAGER_LOCATION, posX + 94, posY + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
        }
    }

    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.entryCyclers.isEmpty()) {
            this.init();
            return;
        }

        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        TraderOffers offers = this.menu.getOffers();
        if (!offers.isEmpty()) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 16 + 1;
            int l = i + 5 + 5;
            this.renderScroller(guiGraphics, i, j, offers);
            int i1 = 0;

            for (TraderOffer offer : offers) {
                if (!this.canScroll(offers.size()) || i1 >= this.scrollOff && i1 < 7 + this.scrollOff) {
                    EntryCycler entryCycler = this.entryCyclers.get(offer);
                    ItemStack costA = entryCycler.getCostA();
                    ItemStack costB = entryCycler.getCostB();
                    ItemStack result = offer.getResult();
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                    int j1 = k + 2;
                    guiGraphics.renderFakeItem(costA, l, j1);
                    guiGraphics.renderItemDecorations(this.font, costA, l, j1);
                    if (!costB.isEmpty()) {
                        guiGraphics.renderFakeItem(costB, i + 5 + 35, j1);
                        guiGraphics.renderItemDecorations(this.font, costB, i + 5 + 35, j1);
                    }

                    this.renderButtonArrows(guiGraphics, i, j1);
                    guiGraphics.renderFakeItem(result, i + 5 + 68, j1);
                    guiGraphics.renderItemDecorations(this.font, result, i + 5 + 68, j1);
                    guiGraphics.pose().popPose();
                    k += 20;
                }
                i1++;
            }

            for (TraderScreen.TradeOfferButton button : this.tradeOfferButtons) {
                if (button.isHoveredOrFocused()) {
                    button.renderToolTip(guiGraphics, mouseX, mouseY);
                }

                button.visible = button.index < this.menu.getOffers().size();
            }

            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderButtonArrows(GuiGraphics guiGraphics, int posX, int posY) {
        RenderSystem.enableBlend();
        guiGraphics.blit(VILLAGER_LOCATION, posX + 5 + 35 + 20, posY + 3, 0, 15.0F, 171.0F, 10, 9, 512, 256);
    }

    private boolean canScroll(int numOffers) {
        return numOffers > 7;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int i = this.menu.getOffers().size();
        if (this.canScroll(i)) {
            int j = i - 7;
            this.scrollOff = Mth.clamp((int) ((double) this.scrollOff - delta), 0, j);
        }

        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int i = this.menu.getOffers().size();
        if (this.isDragging) {
            int j = this.topPos + 18;
            int k = j + 139;
            int l = i - 7;
            float f = ((float) mouseY - (float) j - 13.5F) / ((float) (k - j) - 27.0F);
            f = f * (float) l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu.getOffers().size()) && mouseX > (double) (i + 94) && mouseX < (double) (i + 94 + 6) && mouseY > (double) (j + 18) && mouseY <= (double) (j + 18 + 139 + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static class EntryCycler {

        private final ListEntryGetter<ItemStack> costA;
        private ListEntryGetter<ItemStack> costB;

        public EntryCycler(TraderOffer.ItemStackCollection costA, TraderOffer.ItemStackCollection costB) {
            this.costA = new ListEntryGetter<>(costA.getItems(), 60);
            if (!costB.isEmpty()) {
                this.costB = new ListEntryGetter<>(costB.getItems(), 60);
            }
        }

        public void tick() {
            this.costA.tick();
            if (this.costB != null) {
                this.costB.tick();
            }
        }

        public ItemStack getCostA() {
            return this.costA.getEntry();
        }

        public ItemStack getCostB() {
            return this.costB != null ? this.costB.getEntry() : ItemStack.EMPTY;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private class TradeOfferButton extends Button {
        private final int index;

        public TradeOfferButton(int x, int y, int index, Button.OnPress onPress) {
            super(x, y, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (this.isHovered && TraderScreen.this.menu.getOffers().size() > this.index + TraderScreen.this.scrollOff) {
                TraderOffer offer = TraderScreen.this.menu.getOffers().get(this.index + TraderScreen.this.scrollOff);
                EntryCycler entryCycler = TraderScreen.this.entryCyclers.get(offer);
                if (mouseX < this.getX() + 20) {
                    guiGraphics.renderTooltip(TraderScreen.this.font, entryCycler.getCostA(), mouseX, mouseY);
                } else if (mouseX < this.getX() + 50 && mouseX > this.getX() + 30) {
                    if (!entryCycler.getCostB().isEmpty()) {
                        guiGraphics.renderTooltip(TraderScreen.this.font, entryCycler.getCostB(), mouseX, mouseY);
                    }
                } else if (mouseX > this.getX() + 65) {
                    ItemStack result = offer.getResult();
                    guiGraphics.renderTooltip(TraderScreen.this.font, result, mouseX, mouseY);
                }
            }

        }
    }
}
