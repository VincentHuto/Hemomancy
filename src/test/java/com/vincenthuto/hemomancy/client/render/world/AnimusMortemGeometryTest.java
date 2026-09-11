package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AnimusMortemGeometryTest {
    @Test void bloodAndDecayFormsStayFiniteAndWithinTheSurfaceBudget() {
        for(Form form:Form.values()) if(AnimusMortemGeometry.handles(form))
            for(float age:new float[]{0,3,12,30}) {
                Capture mesh=draw(form,age);
                assertFalse(mesh.points.isEmpty(),form.name());
                Capture authoredSword=new Capture();VisceralMesh.sword(new PoseStack(),authoredSword,1,100);
                int budget=form==Form.CROWN?3*(authoredSword.points.size()+48):form==Form.CROWN_CHARGE?8*(authoredSword.points.size()+48):1600;
                assertTrue(mesh.points.size()<=budget,form+" exceeded surface budget: "+mesh.points.size());
                assertTrue(mesh.points.stream().allMatch(p->Double.isFinite(p.lengthSqr())),form.name());
                assertEquals(0,mesh.points.size()%4);
            }
    }
    @Test void spectralBellIsHollowAndVisibleFromTheSide() {
        Capture mesh=draw(Form.BELL,12);
        assertTrue(mesh.points.size()<220,"Bell should use thin sheets rather than inner and outer lathed walls");
        assertTrue(mesh.points.stream().anyMatch(p->Math.abs(p.z)>.2));
    }
    @Test void distinctSchoolsDoNotCaptureUnrelatedSharedForms() {
        assertFalse(AnimusMortemGeometry.handles(Form.GAZE_CHARGE));
        assertFalse(AnimusMortemGeometry.handles(Form.SWORD_IMPACT));
        assertFalse(AnimusMortemGeometry.handles(Form.IRON_HEART));
    }
    private Capture draw(Form form,float age) {
        Capture capture=new Capture();
        AnimusMortemGeometry.draw(new ManipulationVisualPacket(form,4,Vec3.ZERO,new Vec3(0,0,6),1,60,3),
                new PoseStack(),capture,new Vec3(4,3,-6),new Vec3(1,0,0),new Vec3(0,1,0),100+age,age,1,1);
        return capture;
    }
    static final class Capture implements VertexConsumer {
        final List<Vec3> points=new ArrayList<>();
        public VertexConsumer addVertex(float x,float y,float z){points.add(new Vec3(x,y,z));return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        public VertexConsumer setUv(float u,float v){assertTrue(Float.isFinite(u)&&Float.isFinite(v));return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
}
