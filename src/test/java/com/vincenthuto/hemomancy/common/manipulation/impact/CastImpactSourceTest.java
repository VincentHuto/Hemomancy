package com.vincenthuto.hemomancy.common.manipulation.impact;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CastImpactSourceTest {
    private static final Path MAIN = Path.of("src/main/java/com/vincenthuto/hemomancy");

    private static String read(String relative) throws IOException {
        return Files.readString(MAIN.resolve(relative)).replace("\r\n", "\n");
    }

    @Test void everyLandedDirectSchoolHitSendsAnImpact() throws IOException {
        String events = read("common/damage/SchoolCombatEvents.java");
        int direct = events.indexOf("if (hit.kind() != SchoolHitContext.Kind.DIRECT) return;");
        int impact = events.indexOf("CastImpacts.landed(");
        assertTrue(direct >= 0 && impact > direct, "impacts follow the DIRECT gate in commit, so periodic and reaction ticks never spam them");
        assertTrue(impact > events.indexOf("tryTriggerConductiveArc"), "presentation runs after the hit's gameplay is settled");
        String sender = read("common/manipulation/impact/CastImpacts.java");
        assertTrue(sender.contains("CastImpactRules.allow("), "impacts are throttled per target");
        assertFalse(sender.contains("hurt(") || sender.contains("addEffect("), "impacts are presentation only");
        assertTrue(sender.contains("direct instanceof Projectile"), "only real shots read as projectiles; clouds and constructs land as their cast");
    }

    @Test void castImpactIsAppendedSoEarlierFormOrdinalsHold() throws IOException {
        String forms = read("common/manipulation/ManipulationVisuals.java");
        assertTrue(forms.contains("VESPER_RECALL, CAST_IMPACT"), "CAST_IMPACT must stay appended after Vesper's recall");
    }

    @Test void geometryUsesTheModsOwnMaterialsAndIsDispatchedInBothPasses() throws IOException {
        String geometry = read("client/render/world/CastImpactGeometry.java");
        assertFalse(geometry.contains("ParticleTypes."), "impacts never fall back to vanilla particles");
        for (String school : new String[]{"ANIMUS", "FLAMMEUS", "CONGEATIO", "DUCTILIS", "LUX", "MORTEM", "FERRIC", "TENEBRIS"})
            assertTrue(geometry.contains("case " + school), school + " needs its own flourish");
        String renderer = read("client/render/world/ManipulationVisualRenderer.java");
        assertTrue(renderer.contains("!CastImpactGeometry.handles(packet.form()))continue;"), "Ferric iron needs the solid pass");
        assertTrue(read("client/render/world/ManipulationMotes.java").contains("if(CastImpactGeometry.handles(form))return;"),
                "impacts fray on their own; expiry must not add default blood motes");
        assertTrue(renderer.indexOf("CastImpactGeometry.draw(") < renderer.indexOf("AnimusMortemGeometry.draw("),
                "impacts must be handled before the shared generic tiers");
    }
}
