package com.huto.hemomancy.gui;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiUtil {

	/**
	 * Draws a textured rectangle at the current z-value. Ported From past Versions
	 */
	public static void drawTexturedModalRect(float x, float y, float textureX, float textureY, float width, float height) {
		/*
		 * float f = 0.00390625F; float f1 = 0.00390625F;
		 */
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos((double) (x + 0), (double) (y + height), (double) 1)
				.tex((float) ((float) (textureX + 0) * 0.00390625F),
						(float) ((float) (textureY + height) * 0.00390625F))
				.endVertex();
		bufferbuilder.pos((double) (x + width), (double) (y + height), (double) 1)
				.tex((float) ((float) (textureX + width) * 0.00390625F),
						(float) ((float) (textureY + height) * 0.00390625F))
				.endVertex();
		bufferbuilder.pos((double) (x + width), (double) (y + 0), (double) 1)
				.tex((float) ((float) (textureX + width) * 0.00390625F), (float) ((float) (textureY + 0) * 0.00390625F))
				.endVertex();
		bufferbuilder.pos((double) (x + 0), (double) (y + 0), 1)
				.tex((float) ((float) (textureX + 0) * 0.00390625F), (float) ((float) (textureY + 0) * 0.00390625F))
				.endVertex();
		tessellator.draw();
	}

	/**
	 * Draws a textured rectangle at the current z-value. Ported From past Versions
	 */
	public static void drawScaledTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height,
			float scaleIn) {
		/*
		 * float f = 0.01090625F; float f1 = 0.01090625F;
		 */
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos((double) (x + 0), (double) (y + height), (double) 1)
				.tex((float) ((float) (textureX + 0) * scaleIn), (float) ((float) (textureY + height) * scaleIn))
				.endVertex();
		bufferbuilder.pos((double) (x + width), (double) (y + height), (double) 1)
				.tex((float) ((float) (textureX + width) * scaleIn), (float) ((float) (textureY + height) * scaleIn))
				.endVertex();
		bufferbuilder.pos((double) (x + width), (double) (y + 0), (double) 1)
				.tex((float) ((float) (textureX + width) * scaleIn), (float) ((float) (textureY + 0) * scaleIn))
				.endVertex();
		bufferbuilder.pos((double) (x + 0), (double) (y + 0), 1)
				.tex((float) ((float) (textureX + 0) * scaleIn), (float) ((float) (textureY + 0) * scaleIn))
				.endVertex();
		tessellator.draw();
	}

}
