package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PutricielOfferingGeometryTest {
    @Test void cleansingFireIsAbsentOutsideTheAbsolutionWindow() {
        for (boolean open : new boolean[]{false, true}) {
            var rot = new Capture();
            var fire = new Capture();
            var frame = new PutricielOfferingGeometry.Frame(0, 0, open, 40, 100, 0, 0,
                    false, 100, 11, new Vec3(0, 2, 0), new Vec3(1, 2, 0),
                    new Vec3[]{new Vec3(0, 1.6, 0)}, new Vec3[]{new Vec3(-1, 3, 0)}, Vec3.ZERO);
            PutricielOfferingGeometry.body(new PoseStack(), rot, fire, CAMERA, RIGHT, UP, frame);
            assertTrue(rot.vertices > 0);
            if (open) assertTrue(fire.vertices > 0);
            else assertEquals(0, fire.vertices);
        }
    }
    private static final Vec3 CAMERA = new Vec3(4, 4, -8), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);
    @Test void sporesCensersOfferingAndNovaRemainFiniteAndBoundedTogether() {
        for (int visual=0; visual<=2; visual++) for (float remaining : new float[]{0,1,8,18,24,80}) {
            var mesh=new Capture();
            var frame=new PutricielOfferingGeometry.Frame(visual,remaining,true,remaining,remaining,15,3,true,100-remaining,11,
                    new Vec3(0,2,0),new Vec3(1,2,0),new Vec3[]{new Vec3(0,1.6,0),new Vec3(.2,1.8,0),new Vec3(-.2,1.9,0),new Vec3(.1,2,0)},
                    new Vec3[]{new Vec3(-1,3,0),new Vec3(-1.5,3,0)},Vec3.ZERO);
            PutricielOfferingGeometry.body(new PoseStack(),mesh,mesh,CAMERA,RIGHT,UP,frame);
            mesh.check(1800); assertTrue(mesh.vertices>0);
        }
    }
    @Test void everyBurstAndTheRetiringFinaleExpires() {
        for(int kind=1;kind<=7;kind++) for(float age:new float[]{0,1,8,20,39,40}) {
            var mesh=new Capture();
            var packet=new ManipulationVisualPacket(Form.PUTRICIEL_OFFERING,-1,Vec3.ZERO,Vec3.ZERO,12,40,kind);
            PutricielOfferingGeometry.draw(packet,new PoseStack(),mesh,mesh,CAMERA,RIGHT,UP,100+age,age,1);
            mesh.check(1800);
            if(age==40) assertEquals(0,mesh.vertices); else assertTrue(mesh.vertices>0);
        }
        assertFalse(AnimusMortemGeometry.handles(Form.PUTRICIEL_OFFERING));
    }
    @Test void warningMatchesTheBlockCenteredDamageSquareIncludingCorners() {
        Vec3 center=new Vec3(.5,0,.5);
        for(double extent:new double[]{8.5,12.5}) for(int i=0;i<32;i++) {
            Vec3 point=PutricielOfferingGeometry.perimeter(center,extent,i/32.0).subtract(center);
            assertEquals(extent,Math.max(Math.abs(point.x),Math.abs(point.z)),.00001);
        }
        assertEquals(new Vec3(-8,.08,-8),PutricielOfferingGeometry.perimeter(center,8.5,0));
    }
    private static final class Capture implements VertexConsumer {
        int vertices;
        public VertexConsumer addVertex(float x, float y, float z) {
            assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z)); vertices++; return this;
        }
        public VertexConsumer setColor(int r, int g, int b, int a) {
            for (int color : new int[]{r,g,b,a}) assertTrue(color >= 0 && color <= 255);
            return this;
        }
        public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
        void check(int max) { assertEquals(0, vertices % 4); assertTrue(vertices <= max, "Vertices: " + vertices); }
    }
}
