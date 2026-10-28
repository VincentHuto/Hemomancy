package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintAttackPattern;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaintPatternGeometryTest {
    @Test void everyWarningImpactAndRecoveryIsFiniteBoundedAndExpires() {
        for(var move:SaintAttackPattern.values())for(boolean solid:new boolean[]{false,true})
            for(float age:new float[]{0,move.windup()-1,move.windup(),move.windup()+move.active()/2F,move.duration()-1,move.duration()}) {
                var mesh=new Capture();
                var form=move.id()<=2?Form.HEMORATH_RELIQUARY:move.id()<=4?Form.SERAPHAE_RELIQUARY:move.id()<=6?Form.PUTRICIEL_OFFERING:Form.VELORUM_RIME;
                var packet=new ManipulationVisualPacket(form,17,Vec3.ZERO,new Vec3(0,0,8),(float)move.radius(),move.duration(),move.cue());
                assertTrue(SaintPatternGeometry.handles(packet));
                SaintPatternGeometry.draw(packet,new PoseStack(),mesh,mesh,mesh,solid,new Vec3(4,4,-8),new Vec3(1,0,0),new Vec3(0,1,0),age);
                assertTrue(mesh.vertices<=4096,"Unbounded mesh for "+move);
                assertEquals(0,mesh.vertices%4);
                if(age==move.duration())assertEquals(0,mesh.vertices);
                else if(!solid)assertTrue(mesh.vertices>0,move.toString());
            }
    }
    @Test void anotherSpellCannotAccidentallySelectASaintPattern() {
        assertFalse(SaintPatternGeometry.handles(new ManipulationVisualPacket(Form.VERDICT,1,Vec3.ZERO,Vec3.ZERO,1,60,101)));
        assertFalse(SaintPatternGeometry.handles(new ManipulationVisualPacket(Form.SERAPHAE_RELIQUARY,1,Vec3.ZERO,Vec3.ZERO,1,60,101)));
    }
    private static final class Capture implements VertexConsumer {
        int vertices;
        public VertexConsumer addVertex(float x,float y,float z) {
            assertTrue(Float.isFinite(x)&&Float.isFinite(y)&&Float.isFinite(z));
            assertTrue(Math.abs(x)<40 && Math.abs(y)<40 && Math.abs(z)<40);vertices++;return this;
        }
        public VertexConsumer setColor(int r,int g,int b,int a){for(int n:new int[]{r,g,b,a})assertTrue(n>=0&&n<=255);return this;}
        public VertexConsumer setUv(float u,float v){assertTrue(Float.isFinite(u)&&Float.isFinite(v));return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
}
