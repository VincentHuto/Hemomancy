package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThermalGeometryTest {
    @Test void activeMaterialsRemainFiniteAcrossFormationAndRetirementWithMinimalParticles() {
        for(Form form:Form.values()) {
            if(!ThermalGeometry.handles(form) || form==Form.FROZEN_VEINS)continue;
            for(float age:new float[]{1,12,38,64}) {
                if(form==Form.FROST_ADVANCE && age<8)continue;
                var packet=new ManipulationVisualPacket(form,17,Vec3.ZERO,new Vec3(0,1,4),3,70,63);
                var fragments=new ThermalFragments();fragments.update(packet,age);
                var batch=new LuxUmbraBatch();var output=new Bounds();
                ThermalGeometry.draw(packet,new PoseStack(),batch,output,Vec3.ZERO,new Vec3(0,0,-5),
                        new Vec3(1,0,0),new Vec3(0,1,0),1000+age,age,.5f,.6f,
                        new ThermalSurface(),fragments,.5f,false);
                batch.drawMaterials(material->output);
                if(form==Form.CAUTERIZE && age>=24)assertEquals(0,output.count,"Cauterization's flame must end after the seam closes");
                else assertTrue(output.count>0,form+" lost its body on Minimal at "+age);
                assertEquals(0,output.count%4,form.name());
            }
        }
    }
    @Test void glassHasThinVolumeWhileCruorHasAHeavyCrossSection() {
        var glass=new Bounds();ThermalGeometry.shard(new PoseStack(),glass,.5,41,1);
        assertTrue(glass.maxZ-glass.minZ>0);
        assertTrue(glass.maxZ-glass.minZ<(glass.maxY-glass.minY)*.1);
        var ice=new Bounds();ThermalGeometry.chunk(new PoseStack(),ice,.5,41,1,0);
        assertTrue(ice.maxZ-ice.minZ>(ice.maxY-ice.minY)*.65);
        assertEquals(0,glass.count%4);assertEquals(0,ice.count%4);
    }
    @Test void allTranslucentMaterialsKeepOneDepthOrderAndClearBetweenFrames() {
        var batch=new LuxUmbraBatch();
        var order=List.of(LuxUmbraBatch.Material.GLASS,LuxUmbraBatch.Material.UMBRA,LuxUmbraBatch.Material.FLAME,
                LuxUmbraBatch.Material.LUX,LuxUmbraBatch.Material.CRUOR,LuxUmbraBatch.Material.MOLTEN);
        for(int i=0;i<order.size();i++)for(int corner=0;corner<4;corner++)
            batch.vertices(order.get(i)).addVertex(0,0,order.size()-i).setColor(255,255,255,170).setUv(corner%2,corner/2);
        List<LuxUmbraBatch.Material> actual=new ArrayList<>();
        batch.drawMaterials(material->{actual.add(material);return new Bounds();});
        assertEquals(order,actual);
        batch.clear();actual.clear();batch.drawMaterials(material->{actual.add(material);return new Bounds();});
        assertTrue(actual.isEmpty());
    }
    private static final class Bounds implements VertexConsumer {
        float minY=Float.POSITIVE_INFINITY,maxY=Float.NEGATIVE_INFINITY,minZ=Float.POSITIVE_INFINITY,maxZ=Float.NEGATIVE_INFINITY;
        int count;
        public VertexConsumer addVertex(float x,float y,float z){assertTrue(Float.isFinite(x+y+z));minY=Math.min(minY,y);maxY=Math.max(maxY,y);minZ=Math.min(minZ,z);maxZ=Math.max(maxZ,z);count++;return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}
        public VertexConsumer setUv(float u,float v){assertTrue(Float.isFinite(u+v));return this;}
        public VertexConsumer setUv1(int u,int v){return this;}
        public VertexConsumer setUv2(int u,int v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
}
