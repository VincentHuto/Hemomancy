package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManipulationUndulationTest {
    @Test void wavesAreBoundedContinuousAndKeepTheLocalRootAnchored() {
        var warp=new ManipulationUndulation(Form.CROWN,100,20,71,1,Vec3.ZERO);
        assertEquals(Vec3.ZERO,warp.offset(0,0,0));
        for(int i=0;i<100;i++) {
            Vec3 p=new Vec3(i*.071,i*.13,-i*.049);
            assertTrue(warp.offset(p.x,p.y,p.z).length()<.16);
            var next=new ManipulationUndulation(Form.CROWN,100.01,20.01F,71,1,Vec3.ZERO);
            assertTrue(warp.offset(p.x,p.y,p.z).distanceTo(next.offset(p.x,p.y,p.z))<.001);
        }
    }

    @Test void differentCastsHaveDifferentContoursAndIronMovesLessThanBlood() {
        var blood=new ManipulationUndulation(Form.CROWN,100,20,71,1,Vec3.ZERO);
        var other=new ManipulationUndulation(Form.CROWN,100,20,72,1,Vec3.ZERO);
        var iron=new ManipulationUndulation(Form.CHOIR,100,20,71,1,Vec3.ZERO);
        assertNotEquals(blood.offset(.3,.8,.2),other.offset(.3,.8,.2));
        assertTrue(iron.amplitude()<blood.amplitude());
    }

    @Test void settledStillnessDoesNotAnimate() {
        var a=new ManipulationUndulation(Form.STILLNESS,100,12,71,3,Vec3.ZERO);
        var b=new ManipulationUndulation(Form.STILLNESS,130,42,71,3,Vec3.ZERO);
        assertEquals(a.offset(.3,.8,.2),b.offset(.3,.8,.2));
    }

    @Test void verdictKeepsItsEndpointsAndDamageEnvelope() {
        for(Form form:new Form[]{Form.VERDICT,Form.VERDICT_CHARGE})
        for(Vec3 end:new Vec3[]{new Vec3(0,8,0),new Vec3(3,2,7)}) {
            var warp=new ManipulationUndulation(form,100,12,71,1,end);
            assertEquals(0,warp.offset(end.x,end.y,end.z).length(),1e-8);
            Vec3 p=end.scale(.5),offset=warp.offset(p.x,p.y,p.z);
            assertEquals(0,offset.dot(end.normalize()),1e-6);
            Vec3 edge=p.add(VisceralGeometry.side(end).scale(.97));
            assertEquals(0,warp.offset(edge.x,edge.y,edge.z).length(),1e-8);
        }
    }
}
