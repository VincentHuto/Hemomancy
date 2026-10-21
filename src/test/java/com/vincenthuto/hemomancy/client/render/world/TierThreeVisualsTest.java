package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Status and minor forms that used to share one shape now each move their own way. */
class TierThreeVisualsTest {
    @Test void mortemStatusesNoLongerShareOneShape() {
        Set<Integer> sizes = new HashSet<>();
        for (Form form : new Form[] { Form.WOUND, Form.HUNGER, Form.GRAVE, Form.ROT_INFECTION }) {
            var mesh = draw(form, Vec3.ZERO, 20);
            assertFalse(mesh.points.isEmpty(), form.name());
            assertEquals(0, mesh.points.size() % 4);
            sizes.add(mesh.points.size());
        }
        assertEquals(4, sizes.size(), "each status should have its own silhouette");
    }

    @Test void minorAnimusFormsStayFiniteAndBounded() {
        for (Form form : new Form[] { Form.COMMAND, Form.GROWTH, Form.COMMUNION, Form.ANIMUS_CONJURE, Form.MORTEM_CONJURE })
            for (float age : new float[] { 1, 6, 30 }) {
                var mesh = draw(form, new Vec3(1, 1, 3), age);
                assertFalse(mesh.points.isEmpty(), form + " at " + age);
                assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 6), form.name());
            }
    }

    @Test void sovereignLashReachesEachTurnedMob() {
        Vec3 mob = new Vec3(4, .5, 3);
        var early = draw(Form.SOVEREIGN_LASH, mob, 1);
        var reached = draw(Form.SOVEREIGN_LASH, mob, 5);
        double far = reached.points.stream().mapToDouble(p -> p.distanceTo(Vec3.ZERO)).max().orElseThrow();
        double near = early.points.stream().mapToDouble(p -> p.distanceTo(Vec3.ZERO)).max().orElseThrow();
        assertTrue(far > mob.length() - .3, "the lash should reach the mob: " + far);
        assertTrue(near < far, "the lash should travel out from the caster");
    }

    @Test void sourceWiringForSovereignAndPulse() throws IOException {
        String reactive = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationReactiveEvents.java"));
        assertTrue(reactive.contains("ManipulationVisuals.Form.SOVEREIGN_LASH, chest,"));
        String ferric = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/world/FerricDuctilisGeometry.java"));
        assertTrue(ferric.contains("One continuous nerve loop"), "the pulse ring should not be 24 separate ribbons");
    }

    private static HematicRiposteGeometryTest.Capture draw(Form form, Vec3 end, float age) {
        var mesh = new HematicRiposteGeometryTest.Capture();
        AnimusMortemGeometry.draw(new ManipulationVisualPacket(form, 5, Vec3.ZERO, end, 1.5f, 40, 2), new PoseStack(), mesh,
                new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 300 + age, age, 1, 1);
        return mesh;
    }
}
