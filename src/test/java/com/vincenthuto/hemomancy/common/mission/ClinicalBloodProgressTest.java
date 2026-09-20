package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.*;
import static org.junit.jupiter.api.Assertions.*;

class ClinicalBloodProgressTest {
    @Test void cabinetRequiresThreeDifferentExaminedSources() {
        var p = new ClinicalBloodProgress();
        p.collected = true;
        assertTrue(p.canLearn(MICROSCOPE, 1));
        p.learn(MICROSCOPE);
        assertFalse(p.canLearn(INJECTION, 1));
        p.recordExamination("minecraft:cow");
        p.recordExamination("minecraft:cow");
        assertEquals(1, p.sourceCount());
        assertTrue(p.canLearn(INJECTION, 1));
        assertFalse(p.canLearn(CABINET, 1));
        p.learn(INJECTION);
        p.recordExamination("minecraft:pig");
        p.recordExamination("minecraft:sheep");
        assertTrue(p.canLearn(CABINET, 1));
        assertFalse(p.canLearn(CABINET, 0));
        p.learn(CABINET);
        assertFalse(p.canLearn(CABINET, 1));
    }
    @Test void fieldCaseNeedsActualCabinetPracticeIronAndReferral() {
        var p = new ClinicalBloodProgress();
        p.learn(CABINET);
        p.cabinetCrafted = true;
        p.cabinetInserted = true;
        p.hematicIronObtained = true;
        p.artificerMet = true;
        assertFalse(p.canLearn(FIELD_REFERRAL, 2));
        p.cabinetWithdrawn = true;
        assertFalse(p.canLearn(FIELD_REFERRAL, 1));
        assertTrue(p.canLearn(FIELD_REFERRAL, 2));
        assertFalse(p.canLearn(FIELD_CASE, 2));
        p.learn(FIELD_REFERRAL);
        assertTrue(p.canLearn(FIELD_CASE, 2));
    }
    @Test void mnemonicStudyStartsAtTwoAndTeachesAtThree() {
        var p = new ClinicalBloodProgress();
        for (String source : new String[]{"minecraft:cow", "minecraft:pig", "minecraft:sheep"}) p.recordExamination(source);
        assertFalse(p.canLearn(ECHO_REFERRAL, 1));
        assertTrue(p.canLearn(ECHO_REFERRAL, 2));
        p.learn(ECHO_REFERRAL);
        assertFalse(p.canLearn(CLAIRAUDIOGRAPH, 3));
        p.mnemonistMet = true;
        assertFalse(p.canLearn(CLAIRAUDIOGRAPH, 2));
        assertTrue(p.canLearn(CLAIRAUDIOGRAPH, 3));
    }
    @Test void stateRoundTripsSourcesAndIndependentMilestones() {
        var p = new ClinicalBloodProgress();
        p.recordExamination("minecraft:cow");
        p.learn(MICROSCOPE);
        p.cabinetCrafted = true;
        p.cabinetInserted = true;
        p.artificerMet = true;
        var copy = new ClinicalBloodProgress();
        copy.deserializeNBT(null, p.serializeNBT(null));
        assertEquals(1, copy.sourceCount());
        assertTrue(copy.knows(MICROSCOPE));
        assertTrue(copy.cabinetCrafted);
        assertTrue(copy.cabinetInserted);
        assertTrue(copy.artificerMet);
        assertFalse(copy.cabinetWithdrawn);
        assertFalse(copy.knows(INJECTION));
        assertFalse(copy.recordExamination("minecraft:cow"));
    }
}
