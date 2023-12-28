package com.vincenthuto.hemomancy.client.screen.codex.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.codex.HemoCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.BookEntry;
import com.vincenthuto.hemomancy.client.screen.codex.EntryScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class BookPage {
    public final ResourceLocation background;
    protected BookEntry parentEntry;

    public BookPage(ResourceLocation background) {
        this.background = background;
    }

    public boolean isValid() {
        return true;
    }

    public void render(Minecraft minecraft, GuiGraphics guiGraphics, EntryScreen screen, int mouseX, int mouseY, float partialTicks, boolean isRepeat) {
    }

    public void renderLeft(Minecraft minecraft, GuiGraphics guiGraphics, EntryScreen screen, int mouseX, int mouseY, float partialTicks) {

    }

    public void renderRight(Minecraft minecraft, GuiGraphics guiGraphics, EntryScreen screen, int mouseX, int mouseY, float partialTicks) {

    }

    public void renderBackgroundLeft(PoseStack poseStack) {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        HemoCodexHelper.renderTexture(background, poseStack, guiLeft, guiTop, 1, 1, EntryScreen.entryScreen.bookWidth - 147, EntryScreen.entryScreen.bookHeight, 512, 512);
    }

    public void renderBackgroundRight(PoseStack poseStack) {
        int guiLeft = guiLeft();
        int guiTop = guiTop();
        HemoCodexHelper.renderTexture(background, poseStack, guiLeft + 147, guiTop, 148, 1, EntryScreen.entryScreen.bookWidth - 147, EntryScreen.entryScreen.bookHeight, 512, 512);
    }

    public int guiLeft() {
        return (EntryScreen.entryScreen.width - EntryScreen.entryScreen.bookWidth) / 2;
    }

    public int guiTop() {
        return (EntryScreen.entryScreen.height - EntryScreen.entryScreen.bookHeight) / 2;
    }

    public BookPage setParentEntry(BookEntry parentEntry) {
        this.parentEntry = parentEntry;
        return this;
    }
}