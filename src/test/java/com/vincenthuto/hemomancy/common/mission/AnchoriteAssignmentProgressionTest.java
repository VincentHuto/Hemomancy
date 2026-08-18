package com.vincenthuto.hemomancy.common.mission;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnchoriteAssignmentProgressionTest {
    @Test
    void assignmentsRespectDegreeAndPriorReward() {
        assertFalse(AnchoriteAssignmentProgression.canStartD5(4, true));
        assertFalse(AnchoriteAssignmentProgression.canStartD5(5, false));
        assertTrue(AnchoriteAssignmentProgression.canStartD5(5, true));
        assertFalse(AnchoriteAssignmentProgression.canStartD6(5, true));
        assertTrue(AnchoriteAssignmentProgression.canStartD6(6, true));
    }

    @Test
    void loadoutProofRequiresARealSetChange() {
        var heart = ResourceLocation.fromNamespaceAndPath("hemomancy", "scar_heart");
        var pyre = ResourceLocation.fromNamespaceAndPath("hemomancy", "scar_pyre");
        assertFalse(AnchoriteAssignmentProgression.changedLoadout(List.of(heart), List.of(heart)));
        assertFalse(AnchoriteAssignmentProgression.changedLoadout(List.of(heart, pyre), List.of(pyre, heart)));
        assertTrue(AnchoriteAssignmentProgression.changedLoadout(List.of(heart), List.of(pyre)));
    }

    @Test
    void routingProofCannotSkipCounselOrChangedLoadout() {
        assertEquals(AnchoriteAssignmentProgression.RoutingStep.NONE,
                AnchoriteAssignmentProgression.nextRoutingStep(false, false, false));
        assertEquals(AnchoriteAssignmentProgression.RoutingStep.FIRST,
                AnchoriteAssignmentProgression.nextRoutingStep(true, false, false));
        assertEquals(AnchoriteAssignmentProgression.RoutingStep.NONE,
                AnchoriteAssignmentProgression.nextRoutingStep(true, true, false));
        assertEquals(AnchoriteAssignmentProgression.RoutingStep.SECOND,
                AnchoriteAssignmentProgression.nextRoutingStep(true, true, true));
    }
}
