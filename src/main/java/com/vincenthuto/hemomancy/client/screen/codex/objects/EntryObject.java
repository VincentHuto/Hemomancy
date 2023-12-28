package com.vincenthuto.hemomancy.client.screen.codex.objects;

import java.util.Arrays;

import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;
import com.vincenthuto.hemomancy.client.screen.codex.HemoCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.HemoProgressionScreen;
import com.vincenthuto.hemomancy.client.screen.codex.BookEntry;
import com.vincenthuto.hemomancy.client.screen.codex.EntryScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EntryObject extends BookObject {
	public final BookEntry entry;

	public EntryObject(AbstractProgressionCodexScreen screen, BookEntry entry, int posX, int posY) {
		super(screen, posX, posY, 32, 32);
		this.entry = entry;
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
		HemoCodexHelper.renderTransparentTexture(HemoProgressionScreen.FADE_TEXTURE, guiGraphics.pose(), posX - 13,
				posY - 13, 1, 252, 58, 58, 512, 512);
		HemoCodexHelper.renderTexture(HemoProgressionScreen.FRAME_TEXTURE, guiGraphics.pose(), posX, posY, 1,
				getFrameTextureV(), width, height, 512, 512);
		HemoCodexHelper.renderTexture(HemoProgressionScreen.FRAME_TEXTURE, guiGraphics.pose(), posX, posY, 100,
				getBackgroundTextureV(), width, height, 512, 512);
		guiGraphics.renderItem(entry.iconStack, posX + 8, posY + 8);
	}

	@Override
	public void lateRender(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks) {
		if (isHovering) {
			guiGraphics.renderComponentTooltip(minecraft.font,
					Arrays.asList(Component.translatable(entry.translationKey()),
							Component.translatable(entry.descriptionTranslationKey()).withStyle(ChatFormatting.GRAY)),
					mouseX, mouseY);
			// screen.renderComponentTooltip(guiGraphics,
			// Arrays.asList(Component.translatable(entry.translationKey()),
			// Component.translatable(entry.descriptionTranslationKey()).withStyle(ChatFormatting.GRAY)),
			// mouseX, mouseY, minecraft.font);
		}
	}

	public int getFrameTextureV() {
		return entry.isSoulwood ? 285 : 252;
	}

	public int getBackgroundTextureV() {
		return entry.isDark ? 285 : 252;
	}
}