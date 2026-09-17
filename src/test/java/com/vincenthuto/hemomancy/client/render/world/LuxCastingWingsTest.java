package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LuxCastingWingsTest {
    @Test void wingsStartAtTheShadowFiguresRankAndGrowWithRank() {
        double previousSpan = 0;
        for (var rank : EnumManipulationRank.values()) {
            Capture mesh = draw(rank, 16, 1);
            if (rank.ordinal() < 2) { assertTrue(mesh.points.isEmpty()); continue; }
            double span = mesh.points.stream().mapToDouble(p -> p.x).max().orElseThrow()
                    - mesh.points.stream().mapToDouble(p -> p.x).min().orElseThrow();
            assertTrue(span > 4.5 && span > previousSpan, "Wings must be large and grow by rank");
            previousSpan = span;
            assertTrue(mesh.points.size() < 6000, "Wing mesh exceeded its vertex budget");
            assertTrue(mesh.colors.stream().allMatch(c -> c[0] >= 220 && c[1] >= 220 && c[2] >= 220),
                    "White wings picked up a blood tint");
        }
    }
    @Test void wingsAreOneMirroredPairAttachedBehindTheShoulders() {
        for (float time : new float[]{0, 3, 6, 16, 100, 400}) {
            Capture mesh = draw(EnumManipulationRank.PERFECTUS, time, 1);
            int half = mesh.points.size() / 2;
            for (int i = 0; i < half; i++) {
                var left = mesh.points.get(i); var right = mesh.points.get(i + half);
                assertEquals(-left.x, right.x, .00001);
                assertEquals(left.y, right.y, .00001);
                assertEquals(left.z, right.z, .00001);
                assertTrue(Double.isFinite(left.lengthSqr()) && left.z > .1);
            }
            for (int side : new int[]{-1, 1}) assertTrue(mesh.points.stream()
                    .anyMatch(p -> p.distanceTo(new Vec3(side * .22, 1.42, .22)) < .00001), "Shoulder root drifted");
        }
    }
    @Test void wingsUnfoldAndRespectTheCastingFade() {
        Capture folded = draw(EnumManipulationRank.MAGISTER, 0, 1);
        Capture open = draw(EnumManipulationRank.MAGISTER, 6, 1);
        assertTrue(open.points.stream().mapToDouble(p -> Math.abs(p.x)).max().orElseThrow()
                > folded.points.stream().mapToDouble(p -> Math.abs(p.x)).max().orElseThrow());
        assertTrue(draw(EnumManipulationRank.PERFECTUS, 16, 0).points.isEmpty());
        Capture fading = draw(EnumManipulationRank.PERFECTUS, 16, .5F);
        assertTrue(fading.colors.stream().allMatch(c -> c[3] > 0 && c[3] <= 128));
    }
    private static Capture draw(EnumManipulationRank rank, float time, float alpha) {
        Capture mesh = new Capture();
        LuxCastingWings.draw(new PoseStack(), mesh, rank, time, alpha);
        return mesh;
    }
    private static final class Capture implements VertexConsumer {
        final List<Vec3> points = new ArrayList<>();
        final List<int[]> colors = new ArrayList<>();
        public VertexConsumer addVertex(float x,float y,float z) { points.add(new Vec3(x,y,z)); return this; }
        public VertexConsumer setColor(int r,int g,int b,int a) { colors.add(new int[]{r,g,b,a}); return this; }
        public VertexConsumer setUv(float u,float v) { return this; }
        public VertexConsumer setUv1(int u,int v) { return this; }
        public VertexConsumer setUv2(int u,int v) { return this; }
        public VertexConsumer setNormal(float x,float y,float z) { return this; }
    }
}
