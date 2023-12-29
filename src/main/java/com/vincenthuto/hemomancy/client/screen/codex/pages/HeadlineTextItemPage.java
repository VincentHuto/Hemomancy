package com.vincenthuto.hemomancy.client.screen.codex.pages;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.codex.HemoCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.EntryScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HeadlineTextItemPage extends BookPage {
    private final String headlineTranslationKey;
    private final String translationKey;
    private final ItemStack spiritStack;

    public HeadlineTextItemPage(String headlineTranslationKey, String translationKey, ItemStack spiritStack) {
        super(Hemomancy.rloc("textures/gui/book/pages/headline_item_page.png"));
        this.headlineTranslationKey = headlineTranslationKey;
        this.translationKey = translationKey;
        this.spiritStack = spiritStack;
    }

    public HeadlineTextItemPage(String headlineTranslationKey, String translationKey, Item spirit) {
        this(headlineTranslationKey, translationKey, spirit.getDefaultInstance());
    }

    public String headlineTranslationKey() {
        return "hemo.gui.book.entry.page.headline." + headlineTranslationKey;
    }

    public String translationKey() {
        return "hemo.gui.book.entry.page.text." + translationKey;
    }

    @Override
    public void renderLeft(Minecraft minecraft, GuiGraphics guiGraphics, EntryScreen screen, int mouseX, int mouseY, float partialTicks) {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        Component component = Component.translatable(headlineTranslationKey());
        HemoCodexHelper.renderText(guiGraphics, component, guiLeft + 75 - minecraft.font.width(component.getString()) / 2, guiTop + 10);
        HemoCodexHelper.renderWrappingText(guiGraphics, translationKey(), guiLeft + 14, guiTop + 76, 126);
        HemoCodexHelper.renderItem(screen, guiGraphics, spiritStack, guiLeft + 67, guiTop + 44, mouseX, mouseY);
    }

    @Override
    public void renderRight(Minecraft minecraft, GuiGraphics guiGraphics, EntryScreen screen, int mouseX, int mouseY, float partialTicks) {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        Component component = Component.translatable(headlineTranslationKey());
        HemoCodexHelper.renderText(guiGraphics, component, guiLeft + 218 - minecraft.font.width(component.getString()) / 2, guiTop + 10);
        HemoCodexHelper.renderWrappingText(guiGraphics, translationKey(), guiLeft + 156, guiTop + 76, 126);
        HemoCodexHelper. renderItem(screen, guiGraphics, spiritStack, guiLeft + 209, guiTop + 44, mouseX, mouseY);
    }
}