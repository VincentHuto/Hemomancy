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
    void writtenCovenantComesBeforeLineMembership() {
        DialogueTree base = HarbingerVicarDialogueTrees.forDegree(5, 42, false);
        // A member's deposit writes the covenant: the rank rite is next, whoever leads the line.
        assertEquals(List.of("hemomancy.vicar.covenant.perform_bloodline_rite"),
                guidance(base, 5, false, true, false, false, false));
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

    /** PT-69: at the sixth Degree the Throne step follows the line and its Fane, since a Throne binds only there. */
    @Test
    void degreeSixThroneStepFollowsTheLineAndItsFane() {
        DialogueTree base = HarbingerVicarDialogueTrees.forDegree(6, 42, false);
        assertEquals(List.of("hemomancy.vicar.covenant.found_bloodline"), sixth(base,
                new CovenantProgress(false, true, true, false, true, false, false)));
        assertEquals(List.of("hemomancy.vicar.covenant.found_fane"), sixth(base,
                new CovenantProgress(true, true, true, false, true, true, false)));
        assertEquals(List.of("hemomancy.vicar.covenant.line_has_no_fane"), sixth(base,
                new CovenantProgress(true, true, true, false, true, false, false)));
        assertEquals(List.of("hemomancy.vicar.covenant.bind_throne"), sixth(base,
                new CovenantProgress(true, true, true, false, true, false, true)));
    }

    private static List<String> sixth(DialogueTree base, CovenantProgress covenant) {
        return HarbingerVicarDialogueTrees.withCovenantGuidance(base, 6, covenant).getNode("degree_hint").lines();
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
