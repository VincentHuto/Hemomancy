package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** Bakes shared tendency-tree connections into a texture for inexpensive pan/zoom rendering. */
final class TendencyTraceLayerCache {
	private static final AtomicInteger NEXT_ID = new AtomicInteger();

	private static final int TRACE_NODE_TRIM_RADIUS = 11;
	private static final double TRACE_ORGANIC_SWAY_MIN = 8.0;
	private static final double TRACE_ORGANIC_SWAY_MAX = 18.0;
	private static final double TRACE_ORGANIC_SWAY_FACTOR = 0.08;

	private final int textureId = NEXT_ID.incrementAndGet();
	private final ResourceLocation textureLocation = Hemomancy.rloc("dynamic/tendency_trace_layer_" + textureId);

	private DynamicTexture texture;
	private String signature = "";
	private int textureW;
	private int textureH;

	void rebuildIfNeeded(
			List<ManipulationTreeEntry> entries,
			Map<ManipulationTreeEntry, int[]> nodePositions,
			Set<String> knownManipNames,
			int playerDegree,
			int contentW,
			int contentH,
			int centerX,
			int centerY) {
		List<TendencyTraceNode> nodes = new ArrayList<>();
		for (ManipulationTreeEntry entry : sortedEntries(entries)) {
			int[] pos = nodePositions.get(entry);
			if (pos == null) continue;
			BloodManipulation manipulation = entry.resolve();
			nodes.add(new TendencyTraceNode(entry.getManipName(),
					manipulation != null ? manipulation.getTend() : null,
					pos[0], pos[1], entry.getConnectionParentNames(),
					knownManipNames.contains(entry.getManipName()), isRankLocked(manipulation, playerDegree)));
		}
		rebuildIfNeeded(nodes, contentW, contentH, centerX, centerY);
	}

	void rebuildIfNeeded(List<TendencyTraceNode> nodes, int contentW, int contentH, int centerX, int centerY) {
		String nextSignature = buildSignature(nodes, contentW, contentH, centerX, centerY);
		if (nextSignature.equals(signature) && texture != null) {
			return;
		}

		signature = nextSignature;
		textureW = Math.max(1, contentW);
		textureH = Math.max(1, contentH);
		NativeImage image = new NativeImage(textureW, textureH, false);
		clearImage(image);

		Map<String, TendencyTraceNode> byId = new java.util.HashMap<>();
		for (TendencyTraceNode node : nodes) byId.put(node.id(), node);
		List<TendencyTraceNode> sorted = new ArrayList<>(nodes);
		sorted.sort(Comparator.comparing(TendencyTraceNode::id));
		for (TendencyTraceNode child : sorted) {
			int traceColorBase = tendencyColor(child.tendency());
			for (String parentId : child.parentIds()) {
				TendencyTraceNode parent = byId.get(parentId);
				if (parent == null) continue;
				TendencyTraceStyle style = TendencyTraceStyle.resolve(
						parent.known(), parent.locked(), child.known(), child.locked());
				int color = style == TendencyTraceStyle.KNOWN ? traceColorBase : dimTraceColor(traceColorBase);
				int alpha = style == TendencyTraceStyle.LOCKED ? 78
						: style == TendencyTraceStyle.KNOWN ? 148 : 112;
				bakeConnection(image, parent.x(), parent.y(), child.x(), child.y(),
						withAlpha(color, alpha), centerX, centerY);
			}
		}

		upload(image);
	}

	void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom) {
		render(gfx, ctx, panZoom, 1.0f);
	}

	void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float alpha) {
		if (texture == null) return;
		float clampedAlpha = clamp01(alpha);
		if (clampedAlpha <= 0.01f) return;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, clampedAlpha);
		PoseStack pose = gfx.pose();
		pose.pushPose();
		pose.translate(ctx.guiLeft() + panZoom.panX, ctx.guiTop() + panZoom.panY, 0);
		pose.scale(panZoom.zoom, panZoom.zoom, 1.0f);
		gfx.blit(textureLocation, 0, 0, 0, 0, textureW, textureH, textureW, textureH);
		pose.popPose();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.disableBlend();
	}

	private void upload(NativeImage image) {
		if (texture == null || texture.getPixels() == null
				|| texture.getPixels().getWidth() != textureW
				|| texture.getPixels().getHeight() != textureH) {
			texture = new DynamicTexture(image);
			Minecraft.getInstance().getTextureManager().register(textureLocation, texture);
			return;
		}

		NativeImage target = texture.getPixels();
		for (int y = 0; y < textureH; y++) {
			for (int x = 0; x < textureW; x++) {
				target.setPixelRGBA(x, y, image.getPixelRGBA(x, y));
			}
		}
		texture.upload();
		image.close();
	}

	private static List<ManipulationTreeEntry> sortedEntries(List<ManipulationTreeEntry> entries) {
		List<ManipulationTreeEntry> list = new ArrayList<>(entries);
		list.sort(Comparator.comparing(ManipulationTreeEntry::getManipName));
		return list;
	}

	private static String buildSignature(List<TendencyTraceNode> nodes, int contentW, int contentH,
	                                     int centerX, int centerY) {
		StringBuilder out = new StringBuilder();
		out.append(contentW).append('x').append(contentH)
				.append(':').append(centerX).append(',').append(centerY);
		List<TendencyTraceNode> sorted = new ArrayList<>(nodes);
		sorted.sort(Comparator.comparing(TendencyTraceNode::id));
		for (TendencyTraceNode node : sorted) {
			out.append('|').append(node.id())
					.append('@').append(node.x()).append(',').append(node.y())
					.append(node.known() ? ":K" : ":U")
					.append(node.locked() ? ":L" : ":O")
					.append(":t=").append(node.tendency() != null ? node.tendency().name() : "null");
			for (String parent : node.parentIds()) {
				out.append(">").append(parent);
			}
		}
		return out.toString();
	}

	private static boolean isRankLocked(BloodManipulation manip, int playerDegree) {
		if (manip == null) return false;
		return !ManipulationRankGates.playerMeetsRank(playerDegree, manip.getRank());
	}

	private static int tendencyColor(EnumBloodTendency tendency) {
		if (tendency == null) return 0xFFAA6600;
		ParticleColor pc = tendency.getColor();
		int r = (int) pc.getRed();
		int g = (int) pc.getGreen();
		int b = (int) pc.getBlue();
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	private static int dimTraceColor(int color) {
		return (color & 0x00FFFFFF) | 0x88000000;
	}

	private static void bakeConnection(NativeImage image, int x1, int y1, int x2, int y2, int color, int centerX, int centerY) {
		List<TracePoint> points = sampleCubic(x1, y1, x2, y2, centerX, centerY);
		if (points.size() < 2) return;
		TracePoint prev = points.get(0);
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
		int steps = Math.max(18, (int) (estimateCubicLength(start[0], start[1], controls[0], controls[1], controls[2], controls[3], end[0], end[1]) / 8));
		List<TracePoint> points = new ArrayList<>(steps + 1);
		for (int i = 0; i <= steps; i++) {
			double t = i / (double) steps;
			int x = (int) Math.round(cubicPoint(start[0], controls[0], controls[2], end[0], t));
			int y = (int) Math.round(cubicPoint(start[1], controls[1], controls[3], end[1], t));
			if (points.isEmpty() || points.get(points.size() - 1).x() != x || points.get(points.size() - 1).y() != y) {
				points.add(new TracePoint(x, y));
			}
		}
		return points;
	}

	private static int[] trimTraceEndpoint(int x, int y, int towardX, int towardY) {
		double dx = towardX - x;
		double dy = towardY - y;
		double length = Math.hypot(dx, dy);
		if (length < 0.001) return new int[] {x, y};
		double offset = Math.min(TRACE_NODE_TRIM_RADIUS, Math.max(0.0, length / 2.0 - 1.0));
		return new int[] {
				(int) Math.round(x + dx / length * offset),
				(int) Math.round(y + dy / length * offset)
		};
	}

	private static double[] cubicControls(int x1, int y1, int x2, int y2, int centerX, int centerY) {
		double distance = Math.hypot(x2 - x1, y2 - y1);
		double handle = Math.max(36.0, Math.min(120.0, distance * 0.34));
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
		if (distance < 0.001) return new double[] {0.0, 0.0};
		double amount = Math.min(TRACE_ORGANIC_SWAY_MAX, Math.max(TRACE_ORGANIC_SWAY_MIN, distance * TRACE_ORGANIC_SWAY_FACTOR));
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

	private static double[] radialFromCenter(int x, int y, int fallbackX, int fallbackY, int centerX, int centerY) {
		double dx = x - centerX;
		double dy = y - centerY;
		double length = Math.hypot(dx, dy);
		if (length < 0.001) {
			dx = fallbackX;
			dy = fallbackY;
			length = Math.hypot(dx, dy);
		}
		if (length < 0.001) return new double[] {0.0, -1.0};
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
		if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return;
		int alpha = (argb >>> 24) & 0xFF;
		if (alpha <= 0) return;
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

		float srcA = fgA / 255.0f;
		float dstA = bgA / 255.0f;
		float outA = srcA + dstA * (1.0f - srcA);
		if (outA <= 0.0f) return 0;

		int outR = (int) ((fgR * srcA + bgR * dstA * (1.0f - srcA)) / outA);
		int outG = (int) ((fgG * srcA + bgG * dstA * (1.0f - srcA)) / outA);
		int outB = (int) ((fgB * srcA + bgB * dstA * (1.0f - srcA)) / outA);
		int outAi = (int) (outA * 255.0f);
		return (outAi << 24) | (outB << 16) | (outG << 8) | outR;
	}

	private static void clearImage(NativeImage image) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				image.setPixelRGBA(x, y, 0);
			}
		}
	}

	private static double estimateCubicLength(int x0, int y0, double c1x, double c1y, double c2x, double c2y, int x3, int y3) {
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

	private static float clamp01(float v) {
		return Math.max(0.0f, Math.min(1.0f, v));
	}

	private record TracePoint(int x, int y) {}
}
