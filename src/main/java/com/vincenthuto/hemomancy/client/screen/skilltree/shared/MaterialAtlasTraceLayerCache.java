package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.mojang.blaze3d.platform.NativeImage;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class MaterialAtlasTraceLayerCache {
	private static final int TRACE_NODE_TRIM_RADIUS = 12;
	private static final double TRACE_ORGANIC_SWAY_MIN = 10.0;
	private static final double TRACE_ORGANIC_SWAY_MAX = 22.0;
	private static final double TRACE_ORGANIC_SWAY_FACTOR = 0.09;

	private final StaticTraceLayerTexture texture = new StaticTraceLayerTexture("material_atlas_trace_layer");

	void rebuildIfNeeded(List<MaterialAtlasNode> nodes,
			Map<MaterialAtlasNode, int[]> nodePositions,
			int contentW,
			int contentH,
			int hubX,
			int hubY) {
		String nextSignature = buildSignature(nodes, nodePositions, contentW, contentH, hubX, hubY);
		if (!texture.needsRebuild(nextSignature, contentW, contentH)) return;
		NativeImage image = texture.createImage();
		clearImage(image);

		for (MaterialAtlasNode node : sortedNodes(nodes)) {
			int[] target = nodePositions.get(node);
			if (target == null) {
				continue;
			}
			MaterialAtlasBucket bucket = node.atlasEntry().bucket();
			int alpha = node.visibility() == MaterialVisibility.NEXT_PREVIEW ? 76 : 136;
			for (String parentId : node.atlasEntry().parentIds()) {
				int[] parent = positionFor(parentId, nodes, nodePositions);
				if (parent == null) {
					continue;
				}
				bakeConnection(image, parent[0], parent[1], target[0], target[1],
						withAlpha(bucket.color(), alpha), hubX, hubY);
			}
		}

		texture.upload(image);
	}

	void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom) {
		texture.render(gfx, ctx, panZoom);
	}

	private static String buildSignature(List<MaterialAtlasNode> nodes,
			Map<MaterialAtlasNode, int[]> nodePositions,
			int contentW,
			int contentH,
			int hubX,
			int hubY) {
		StringBuilder out = new StringBuilder();
		out.append(contentW).append('x').append(contentH).append(':').append(hubX).append(',').append(hubY);
		for (MaterialAtlasNode node : sortedNodes(nodes)) {
			int[] pos = nodePositions.get(node);
			if (pos == null) {
				continue;
			}
			out.append('|').append(node.entry().name())
					.append('@').append(pos[0]).append(',').append(pos[1])
					.append(':').append(node.visibility().name())
					.append(':').append(node.atlasEntry().bucket().id())
					.append(':').append(String.join(",", node.atlasEntry().parentIds()));
		}
		return out.toString();
	}

	private static int[] positionFor(String materialId, List<MaterialAtlasNode> nodes,
			Map<MaterialAtlasNode, int[]> nodePositions) {
		for (MaterialAtlasNode node : nodes) {
			if (node.entry().name().equals(materialId)) {
				return nodePositions.get(node);
			}
		}
		return null;
	}

	private static List<MaterialAtlasNode> sortedNodes(List<MaterialAtlasNode> nodes) {
		List<MaterialAtlasNode> sorted = new ArrayList<>(nodes);
		sorted.sort(Comparator
				.comparing((MaterialAtlasNode n) -> n.atlasEntry().bucket().id())
				.thenComparingInt(n -> n.atlasEntry().order()));
		return sorted;
	}

	private static void bakeConnection(NativeImage image, int x1, int y1, int x2, int y2,
			int color, int centerX, int centerY) {
		List<TracePoint> points = sampleCubic(x1, y1, x2, y2, centerX, centerY);
		if (points.size() < 2) {
			return;
		}
		TracePoint prev = points.getFirst();
		for (int i = 1; i < points.size(); i++) {
			TracePoint next = points.get(i);
			bakeLine(image, prev.x(), prev.y(), next.x(), next.y(), color);
			prev = next;
		}
	}

	private static List<TracePoint> sampleCubic(int x1, int y1, int x2, int y2, int centerX, int centerY) {
		int[] start = trimTraceEndpoint(x1, y1, x2, y2);
		int[] end = trimTraceEndpoint(x2, y2, x1, y1);
		double[] controls = cubicControls(start[0], start[1], end[0], end[1], centerX, centerY);
		int steps = Math.max(18, (int) (estimateCubicLength(start[0], start[1],
				controls[0], controls[1], controls[2], controls[3], end[0], end[1]) / 8));
		List<TracePoint> points = new ArrayList<>(steps + 1);
		for (int i = 0; i <= steps; i++) {
			double t = i / (double) steps;
			int x = (int) Math.round(cubicPoint(start[0], controls[0], controls[2], end[0], t));
			int y = (int) Math.round(cubicPoint(start[1], controls[1], controls[3], end[1], t));
			if (points.isEmpty() || points.getLast().x() != x || points.getLast().y() != y) {
				points.add(new TracePoint(x, y));
			}
		}
		return points;
	}

	private static int[] trimTraceEndpoint(int x, int y, int towardX, int towardY) {
		double dx = towardX - x;
		double dy = towardY - y;
		double length = Math.hypot(dx, dy);
		if (length < 0.001) {
			return new int[] {x, y};
		}
		double offset = Math.min(TRACE_NODE_TRIM_RADIUS, Math.max(0.0, length / 2.0 - 1.0));
		return new int[] {
				(int) Math.round(x + dx / length * offset),
				(int) Math.round(y + dy / length * offset)
		};
	}

	private static double[] cubicControls(int x1, int y1, int x2, int y2, int centerX, int centerY) {
		double distance = Math.hypot(x2 - x1, y2 - y1);
		double handle = Math.max(34.0, Math.min(126.0, distance * 0.33));
		double[] fromRadial = radialFromCenter(x1, y1, x2 - x1, y2 - y1, centerX, centerY);
		double[] toRadial = radialFromCenter(x2, y2, x2 - x1, y2 - y1, centerX, centerY);
		double[] sway = organicSway(x1, y1, x2, y2);
		return new double[] {
				x1 + fromRadial[0] * handle + sway[0],
				y1 + fromRadial[1] * handle + sway[1],
				x2 - toRadial[0] * handle - sway[0],
				y2 - toRadial[1] * handle - sway[1]
		};
	}

	private static double[] organicSway(int x1, int y1, int x2, int y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double distance = Math.hypot(dx, dy);
		if (distance < 0.001) {
			return new double[] {0.0, 0.0};
		}
		double amount = Math.min(TRACE_ORGANIC_SWAY_MAX,
				Math.max(TRACE_ORGANIC_SWAY_MIN, distance * TRACE_ORGANIC_SWAY_FACTOR));
		double sign = organicSwaySign(x1, y1, x2, y2);
		return new double[] {
				-dy / distance * amount * sign,
				dx / distance * amount * sign
		};
	}

	private static double organicSwaySign(int x1, int y1, int x2, int y2) {
		int hash = x1 * 31 + y1 * 17 + x2 * 13 + y2 * 7;
		return (hash & 1) == 0 ? 1.0 : -1.0;
	}

	private static double[] radialFromCenter(int x, int y, int fallbackX, int fallbackY,
			int centerX, int centerY) {
		double dx = x - centerX;
		double dy = y - centerY;
		double length = Math.hypot(dx, dy);
		if (length < 0.001) {
			dx = fallbackX;
			dy = fallbackY;
			length = Math.hypot(dx, dy);
		}
		if (length < 0.001) {
			return new double[] {0.0, -1.0};
		}
		return new double[] {dx / length, dy / length};
	}

	private static void bakeLine(NativeImage image, int x0, int y0, int x1, int y1, int color) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		if (steps == 0) {
			bakeSoftPixel(image, x0, y0, color);
			return;
		}
		for (int i = 0; i <= steps; i++) {
			double t = i / (double) steps;
			int x = (int) Math.round(x0 + (x1 - x0) * t);
			int y = (int) Math.round(y0 + (y1 - y0) * t);
			bakeSoftPixel(image, x, y, color);
		}
	}

	private static void bakeSoftPixel(NativeImage image, int x, int y, int color) {
		blendPixel(image, x, y, color);
		int alpha = (color >>> 24) & 0xFF;
		int soft = withAlpha(color, alpha / 3);
		blendPixel(image, x + 1, y, soft);
		blendPixel(image, x - 1, y, soft);
		blendPixel(image, x, y + 1, soft);
		blendPixel(image, x, y - 1, soft);
	}

	private static void blendPixel(NativeImage image, int x, int y, int argb) {
		if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
			return;
		}
		int alpha = (argb >>> 24) & 0xFF;
		if (alpha <= 0) {
			return;
		}
		int nativeColor = argbToNativeAbgr(argb);
		image.setPixelRGBA(x, y, alphaBlendNativeAbgr(image.getPixelRGBA(x, y), nativeColor));
	}

	private static int argbToNativeAbgr(int argb) {
		int a = (argb >>> 24) & 0xFF;
		int r = (argb >>> 16) & 0xFF;
		int g = (argb >>> 8) & 0xFF;
		int b = argb & 0xFF;
		return (a << 24) | (b << 16) | (g << 8) | r;
	}

	private static int alphaBlendNativeAbgr(int background, int foreground) {
		int fgA = (foreground >> 24) & 0xFF;
		int fgB = (foreground >> 16) & 0xFF;
		int fgG = (foreground >> 8) & 0xFF;
		int fgR = foreground & 0xFF;

		int bgA = (background >> 24) & 0xFF;
		int bgB = (background >> 16) & 0xFF;
		int bgG = (background >> 8) & 0xFF;
		int bgR = background & 0xFF;

		float srcA = fgA / 255.0F;
		float dstA = bgA / 255.0F;
		float outA = srcA + dstA * (1.0F - srcA);
		if (outA <= 0.0F) {
			return 0;
		}

		int outR = (int) ((fgR * srcA + bgR * dstA * (1.0F - srcA)) / outA);
		int outG = (int) ((fgG * srcA + bgG * dstA * (1.0F - srcA)) / outA);
		int outB = (int) ((fgB * srcA + bgB * dstA * (1.0F - srcA)) / outA);
		int outAi = (int) (outA * 255.0F);
		return (outAi << 24) | (outB << 16) | (outG << 8) | outR;
	}

	private static void clearImage(NativeImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				image.setPixelRGBA(x, y, 0);
			}
		}
	}

	private static double estimateCubicLength(int x0, int y0, double c1x, double c1y,
			double c2x, double c2y, int x3, int y3) {
		double length = 0;
		double prevX = x0;
		double prevY = y0;
		for (int i = 1; i <= 12; i++) {
			double t = i / 12.0;
			double x = cubicPoint(x0, c1x, c2x, x3, t);
			double y = cubicPoint(y0, c1y, c2y, y3, t);
			length += Math.hypot(x - prevX, y - prevY);
			prevX = x;
			prevY = y;
		}
		return length;
	}

	private static double cubicPoint(double p0, double p1, double p2, double p3, double t) {
		double inv = 1.0 - t;
		return inv * inv * inv * p0
				+ 3.0 * inv * inv * t * p1
				+ 3.0 * inv * t * t * p2
				+ t * t * t * p3;
	}

	private static int withAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (Math.max(0, Math.min(255, alpha)) << 24);
	}

	private record TracePoint(int x, int y) {
	}
}
