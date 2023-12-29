package com.vincenthuto.hemomancy.client.screen.codex.objects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;
import com.vincenthuto.hemomancy.client.screen.codex.HemoCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.HemoProgressionScreen;
import com.vincenthuto.hemomancy.client.screen.codex.ProgressionEntry;
import com.vincenthuto.hemomancy.client.screen.codex.BookEntry;
import com.vincenthuto.hemomancy.client.screen.codex.EntryScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class IconObject extends EntryObject {
	public final ResourceLocation textureLocation;

	public IconObject(AbstractProgressionCodexScreen screen, String identifier, int chapter, String parentId,
			ProgressionEntry<EntryObject> entry, ResourceLocation textureLocation, int posX, int posY) {
		super(screen, identifier, chapter, parentId, entry.setDark(), posX, posY);
		this.textureLocation = textureLocation;
	}

	@Override
	public void click(float xOffset, float yOffset, double mouseX, double mouseY) {
		EntryScreen.openScreen(this);
	}

	@Override
	public void render(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks) {
		int posX = offsetPosX(xOffset);
		int posY = offsetPosY(yOffset);
		PoseStack poseStack = guiGraphics.pose();
		HemoCodexHelper.renderTransparentTexture(HemoProgressionScreen.FADE_TEXTURE, poseStack, posX - 13, posY - 13, 1,
				252, 58, 58, 512, 512);
		HemoCodexHelper.renderTexture(HemoProgressionScreen.FRAME_TEXTURE, poseStack, posX, posY, 67,
				getFrameTextureV(), width, height, 512, 512);
		HemoCodexHelper.renderTexture(HemoProgressionScreen.FRAME_TEXTURE, poseStack, posX, posY, 166,
				getBackgroundTextureV(), width, height, 512, 512);
		HemoCodexHelper.renderWavyIcon(textureLocation, poseStack, posX + 8, posY + 8);
	}
}