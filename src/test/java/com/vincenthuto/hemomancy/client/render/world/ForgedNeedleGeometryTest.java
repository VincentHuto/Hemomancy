package com.vincenthuto.hemomancy.client.render.world;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ForgedNeedleGeometryTest {
    private static final Form[] CHARGES = { Form.NEEDLE_CHARGE, Form.FAN_CHARGE, Form.LANCE_CHARGE };

    @Test void chargesForgeIronAndGatherBloodWithinBudget() {
        for (Form form : CHARGES)
            for (float progress : new float[] { 0, .2f, .5f, .9f, 1 }) {
                var iron = new HematicRiposteGeometryTest.Capture();
                var flow = new HematicRiposteGeometryTest.Capture();
                FerricDuctilisGeometry.begin();
                ForgedNeedleGeometry.charge(new ManipulationVisualPacket(form, 12, Vec3.ZERO, new Vec3(.3, -.1, 8), progress, 20, 1),
                        new PoseStack(), iron, flow, new Vec3(.4, .3, -2), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 140, 1);
                assertFalse(iron.points.isEmpty(), form + " forged no iron at " + progress);
                assertEquals(0, iron.points.size() % 4);
                assertEquals(0, flow.points.size() % 4);
                assertTrue(iron.points.size() + flow.points.size() <= 1200, form + " exceeded budget");
                assertTrue(iron.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 3), form.name());
            }
    }

    @Test void flightKeepsIronAndBloodInTheirOwnPasses() {
        for (boolean lance : new boolean[] { false, true })
            for (boolean lodged : new boolean[] { false, true }) {
                List<Vec3> path = new ArrayList<>();
                for (int i = 0; i < 10; i++) path.add(new Vec3(i * .6, 3 - i * i * .02, 0));
                Vec3 heading = lodged ? Vec3.ZERO : new Vec3(2, -.3, 0);
                var iron = new HematicRiposteGeometryTest.Capture();
                ForgedNeedleGeometry.flightIron(new PoseStack(), iron, path, heading, new Vec3(2, 5, -6), 6, 31, 1, lance, lodged,
                        lodged ? 5 : 0);
                assertFalse(iron.points.isEmpty());
                assertEquals(0, iron.points.size() % 4);
                assertTrue(iron.points.size() <= 160, "a single flechette must stay cheap: " + iron.points.size());
                var flow = new HematicRiposteGeometryTest.Capture();
                FerricDuctilisGeometry.begin();
                ForgedNeedleGeometry.flightFlow(new PoseStack(), flow, path, heading, new Vec3(2, 5, -6), new Vec3(1, 0, 0),
                        new Vec3(0, 1, 0), 120, 6, 31, 1, 1, lance, lodged);
                assertFalse(flow.points.isEmpty());
                assertEquals(0, flow.points.size() % 4);
            }
    }

    @Test void lancesGrowBarbsThatBaselineNeedlesLack() {
        var plain = new HematicRiposteGeometryTest.Capture();
        var lance = new HematicRiposteGeometryTest.Capture();
        ForgedNeedleGeometry.flechette(new PoseStack(), plain, Vec3.ZERO, new Vec3(0, 0, 1), 1, .04, false, 3, 1);
        ForgedNeedleGeometry.flechette(new PoseStack(), lance, Vec3.ZERO, new Vec3(0, 0, 1), 1, .04, true, 3, 1);
        assertTrue(lance.points.size() > plain.points.size());
        assertTrue(plain.points.stream().allMatch(p -> p.z >= -.2 && p.z <= 1.0001), "the flechette stays on its axis");
    }
}
