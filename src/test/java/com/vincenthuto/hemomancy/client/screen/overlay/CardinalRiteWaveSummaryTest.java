package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteWaveSummaryTest {
    @Test
    void simpleCeremonyDoesNotInventAnOrdeal() {
        assertEquals("No ordeal", CardinalRiteOverlay.waveSummary(0, 0));
    }

    @Test
    void authoredOrdealsKeepTheirWaveCount() {
        assertEquals("Waves 1/3", CardinalRiteOverlay.waveSummary(0, 3));
        assertEquals("Waves 2/3", CardinalRiteOverlay.waveSummary(1, 3));
        assertEquals("Waves 3/3", CardinalRiteOverlay.waveSummary(3, 3));
    }
}
