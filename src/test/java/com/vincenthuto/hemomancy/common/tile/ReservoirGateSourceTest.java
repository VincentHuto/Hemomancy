package com.vincenthuto.hemomancy.common.tile;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Stations close their reservoirs during a rite; every path that moves block blood must ask first. */
class ReservoirGateSourceTest {
    private static final Path MAIN = Path.of("src/main/java/com/vincenthuto/hemomancy");

    @Test
    void absorptionAsksTheReservoir() throws IOException {
        assertTrue(read("common/block/harbinger/BlockBloodInteractions.java").contains("canProvideBlood()"));
    }

    @Test
    void routingAsksItsTarget() throws IOException {
        // Routing draws from the owner, their gourd and bloodline, never from a block, so only the target is gated.
        String routing = read("common/capability/player/harbinger/bloodvolume/routing/BloodRoutingHelper.java");
        assertTrue(routing.contains("canReceiveBlood()"), "routing fills closed reservoirs");
    }

    @Test
    void thrallCouriersAskBothEnds() throws IOException {
        String thrall = read("common/entity/summon/BloodThrallEntity.java");
        assertTrue(thrall.contains("canReceiveBlood()"), "couriers fill closed reservoirs");
        assertTrue(thrall.contains("canProvideBlood()"), "couriers drain closed reservoirs");
    }

    private static String read(String path) throws IOException {
        return Files.readString(MAIN.resolve(path));
    }
}
