package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillLayout;
import com.vincenthuto.hemomancy.common.entity.utility.arbor.ArborOfWillVisualRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborMeshEmitterTest {
    private static final String[] FAMILIES = {"core", "living_staff", "mycelial", "scars", "covenant", "summons"};

    @Test
    void culledStaticLayersWindOutwardAtFullGrowth() {
        for (int degree : new int[]{7, 8}) {
            Recorder bark = new Recorder(), mycelium = new Recorder(), leaves = new Recorder(), fungus = new Recorder();
            ArborMeshEmitter.emitStaticTree(ArborStaticVisualKey.create(degree, 10, 1.0F, 9, 0xF000F0),
                    placements(), new ArborMeshEmitter.MeshConsumers(bark, mycelium, leaves, fungus));

            bark.assertOutward("bark D" + degree);
            mycelium.assertOutward("mycelium D" + degree);
            fungus.assertOutward("fungus D" + degree);
            assertEquals(0, leaves.vertexCount() % ArborRenderBudget.leafBladeVertices(),
                    "only flat leaf blades belong in the double-sided layer");
        }
    }

    @Test
    void everyFruitBodyWindsOutward() {
        for (String family : FAMILIES) {
            for (ArborOfWillVisualRules.GrowthState state : ArborOfWillVisualRules.GrowthState.values()) {
                Recorder bark = new Recorder(), mycelium = new Recorder(), leaves = new Recorder(), fungus = new Recorder();
                ArborFruitMeshes.emit(new ArborMeshEmitter.MeshConsumers(bark, mycelium, leaves, fungus),
                        ArborFruitMeshKey.create(family, state, 1, 1));
                bark.assertOutward(family + " " + state + " bark");
                mycelium.assertOutward(family + " " + state + " mycelium");
                assertEquals(0, leaves.vertexCount() + fungus.vertexCount(), "fruits only use culled layers");
            }
        }
    }

    @Test
    void degreeSevenStaticTreeStaysWithinBudget() {
        Recorder bark = new Recorder(), mycelium = new Recorder(), leaves = new Recorder(), fungus = new Recorder();
        ArborMeshEmitter.emitStaticTree(ArborStaticVisualKey.create(7, 10, 1.0F, 9, 0xF000F0),
                placements(), new ArborMeshEmitter.MeshConsumers(bark, mycelium, leaves, fungus));
        int total = bark.vertexCount() + mycelium.vertexCount() + leaves.vertexCount();
        // Full-foliage D7 emitted ~283k vertices before culling/budgets and ~195k before the bough redesign;
        // willow streamers and the furrowed bark (with all nine Pome gashes) account for most of what remains.
        assertTrue(total < 130_000, "static D7 tree emitted " + total + " vertices");
    }

    @Test
    void everyFruitMeshStaysWithinItsVertexBudget() {
        for (String family : FAMILIES) {
            for (ArborOfWillVisualRules.GrowthState state : ArborOfWillVisualRules.GrowthState.values()) {
                Recorder all = new Recorder();
                ArborFruitMeshes.emit(new ArborMeshEmitter.MeshConsumers(all, all, all, all),
                        ArborFruitMeshKey.create(family, state, 1, 1));
                assertTrue(all.vertexCount() > 0, family + " " + state + " emitted nothing");
                assertTrue(all.vertexCount() <= 2_000, family + " " + state + " emitted " + all.vertexCount() + " vertices");
            }
        }
    }

    private static List<ArborOfWillLayout.FruitPlacement> placements() {
        List<ArborOfWillLayout.SkillSpec> specs = new ArrayList<>();
        Map<String, Integer> slots = new HashMap<>();
        for (int id = 1; id <= 73; id++) {
            String family = FAMILIES[id % FAMILIES.length];
            int degree = 1 + (id * 5) % 7;
            int slot = slots.merge(family + degree, 1, Integer::sum) - 1;
            specs.add(new ArborOfWillLayout.SkillSpec(id, family, degree, id % 4, slot));
        }
        return ArborOfWillLayout.place(specs, 10, 7);
    }

    /** Records transformed positions and normals so quad winding can be checked against shading normals. */
    private static final class Recorder implements VertexConsumer {
        private final List<float[]> positions = new ArrayList<>();
        private final List<float[]> normals = new ArrayList<>();

        int vertexCount() { return positions.size(); }

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            positions.add(new float[]{x, y, z});
            return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) {
            normals.add(new float[]{x, y, z});
            return this;
        }

        void assertOutward(String label) {
            assertEquals(0, positions.size() % 4, label + " must emit whole quads");
            int inward = 0;
            for (int quad = 0; quad < positions.size(); quad += 4) {
                // Newell's method stays stable for the degenerate quads at caps, poles and pointed tips.
                double nx = 0, ny = 0, nz = 0;
                for (int i = 0; i < 4; i++) {
                    float[] a = positions.get(quad + i), b = positions.get(quad + (i + 1) % 4);
                    nx += (a[1] - b[1]) * (a[2] + b[2]);
                    ny += (a[2] - b[2]) * (a[0] + b[0]);
                    nz += (a[0] - b[0]) * (a[1] + b[1]);
                }
                if (nx * nx + ny * ny + nz * nz < 1.0e-14) continue;
                double sx = 0, sy = 0, sz = 0;
                for (int i = 0; i < 4; i++) {
                    float[] n = normals.get(quad + i);
                    sx += n[0]; sy += n[1]; sz += n[2];
                }
                if (nx * sx + ny * sy + nz * sz <= 0) inward++;
            }
            assertEquals(0, inward, label + " has " + inward + " quads wound against their shading normal");
        }
    }
}
