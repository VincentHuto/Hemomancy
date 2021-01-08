package com.huto.hemomancy.render;

import java.util.List;

import com.huto.hemomancy.recipes.BlockPosBlockPair;
import com.huto.hemomancy.recipes.BloodCraftingBundledPattern;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class RenderMultiBlock {
	@SuppressWarnings("deprecation")
	public static void renderPatternInGUI(MatrixStack ms, Minecraft mc, BloodCraftingBundledPattern pattern) {
		GlStateManager.pushMatrix();
		ms.push();
		RenderHelper.enableStandardItemLighting();
		List<BlockPosBlockPair> patternList = pattern.getBlockPosBlockList();
		GlStateManager.rotatef(45, 0, 0, 0);
		GlStateManager.scaled(0.5, 0.5, 0.5);
		
		GlStateManager.translatef(0, 2, 0);
		for (BlockPosBlockPair pair : patternList) {
			GlStateManager.translatef(1, 1, pair.getPos().getZ() );
			mc.getItemRenderer().renderItemAndEffectIntoGUI(new ItemStack(pair.getBlock()), pair.getPos().getX() * -16,
					pair.getPos().getY() * -16);
		}
		ms.pop();
		GlStateManager.popMatrix();

	}

}
