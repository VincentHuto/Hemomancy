package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import net.minecraft.client.gui.GuiGraphics;

/** Shared top-left Family switch used by Harbinger progress tabs. */
public final class FamilyFilterControlView {
	private static final int LEFT_PAD = 8;
	private static final int TOP_PAD = 5;
	private static final int WIDTH = 68;
	private static final int HEIGHT = 13;
	private static final float Z = 650.0F;

	private FamilyFilterControlView() {
	}

	public static Bounds bounds(ProgressScreenContext ctx) {
		return new Bounds(ctx.guiLeft() + LEFT_PAD, ctx.guiTop() + TOP_PAD, WIDTH, HEIGHT);
	}

	public static void draw(GuiGraphics gfx, ProgressScreenContext ctx, String text,
			int accent, int mouseX, int mouseY) {
		Bounds bounds = bounds(ctx);
		gfx.pose().pushPose();
		gfx.pose().translate(0.0F, 0.0F, Z);
		gfx.fill(bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 0xDD10060B);
		ScreenDrawUtils.drawSimpleBorder(gfx, bounds.left(), bounds.top(), bounds.width(), bounds.height(),
				(0x88 << 24) | (accent & 0x00FFFFFF));
		HarbingerChromeRenderer.drawFrame(gfx, bounds.left(), bounds.top(), bounds.width(), bounds.height(), accent,
				bounds.contains(mouseX, mouseY) ? HarbingerChromeRenderer.State.HOVERED
						: HarbingerChromeRenderer.State.IDLE);
		gfx.drawCenteredString(ctx.font(), ctx.font().plainSubstrByWidth(text, bounds.width() - 6),
				bounds.left() + bounds.width() / 2, bounds.top() + 3, 0xFFBBBBBB);
		gfx.pose().popPose();
	}

	public record Bounds(int left, int top, int width, int height) {
		public int right() { return left + width; }
		public int bottom() { return top + height; }
		public boolean contains(double x, double y) {
			return x >= left && x <= right() && y >= top && y <= bottom();
		}
	}
}
