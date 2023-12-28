package com.vincenthuto.hemomancy.client.screen.codex.pages;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.codex.ArcanaCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.EntryScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class HeadlineTextPage extends BookPage {
    private final String headlineTranslationKey;
    private final String translationKey;

    public HeadlineTextPage(String headlineTranslationKey, String translationKey) {
        super(Hemomancy.rloc("textures/gui/book/pages/headline_page.png"));
        this.headlineTranslationKey = headlineTranslationKey;
        this.translationKey = translationKey;
    }

    public String headlineTranslationKey() {
        return "malum.gui.book.entry.page.headline." + headlineTranslationKey;
    }

    public String translationKey() {
        return "malum.gui.book.entry.page.text." + translationKey;
    }

    @Override
    public void renderLeft(Minecraft minecraft, GuiGraphics guiGraphics, EntryScreen screen, int mouseX, int mouseY, float partialTicks) {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        Component component = Component.translatable(headlineTranslationKey());
        ArcanaCodexHelper.renderText(guiGraphics, component, guiLeft + 75 - minecraft.font.width(component.getString()) / 2, guiTop + 10);
        ArcanaCodexHelper.renderWrappingText(guiGraphics, translationKey(), guiLeft + 14, guiTop + 31, 125);
    }

    @Override
    public void renderRight(Minecraft minecraft, GuiGraphics guiGraphics, EntryScreen screen, int mouseX, int mouseY, float partialTicks) {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        Component component = Component.translatable(headlineTranslationKey());
        ArcanaCodexHelper.renderText(guiGraphics, component, guiLeft + 218 - minecraft.font.width(component.getString()) / 2, guiTop + 10);
        ArcanaCodexHelper.renderWrappingText(guiGraphics, translationKey(), guiLeft + 156, guiTop + 31, 125);
    }
}