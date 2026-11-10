package com.vincenthuto.hemomancy.common.manipulation.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringWorldEffectsTest {

    @Test void allEffectsAreNoOps() {
        for (var school : EnumBloodTemper.values()) assertSame(OutpouringWorldEffects.NONE, OutpouringWorldEffects.effect(school));
    }

    @Test void configDefaultIsNever() throws IOException {
        String config = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java"));
    }

    @Test void releaseRunsTheHook() throws IOException {
        String manipulation = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java"));
        assertTrue(manipulation.contains("OutpouringWorldEffects.release("));
    }
}
