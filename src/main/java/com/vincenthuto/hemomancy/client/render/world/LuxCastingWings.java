package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** A continuous white wing body with overlapping, ridged flight feathers. */
final class LuxCastingWings {
    private static final int FEATHERS = 11;

    private LuxCastingWings() {}

    static RenderType renderType() { return Material.TYPE; }

    // Keep render state initialization separate from the geometry used by JVM checks.
    private static final class Material {
        private static final RenderType TYPE = RenderType.create("lux_casting_wings",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 65536, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(RenderType.COLOR_WRITE)
                        .setCullState(RenderType.NO_CULL)
                        .createCompositeState(false));
    }

    static void draw(PoseStack poses, VertexConsumer vertices, EnumManipulationRank rank, float time, float alpha) {
        if (rank.ordinal() < EnumManipulationRank.SUMMA.ordinal() || alpha <= 0) return;
        float size = switch (rank) {
            case SUMMA -> 1F;
            case MAGISTER -> 1.15F;
            case PERFECTUS -> 1.35F;
            default -> throw new IllegalArgumentException("Wings require Summa or higher");
        };
        float opening = Mth.clamp(time / 6F, 0F, 1F);
        opening = opening * opening * (3F - 2F * opening);
        float fold = .8F * (1F - opening) + .03F * (1F + Mth.sin(time * .04F));
        int opacity = Math.round(Mth.clamp(alpha, 0F, 1F) * 255);
        for (int side : new int[]{-1, 1}) {
            poses.pushPose();
            // Roots stay at shoulder height as higher ranks extend the wingspan.
            poses.translate(side * .22, 1.42, .22);
            poses.mulPose(Axis.YP.rotation(-side * fold));
            poses.scale(side * size, size, size);
            wing(poses, vertices, opacity);
            poses.popPose();
        }
    }

    private static void wing(PoseStack poses, VertexConsumer vertices, int alpha) {
        // The web shares station edges, joining every feather base into a solid silhouette.
        for (int i = 0; i < FEATHERS - 1; i++) {
            double s = i / (double) (FEATHERS - 1), next = (i + 1D) / (FEATHERS - 1);
            quad(poses, vertices, root(s), root(next), root(next).lerp(tip(next), .6),
                    root(s).lerp(tip(s), .6), 0xEDF1F7, alpha);
        }
        for (int i = 0; i < FEATHERS; i++) {
            double s = i / (double) (FEATHERS - 1);
            feather(poses, vertices, root(s), tip(s), .16 - .05 * s, alpha);
        }
        // Short cover feathers overlap the flight feathers on both visible faces.
        for (int i = 0; i < 8; i++) {
            double s = i / 7D;
            feather(poses, vertices, root(s), root(s).lerp(tip(s), .53), .17, alpha);
        }
    }

    private static Vec3 root(double s) {
        return new Vec3(1.6 * s, 1.05 * Math.sin(s * Math.PI / 2), .15 * s);
    }

    private static Vec3 tip(double s) {
        return new Vec3(.2 + 2.45 * s, -.87 + 1.88 * s, .03 + .23 * s);
    }

    private static void feather(PoseStack poses, VertexConsumer vertices, Vec3 base, Vec3 tip,
                                double width, int alpha) {
        Vec3 axis = tip.subtract(base);
        Vec3 across = new Vec3(-axis.y, axis.x, 0).normalize();
        Vec3[] previous = featherRing(base, across, 0, width);
        for (int i = 1; i <= 4; i++) {
            double progress = i / 4D;
            Vec3[] current = featherRing(base.lerp(tip, progress), across, progress, width);
            for (int face = 0; face < 4; face++) {
                int next = (face + 1) % 4;
                quad(poses, vertices, previous[face], previous[next], current[next], current[face],
                        face % 2 == 0 ? 0xFFFFFF : 0xE4EAF2, alpha);
            }
            previous = current;
        }
    }

    private static Vec3[] featherRing(Vec3 center, Vec3 across, double progress, double width) {
        double taper = progress == 0 || progress == 1 ? 0 : Math.pow(Math.sin(Math.PI * progress), .6);
        double radius = width * taper;
        return new Vec3[]{center.add(across.scale(radius)), center.add(0, 0, -.035 * taper),
                center.add(across.scale(-radius)), center.add(0, 0, .035 * taper)};
    }

    private static void quad(PoseStack poses, VertexConsumer vertices, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                             int color, int alpha) {
        vertex(poses, vertices, a, color, alpha);
        vertex(poses, vertices, b, color, alpha);
        vertex(poses, vertices, c, color, alpha);
        vertex(poses, vertices, d, color, alpha);
    }

    private static void vertex(PoseStack poses, VertexConsumer vertices, Vec3 point, int color, int alpha) {
        vertices.addVertex(poses.last().pose(), (float) point.x, (float) point.y, (float) point.z)
                .setColor(color >> 16 & 255, color >> 8 & 255, color & 255, alpha);
    }
}
