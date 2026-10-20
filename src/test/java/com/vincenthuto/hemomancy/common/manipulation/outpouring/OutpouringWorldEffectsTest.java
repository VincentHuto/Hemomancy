package com.vincenthuto.hemomancy.common.manipulation.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringWorldEffectsTest {
    @Test void neverByDefault() {
        for (var rank : EnumManipulationRank.values()) assertFalse(OutpouringWorldEffects.allowed(rank, -1), rank.name());
    }

    @Test void gateOpensAtRank() {
        assertTrue(OutpouringWorldEffects.allowed(EnumManipulationRank.SUMMA, 2));
        assertTrue(OutpouringWorldEffects.allowed(EnumManipulationRank.PERFECTUS, 2));
        assertFalse(OutpouringWorldEffects.allowed(EnumManipulationRank.MEDIOCRITAS, 2));
    }

    @Test void allEffectsAreNoOps() {
        for (var school : EnumBloodTendency.values()) assertSame(OutpouringWorldEffects.NONE, OutpouringWorldEffects.effect(school));
    }

    @Test void configDefaultIsNever() throws IOException {
        String config = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java"));
        assertTrue(config.contains("\"realEffectMinRank\", -1, -1, 4"));
    }

    @Test void releaseRunsTheHook() throws IOException {
        String manipulation = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java"));
        assertTrue(manipulation.contains("OutpouringWorldEffects.release("));
    }
}
