package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Still Art rings read pale in any light. The 2026-10-19 captures showed charcoal rings (mean RGB about 75/73/63): thin
 * tubes on the textured LUX material sampled the texture's dark corner texel (97/255) and shaded the colour further.
 */
class StillArtRingsTest {
    @Test void handRingIsFlatPaleAndUntextured() {
        for (boolean first : new boolean[]{true, false}) for (float time : new float[]{1, 4, 8, 12}) {
            Capture ring = new Capture();
            StillArtRings.hand(new PoseStack(), ring, time, 1, first);
            assertFalse(ring.points.isEmpty(), "ring drawn at " + time);
            assertEquals(0, ring.uvs, "untextured: no UVs");
            assertTrue(ring.colors.stream().allMatch(c -> c[0] >= 0xC9 && c[1] >= 0xC9 && c[2] >= 0xC9), "pale, never shaded dark");
            assertTrue(ring.colors.stream().allMatch(c -> c[0] <= c[2]), "no blood tint");
            assertTrue(ring.colors.stream().anyMatch(c -> c[0] == 0xF4 && c[1] == 0xF6 && c[2] == 0xF8), "white core");
            assertTrue(ring.colors.stream().anyMatch(c -> c[0] == 0xC9 && c[1] == 0xD1 && c[2] == 0xD9), "silver edge");
        }
    }
    @Test void groundRingGrowsWithStageAndFades() {
        double previous = 0;
        for (int stage = 1; stage <= 4; stage++) {
            Capture ring = new Capture();
            StillArtRings.ground(new PoseStack(), ring, stage, 8, 1);
            double radius = ring.points.stream().mapToDouble(p -> Math.hypot(p.x, p.z)).max().orElseThrow();
            assertTrue(radius > previous, "stage " + stage + " grows");
            previous = radius;
            assertEquals(0, ring.uvs);
            assertTrue(ring.colors.stream().allMatch(c -> c[0] >= 0xC9 && c[0] <= c[2]), "pale, no blood tint");
        }
        Capture faded = new Capture();
        StillArtRings.ground(new PoseStack(), faded, 4, 8, .5F);
        assertTrue(faded.colors.stream().allMatch(c -> c[3] > 0 && c[3] <= 128));
    }
    @Test void stillArtRingsUseTheUntexturedRingType() throws Exception {
        var source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/world/CastingAnatomyRenderer.java"));
        String still = source.substring(source.indexOf("public static void stillHand"));
        assertFalse(still.contains("ManipulationMaterials"), "Still Art rings must not sample the textured LUX material");
        assertTrue(still.contains("StillArtRings.renderType()"), "Still Art rings draw through their untextured, unlit type");
    }
    private static final class Capture implements VertexConsumer {
        final List<Vec3> points = new ArrayList<>();
        final List<int[]> colors = new ArrayList<>();
        int uvs;
        public VertexConsumer addVertex(float x,float y,float z) { points.add(new Vec3(x,y,z)); return this; }
        public VertexConsumer setColor(int r,int g,int b,int a) { colors.add(new int[]{r,g,b,a}); return this; }
        public VertexConsumer setUv(float u,float v) { uvs++; return this; }
        public VertexConsumer setUv1(int u,int v) { return this; }
        public VertexConsumer setUv2(int u,int v) { return this; }
        public VertexConsumer setNormal(float x,float y,float z) { return this; }
    }
}
