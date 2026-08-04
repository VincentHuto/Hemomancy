package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import net.minecraft.client.gui.GuiGraphics;

/** Bakes the static rings, family spokes, and node connections into one GPU texture. */
final class RecipeMapTraceLayerCache {
	private static final int NODE_TRIM_RADIUS = RecipeMapLayout.NODE_SIZE / 2;

	private final StaticTraceLayerTexture texture = new StaticTraceLayerTexture("recipe_map_trace_layer");

	void rebuildIfNeeded(RecipeMapTracePlan plan, int contentW, int contentH) {
		if (!texture.needsRebuild(plan, contentW, contentH)) return;
		NativeImage image = texture.createImage();
		clearImage(image);
		for (RecipeMapTracePlan.Ring ring : plan.rings()) {
			bakeRing(image, ring);
		}
		for (RecipeMapTracePlan.Line spoke : plan.spokes()) {
			bakeDashedLine(image, spoke.x0(), spoke.y0(), spoke.x1(), spoke.y1(), spoke.color());
		}
		for (RecipeMapTracePlan.Connection connection : plan.connections()) {
			bakeConnection(image, connection);
		}
		texture.upload(image);
	}

	void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom) {
		texture.render(gfx, ctx, panZoom);
	}

	void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float alpha) {
		texture.render(gfx, ctx, panZoom, alpha);
	}

	private static void bakeRing(NativeImage image, RecipeMapTracePlan.Ring ring) {
		int steps = ConcentricRingStyle.segmentCount(ring.radius());
		int previousX = ring.centerX() + ring.radius();
		int previousY = ring.centerY();
		for (int step = 1; step <= steps; step++) {
			double angle = Math.PI * 2.0 * step / steps;
			int x = ring.centerX() + (int) Math.round(Math.cos(angle) * ring.radius());
			int y = ring.centerY() + (int) Math.round(Math.sin(angle) * ring.radius());
			bakeLine(image, previousX, previousY, x, y, ring.color());
			previousX = x;
			previousY = y;
		}
	}

	private static void bakeConnection(NativeImage image, RecipeMapTracePlan.Connection connection) {
		int[] start = trimEndpoint(connection.x0(), connection.y0(), connection.x1(), connection.y1());
		int[] end = trimEndpoint(connection.x1(), connection.y1(), connection.x0(), connection.y0());
		if (connection.kind() == RecipeMapLink.Kind.CONCEPTUAL) {
			bakeDashedLine(image, start[0], start[1], end[0], end[1], connection.color());
			return;
		}
		int control = Math.max(24, Math.abs(end[0] - start[0]) / 2);
		int steps = Math.max(18, (int) Math.ceil(Math.hypot(end[0] - start[0], end[1] - start[1]) / 4.0));
		int previousX = start[0];
		int previousY = start[1];
		for (int step = 1; step <= steps; step++) {
			double t = step / (double) steps;
			int x = (int) Math.round(cubic(start[0], start[0] + control, end[0] - control, end[0], t));
			int y = (int) Math.round(cubic(start[1], start[1], end[1], end[1], t));
			bakeLine(image, previousX, previousY, x, y, connection.color());
			previousX = x;
			previousY = y;
		}
	}

	private static int[] trimEndpoint(int x, int y, int towardX, int towardY) {
		double dx = towardX - x;
		double dy = towardY - y;
		double length = Math.hypot(dx, dy);
		if (length < 0.001) return new int[] {x, y};
		double trim = Math.min(NODE_TRIM_RADIUS, Math.max(0.0, length / 2.0 - 1.0));
		return new int[] {(int) Math.round(x + dx / length * trim), (int) Math.round(y + dy / length * trim)};
	}

	private static void bakeDashedLine(NativeImage image, int x0, int y0, int x1, int y1, int color) {
		int steps = Math.max(1, (int) Math.ceil(Math.hypot(x1 - x0, y1 - y0) / 4.0));
		for (int step = 0; step <= steps; step += 2) {
			double t = step / (double) steps;
			int x = (int) Math.floor(x0 + (x1 - x0) * t);
			int y = (int) Math.floor(y0 + (y1 - y0) * t);
			bakeLine(image, x, y, x + 1, y + 1, color);
		}
	}

	private static void bakeLine(NativeImage image, int x0, int y0, int x1, int y1, int color) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		if (steps == 0) {
			bakeSoftPixel(image, x0, y0, color);
			return;
		}
		for (int step = 0; step <= steps; step++) {
			double t = step / (double) steps;
			bakeSoftPixel(image, (int) Math.round(x0 + (x1 - x0) * t),
					(int) Math.round(y0 + (y1 - y0) * t), color);
		}
	}

	private static void bakeSoftPixel(NativeImage image, int x, int y, int color) {
		blendPixel(image, x, y, color);
		int soft = withAlpha(color, ((color >>> 24) & 0xFF) / 3);
		blendPixel(image, x + 1, y, soft);
		blendPixel(image, x - 1, y, soft);
		blendPixel(image, x, y + 1, soft);
		blendPixel(image, x, y - 1, soft);
	}

	private static void blendPixel(NativeImage image, int x, int y, int argb) {
		if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return;
		int alpha = (argb >>> 24) & 0xFF;
		if (alpha <= 0) return;
		int foreground = argbToNativeAbgr(argb);
		image.setPixelRGBA(x, y, alphaBlendNativeAbgr(image.getPixelRGBA(x, y), foreground));
	}

	private static int argbToNativeAbgr(int argb) {
		return (argb & 0xFF00FF00) | ((argb & 0x00FF0000) >>> 16) | ((argb & 0x000000FF) << 16);
	}

	private static int alphaBlendNativeAbgr(int background, int foreground) {
		int fgA = (foreground >>> 24) & 0xFF;
		int bgA = (background >>> 24) & 0xFF;
		float srcA = fgA / 255.0F;
		float dstA = bgA / 255.0F;
		float outA = srcA + dstA * (1.0F - srcA);
		if (outA <= 0.0F) return 0;
		int outR = blendChannel(foreground & 0xFF, background & 0xFF, srcA, dstA, outA);
		int outG = blendChannel((foreground >>> 8) & 0xFF, (background >>> 8) & 0xFF, srcA, dstA, outA);
		int outB = blendChannel((foreground >>> 16) & 0xFF, (background >>> 16) & 0xFF, srcA, dstA, outA);
		return ((int) (outA * 255.0F) << 24) | (outB << 16) | (outG << 8) | outR;
	}

	private static int blendChannel(int foreground, int background, float srcA, float dstA, float outA) {
		return (int) ((foreground * srcA + background * dstA * (1.0F - srcA)) / outA);
	}

	private static void clearImage(NativeImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) image.setPixelRGBA(x, y, 0);
		}
	}

	private static double cubic(double p0, double p1, double p2, double p3, double t) {
		double inv = 1.0 - t;
		return inv * inv * inv * p0 + 3.0 * inv * inv * t * p1 + 3.0 * inv * t * t * p2 + t * t * t * p3;
	}

	private static int withAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (alpha << 24);
	}
}
