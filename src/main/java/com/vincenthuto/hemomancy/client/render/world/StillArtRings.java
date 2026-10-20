package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

/**
 * Still Art accents as flat-coloured, untextured, unlit quads: a white core with a silver edge,
 * never shaded and never tinted, so the rings read pale in daylight and in the dark. Pure geometry for JVM checks.
 */
final class StillArtRings {
    static final int PALE = 0xF4F6F8, SILVER = 0xC9D1D9;
    private StillArtRings() {}

    static RenderType renderType() { return Material.TYPE; }

    // Keep render state initialization separate from the geometry used by JVM checks.
    private static final class Material {
        private static final RenderType TYPE = RenderType.create("still_art_rings",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 65536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderType.COLOR_WRITE)
                        .setCullState(RenderType.NO_CULL)
                        .createCompositeState(false));
    }

    /**
     * The hand ring: facing the camera in first person, level around the hand in third person. It pulses once over the
     * release, then its segments leave in a scattered order like a reflection breaking up.
     */
    static void hand(PoseStack p, VertexConsumer v, float time, float fade, boolean first) {
        if (fade <= .01F) return;
        double pulse = Math.sin(Math.PI*Math.clamp(time/8, 0, 1));
        double radius = (first ? .09 : .18)*(1+.25*pulse), core = first ? .012 : .02, edge = first ? .006 : .01;
        double kept = 1-Math.clamp((time-10)/20, 0, 1);
        ring(p, v, radius, core, first, PALE, fade, kept, 20);
        ring(p, v, radius+(core+edge)/2, edge, first, SILVER, fade*.8F, kept, 20);
    }

    /** The still-water ring at the feet: radius 0.6 + 0.25 per stage, expanding over 8 ticks, with a fainter inner ripple. */
    static void ground(PoseStack p, VertexConsumer v, int stage, float time, float fade) {
        if (fade <= .01F) return;
        double grow = 1-Math.pow(1-Math.clamp(time/8, 0, 1), 2);
        double radius = (.6+.25*stage)*(.4+.6*grow);
        p.pushPose();
        p.translate(0, .02, 0);
        ring(p, v, radius, .07, false, PALE, fade, 1, 40);
        ring(p, v, radius+.0425, .015, false, SILVER, fade*.8F, 1, 40);
        ring(p, v, radius*.55, .03, false, PALE, fade*.35F, 1, 32);
        p.popPose();
    }

    /** An annulus in the ring plane plus a thin upright band at its centre line, so it reads edge-on too. */
    private static void ring(PoseStack p, VertexConsumer v, double radius, double width, boolean facing, int color,
                             float alpha, double kept, int segments) {
        int a = Math.round(Math.clamp(alpha, 0, 1)*255);
        if (a <= 0) return;
        double inner = radius-width/2, outer = radius+width/2, half = width/2;
        for (int i = 0; i < segments; i++) {
            if ((i*7%segments)/(double)segments > kept) continue;
            double s = i*Math.PI*2/segments, e = (i+1)*Math.PI*2/segments;
            quad(p, v, point(s, inner, 0, facing), point(e, inner, 0, facing), point(e, outer, 0, facing), point(s, outer, 0, facing), color, a);
            quad(p, v, point(s, radius, -half, facing), point(e, radius, -half, facing), point(e, radius, half, facing), point(s, radius, half, facing), color, a);
        }
    }
    /** Facing rings lie in the camera's x/y plane; level rings lie in x/z. {@code across} is the offset off that plane. */
    private static Vec3 point(double angle, double radius, double across, boolean facing) {
        double x = Math.cos(angle)*radius, y = Math.sin(angle)*radius;
        return facing ? new Vec3(x, y, across) : new Vec3(x, across, y);
    }
    private static void quad(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color, int alpha) {
        for (var point : new Vec3[]{a, b, c, d})
            v.addVertex(p.last().pose(), (float)point.x, (float)point.y, (float)point.z)
                    .setColor(color >> 16 & 255, color >> 8 & 255, color & 255, alpha);
    }
}
