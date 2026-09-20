package com.vincenthuto.hemomancy.common.antecedent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;

class AntecedentResearchTest {
    @Test void earlyArchiveEvidenceSurvivesAndDoesNotClaimRecognition() {
        var state = new AntecedentResearch();
        state.record(ARCHIVE_RESPONSE);
        assertFalse(state.complete());
        var restored = new AntecedentResearch();
        restored.deserializeNBT(null, state.serializeNBT(null));
        assertTrue(restored.has(ARCHIVE_RESPONSE));
        restored.record(VICAR_RECOGNITION);
        assertTrue(restored.complete());
        assertFalse(restored.record(ARCHIVE_RESPONSE));
    }
    @Test void windowRequiresContinuousWitnessingAndResetsOnMissingSample() {
        var witness = new AntecedentObservation();
        for (int tick=1120; tick<=1299; tick++) assertFalse(witness.tick(tick,true));
        assertTrue(witness.tick(1300,true));
        witness = new AntecedentObservation();
        for (int tick=1120; tick<=1300; tick++) assertFalse(witness.tick(tick,tick!=1190));
    }
}
