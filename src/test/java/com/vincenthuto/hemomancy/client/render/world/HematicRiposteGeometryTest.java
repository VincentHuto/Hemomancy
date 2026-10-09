package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.manipulation.animus.HematicRiposteRules;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HematicRiposteGeometryTest {
    private static final HematicRiposteGeometry.Frame THIRD = new HematicRiposteGeometry.Frame(0, 0, 1, false, false);
    private static final HematicRiposteGeometry.Frame FIRST = new HematicRiposteGeometry.Frame(0, 0, 1, false, true);

    @Test void armAndStrikeStayFiniteQuadAlignedAndWithinBudget() {
        for (Form form : new Form[] { Form.RIPOSTE, Form.RIPOSTE_STRIKE })
            for (int count : new int[] { 1, 2, HematicRiposteRules.encodeSeed(1, 4) })
                for (HematicRiposteGeometry.Frame frame : new HematicRiposteGeometry.Frame[] { THIRD, FIRST })
                    for (float age : new float[] { 0, 1, 3, 5, 7, 9, 12, 15.9f }) {
                        Capture mesh = draw(form, count, age, frame);
                        assertTrue(mesh.points.size() <= 1600, form + " exceeded surface budget: " + mesh.points.size());
                        assertEquals(0, mesh.points.size() % 4, form.name());
                        assertTrue(mesh.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr())), form.name());
                    }
        assertFalse(draw(Form.RIPOSTE, 1, 5, THIRD).points.isEmpty());
        assertFalse(draw(Form.RIPOSTE_STRIKE, 1, 2, THIRD).points.isEmpty());
    }

    @Test void handStartsBehindThenSlapsInFrontAtOversizedReach() {
        for (int side : new int[] { -1, 1 }) {
            Vec3 start = HematicRiposteGeometry.hand(side, 2, 0, THIRD);
            Vec3 slap = HematicRiposteGeometry.hand(side, 2, HematicRiposteRules.SWEEP_END, THIRD);
            assertTrue(start.z < 0, "the arm should rise behind the wearer");
            assertTrue(slap.z > 2, "the slap should land well in front (yaw 0 faces +Z): " + slap);
            assertTrue(Math.abs(slap.x) < .2, "the slap should cross the centre of the view");
        }
    }

    @Test void flippingTheSideMirrorsTheSweep() {
        for (float age : new float[] { 1, 3, 4, 7 }) {
            Vec3 right = HematicRiposteGeometry.hand(1, 2, age, THIRD);
            Vec3 left = HematicRiposteGeometry.hand(-1, 2, age, THIRD);
            assertEquals(right.x, -left.x, 1e-9);
            assertEquals(right.z, left.z, 1e-9);
        }
    }

    @Test void tiltSelectsAnUppercutOrADownwardSlap() {
        double low = HematicRiposteGeometry.hand(1, 0, HematicRiposteRules.SWEEP_END, THIRD).y;
        double high = HematicRiposteGeometry.hand(1, 4, HematicRiposteRules.SWEEP_END, THIRD).y;
        assertTrue(high > low + .4);
    }

    @Test void onlyRiposteFormsAreClaimed() {
        assertTrue(HematicRiposteGeometry.handles(Form.RIPOSTE));
        assertTrue(HematicRiposteGeometry.handles(Form.RIPOSTE_STRIKE));
        assertFalse(HematicRiposteGeometry.handles(Form.RUSH));
        assertFalse(AnimusMortemGeometry.handles(Form.RIPOSTE));
    }

    private Capture draw(Form form, int count, float age, HematicRiposteGeometry.Frame frame) {
        Capture capture = new Capture();
        int entity = form == Form.RIPOSTE ? 4 : -1;
        Vec3 from = form == Form.RIPOSTE ? Vec3.ZERO : new Vec3(0, 1.3, .7);
        HematicRiposteGeometry.draw(new ManipulationVisualPacket(form, entity, from, from.add(0, 0, 1), 1, 16, count),
                new PoseStack(), capture, new Vec3(4, 3, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 100 + age, age, 1, frame);
        return capture;
    }

    static final class Capture implements VertexConsumer {
        final List<Vec3> points = new ArrayList<>();
        public VertexConsumer addVertex(float x, float y, float z) { points.add(new Vec3(x, y, z)); return this; }
        public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        public VertexConsumer setUv(float u, float v) { assertTrue(Float.isFinite(u) && Float.isFinite(v)); return this; }
        public VertexConsumer setUv1(int u, int v) { return this; }
        public VertexConsumer setUv2(int u, int v) { return this; }
        public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
