package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.util.HashSet;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

class ManipulationSignatureEffectsTest {
    @Test void allExemplarsHaveTexturedFiniteBoundedMeshesThroughTheirLifecycle() {
        Form[] forms={Form.CROWN,Form.BELL,Form.THREAD,Form.CHOIR,
                Form.CROWN_CHARGE,Form.BELL_CHARGE,Form.THREAD_CHARGE,Form.SWORD_IMPACT};
        for(Form form:forms)for(float age:new float[]{0,3,12,30}) {
            Capture vertices=draw(form,new Vec3(0,8,0),age,8);
            assertFalse(vertices.points.isEmpty(),form+" has no subject");
            assertEquals(vertices.points.size(),vertices.uvCount,form+" has untextured vertices");
            assertTrue(vertices.points.size()<=32768,form+" exceeded the per-effect mesh budget");
            assertTrue(vertices.points.stream().allMatch(p->Double.isFinite(p.lengthSqr())),form+" contains invalid geometry");
        }
    }

    @Test void crownAndChoirTrackTheAuthoritativeRemainingCount() {
        for(Form form:new Form[]{Form.CROWN,Form.CHOIR}) {
            assertEquals(0,draw(form,Vec3.ZERO,20,0).points.size());
            assertEquals(draw(form,Vec3.ZERO,20,1).points.size()*3,draw(form,Vec3.ZERO,20,3).points.size());
        }
    }



    @Test void twentyFourOreLocatorsRemainCheaperThanOneSignatureEffect() {
        Capture vertices=new Capture();
        VisceralMesh.locatorBox(new PoseStack(),vertices,.48,0xFFFFFF,1);
        assertTrue(vertices.points.size()*24<=8192);
    }

    @Test void schoolMaterialsStayOnTheEightToneThirtyTwoPixelGrid() throws Exception {
        for(String school:List.of("animus","mortem","ductilis","ferric","flammeus","congeatio","lux","tenebris")) {
            var texture=ImageIO.read(Path.of("src/main/resources/assets/hemomancy/textures/effect/manipulation",school+".png").toFile());
            assertEquals(32,texture.getWidth(),school);
            assertEquals(32,texture.getHeight(),school);
            var colors=new HashSet<Integer>();
            for(int y=0;y<32;y++)for(int x=0;x<32;x++)colors.add(texture.getRGB(x,y));
            assertTrue(colors.size()<=8,school+" regained fine tonal noise");
        }
    }

    @Test void smallSurfacesDoNotCompressWholeTextureTiles() {
        Capture vertices=new Capture();
        VisceralMesh.quad(new PoseStack(),vertices,Vec3.ZERO,new Vec3(.125,0,0),
                new Vec3(.125,.25,0),new Vec3(0,.25,0),0xFFFFFF,1,0,0,1,1);
        assertEquals(.125,vertices.uvs.get(1)[0],1e-6);
        assertEquals(.25,vertices.uvs.get(2)[1],1e-6);
    }

    @Test void deformationStaysAttachedThroughCameraTransformsAndUvSeams() {
        Capture vertices=new Capture();
        var warped=new UndulatingVertexConsumer(vertices,
                new ManipulationUndulation(Form.BELL,100,20,71,1,Vec3.ZERO));
        Matrix4f identity=new Matrix4f();
        Matrix4f camera=new Matrix4f().rotateY(.7F).translate(-12,-4,6);
        warped.addVertex(identity,.4F,.7F,.2F).setUv(0,0);
        warped.addVertex(identity,.4F,.7F,.2F).setUv(1,1);
        warped.addVertex(camera,.4F,.7F,.2F).setUv(0,0);
        assertEquals(3,vertices.points.size());
        assertEquals(vertices.points.get(0),vertices.points.get(1),"UV seams must share a contour");
        Vec3 local=vertices.points.get(0);
        Vector3f transformed=camera.transformPosition(new Vector3f((float)local.x,(float)local.y,(float)local.z));
        assertEquals(0,vertices.points.get(2).distanceTo(new Vec3(transformed.x,transformed.y,transformed.z)),1e-6);
        assertTrue(local.distanceTo(new Vec3(.4,.7,.2))>.001,"The wrapper must actually deform the mesh");
    }

    @Test void squareFieldBoundaryRemainsExactWithDeformationEnabled() {
        Capture plain=new Capture(),warped=new Capture();
        VisceralMesh.boundary(new PoseStack(),plain,4,0xFFFFFF,1,0);
        VisceralMesh.boundary(new PoseStack(),new UndulatingVertexConsumer(warped,
                new ManipulationUndulation(Form.WELL,100,20,71,4,Vec3.ZERO)),4,0xFFFFFF,1,0);
        assertEquals(plain.points,warped.points);
    }

    @Test void travelingDropletsUseTheSameDeformationFrameAsTheirStrands() {
        Capture drop=new Capture(),strand=new Capture();
        var warp=new ManipulationUndulation(Form.DRAIN,100,20,71,1,Vec3.ZERO);
        Vec3 at=new Vec3(.3,1.7,2.4);
        // A zero-width droplet and strand endpoint coincide at the same sample position.
        VisceralMesh.drop(new PoseStack(),new UndulatingVertexConsumer(drop,warp),at,0,0,0xFFFFFF,1,0);
        var vertices=new UndulatingVertexConsumer(strand,warp);
        vertices.addVertex(new Matrix4f(),(float)at.x,(float)at.y,(float)at.z);
        assertTrue(drop.points.stream().allMatch(point->point.equals(strand.points.get(0))));
    }

    @Test void materialFormationRevealsPatchesAndLeavesFinishedGeometryUnchanged() {
        Capture plain=new Capture(),forming=new Capture(),finished=new Capture();
        VisceralMesh.drop(new PoseStack(),plain,Vec3.ZERO,.4,.7,0xFFFFFF,1,0);
        VisceralMesh.drop(new PoseStack(),new CoalescingVertexConsumer(forming,.5F,0),Vec3.ZERO,.4,.7,0xFFFFFF,1,0);
        VisceralMesh.drop(new PoseStack(),new CoalescingVertexConsumer(finished,1,0),Vec3.ZERO,.4,.7,0xFFFFFF,1,0);
        assertEquals(plain.points,finished.points);
        assertEquals(plain.alphas,finished.alphas);
        assertTrue(new HashSet<>(forming.alphas).size()>3,"Formation should reveal connecting patches");
        assertTrue(forming.alphas.stream().anyMatch(a->a<255));
    }

    @Test void retiringFieldHasNoBoundaryAndReadinessSubjectsDoNotLinger() {
        Capture vertices=new Capture();
        var consumer=new UndulatingVertexConsumer(new CoalescingVertexConsumer(vertices,1,.3F,true),
                new ManipulationUndulation(Form.WELL,100,20,71,3,Vec3.ZERO));
        VisceralMesh.boundary(new PoseStack(),consumer,3,0xFFFFFF,1,0);
        assertTrue(vertices.points.isEmpty());
        for(Form form:new Form[]{Form.VERDICT,Form.CROWN,Form.CHOIR,Form.WARD,Form.RETORT,Form.MARK,Form.HOUR})
            assertFalse(ManipulationBloodFormation.retainsDissolvingBody(form),form.name());
    }

    private Capture draw(Form form,Vec3 end,float age,int count) {
        Capture vertices=new Capture();
        float radius=form.name().endsWith("_CHARGE")?.7F:1;
        var warped=new UndulatingVertexConsumer(vertices,
                new ManipulationUndulation(form,100+age,age,71,radius,end));
        assertTrue(ManipulationSignatureEffects.draw(form,radius,
                count,end,new PoseStack(),warped,100+age,age,1));
        return vertices;
    }

    private static class Capture implements VertexConsumer {
        final List<Vec3> points=new ArrayList<>();
        final List<float[]> uvs=new ArrayList<>();
        final List<Integer> alphas=new ArrayList<>();
        int uvCount;
        @Override public VertexConsumer addVertex(float x,float y,float z){points.add(new Vec3(x,y,z));return this;}
        @Override public VertexConsumer setColor(int r,int g,int b,int a){alphas.add(a);return this;}
        @Override public VertexConsumer setUv(float u,float v){assertTrue(Float.isFinite(u)&&Float.isFinite(v));uvs.add(new float[]{u,v});uvCount++;return this;}
        @Override public VertexConsumer setUv1(int u,int v){return this;}
        @Override public VertexConsumer setUv2(int u,int v){return this;}
        @Override public VertexConsumer setNormal(float x,float y,float z){return this;}
    }
}
