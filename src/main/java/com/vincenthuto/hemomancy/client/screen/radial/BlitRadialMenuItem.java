package com.vincenthuto.hemomancy.client.screen.radial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

	Minecraft mc = Minecraft.getInstance();

	@Override
	public void draw(DrawingContext context) {
		context.graphics.pose().pushPose();
		GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
		graphics.blit(rLoc, ((int) context.x-8), ((int) context.y-8), this.u, this.v, this.w, this.h, this.tex_w,
				this.tex_h);
		context.graphics.pose().popPose();
	}

	@Override
	public void drawTooltips(DrawingContext context) {
		if (this.getText() != null) {
			context.graphics.renderTooltip(context.font, this.getText(), (int) context.x, (int) context.y);
		}
	}

	public int getSlot() {
		return this.slot;
	}

	public ResourceLocation getStack() {
		return this.rLoc;
	}
}
