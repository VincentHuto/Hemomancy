package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class VisceralMeshContinuityTest {
    @Test void shadowBodyAndLimbStrandsShareEveryRingVertex() {
        for (double phase : new double[]{-1, 0, 1, 2.7}) {
            for (Vec3 end : List.of(new Vec3(.4, 1.5, -.18), new Vec3(.46, -.2, 0),
                    new Vec3(-.5, -.8, -.25), new Vec3(.12, -.3, 0))) {
                Capture mesh = draw(end, phase);
                assertEquals(12 * 6 * 4, mesh.points.size(), "Keep the existing mesh budget");
                for (int ring = 1; ring < 12; ring++) for (int face = 0; face < 6; face++) {
                    int previous = ((ring - 1) * 6 + face) * 4;
                    int next = (ring * 6 + face) * 4;
                    assertEquals(mesh.points.get(previous + 3), mesh.points.get(next), "Open tube joint");
                    assertEquals(mesh.points.get(previous + 2), mesh.points.get(next + 1), "Open tube joint");
                }
                assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr())));
            }
        }
    }

    @Test void textureCoordinatesContinueAcrossCurvedTaperedJoins() {
        Capture mesh = draw(new Vec3(.46, -.2, 0), 1);
        for (int ring = 1; ring < 12; ring++) for (int face = 0; face < 6; face++) {
            int previous = ((ring - 1) * 6 + face) * 4;
            int next = (ring * 6 + face) * 4;
            assertArrayEquals(mesh.uvs.get(previous + 3), mesh.uvs.get(next), 0F, "Texture restarted at a joint");
            assertArrayEquals(mesh.uvs.get(previous + 2), mesh.uvs.get(next + 1), 0F, "Texture restarted at a joint");
        }
    }

    private static Capture draw(Vec3 end, double phase) {
        Capture mesh = new Capture();
        VisceralMesh.strand(new PoseStack(), mesh, Vec3.ZERO, end, .12, .216, phase, 0x390D24, .7F);
        return mesh;
    }
    private static final class Capture implements VertexConsumer {
        final List<Vec3> points = new ArrayList<>();
        final List<float[]> uvs = new ArrayList<>();
        public VertexConsumer addVertex(float x, float y, float z) { points.add(new Vec3(x,y,z)); return this; }
        public VertexConsumer setColor(int r,int g,int b,int a) { return this; }
        public VertexConsumer setUv(float u,float v) { uvs.add(new float[]{u,v}); return this; }
        public VertexConsumer setUv1(int u,int v) { return this; }
        public VertexConsumer setUv2(int u,int v) { return this; }
        public VertexConsumer setNormal(float x,float y,float z) { return this; }
    }
}
