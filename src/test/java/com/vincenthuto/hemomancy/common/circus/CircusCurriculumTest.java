package com.vincenthuto.hemomancy.common.circus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CircusCurriculumTest {
    @Test void instructionAndDegreeAreBothRequiredForUnknownShapes() {
        assertFalse(CircusCurriculum.mayBeginOrdeal("marrow_spitter", 3, false, false));
        assertFalse(CircusCurriculum.mayBeginOrdeal("gorebound_hulk", 3, true, false));
        assertTrue(CircusCurriculum.mayBeginOrdeal("marrow_spitter", 3, true, false));
        assertTrue(CircusCurriculum.mayBeginOrdeal("cinder_bellows", 4, true, false));
        assertFalse(CircusCurriculum.mayBeginOrdeal("ringmaster_pattern", 4, true, false));
        assertFalse(CircusCurriculum.mayBeginOrdeal("unknown", 7, true, false));
    }

    @Test void LegacyKnowledgeSatisfiesInstructionWithoutAwardingPracticalProof() {
        assertTrue(CircusCurriculum.instructionSatisfied(false, true));
        assertFalse(CircusCurriculum.instructionSatisfied(false, false));
        assertEquals(7, CircusCurriculum.lessons().size());
        assertEquals("circus_knife_thrower", CircusCurriculum.lesson("marrow_spitter").orElseThrow().teacher());
        assertEquals(5, CircusCurriculum.lesson("mnemonist_puppet").orElseThrow().degree());
    }
}
