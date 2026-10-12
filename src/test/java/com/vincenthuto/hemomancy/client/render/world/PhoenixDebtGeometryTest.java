package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PhoenixDebtGeometryTest {
    private static final PhoenixDebtGeometry.Frame THIRD = new PhoenixDebtGeometry.Frame(30, 1, false, false);
    private static final PhoenixDebtGeometry.Frame FIRST = new PhoenixDebtGeometry.Frame(30, 1, false, true);

    @Test void eruptionAndReadyStayFiniteQuadAlignedAndWithinBudget() {
        for (var frame : new PhoenixDebtGeometry.Frame[] { THIRD, FIRST }) {
            for (float age = 0; age < PhoenixDebtGeometry.TICKS + 4; age += 1.7f) {
                var mesh = draw(Form.PHOENIX, age, frame);
                assertEquals(0, mesh[0].points.size() % 4);
                assertEquals(0, mesh[1].points.size() % 4);
                assertTrue(mesh[0].points.size() + mesh[1].points.size() <= 1800, "budget at age " + age);
                for (var capture : mesh)
                    assertTrue(capture.points.stream().allMatch(p -> Double.isFinite(p.lengthSqr()) && p.length() < 8));
            }
            var ready = draw(Form.PHOENIX_READY, 10, frame);
            assertFalse(ready[1].points.isEmpty(), "the armed heart should always show");
        }
        assertTrue(draw(Form.PHOENIX, 20, FIRST)[0].points.size() < draw(Form.PHOENIX, 20, THIRD)[0].points.size(),
                "first person should drop the near root and column");
    }

    @Test void wingsEmergeFoldedUnfurlBeatAndFold() {
        assertEquals(0, PhoenixDebtGeometry.spread(0), 1e-9);
        assertTrue(PhoenixDebtGeometry.spread(PhoenixDebtGeometry.ERUPT_END) < .3);
        assertEquals(1, PhoenixDebtGeometry.spread(20), 1e-9);
        assertTrue(PhoenixDebtGeometry.spread(PhoenixDebtGeometry.TICKS) < .4, "wings fold as they burn away");
        double max = 0;
        for (double a = PhoenixDebtGeometry.ERUPT_END; a <= PhoenixDebtGeometry.SPREAD_END; a += .25)
            max = Math.max(max, PhoenixDebtGeometry.spread(a));
        assertTrue(max > 1, "the unfurl should overshoot before settling");
        double span = PhoenixDebtGeometry.joint(3, 1, 0).x, folded = PhoenixDebtGeometry.joint(3, 0, 0).x;
        assertTrue(span > 3 && folded < .5, "spread wings should span far wider than folded ones");
    }

    @Test void beatsRaiseAndLowerTheWingtip() {
        assertEquals(0, PhoenixDebtGeometry.beat(0), 1e-9);
        assertEquals(0, PhoenixDebtGeometry.beat(PhoenixDebtGeometry.TICKS), 1e-9);
        double high = -9, low = 9;
        for (double a = PhoenixDebtGeometry.SPREAD_END; a < PhoenixDebtGeometry.BEAT_END; a += .5) {
            double tip = PhoenixDebtGeometry.joint(3, 1, PhoenixDebtGeometry.beat(a)).y;
            high = Math.max(high, tip);
            low = Math.min(low, tip);
        }
        assertTrue(high - low > 2, "two full beats should sweep the wingtip well up and down: " + (high - low));
    }

    @Test void phoenixMovedOffTheThermalTierAndFollowsTheBody() throws IOException {
        for (Form form : new Form[] { Form.PHOENIX, Form.PHOENIX_READY }) {
            assertTrue(PhoenixDebtGeometry.handles(form));
            assertFalse(ThermalGeometry.handles(form), form + " is still claimed by the thermal tier");
        }
        String reactive = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationReactiveEvents.java"));
        assertTrue(reactive.contains("ManipulationVisuals.attached(player, ManipulationVisuals.Form.PHOENIX, 5, 48, 1);"),
                "a world burst cannot follow the body the wings grow from");
        String renderer = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/world/ManipulationVisualRenderer.java"));
        assertTrue(renderer.contains("|| packet.form()==ManipulationVisuals.Form.RIPOSTE || PhoenixDebtGeometry.handles(packet.form()))"),
                "the wings need the wearer's body yaw");
    }

    private static HematicRiposteGeometryTest.Capture[] draw(Form form, float age, PhoenixDebtGeometry.Frame frame) {
        var flame = new HematicRiposteGeometryTest.Capture();
        var molten = new HematicRiposteGeometryTest.Capture();
        PhoenixDebtGeometry.draw(new ManipulationVisualPacket(form, 21, Vec3.ZERO, Vec3.ZERO, 5, 48, 1), new PoseStack(),
                flame, molten, new Vec3(3, 2, -6), new Vec3(1, 0, 0), new Vec3(0, 1, 0), 300 + age, age, 1, frame);
        return new HematicRiposteGeometryTest.Capture[] { flame, molten };
    }
}
