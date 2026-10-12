package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DendriticSelectorRulesTest {
    @Test
    void slotsFanAcrossTheSevenLobesAndClamp() {
        assertEquals(-67.5F, DendriticSelectorRules.slotDegrees(0));
        assertEquals(0.0F, DendriticSelectorRules.slotDegrees(3));
        assertEquals(67.5F, DendriticSelectorRules.slotDegrees(6));
        assertEquals(-67.5F, DendriticSelectorRules.slotDegrees(-4));
        assertEquals(67.5F, DendriticSelectorRules.slotDegrees(99));
    }

    @Test
    void selectorSweepsWithoutSnappingAndSettlesExactly() {
        float target = DendriticSelectorRules.slotDegrees(6);
        float angle = DendriticSelectorRules.slotDegrees(0);
        int ticks = 0;
        while (!DendriticSelectorRules.settled(angle, target)) {
            float next = DendriticSelectorRules.approach(angle, target);
            assertTrue(next - angle <= DendriticSelectorRules.MAX_SWING_DEGREES + 1e-4F, "swing per tick is capped");
            assertTrue(next > angle, "the sweep never reverses");
            angle = next;
            assertTrue(++ticks < 100, "the selector settles");
        }
        assertTrue(ticks >= 135 / DendriticSelectorRules.MAX_SWING_DEGREES, "a full sweep takes several ticks");
        assertEquals(target, DendriticSelectorRules.approach(angle, target), "a settled selector lands on the slot");
    }

    @Test
    void impulseTravelsThenTheBoutonFlaresAndRests() {
        assertEquals(0.0F, DendriticSelectorRules.impulseProgress(0L, 0.0F));
        assertEquals(0.5F, DendriticSelectorRules.impulseProgress(5L, 0.0F));
        assertEquals(-1.0F, DendriticSelectorRules.impulseProgress(DendriticSelectorRules.IMPULSE_TRAVEL_TICKS, 0.0F));
        assertEquals(1.0F, DendriticSelectorRules.glow(DendriticSelectorRules.IMPULSE_TRAVEL_TICKS, 0.0F));
        assertEquals(DendriticSelectorRules.RESTING_GLOW, DendriticSelectorRules.glow(0L, 0.0F));
        assertEquals(DendriticSelectorRules.RESTING_GLOW, DendriticSelectorRules.glow(39L, 0.5F));
        long late = 4_000_000_000L * DendriticSelectorRules.IMPULSE_PERIOD_TICKS + 5;
        assertEquals(0.625F, DendriticSelectorRules.impulseProgress(late, 1.25F), 1e-6F,
                "old worlds keep sub-tick impulse precision");
    }

    @Test
    void javaGeometryMirrorsTheAuthoringScript() throws IOException {
        String script = Files.readString(Path.of("tools/model_export/dendritic_distributor.py"));
        String renderer = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/DendriticDistributorRenderer.java"));
        StringBuilder slots = new StringBuilder("SLOTS = [");
        for (int slot = 0; slot < DendriticSelectorRules.SLOT_COUNT; slot++) {
            float degrees = DendriticSelectorRules.slotDegrees(slot);
            slots.append(slot == 0 ? "" : ", ").append(degrees == Math.rint(degrees) ? String.valueOf((int) degrees)
                    : String.valueOf(degrees));
        }
        assertTrue(script.contains(slots + "]"), "slot angles match the script: " + slots);
        assertTrue(script.contains("HUB = (8, 9)"), "script hub moved");
        assertTrue(renderer.contains("HUB_X = 8 * PX") && renderer.contains("HUB_Z = 9 * PX"), "renderer hub matches");
        assertTrue(script.contains("IMPULSE_START, IMPULSE_END = 1.5, 4.5")
                && renderer.contains("IMPULSE_RUN = 3 * PX"), "impulse run matches");
        for (String part : new String[] { "selector", "bouton_lit", "impulse" }) {
            assertTrue(Files.exists(Path.of("src/main/resources/assets/hemomancy/models/block/dendritic_distributor_"
                    + part + ".json")), "part model exported: " + part);
        }
    }
}
