package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FerricDuctilisGeometryTest {
    @Test void chargedFocalPointsStayOnTheCrosshairRay() {
        Vec3 aim=new Vec3(.31,-.22,.925).normalize();
        for(var form:new Form[]{Form.LIGHTNING_CHARGE,Form.IRON_CHARGE,Form.THREAD_CHARGE,
                Form.NEEDLE_CHARGE,Form.FAN_CHARGE,Form.LANCE_CHARGE,Form.MORTAR_CHARGE,
                Form.ANEURYSM_CHARGE,Form.GAZE_CHARGE}) {
            Vec3 focus=ChargeVisualGeometry.focus(form,aim);
            assertEquals(0,focus.cross(aim).length(),1e-9,form+" shifted off the aim ray");
            assertTrue(focus.dot(aim)>0,form+" rendered behind the player");
        }
    }

    @Test void everyNerveStateAndPlateRemainsVisibleWithoutParticleGeometry() {
        for(var form:Form.values())if(FerricDuctilisGeometry.handles(form))for(float phase:new float[]{1,12,40}) {
            FerricDuctilisGeometry.begin();var output=new Vertices();
            FerricDuctilisGeometry.draw(new ManipulationVisualPacket(form,4,Vec3.ZERO,new Vec3(0,0,4),2,60,3),new PoseStack(),output,output,
                    new Vec3(0,1,-4),new Vec3(1,0,0),new Vec3(0,1,0),1000+phase,phase,.6f,.7f,null);
            assertTrue(output.count>0,form+" vanished at "+phase);assertEquals(0,output.count%4);
        }
    }
    @Test void edgeOnRibbonsStayFiniteAndDenseCombatStopsAtFrameBudget() {
        FerricDuctilisGeometry.begin();var output=new Vertices();
        for(int i=0;i<700;i++)FerricDuctilisGeometry.ribbon(new PoseStack(),output,Vec3.ZERO,new Vec3(0,1,0),.03,.06,100,i,
                new Vec3(0,4,0),0xFFE8A0,1);
        assertTrue(output.count>0);assertTrue(output.count<=512*12*4,"Ribbon budget leaked");
        int before=output.count;
        FerricDuctilisGeometry.ribbon(new PoseStack(),output,Vec3.ZERO,new Vec3(0,1,0),.03,.06,100,99,Vec3.ZERO,0xFFFFFF,1);
        assertEquals(before,output.count);
        FerricDuctilisGeometry.begin();FerricDuctilisGeometry.ribbon(new PoseStack(),output,Vec3.ZERO,new Vec3(0,1,0),.03,.06,100,99,Vec3.ZERO,0xFFFFFF,1);
        assertTrue(output.count>before,"Next frame retained the exhausted budget");
    }
    private static final class Vertices implements VertexConsumer {
        int count;
        public VertexConsumer addVertex(float x,float y,float z){assertTrue(Float.isFinite(x+y+z));count++;return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        public VertexConsumer setUv(float u,float v){assertTrue(Float.isFinite(u+v));return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
}
