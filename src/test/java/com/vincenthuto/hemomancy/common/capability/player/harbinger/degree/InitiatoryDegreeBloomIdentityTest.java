package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InitiatoryDegreeBloomIdentityTest {
    @Test
    void legacyPositionProgressMigratesToOneBloomAndNewLifecyclesStaySeparate() {
        InitiatoryDegree degree = new InitiatoryDegree();
        long origin = 12345L;
        degree.recordPomeConsumed(origin);
        degree.recordPomeConsumed(origin);
        InitiatoryDegree loaded = new InitiatoryDegree();
        loaded.deserializeNBT(null, degree.serializeNBT(null));

        UUID firstBloom = UUID.randomUUID();
        UUID laterBloom = UUID.randomUUID();
        assertEquals(0, loaded.getPomesConsumedFromBloom(laterBloom, origin, false));
        assertEquals(2, loaded.getPomesConsumedFromBloom(firstBloom, origin, true));
        assertEquals(3, loaded.recordPomeConsumed(firstBloom, origin, true));
        assertEquals(0, loaded.getPomesConsumedFromBloom(laterBloom, origin));

        InitiatoryDegree reloaded = new InitiatoryDegree();
        reloaded.deserializeNBT(null, loaded.serializeNBT(null));
        assertEquals(3, reloaded.getPomesConsumedFromBloom(firstBloom, origin));
        assertEquals(0, reloaded.getPomesConsumedFromBloom(laterBloom, origin));
    }
}
