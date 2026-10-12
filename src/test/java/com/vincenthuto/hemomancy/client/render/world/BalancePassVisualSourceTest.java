package com.vincenthuto.hemomancy.client.render.world;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/** The balance-pass focal visuals stay hooked ahead of the shared fallbacks and are sent by their casts. */
class BalancePassVisualSourceTest {
    private static final Path ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy");

    @Test
    void rendererDrawsBothGeometriesBeforeTheSharedSchoolBranches() throws IOException {
        String renderer = read("client/render/world/ManipulationVisualRenderer.java");
        int collapse = renderer.indexOf("if(LodestoneCollapseGeometry.handles(packet.form()))");
        int rend = renderer.indexOf("if(UmbralRendGeometry.handles(packet.form()))");
        int shared = renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form()))");
        int lux = renderer.indexOf("if (LuxUmbraGeometry.handles(packet.form()))");
        assertTrue(collapse > 0 && rend > 0, "focal hooks missing");
        assertTrue(collapse < shared && rend < shared && rend < lux, "focal hooks must precede the shared branches");
        String solidsGate = renderer.substring(renderer.indexOf("if(solids && !FerricDuctilisGeometry.handles(packet.form())"));
        solidsGate = solidsGate.substring(0, solidsGate.indexOf("continue;"));
        assertTrue(solidsGate.contains("!LodestoneCollapseGeometry.handles(packet.form())")
                        && solidsGate.contains("!ForgedNeedleGeometry.handlesCharge(packet.form())"),
                "Lodestone Collapse and forged needle charges must reach the solid iron pass");
        assertTrue(renderer.contains("UmbralRendGeometry.draw(packet,poses,FLOWS.vertices(LuxUmbraBatch.Material.UMBRA)"),
                "Umbral Rend draws on the Umbra material");
    }

    @Test
    void castsSendTheirOwnForms() throws IOException {
        assertTrue(read("common/manipulation/tenebris/UmbralRendManip.java").contains("Form.UMBRAL_REND"));
        assertTrue(read("common/manipulation/ferric/LodestoneCollapseManip.java").contains("Form.LODESTONE_COLLAPSE"));
        String visuals = read("common/manipulation/ManipulationVisuals.java");
        assertTrue(visuals.indexOf("PUPPET_UNSPOOL, UMBRAL_REND, LODESTONE_COLLAPSE") > 0,
                "new forms must be appended after the existing ordinals");
    }

    @Test
    void reusedFormSpellsNowHaveTheirOwnGeometry() throws IOException {
        String renderer = read("client/render/world/ManipulationVisualRenderer.java");
        int shared = renderer.indexOf("if(AnimusMortemGeometry.handles(packet.form()))");
        for (String geometry : new String[] {"SearingLashGeometry", "CruorJavelinGeometry", "RadiantWakeGeometry",
                "FruitingContagionGeometry"}) {
            int hook = renderer.indexOf("if(" + geometry + ".handles(packet.form()))");
            assertTrue(hook > 0 && hook < shared, geometry + " must be hooked before the shared branches");
        }
        assertTrue(renderer.contains("|| RadiantWakeGeometry.handles(packet.form())\n"), "wings need the body yaw");
        assertTrue(read("common/manipulation/flammeus/SearingLashManip.java").contains("Form.SEARING_LASH"));
        assertTrue(read("common/manipulation/congeatio/CruorJavelinManip.java").contains("Form.CRUOR_JAVELIN"));
        assertTrue(read("common/manipulation/lux/RadiantWakeManip.java").contains("Form.RADIANT_WAKE"));
        assertTrue(read("common/manipulation/mortem/FruitingContagionManip.java").contains("Form.CONTAGION"));
        assertTrue(read("common/manipulation/ManipulationVisuals.java").contains("case \"radiant_wake\" -> Form.RADIANT_WAKE;"),
                "stopping the channel must retire the wings");
    }

    private static String read(String relative) throws IOException {
        return Files.readString(ROOT.resolve(relative)).replace("\r\n", "\n");
    }
}
