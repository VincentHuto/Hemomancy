package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppetStringsRules;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PuppetStringsGeometryTest {
    @Test void arrivalAndDepartureStayFiniteQuadAlignedAndWithinBudget() {
        for (Form form : new Form[] { Form.PUPPET_DESCEND, Form.PUPPET_UNSPOOL })
            for (int count : new int[] { 1, 3, 5, 9 })
                for (float age : new float[] { 0, 1, 2.5f, 4, 6, 9, 10.5f, 12, 13.5f, 16 }) {
                    HematicRiposteGeometryTest.Capture mesh = draw(form, count, age, 1.9, 0.6);
                    assertTrue(mesh.points.size() <= 2400, form + " exceeded surface budget: " + mesh.points.size());
                    assertEquals(0, mesh.points.size() % 4, form.name());
                    assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr())), form.name());
                }
        assertFalse(draw(Form.PUPPET_DESCEND, 4, 2, 1.9, 0.6).points.isEmpty(), "falling strings are drawn");
        assertFalse(draw(Form.PUPPET_DESCEND, 4, 7, 1.9, 0.6).points.isEmpty(), "the lowered body is knitted");
        assertFalse(draw(Form.PUPPET_UNSPOOL, 4, 6, 1.9, 0.6).points.isEmpty(), "the haul is drawn");
        assertFalse(draw(Form.PUPPET_UNSPOOL, 4, PuppetStringsRules.HAUL_END + 1, 1.9, 0.6).points.isEmpty(),
                "the flash covers the last of the body");
    }

    @Test void stringsHoldTheTopOfTheBodyAndGatherAboveIt() {
        double height = 2.2, width = 1.4;
        for (int i = 0; i < 4; i++) {
            Vec3 attach = PuppetStringsGeometry.attach(i, 4, 7, height, width, 0.5, 1, 0);
            Vec3 control = PuppetStringsGeometry.control(i, 4, 7, height, width, 0);
            assertEquals(height + 0.5, attach.y, 1e-9, "strings hold the head as the body is lifted");
            assertEquals(height + PuppetStringsRules.STRING_RISE, control.y, 1e-9);
            assertTrue(horizontal(control) < horizontal(attach), "strings gather toward the control above");
        }
        Vec3 wide = PuppetStringsGeometry.attach(0, 3, 1, height, width, 0, 1, 0);
        Vec3 shrunk = PuppetStringsGeometry.attach(0, 3, 1, height, width, 0, 0.2, 0);
        assertTrue(horizontal(shrunk) < horizontal(wide), "the attachment ring shrinks with the hauled body");
    }

    @Test void onlyPuppetStringFormsAreClaimed() {
        assertTrue(PuppetStringsGeometry.handles(Form.PUPPET_DESCEND));
        assertTrue(PuppetStringsGeometry.handles(Form.PUPPET_UNSPOOL));
        assertFalse(PuppetStringsGeometry.handles(Form.MARIONETTE_TETHER));
        assertFalse(AnimusMortemGeometry.handles(Form.PUPPET_UNSPOOL));
        assertFalse(HematicRiposteGeometry.handles(Form.PUPPET_DESCEND));
    }

    private static double horizontal(Vec3 point) {
        return Math.sqrt(point.x * point.x + point.z * point.z);
    }

    private static HematicRiposteGeometryTest.Capture draw(Form form, int count, float age, double height, double width) {
        HematicRiposteGeometryTest.Capture capture = new HematicRiposteGeometryTest.Capture();
        Vec3 from = new Vec3(10, 64, -3);
        PuppetStringsGeometry.draw(new ManipulationVisualPacket(form, 41, from, from.add(width, 0, 0), (float) height, 16, count),
                new PoseStack(), capture, new Vec3(4, 3, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 100 + age, age, 1);
        return capture;
    }
}
