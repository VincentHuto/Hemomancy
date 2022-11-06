package com.vincenthuto.hemomancy.gui.radial.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.gui.radial.DrawingContext;
import com.vincenthuto.hemomancy.gui.radial.GenericRadialMenu;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BlitRadialMenuItem extends TextRadialMenuItem {
	private final int slot;
	private final ResourceLocation rLoc;
	private final int u;
	private final int v;
	private final int w;
	private final int h;
	private final int tex_w;
	private final int tex_h;

	public BlitRadialMenuItem(GenericRadialMenu owner, int slot, ResourceLocation rLoc, int u, int v, int w, int h,
			int texW, int texH, Component altText) {
		super(owner, altText, Integer.MAX_VALUE);
		this.slot = slot;
		this.rLoc = rLoc;
		this.u = u;
		this.v = v;
		this.w = w;
		this.h = h;
		this.tex_w = texW;
		this.tex_h = texH;
	}

	@Override
	public void draw(DrawingContext context) {
		context.matrixStack.pushPose();
		context.matrixStack.translate(-8.0, -8.0, context.z);
		RenderSystem.setShaderTexture(0, this.rLoc);
		GuiComponent.blit(context.matrixStack, ((int) context.x), ((int) context.y),
				this.u, this.v, this.w, this.h, this.tex_w, this.tex_h);
		context.matrixStack.popPose();
	}

	@Override
	public void drawTooltips(DrawingContext context) {
		if (this.getText() != null) {
			context.drawingHelper.renderTooltip(context.matrixStack, this.getText(), (int) context.x, (int) context.y);
		}
	}

	public int getSlot() {
		return this.slot;
	}

	public ResourceLocation getStack() {
		return this.rLoc;
	}
}
