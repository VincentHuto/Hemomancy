package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VeinMasonAssignmentsTest {
    @Test
    void assignmentsRespectDegreeAndPriorReward() {
        assertFalse(VeinMasonAssignments.canStartD5(4, true));
        assertFalse(VeinMasonAssignments.canStartD5(5, false));
        assertTrue(VeinMasonAssignments.canStartD5(5, true));
        assertFalse(VeinMasonAssignments.canStartD6(5, true));
        assertTrue(VeinMasonAssignments.canStartD6(6, true));
    }

    @Test
    void aSectionLeftVaricoseCountsEvenWhenItWasVaricoseBefore() {
        assertTrue(VeinMasonAssignments.strainCounts(EnumBloodFlow.VARICOSE));
        assertFalse(VeinMasonAssignments.strainCounts(EnumBloodFlow.STABLE));
    }

    @Test
    void anyVaricoseSectionCountsButADeadOneDoesNot() {
        assertTrue(VeinMasonAssignments.strainCounts(List.of(EnumBloodFlow.STABLE, EnumBloodFlow.VARICOSE)));
        assertFalse(VeinMasonAssignments.strainCounts(List.of(EnumBloodFlow.STABLE, EnumBloodFlow.DEAD)));
    }

    @Test
    void loadoutProofRequiresARealSetChange() {
        var heart = ResourceLocation.fromNamespaceAndPath("hemomancy", "scar_heart");
        var pyre = ResourceLocation.fromNamespaceAndPath("hemomancy", "scar_pyre");
        assertFalse(VeinMasonAssignments.changedLoadout(List.of(heart), List.of(heart)));
        assertFalse(VeinMasonAssignments.changedLoadout(List.of(heart, pyre), List.of(pyre, heart)));
        assertTrue(VeinMasonAssignments.changedLoadout(List.of(heart), List.of(pyre)));
    }

    @Test
    void routingProofCannotSkipCounselOrChangedLoadout() {
        assertEquals(VeinMasonAssignments.RoutingStep.NONE,
                VeinMasonAssignments.nextRoutingStep(false, false, false));
        assertEquals(VeinMasonAssignments.RoutingStep.FIRST,
                VeinMasonAssignments.nextRoutingStep(true, false, false));
        assertEquals(VeinMasonAssignments.RoutingStep.NONE,
                VeinMasonAssignments.nextRoutingStep(true, true, false));
        assertEquals(VeinMasonAssignments.RoutingStep.SECOND,
                VeinMasonAssignments.nextRoutingStep(true, true, true));
    }
}
