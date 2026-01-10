package de.melanx.datatrader.client;

import com.mojang.blaze3d.systems.RenderSystem;
import de.melanx.datatrader.DataTrader;
import de.melanx.datatrader.ingredients.TaggedDataComponentIngredient;
import de.melanx.datatrader.registration.ModIngredientTypes;
import de.melanx.datatrader.trader.TraderMenu;
import de.melanx.datatrader.trader.TraderOffer;
import de.melanx.datatrader.trader.TraderOffers;
import de.melanx.datatrader.util.ListEntryGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraderScreen extends AbstractContainerScreen<TraderMenu> {

    private static final ResourceLocation VILLAGER_LOCATION = ResourceLocation.parse("textures/gui/container/villager.png");
    private static final ResourceLocation TRADE_ARROW_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/trade_arrow");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller_disabled");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
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

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int buttonY = y + SCROLL_BAR_TOP_POS_Y;

        for (int i = 0; i < NUMBER_OF_OFFER_BUTTONS; i++) {
            this.tradeOfferButtons[i] = this.addRenderableWidget(new TraderScreen.TradeOfferButton(x + TRADE_BUTTON_X, buttonY, i, button -> {
                if (button instanceof TraderScreen.TradeOfferButton offerButton) {
                    this.shopItem = offerButton.getIndex() + this.scrollOff;
                    this.postButtonClick();
                }
            }));

            buttonY += TRADE_BUTTON_HEIGHT;
        }

        this.entryCyclers.clear();
        for (TraderOffer offer : this.menu.getOffers()) {
            this.entryCyclers.put(offer, new EntryCycler(offer.getCostA(), offer.getCostB()));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 49 + this.imageWidth / 2 - this.font.width(this.title) / 2, LABEL_Y, Color.DARK_GRAY.getRGB(), false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, Color.DARK_GRAY.getRGB(), false);
        int l = this.font.width(TRADES_LABEL);
        guiGraphics.drawString(this.font, TRADES_LABEL, TRADE_BUTTON_X - l / 2 + 48, LABEL_Y, Color.DARK_GRAY.getRGB(), false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(VILLAGER_LOCATION, x, y, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private void renderScroller(GuiGraphics guiGraphics, int posX, int posY, TraderOffers offers) {
        int steps = offers.size() + 1 - NUMBER_OF_OFFER_BUTTONS;
        if (steps > 1) {
            int leftOver = SCROLL_BAR_HEIGHT - (SCROLLER_HEIGHT + (steps - 1) * SCROLL_BAR_HEIGHT / steps);
            int stepHeight = 1 + leftOver / steps + SCROLL_BAR_HEIGHT / steps;
            int maxScrollerOff = 113;
            int scrollerYOff = Math.min(maxScrollerOff, this.scrollOff * stepHeight);
            if (this.scrollOff == steps - 1) {
                scrollerYOff = maxScrollerOff;
            }

            guiGraphics.blitSprite(SCROLLER_SPRITE, posX + SCROLL_BAR_START_X, posY + SCROLL_BAR_TOP_POS_Y + scrollerYOff, 0, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        } else {
            guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, posX + SCROLL_BAR_START_X, posY + SCROLL_BAR_TOP_POS_Y, 0, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.entryCyclers.isEmpty()) {
            this.init();
            return;
        }

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        TraderOffers offers = this.menu.getOffers();
        if (!offers.isEmpty()) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            int offerY = y + 17;
            int sellItem1X = x + SELL_ITEM_1_X + TRADE_BUTTON_X;
            this.renderScroller(guiGraphics, x, y, offers);
            int currentOfferIndex = 0;

            for (TraderOffer offer : offers) {
                if (!this.canScroll(offers.size()) || currentOfferIndex >= this.scrollOff && currentOfferIndex < NUMBER_OF_OFFER_BUTTONS + this.scrollOff) {
                    EntryCycler entryCycler = this.entryCyclers.get(offer);
                    ItemStack costA = entryCycler.getCostA();
                    ItemStack costB = entryCycler.getCostB();
                    ItemStack result = offer.getResult();
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                    int decorHeight = offerY + 2;
                    guiGraphics.renderFakeItem(costA, sellItem1X, decorHeight);
                    guiGraphics.renderItemDecorations(this.font, costA, sellItem1X, decorHeight);
                    if (!costB.isEmpty()) {
                        guiGraphics.renderFakeItem(costB, x + TRADE_BUTTON_X + SELL_ITEM_2_X, decorHeight);
                        guiGraphics.renderItemDecorations(this.font, costB, x + TRADE_BUTTON_X + SELL_ITEM_2_X, decorHeight);
                    }

                    this.renderButtonArrows(guiGraphics, x, decorHeight);
                    guiGraphics.renderFakeItem(result, x + TRADE_BUTTON_X + BUY_ITEM_X, decorHeight);
                    guiGraphics.renderItemDecorations(this.font, result, x + TRADE_BUTTON_X + BUY_ITEM_X, decorHeight);
                    guiGraphics.pose().popPose();
                    offerY += TRADE_BUTTON_HEIGHT;
                }

                currentOfferIndex++;
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
        guiGraphics.blitSprite(TRADE_ARROW_SPRITE, posX + TRADE_BUTTON_X + SELL_ITEM_2_X + TRADE_BUTTON_HEIGHT, posY + 3, 0, 10, 9);
    }

    private boolean canScroll(int numOffers) {
        return numOffers > NUMBER_OF_OFFER_BUTTONS;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int i = this.menu.getOffers().size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_OFFER_BUTTONS;
            this.scrollOff = Mth.clamp((int) ((double) this.scrollOff - scrollY), 0, j);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int i = this.menu.getOffers().size();
        if (this.isDragging) {
            int j = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int k = j + SCROLL_BAR_HEIGHT;
            int l = i - 7;
            float f = ((float) mouseY - (float) j - 13.5F) / ((float) (k - j) - 27.0F);
            f = f * (float) l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu.getOffers().size())
                && mouseX > (double) (x + SCROLL_BAR_START_X)
                && mouseX < (double) (x + SCROLL_BAR_START_X + SCROLLER_WIDTH)
                && mouseY > (double) (y + SCROLL_BAR_TOP_POS_Y)
                && mouseY <= (double) (y + SCROLL_BAR_TOP_POS_Y + SCROLL_BAR_HEIGHT + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static class EntryCycler {

        private final ListEntryGetter<ItemStack> costA;
        private ListEntryGetter<ItemStack> costB;
        private final int costACount;
        private int costBCount = 0;

        public EntryCycler(TraderOffer.ItemStackCollection costA, TraderOffer.ItemStackCollection costB) {
            this.costA = new ListEntryGetter<>(costA.getItems(), 30);
            this.costACount = costA.getCount();
            if (!costB.isEmpty()) {
                this.costB = new ListEntryGetter<>(costB.getItems(), 30);
                this.costBCount = costB.getCount();
            }
        }

        public void tick() {
            this.costA.tick();
            if (this.costB != null) {
                this.costB.tick();
            }
        }

        public ItemStack getCostA() {
            return this.costA.getEntry().copyWithCount(this.costACount);
        }

        public ItemStack getCostB() {
            return this.costB != null ? this.costB.getEntry().copyWithCount(this.costBCount) : ItemStack.EMPTY;
        }
    }

    private class TradeOfferButton extends Button {
        private final int index;

        public TradeOfferButton(int x, int y, int index, Button.OnPress onPress) {
            super(x, y, TRADE_BUTTON_WIDTH, TRADE_BUTTON_HEIGHT, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
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
                    guiGraphics.renderTooltip(TraderScreen.this.font, this.tooltipFromIngredient(offer.getCostA(), entryCycler.getCostA()), entryCycler.getCostA().getTooltipImage(), mouseX, mouseY);
                } else if (mouseX < this.getX() + 50 && mouseX > this.getX() + 30) {
                    if (!entryCycler.getCostB().isEmpty()) {
                        guiGraphics.renderTooltip(TraderScreen.this.font, this.tooltipFromIngredient(offer.getCostB(), entryCycler.getCostB()), entryCycler.getCostB().getTooltipImage(), mouseX, mouseY);
                    }
                } else if (mouseX > this.getX() + 65) {
                    ItemStack result = offer.getResult();
                    guiGraphics.renderTooltip(TraderScreen.this.font, result, mouseX, mouseY);
                }
            }
        }

        public List<Component> tooltipFromIngredient(TraderOffer.ItemStackCollection collection, ItemStack itemStack) {
            List<Component> tooltipFromItem = Screen.getTooltipFromItem(Minecraft.getInstance(), itemStack);
            Ingredient ingredient = collection.getIngredient();
            MutableComponent tagIngredient = Component.translatable("tooltip.datatrader.tag_ingredient")
                    .withStyle(ChatFormatting.ITALIC)
                    .withStyle(ChatFormatting.GRAY);

            ICustomIngredient customIngredient = ingredient.getCustomIngredient();
            if (customIngredient != null && customIngredient.getType() == ModIngredientTypes.tagWithComponents) {
                MutableComponent translatable = Component.translatable(Tags.getTagTranslationKey(((TaggedDataComponentIngredient) customIngredient).getTag()));
                translatable.withStyle(itemStack.getRarity().getStyleModifier());
                tooltipFromItem.set(0, translatable.append(" ").append(tagIngredient));
            } else if (customIngredient == null && ingredient.getValues()[0] instanceof Ingredient.TagValue(TagKey<Item> tag)) {
                MutableComponent translatable = Component.translatable(Tags.getTagTranslationKey(tag));
                translatable.withStyle(itemStack.getRarity().getStyleModifier());
                tooltipFromItem.set(0, translatable.append(" ").append(tagIngredient));
            }

            return tooltipFromItem;
        }
    }
}
