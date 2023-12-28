package com.vincenthuto.hemomancy.client.screen.codex.objects;


import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;
import com.vincenthuto.hemomancy.client.screen.codex.ArcanaCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.ArcanaProgressionScreen;
import com.vincenthuto.hemomancy.client.screen.codex.BookEntry;
import com.vincenthuto.hemomancy.client.screen.codex.EntryScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class IconObject extends EntryObject {
    public final ResourceLocation textureLocation;

    public IconObject(AbstractProgressionCodexScreen screen, BookEntry entry, ResourceLocation textureLocation, int posX, int posY) {
        super(screen, entry.setDark(), posX, posY);
        this.textureLocation = textureLocation;
    }

    @Override
    public void click(float xOffset, float yOffset, double mouseX, double mouseY) {
        EntryScreen.openScreen(this);
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX, int mouseY, float partialTicks) {
        int posX = offsetPosX(xOffset);
        int posY = offsetPosY(yOffset);
        PoseStack poseStack = guiGraphics.pose();
        ArcanaCodexHelper.renderTransparentTexture(ArcanaProgressionScreen.FADE_TEXTURE, poseStack, posX - 13, posY - 13, 1, 252, 58, 58, 512, 512);
        ArcanaCodexHelper.renderTexture(ArcanaProgressionScreen.FRAME_TEXTURE, poseStack, posX, posY, 67, getFrameTextureV(), width, height, 512, 512);
        ArcanaCodexHelper.renderTexture(ArcanaProgressionScreen.FRAME_TEXTURE, poseStack, posX, posY, 166, getBackgroundTextureV(), width, height, 512, 512);
        ArcanaCodexHelper.renderWavyIcon(textureLocation, poseStack, posX + 8, posY + 8);
    }
}