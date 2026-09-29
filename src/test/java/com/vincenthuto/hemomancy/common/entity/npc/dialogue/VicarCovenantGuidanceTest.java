package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class VicarCovenantGuidanceTest {
    @Test
    void degreeFiveAdviceFollowsFoundingAndFaneProgress() {
        DialogueTree base = HarbingerVicarDialogueTrees.forDegree(5, 42, false);
        assertEquals(List.of("hemomancy.vicar.covenant.found_bloodline"),
                guidance(base, 5, false, false, false, false, false));
        assertEquals(List.of("hemomancy.vicar.covenant.found_fane"),
                guidance(base, 5, true, false, false, false, false));
        assertEquals(List.of("hemomancy.vicar.covenant.perform_bloodline_rite"),
                guidance(base, 5, true, true, false, false, false));
    }

    @Test
    void degreeSixAdviceNamesOnlyMissingProofs() {
        DialogueTree base = HarbingerVicarDialogueTrees.forDegree(6, 42, false);
        assertEquals(List.of("hemomancy.vicar.covenant.chamber_return",
                        "hemomancy.vicar.covenant.bind_throne", "hemomancy.vicar.covenant.complete_vigil"),
                guidance(base, 6, true, true, false, false, false));
        assertEquals(List.of("hemomancy.vicar.covenant.complete_vigil"),
                guidance(base, 6, true, true, true, true, false));
        assertEquals(List.of("hemomancy.vicar.covenant.perform_hematic_order_rite"),
                guidance(base, 6, true, true, true, true, true));
    }

    @Test
    void otherDegreesKeepTheirExistingDialogue() {
        DialogueTree base = HarbingerVicarDialogueTrees.forDegree(4, 42, false);
        assertSame(base, HarbingerVicarDialogueTrees.withCovenantGuidance(
                base, 4, false, false, false, false, false));
    }

    private static List<String> guidance(DialogueTree base, int degree, boolean foundedBloodline,
            boolean faneComplete, boolean chamberReturned, boolean throneBound, boolean vigilComplete) {
        return HarbingerVicarDialogueTrees.withCovenantGuidance(base, degree, foundedBloodline,
                faneComplete, chamberReturned, throneBound, vigilComplete).getNode("degree_hint").lines();
    }
}
