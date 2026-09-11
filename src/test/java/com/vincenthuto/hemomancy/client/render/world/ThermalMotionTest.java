package com.vincenthuto.hemomancy.client.render.world;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ThermalMotionTest {
    @Test void cachedFragmentsInterpolateBetweenTickPositions() {
        var packet=new com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket(
                com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.CRUOR_BREAK,-1,Vec3.ZERO,Vec3.ZERO,.5f,24,1);
        var fragments=new ThermalFragments();fragments.update(packet,10);
        Vec3 before=fragments.offset(0,0),after=fragments.offset(0,1),middle=fragments.offset(0,.5f);
        assertTrue(after.x>before.x);
        assertEquals((before.x+after.x)/2,middle.x,1e-10);
        assertEquals((before.y+after.y)/2,middle.y,1e-10);
    }
    @Test void coldFragmentsDecelerateAndNeverReverseHorizontally() {
        Vec3 v=new Vec3(.2,.1,0);
        double first=ThermalMotion.drift(v,10,.08,.001).x;
        double second=ThermalMotion.drift(v,20,.08,.001).x-first;
        assertTrue(first>second && second>0);
        assertTrue(ThermalMotion.drift(v,100,.08,.001).x<2.501);
    }
    @Test void zeroDragIsBallisticAndNegativeAgeDoesNotPrecedeBirth() {
        assertEquals(new Vec3(2,-1,0),ThermalMotion.drift(new Vec3(.2,.1,0),10,0,.04));
        assertEquals(Vec3.ZERO,ThermalMotion.drift(new Vec3(1,2,3),-5,.1,.1));
    }
}
