package com.vincenthuto.hemomancy.client.screen.radial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BlitRadialMenuItem extends TextRadialMenuItem {
	private final int slot;
	private final ResourceLocation rLoc;
	private final ResourceLocation baseLoc;
	private final int u;
	private final int v;
	private final int w;
	private final int h;
	private final int tex_w;
	private final int tex_h;

	public BlitRadialMenuItem(GenericRadialMenu owner, int slot, ResourceLocation rLoc, int u, int v, int w, int h,
			int texW, int texH, Component altText) {
		this(owner, slot, rLoc, null, u, v, w, h, texW, texH, altText);
	}

	/**
	 * Creates a radial menu item that renders a base texture with an overlay on top.
	 * @param baseLoc the base (background) texture, rendered first; may be null for single-layer mode
	 * @param rLoc    the overlay texture rendered on top of the base
	 */
	public BlitRadialMenuItem(GenericRadialMenu owner, int slot, ResourceLocation rLoc, ResourceLocation baseLoc,
			int u, int v, int w, int h, int texW, int texH, Component altText) {
		super(owner, altText, Integer.MAX_VALUE);
		this.slot = slot;
		this.rLoc = rLoc;
		this.baseLoc = baseLoc;
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
		int dx = (int) context.x - 8;
		int dy = (int) context.y - 8;
		context.graphics.pose().pushPose();
		GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
		if (baseLoc != null) {
			graphics.blit(baseLoc, dx, dy, this.u, this.v, this.w, this.h, this.tex_w, this.tex_h);
		}
		graphics.blit(rLoc, dx, dy, this.u, this.v, this.w, this.h, this.tex_w, this.tex_h);
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
