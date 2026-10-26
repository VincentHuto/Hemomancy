package com.vincenthuto.hemomancy.client.screen.overlay;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Smooth per-vertex-colour GUI geometry (rects, gradients, tapered strokes, discs, rings) in the GUI render type, for
 * HUD art that pixel fills can't draw: the Mycophant's webbing, the boss bar frames, the scene title cards. Open one
 * inside {@code GuiGraphics.drawManaged} so every quad shares a flush. Quads are wound the way {@code GuiGraphics.fill}
 * winds them, so culling keeps them whichever order the corners come in.
 */
public record GuiCanvas(VertexConsumer consumer, Matrix4f matrix) {
	public static GuiCanvas of(GuiGraphics graphics) {
		return new GuiCanvas(graphics.bufferSource().getBuffer(RenderType.gui()), graphics.pose().last().pose());
	}

	/** A rect with a vertical gradient. */
	public void rect(float x0, float y0, float x1, float y1, int top, int bottom) {
		quad(x0, y0, top, x0, y1, bottom, x1, y1, bottom, x1, y0, top);
	}

	/** A rect with a horizontal gradient. */
	public void hrect(float x0, float y0, float x1, float y1, int left, int right) {
		quad(x0, y0, left, x0, y1, left, x1, y1, right, x1, y0, right);
	}

	public void tri(float ax, float ay, int ac, float bx, float by, int bc, float cx, float cy, int cc) {
		quad(ax, ay, ac, bx, by, bc, cx, cy, cc, cx, cy, cc);
	}

	public void quad(float ax, float ay, int ac, float bx, float by, int bc, float cx, float cy, int cc, float dx, float dy,
			int dc) {
		float area = (ax * by - bx * ay) + (bx * cy - cx * by) + (cx * dy - dx * cy) + (dx * ay - ax * dy);
		if (area > 0.0F) {
			vertex(ax, ay, ac);
			vertex(dx, dy, dc);
			vertex(cx, cy, cc);
			vertex(bx, by, bc);
		} else {
			vertex(ax, ay, ac);
			vertex(bx, by, bc);
			vertex(cx, cy, cc);
			vertex(dx, dy, dc);
		}
	}

	/** A stroke from one point to another, tapering from width {@code w0} to {@code w1}. */
	public void segment(float x0, float y0, float x1, float y1, float w0, float w1, int color) {
		segment(x0, y0, x1, y1, w0, w1, color, color);
	}

	public void segment(float x0, float y0, float x1, float y1, float w0, float w1, int c0, int c1) {
		float dx = x1 - x0, dy = y1 - y0;
		float len = Mth.sqrt(dx * dx + dy * dy);
		if (len < 1.0E-4F) return;
		float nx = -dy / len * 0.5F, ny = dx / len * 0.5F;
		quad(x0 + nx * w0, y0 + ny * w0, c0, x0 - nx * w0, y0 - ny * w0, c0, x1 - nx * w1, y1 - ny * w1, c1,
				x1 + nx * w1, y1 + ny * w1, c1);
	}

	/** A small soft diamond. */
	public void mote(float x, float y, float radius, int color) {
		quad(x, y - radius, color, x - radius, y, color, x, y + radius, color, x + radius, y, color);
	}

	/** A filled ellipse, its centre colour fading to the rim colour. */
	public void disc(float cx, float cy, float rx, float ry, int segments, int center, int rim) {
		for (int i = 0; i < segments; i++) {
			float a0 = Mth.TWO_PI * i / segments, a1 = Mth.TWO_PI * (i + 1) / segments;
			tri(cx, cy, center, cx + Mth.cos(a0) * rx, cy + Mth.sin(a0) * ry, rim, cx + Mth.cos(a1) * rx,
					cy + Mth.sin(a1) * ry, rim);
		}
	}

	/** A ring between two radii, over the arc from {@code from} to {@code to} radians. */
	public void arc(float cx, float cy, float r0, float r1, float from, float to, int segments, int inner, int outer) {
		for (int i = 0; i < segments; i++) {
			float a0 = Mth.lerp(i / (float) segments, from, to), a1 = Mth.lerp((i + 1) / (float) segments, from, to);
			quad(cx + Mth.cos(a0) * r0, cy + Mth.sin(a0) * r0, inner, cx + Mth.cos(a0) * r1, cy + Mth.sin(a0) * r1, outer,
					cx + Mth.cos(a1) * r1, cy + Mth.sin(a1) * r1, outer, cx + Mth.cos(a1) * r0, cy + Mth.sin(a1) * r0, inner);
		}
	}

	private void vertex(float x, float y, int color) {
		consumer.addVertex(matrix, x, y, 0.0F).setColor(color);
	}

	public static int argb(float alpha, int r, int g, int b) {
		return FastColor.ARGB32.color(Math.round(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F), r, g, b);
	}

	/** {@code rgb} (0xRRGGBB) at the given alpha. */
	public static int argb(float alpha, int rgb) {
		return argb(alpha, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
	}

	/** Scales an ARGB colour's alpha. */
	public static int fade(int argb, float alpha) {
		int a = Math.round((argb >>> 24) * Mth.clamp(alpha, 0.0F, 1.0F));
		return a << 24 | argb & 0xFFFFFF;
	}

	/** Mixes two ARGB colours. */
	public static int mix(int a, int b, float t) {
		return FastColor.ARGB32.lerp(Mth.clamp(t, 0.0F, 1.0F), a, b);
	}

	public static float hash(int seed, int a, int b) {
		int h = seed * 374761393 + a * 668265263 + b * 2147483647;
		h = (h ^ (h >>> 13)) * 1274126177;
		return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0F;
	}
}
