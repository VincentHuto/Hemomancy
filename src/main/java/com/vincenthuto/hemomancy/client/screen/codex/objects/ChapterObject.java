package com.vincenthuto.hemomancy.client.screen.codex.objects;

import java.util.Arrays;
import java.util.List;

import com.vincenthuto.hemomancy.client.screen.codex.AbstractProgressionCodexScreen;
import com.vincenthuto.hemomancy.client.screen.codex.HemoCodexHelper;
import com.vincenthuto.hemomancy.client.screen.codex.HemoProgressionScreen;
import com.vincenthuto.hemomancy.client.screen.codex.ProgressionEntry;
import com.vincenthuto.hutoslib.common.data.skilltree.BranchTemplate;
import com.vincenthuto.hutoslib.common.data.skilltree.SkillTreeCodeModel;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ChapterObject extends BookObject {
	
	BranchTemplate branch;

	public ChapterObject(AbstractProgressionCodexScreen screen,SkillTreeCodeModel model,BranchTemplate branch,String identifier,int chapter,List<Integer> parentId,  int posX, int posY) {
		super(screen,model, identifier, chapter, parentId, posX, posY, 32, 32);
		this.branch = branch;
	}

	@Override
	public void click(float xOffset, float yOffset, double mouseX, double mouseY) {
		//EntryScreen.openScreen(this);
		screen.setChapter(branch.getOrdinality());
		screen.cachedChapter =(branch.getOrdinality());

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
		guiGraphics.renderItem(getIconItem(branch), posX + 8, posY + 8);
	}
	
	public ItemStack getIconItem(BranchTemplate pBranch) {
		if (pBranch.getIcon() != null && pBranch.getIcon().contains(":")) {
			String[] split = pBranch.getIcon().split(":");
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0], split[1]));
			if (item != null) {
				return new ItemStack(item);
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void lateRender(Minecraft minecraft, GuiGraphics guiGraphics, float xOffset, float yOffset, int mouseX,
			int mouseY, float partialTicks) {
		if (isHovering) {
			guiGraphics.renderComponentTooltip(minecraft.font,
					Arrays.asList(Component.translatable(branch.getTitle()),
							Component.translatable(branch.getSubtitle()).withStyle(ChatFormatting.GRAY)),
					mouseX, mouseY);
			// screen.renderComponentTooltip(guiGraphics,
			// Arrays.asList(Component.translatable(entry.translationKey()),
			// Component.translatable(entry.descriptionTranslationKey()).withStyle(ChatFormatting.GRAY)),
			// mouseX, mouseY, minecraft.font);
		}
	}

	public int getFrameTextureV() {
		return true ? 285 : 252;
	}

	public int getBackgroundTextureV() {
		return false ? 285 : 252;
	}
}