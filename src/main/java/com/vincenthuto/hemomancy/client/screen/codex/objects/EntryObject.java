package com.vincenthuto.hemomancy.client.screen.codex.objects;

import java.util.Arrays;
import java.util.List;

import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;
import com.vincenthuto.hemomancy.client.screen.codex.EntryScreen;
import com.vincenthuto.hemomancy.client.screen.codex.HemoCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.HemoProgressionScreen;
import com.vincenthuto.hemomancy.client.screen.codex.ProgressionEntry;
import com.vincenthuto.hutoslib.common.data.skilltree.SkillTemplate;
import com.vincenthuto.hutoslib.common.data.skilltree.SkillTreeCodeModel;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EntryObject extends BookObject {
	SkillTemplate skill;
	int ordinality;

	public EntryObject(AbstractProgressionCodexScreen screen, SkillTemplate skill, SkillTreeCodeModel model,
			int ordinality, String identifier, int chapter, List<Integer> list, int posX, int posY) {
		super(screen, model, identifier, chapter, list, posX, posY, 32, 32);
		this.skill = skill;
		this.ordinality = ordinality;

	}

	@Override
	public void click(float xOffset, float yOffset, double mouseX, double mouseY) {
		// EntryScreen.openScreen(model, this);
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
		guiGraphics.renderItem(skill.getIconItem(), posX + 8, posY + 8);
	}

	@Override
	public void lateRender(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks) {
		if (isHovering) {
			guiGraphics.renderComponentTooltip(minecraft.font,
					Arrays.asList(Component.translatable(skill.getTitle()),
							Component.translatable(skill.getSubtitle()).withStyle(ChatFormatting.GRAY)),
					mouseX, mouseY);
		}
	}

	public int getFrameTextureV() {
		return true ? 285 : 252;
	}

	public int getBackgroundTextureV() {
		return false ? 285 : 252;
	}

	public int getOrdinality() {
		return ordinality;
	}

	public void setOrdinality(int ordinality) {
		this.ordinality = ordinality;
	}
}