package com.vincenthuto.hemomancy.common.worldgen.arbor;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborOfWillIntegrationSourceTest {
    private static String source(String relative) throws Exception {
        return Files.readString(Path.of("src/main/java", relative));
    }

    @Test
    void chamberOwnsAndReconcilesOneArborAnchor() throws Exception {
        String manager = source("com/vincenthuto/hemomancy/common/worldgen/ChamberOfWillManager.java");
        assertTrue(manager.contains("ensureArborOfWill"));
        assertTrue(manager.contains("ArborOfWillEntity"));
    }

    @Test
    void arborPacketsAreRegisteredInBothDirections() throws Exception {
        String packets = source("com/vincenthuto/hemomancy/common/network/PacketHandler.java");
        assertTrue(packets.contains("ArborFruitInteractPacket.TYPE"));
        assertTrue(packets.contains("OpenArborSkillsPacket.TYPE"));
    }

    @Test
    void screenSupportsFocusedSkillOpening() throws Exception {
        String screen = source("com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java");
        assertTrue(screen.contains("openScreen(int skillId)"));
        assertTrue(screen.contains("focusSkill"));
    }

    @Test
    void clientPresentationHasRendererAndTargeting() throws Exception {
        String renderer = source("com/vincenthuto/hemomancy/client/render/entity/misc/ArborOfWillRenderer.java");
        String interaction = source("com/vincenthuto/hemomancy/client/event/ArborOfWillClientInteraction.java");
        assertTrue(renderer.contains("renderApotheosisCap"));
        assertTrue(renderer.contains("ArborGrowthAnimations.growthScale"));
        assertTrue(interaction.contains("ArborFruitInteractPacket"));
    }
}
