package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ThelemicMemoryRadialPresentationTest {
    @Test
    void emptyReserveIsGrayscaleAndDeadFlowIsBlocked() {
        var state = ThelemicMemoryRadialPresentation.resolve(0, false, false, EnumBloodFlow.DEAD, 0L);
        assertEquals(0xFF777777, state.iconTint());
        assertEquals(ThelemicMemoryRadialPresentation.CrackSeverity.DEAD, state.cracks());
    }

    @Test
    void activeAndArmedStatesUseDifferentPulseCadence() {
        var activeA = ThelemicMemoryRadialPresentation.resolve(6_000, true, false, EnumBloodFlow.STABLE, 0L);
        var activeB = ThelemicMemoryRadialPresentation.resolve(6_000, true, false, EnumBloodFlow.STABLE, 10L);
        var armedA = ThelemicMemoryRadialPresentation.resolve(6_000, true, true, EnumBloodFlow.STABLE, 0L);
        var armedB = ThelemicMemoryRadialPresentation.resolve(6_000, true, true, EnumBloodFlow.STABLE, 4L);
        assertNotEquals(activeA.backgroundColor(), activeB.backgroundColor());
        assertNotEquals(armedA.backgroundColor(), armedB.backgroundColor());
        assertNotEquals(activeA.backgroundColor(), armedA.backgroundColor());
    }

    @Test
    void vascularFlowMapsToIncreasingCrackSeverity() {
        assertEquals(ThelemicMemoryRadialPresentation.CrackSeverity.NONE,
                ThelemicMemoryRadialPresentation.cracks(EnumBloodFlow.FLOWING));
        assertEquals(ThelemicMemoryRadialPresentation.CrackSeverity.FINE,
                ThelemicMemoryRadialPresentation.cracks(EnumBloodFlow.VARICOSE));
        assertEquals(ThelemicMemoryRadialPresentation.CrackSeverity.DENSE,
                ThelemicMemoryRadialPresentation.cracks(EnumBloodFlow.ClOTTED));
    }

    @Test
    void armedDeadAndEmptyChannelsRemainIndependentInOneCombinedState() {
        var state = ThelemicMemoryRadialPresentation.resolve(0, true, true, EnumBloodFlow.DEAD, 4L);

        assertEquals(0xFF777777, state.iconTint());
        assertEquals(0xFFEF5050, state.backgroundColor());
        assertEquals(ThelemicMemoryRadialPresentation.CrackSeverity.DEAD, state.cracks());
    }
}
