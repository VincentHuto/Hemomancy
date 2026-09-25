package com.vincenthuto.hemomancy.common.enchanting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScriptoriumCurseRulesTest {
    @Test void firstCurseRosterHasDistinctSeverityCosts() {
        assertEquals(1, ScriptoriumCurseRules.bloodCost("sanguine_appetite", 1));
        assertEquals(6, ScriptoriumCurseRules.bloodCost("sanguine_appetite", 3));
        assertEquals(4, ScriptoriumCurseRules.bloodCost("thirsting_edge", 2));
        assertEquals(10, ScriptoriumCurseRules.bloodCost("hemorrhagic_string", 3));
        assertEquals(15, ScriptoriumCurseRules.bloodCost("open_vessel", 3));
    }

    @Test void severityFollowsExcessLoad() {
        assertEquals(1, ScriptoriumCurseRules.severity(0));
        assertEquals(1, ScriptoriumCurseRules.severity(1));
        assertEquals(2, ScriptoriumCurseRules.severity(2));
        assertEquals(2, ScriptoriumCurseRules.severity(3));
        assertEquals(3, ScriptoriumCurseRules.severity(4));
    }
}
