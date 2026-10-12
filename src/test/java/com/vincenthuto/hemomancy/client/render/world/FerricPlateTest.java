package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FerricPlateTest {
    @Test void platesAreChamferedSlabsThatStayInsideTheirBounds() {
        var mesh = new HematicRiposteGeometryTest.Capture();
        FerricDuctilisGeometry.plate(new PoseStack(), mesh, Vec3.ZERO, 1, 1, .285, 3, 1);
        assertEquals(0, mesh.points.size() % 4);
        assertTrue(mesh.points.size() > 24, "a plate should be more than a bare box");
        for (Vec3 p : mesh.points) {
            assertTrue(Math.abs(p.x) <= .5 + 1e-6 && Math.abs(p.y) <= .5 + 1e-6, "outline must match the collision face: " + p);
            assertTrue(Math.abs(p.z) <= .1425 + .05, "rivets may stand proud, the slab may not: " + p);
        }
        // The chamfer pulls the face in from the rim.
        assertTrue(mesh.points.stream().anyMatch(p -> Math.abs(p.z - .1425) < 1e-6 && Math.abs(p.x) < .5 - 1e-6));
    }

    @Test void smallPlatesSkipRivets() {
        var small = new HematicRiposteGeometryTest.Capture();
        var large = new HematicRiposteGeometryTest.Capture();
        FerricDuctilisGeometry.plate(new PoseStack(), small, Vec3.ZERO, .11, .23, .055, 1, 1);
        FerricDuctilisGeometry.plate(new PoseStack(), large, Vec3.ZERO, .75, .7, .75, 1, 1);
        assertTrue(large.points.size() > small.points.size());
    }

    @Test void constructsHoldUntilTheyFailThenComeApart() {
        assertEquals(0, FerricDuctilisGeometry.shatter(1), 1e-9);
        assertTrue(FerricDuctilisGeometry.shatter(.5f) > 0);
        assertTrue(FerricDuctilisGeometry.shatter(0) > FerricDuctilisGeometry.shatter(.5f));
    }
}
