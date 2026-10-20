package com.vincenthuto.hemomancy.client.render.outpouring;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** The pre-outpouring caster effects (hand veins, constructs behind the player, Lux wings) stay deleted. */
class OutpouringRemovalSourceTest {
    private static final Path CLIENT = Path.of("src/main/java/com/vincenthuto/hemomancy/client");

    @Test void oldAnatomyIsGone() throws IOException {
        String anatomy = Files.readString(CLIENT.resolve("render/world/CastingAnatomyRenderer.java"));
        for (String gone : new String[]{"void heart(", "void furnace(", "void nerves(", "void cadaver(", "void crystal(",
                "void foundry(", "void shadow(", "LuxCastingWings", "VisceralMesh"})
            assertFalse(anatomy.contains(gone), gone + " belongs to the removed casting anatomy");
    }

    @Test void wingsDeleted() {
        assertFalse(Files.exists(CLIENT.resolve("render/world/LuxCastingWings.java")));
    }

    @Test void playersGetNoActivationBurst() throws IOException {
        String particles = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationParticles.java"));
        assertTrue(particles.contains("caster instanceof Player"), "the outpouring replaces the generic burst for players");
    }

    @Test void layerNoLongerPivotsForLux() throws IOException {
        String layer = Files.readString(CLIENT.resolve("render/layer/player/CastingAnimationLayer.java"));
        assertFalse(layer.contains("EnumBloodTendency.LUX"));
    }
}
