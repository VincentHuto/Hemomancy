package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VelorumRimeGeometryTest {
    private static final Vec3 CAMERA=new Vec3(4,4,-8),RIGHT=new Vec3(1,0,0),UP=new Vec3(0,1,0);
    @Test void simultaneousFrostVeilDrainAndResistanceStayFiniteAndBounded() {
        for(int t:new int[]{0,1,8,18,24,40,100}) {
            var mesh=new Capture();
            var phase=new VelorumVisualRules.Phase(t,15,t,19,t,17,30);
            VelorumRimeGeometry.body(new PoseStack(),mesh,mesh,CAMERA,RIGHT,UP,
                    new VelorumRimeGeometry.Frame(phase,100-t,11,new Vec3(0,1.5,0),new Vec3(1,1.8,0),new Vec3[]{new Vec3(-1,2.5,0),new Vec3(-1,1,0),new Vec3(.5,1,0)},Vec3.ZERO));
            mesh.check(1800);assertTrue(mesh.vertices>0);
        }
    }
    @Test void spikesAndEveryBurstRetireWithoutInvalidVertices() {
        for(int kind=1;kind<=6;kind++)for(float age:new float[]{0,1,8,20,39,40}) {
            var mesh=new Capture();
            var packet=new ManipulationVisualPacket(Form.VELORUM_RIME,-1,Vec3.ZERO,new Vec3(8,2,0),10,40,kind);
            VelorumRimeGeometry.draw(packet,new PoseStack(),mesh,mesh,CAMERA,RIGHT,UP,100+age,age,1);
            mesh.check(1800);if(age==40)assertEquals(0,mesh.vertices);else assertTrue(mesh.vertices>0);
        }
        var mesh=new Capture();VelorumRimeGeometry.spire(new PoseStack(),mesh,Vec3.ZERO,.1,0,Vec3.ZERO,1,1);
        assertEquals(0,mesh.vertices);
    }
    @Test void perimeterIncludesTheRealDamageSquareCorners() {
        for(double size:new double[]{8.5,10.5,12.5})for(int i=0;i<32;i++) {
            Vec3 p=VelorumRimeGeometry.perimeter(new Vec3(.5,0,.5),size,i/32.0).subtract(new Vec3(.5,0,.5));
            assertEquals(size,Math.max(Math.abs(p.x),Math.abs(p.z)),.00001);
        }
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
