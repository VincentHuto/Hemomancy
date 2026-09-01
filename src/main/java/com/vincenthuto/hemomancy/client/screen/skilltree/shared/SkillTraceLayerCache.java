package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ConcentricTreeGeometry;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgressClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bakes the static skill tree traces and degree rings into a texture. The
 * screen can then pan/zoom that single layer instead of re-rasterizing every
 * cubic and ring every frame.
 */
final class SkillTraceLayerCache {
    private static final AtomicInteger NEXT_ID = new AtomicInteger();

    private static final int COMPASS_CENTER_X = ConcentricTreeGeometry.CENTER_X;
    private static final int COMPASS_CENTER_Y = ConcentricTreeGeometry.CENTER_Y;
    private static final int[] DEGREE_RING_RADII = ConcentricTreeGeometry.degreeRingRadii();

    private static final int TRACE_DEGREE_RING = ConcentricRingStyle.withBaseAlpha(0xFF58231F);
    private static final int TRACE_NODE_TRIM_RADIUS = 11;
    private static final double TRACE_ORGANIC_SWAY_MIN = 8.0;
    private static final double TRACE_ORGANIC_SWAY_MAX = 18.0;
    private static final double TRACE_ORGANIC_SWAY_FACTOR = 0.08;
    private static final float HEARTBEAT_PERIOD_SECONDS = 1.35f;
    private static final float HEARTBEAT_MAX_ALPHA = 0.30f;
    private static final float HEARTBEAT_EXPANSION = 0.018f;
    private static final int HEARTBEAT_TRACE_ALPHA_BOOST = 190;
    private static final int HEARTBEAT_LOCKED_TRACE_ALPHA = 82;
    private static final int CENTER_HEARTBEAT_SOURCE_COLOR = 0xFFE2241E;
    private static final int CENTER_HEARTBEAT_GLOW_COLOR = 0xFFD00000;

    private final int textureId = NEXT_ID.incrementAndGet();
    private final ResourceLocation textureLocation = Hemomancy.rloc("dynamic/skill_trace_layer_" + textureId);
    private final ResourceLocation heartbeatTextureLocation = Hemomancy.rloc("dynamic/skill_trace_heartbeat_" + textureId);

    private DynamicTexture texture;
    private DynamicTexture heartbeatTexture;
    private String signature = "";
    private int textureW;
    private int textureH;
    private List<FlowTrace> flowTraces = List.of();

    void rebuildIfNeeded(Map<SkillPoint, int[]> nodePositions, int contentW, int contentH) {
        rebuildIfNeeded(nodePositions, contentW, contentH, false, null);
    }

    void rebuildIfNeeded(Map<SkillPoint, int[]> nodePositions, int contentW, int contentH, boolean anchorMissingParents) {
        rebuildIfNeeded(nodePositions, contentW, contentH, anchorMissingParents, null);
    }

    void rebuildIfNeeded(Map<SkillPoint, int[]> nodePositions, int contentW, int contentH, boolean anchorMissingParents, SkillTreeLayer layer) {
        String nextSignature = buildSignature(nodePositions, contentW, contentH, anchorMissingParents, layer);
        if (nextSignature.equals(signature) && texture != null) {
            return;
        }

        signature = nextSignature;
        textureW = Math.max(1, contentW);
        textureH = Math.max(1, contentH);
        NativeImage image = new NativeImage(textureW, textureH, false);
        NativeImage heartbeatImage = new NativeImage(textureW, textureH, false);
        clearImage(image);
        clearImage(heartbeatImage);

        for (int degree = 0; degree < DEGREE_RING_RADII.length; degree++) {
            if (layer != null && SkillTreeLayerRules.layerForDegree(degree) != layer) continue;
            int radius = layer == null ? DEGREE_RING_RADII[degree]
                    : SkillTreeLayerRules.ringRadiusForDegree(degree, layer);
            bakeDegreeRing(image, COMPASS_CENTER_X, COMPASS_CENTER_Y, radius, TRACE_DEGREE_RING);
            bakeDegreeRing(heartbeatImage, COMPASS_CENTER_X, COMPASS_CENTER_Y, radius, withAlpha(TRACE_DEGREE_RING, 24));
        }

        List<FlowTrace> traces = new ArrayList<>();
        for (SkillPoint sp : sortedSkills(nodePositions)) {
            for (SkillPoint parent : sp.getParents()) {
                int[] childPos = nodePositions.get(sp);
                int[] parentPos = nodePositions.get(parent);
                if (childPos == null) continue;
                if (parentPos == null) {
                    if (!anchorMissingParents) continue;
					if (layer != null && SkillTreeLayerRules.layerForDegree(parent.getRequiredDegree()) == layer) continue;
                    parentPos = new int[] {COMPASS_CENTER_X, COMPASS_CENTER_Y};
                }

                boolean parentUnlocked = SkillProgressClientCache.current().getState(parent) == EnumSkillStates.UNLOCKED;
                int branchColor = parentUnlocked ? branchTraceColor(parent) : dimTraceColor(branchTraceColor(parent));
                int bakedColor = withAlpha(branchColor, parentUnlocked ? 148 : 78);
                List<TracePoint> points = sampleCubic(parentPos[0], parentPos[1], childPos[0], childPos[1]);
                bakePointTrace(image, points, bakedColor);
                bakePointTrace(heartbeatImage, points, heartbeatTraceColor(branchColor, parentUnlocked));
                traces.add(new FlowTrace(points, withAlpha(branchColor, parentUnlocked ? 160 : 70), sp.getId() * 31 + parent.getId()));
            }
        }

        flowTraces = List.copyOf(traces);
        upload(image, false);
        upload(heartbeatImage, true);
    }

    void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom) {
        render(gfx, ctx, panZoom, 1.0f);
    }

    void render(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float alpha) {
        if (texture == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, clamp01(alpha));
        PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(ctx.guiLeft() + panZoom.panX, ctx.guiTop() + panZoom.panY, 0);
        pose.scale(panZoom.zoom, panZoom.zoom, 1.0f);
        gfx.blit(textureLocation, 0, 0, 0, 0, textureW, textureH, textureW, textureH);
        pose.popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    void renderHeartbeat(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float animTime) {
        renderHeartbeat(gfx, ctx, panZoom, animTime, 1.0f);
    }

    void renderHeartbeat(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float animTime, float alpha) {
        if (heartbeatTexture == null) return;
        float pulse = heartbeatPulse(animTime);
        if (pulse <= 0.015f) return;
        float clampedAlpha = clamp01(alpha);
        if (clampedAlpha <= 0.01f) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, HEARTBEAT_MAX_ALPHA * pulse * clampedAlpha);
        renderBranchHeartbeatTexture(gfx, ctx, panZoom, 1.0f + HEARTBEAT_EXPANSION * pulse);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        renderCenterHeartbeat(gfx, ctx, panZoom, pulse, clampedAlpha);
    }

    void renderFlow(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float animTime) {
        renderFlow(gfx, ctx, panZoom, animTime, 1.0f);
    }

    void renderFlow(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float animTime, float alpha) {
        if (flowTraces.isEmpty() || panZoom.zoom < 0.45f) return;
        float clampedAlpha = clamp01(alpha);
        if (clampedAlpha <= 0.01f) return;

        int size = panZoom.zoom >= 1.85f ? 2 : 1;
        for (FlowTrace trace : flowTraces) {
            if (trace.points().isEmpty()) continue;
            int count = trace.points().size();
            int index = Math.floorMod((int) (animTime * 18.0f + trace.phase()), count);
            TracePoint point = trace.points().get(index);
            int x = panZoom.sx(ctx.guiLeft(), point.x());
            int y = panZoom.sy(ctx.guiTop(), point.y());
            int half = size / 2;
            gfx.fill(x - half, y - half, x - half + size, y - half + size, multiplyAlpha(trace.color(), clampedAlpha));
        }
    }

    void renderPortalPulse(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float progress, boolean locked) {
        float pulse = clamp01(progress);
        if (pulse <= 0.01f) return;
        int centerX = panZoom.sx(ctx.guiLeft(), COMPASS_CENTER_X);
        int centerY = panZoom.sy(ctx.guiTop(), COMPASS_CENTER_Y);
        int baseRadius = Math.max(10, Math.round((18.0f + 10.0f * pulse) * panZoom.zoom));
        int ringRadius = baseRadius + Math.max(7, Math.round(18.0f * panZoom.zoom * pulse));
        int glowColor = locked ? 0xFF4A0B10 : 0xFFD00000;
        int sourceColor = locked ? 0xFF6C1118 : CENTER_HEARTBEAT_SOURCE_COLOR;
        fillCenterCircle(gfx, centerX, centerY, ringRadius + 7, withAlpha(glowColor, (int) (86 * pulse)));
        fillCenterCircle(gfx, centerX, centerY, baseRadius, withAlpha(sourceColor, (int) (130 * pulse)));
        drawCenterPulseRing(gfx, centerX, centerY, ringRadius, Math.max(2, ringRadius / 10), withAlpha(sourceColor, (int) (230 * pulse)));
    }

    void renderScreenTransitionPulse(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float progress, boolean enteringDeep) {
        float pulse = clamp01(progress);
        if (pulse <= 0.01f) return;
        float elapsed = 1.0f - pulse;
        float eased = 1.0f - (1.0f - elapsed) * (1.0f - elapsed);
        int centerX = panZoom.sx(ctx.guiLeft(), COMPASS_CENTER_X);
        int centerY = panZoom.sy(ctx.guiTop(), COMPASS_CENTER_Y);
        int maxRadius = Math.max(ctx.guiWidth(), ctx.guiHeight()) / 2 + 64;
        int ringRadius = 18 + Math.round(maxRadius * eased);
        int sourceColor = enteringDeep ? 0xFFE2241E : 0xFFD66458;
        int glowColor = enteringDeep ? 0xFF7A0208 : 0xFFB3161E;
        drawCenterPulseRing(gfx, centerX, centerY, ringRadius, Math.max(2, ringRadius / 30), withAlpha(sourceColor, Math.round(220 * pulse)));
        drawCenterPulseRing(gfx, centerX, centerY, Math.max(4, ringRadius - 8), 1, withAlpha(glowColor, Math.round(120 * pulse)));
    }

    private void upload(NativeImage image, boolean heartbeat) {
        DynamicTexture targetTexture = heartbeat ? heartbeatTexture : texture;
        ResourceLocation targetLocation = heartbeat ? heartbeatTextureLocation : textureLocation;
        if (targetTexture == null || targetTexture.getPixels() == null || targetTexture.getPixels().getWidth() != textureW
                || targetTexture.getPixels().getHeight() != textureH) {
            targetTexture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(targetLocation, targetTexture);
            if (heartbeat) {
                heartbeatTexture = targetTexture;
            } else {
                texture = targetTexture;
            }
            return;
        }

        NativeImage target = targetTexture.getPixels();
        for (int y = 0; y < textureH; y++) {
            for (int x = 0; x < textureW; x++) {
                target.setPixelRGBA(x, y, image.getPixelRGBA(x, y));
            }
        }
        targetTexture.upload();
        image.close();
    }

	void close() {
		if (texture != null) Minecraft.getInstance().getTextureManager().release(textureLocation);
		if (heartbeatTexture != null) Minecraft.getInstance().getTextureManager().release(heartbeatTextureLocation);
		texture = null;
		heartbeatTexture = null;
		signature = "";
		flowTraces = List.of();
		textureW = 0;
		textureH = 0;
	}

    private void renderBranchHeartbeatTexture(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float expansion) {
        renderPulsedTexture(gfx, ctx, panZoom, heartbeatTextureLocation, expansion);
    }

    private void renderPulsedTexture(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, ResourceLocation location, float expansion) {
        PoseStack pose = gfx.pose();
        pose.pushPose();
        pose.translate(ctx.guiLeft() + panZoom.panX + COMPASS_CENTER_X * panZoom.zoom,
                ctx.guiTop() + panZoom.panY + COMPASS_CENTER_Y * panZoom.zoom, 0);
        pose.scale(panZoom.zoom * expansion, panZoom.zoom * expansion, 1.0f);
        pose.translate(-COMPASS_CENTER_X, -COMPASS_CENTER_Y, 0);
        gfx.blit(location, 0, 0, 0, 0, textureW, textureH, textureW, textureH);
        pose.popPose();
    }

    private static void renderCenterHeartbeat(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom, float pulse, float alpha) {
        int centerX = panZoom.sx(ctx.guiLeft(), COMPASS_CENTER_X);
        int centerY = panZoom.sy(ctx.guiTop(), COMPASS_CENTER_Y);
        int baseRadius = Math.max(9, Math.round(16.0f * panZoom.zoom));
        int pulseRadius = baseRadius + Math.max(4, Math.round(10.0f * panZoom.zoom * pulse));
        int glowRadius = pulseRadius + Math.max(4, Math.round(6.0f * panZoom.zoom));

        int glowAlpha = Math.min(118, Math.max(18, (int) (92.0f * pulse * alpha)));
        int sourceAlpha = Math.min(230, Math.max(48, (int) (190.0f * pulse * alpha)));
        int frameAlpha = Math.min(255, Math.max(76, (int) (225.0f * pulse * alpha)));

        fillCenterCircle(gfx, centerX, centerY, glowRadius, withAlpha(CENTER_HEARTBEAT_GLOW_COLOR, glowAlpha / 3));
        fillCenterCircle(gfx, centerX, centerY, Math.max(baseRadius, glowRadius - 5), withAlpha(CENTER_HEARTBEAT_GLOW_COLOR, glowAlpha));
        fillCenterCircle(gfx, centerX, centerY, Math.max(3, baseRadius / 2), withAlpha(CENTER_HEARTBEAT_SOURCE_COLOR, sourceAlpha));
        drawCenterPulseRing(gfx, centerX, centerY, pulseRadius, Math.max(1, pulseRadius / 12), withAlpha(CENTER_HEARTBEAT_SOURCE_COLOR, frameAlpha));
    }

    private static void fillCenterCircle(GuiGraphics gfx, int centerX, int centerY, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int halfWidth = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            gfx.fill(centerX - halfWidth, centerY + dy, centerX + halfWidth + 1, centerY + dy + 1, color);
        }
    }

    private static void drawCenterPulseRing(GuiGraphics gfx, int centerX, int centerY, int radius, int thickness, int color) {
        int innerRadius = Math.max(0, radius - thickness);
        for (int dy = -radius; dy <= radius; dy++) {
            int outerHalf = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            if (Math.abs(dy) > innerRadius) {
                gfx.fill(centerX - outerHalf, centerY + dy, centerX + outerHalf + 1, centerY + dy + 1, color);
                continue;
            }
            int innerHalf = (int) Math.round(Math.sqrt(innerRadius * innerRadius - dy * dy));
            gfx.fill(centerX - outerHalf, centerY + dy, centerX - innerHalf + 1, centerY + dy + 1, color);
            gfx.fill(centerX + innerHalf, centerY + dy, centerX + outerHalf + 1, centerY + dy + 1, color);
        }
    }

    private static String buildSignature(Map<SkillPoint, int[]> nodePositions, int contentW, int contentH, boolean anchorMissingParents, SkillTreeLayer layer) {
        StringBuilder out = new StringBuilder();
        out.append(contentW).append('x').append(contentH).append(anchorMissingParents ? ":anchor" : ":local")
                .append(':').append(layer == null ? "all" : layer.name());
        for (SkillPoint sp : sortedSkills(nodePositions)) {
            int[] pos = nodePositions.get(sp);
            out.append('|').append(sp.getId())
                    .append('@').append(pos[0]).append(',').append(pos[1])
                    .append(':').append(sp.getBranch())
                    .append(':').append(sp.getBranchColor());
            for (SkillPoint parent : sp.getParents()) {
                out.append(':').append(parent.getId());
            }
            out.append(':').append(SkillProgressClientCache.current().getState(sp).name());
        }
        return out.toString();
    }

    private static List<SkillPoint> sortedSkills(Map<SkillPoint, int[]> nodePositions) {
        List<SkillPoint> skills = new ArrayList<>(nodePositions.keySet());
        skills.sort(Comparator.comparingInt(SkillPoint::getId));
        return skills;
    }

    private static int branchTraceColor(SkillPoint sp) {
        return sp.getBranchColor();
    }

    private static int dimTraceColor(int color) {
        return (color & 0x00FFFFFF) | 0x88000000;
    }

    private static int heartbeatTraceColor(int branchColor, boolean parentUnlocked) {
        return withAlpha(branchColor, parentUnlocked ? HEARTBEAT_TRACE_ALPHA_BOOST : HEARTBEAT_LOCKED_TRACE_ALPHA);
    }

    private static List<TracePoint> sampleCubic(int x1, int y1, int x2, int y2) {
        int[] start = trimTraceEndpoint(x1, y1, x2, y2);
        int[] end = trimTraceEndpoint(x2, y2, x1, y1);
        double[] controls = cubicControls(start[0], start[1], end[0], end[1]);
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

    private static double[] cubicControls(int x1, int y1, int x2, int y2) {
        double distance = Math.hypot(x2 - x1, y2 - y1);
        double handle = Math.max(36.0, Math.min(120.0, distance * 0.34));
        double[] fromRadial = radialFromCenter(x1, y1, x2 - x1, y2 - y1);
        double[] toRadial = radialFromCenter(x2, y2, x2 - x1, y2 - y1);
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

    private static double[] radialFromCenter(int x, int y, int fallbackX, int fallbackY) {
        double dx = x - COMPASS_CENTER_X;
        double dy = y - COMPASS_CENTER_Y;
        double length = Math.hypot(dx, dy);
        if (length < 0.001) {
            dx = fallbackX;
            dy = fallbackY;
            length = Math.hypot(dx, dy);
        }
        if (length < 0.001) return new double[] {0.0, -1.0};
        return new double[] {dx / length, dy / length};
    }

    private static void bakePointTrace(NativeImage image, List<TracePoint> points, int color) {
        if (points.size() < 2) return;
        TracePoint prev = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            TracePoint next = points.get(i);
            bakeLine(image, prev.x(), prev.y(), next.x(), next.y(), color);
            prev = next;
        }
    }

    private static void bakeDegreeRing(NativeImage image, int centerX, int centerY, int radius, int color) {
        int segments = ConcentricRingStyle.segmentCount(radius);
        int prevX = centerX + radius;
        int prevY = centerY;
        for (int i = 1; i <= segments; i++) {
            double angle = Math.PI * 2.0 * i / segments;
            int x = centerX + (int) Math.round(Math.cos(angle) * radius);
            int y = centerY + (int) Math.round(Math.sin(angle) * radius);
            bakeLine(image, prevX, prevY, x, y, color);
            prevX = x;
            prevY = y;
        }
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

    private static int multiplyAlpha(int color, float multiplier) {
        int alpha = (color >>> 24) & 0xFF;
        return withAlpha(color, Math.round(alpha * clamp01(multiplier)));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static float heartbeatPulse(float animTime) {
        float phase = (animTime % HEARTBEAT_PERIOD_SECONDS) / HEARTBEAT_PERIOD_SECONDS;
        float firstBeat = pulseAt(phase, 0.08f, 0.045f);
        float secondBeat = 0.68f * pulseAt(phase, 0.24f, 0.055f);
        return Math.max(firstBeat, secondBeat);
    }

    private static float pulseAt(float phase, float center, float width) {
        float distance = phase - center;
        return (float) Math.exp(-(distance * distance) / (2.0f * width * width));
    }

    private record TracePoint(int x, int y) {}

    private record FlowTrace(List<TracePoint> points, int color, int phase) {}
}
