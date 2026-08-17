package com.vincenthuto.hemomancy.client.screen.manips;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MuscleMemoryRadialPresentationTest {
    @Test
    void emptyReserveIsGrayscaleAndDeadFlowIsBlocked() {
        var state = MuscleMemoryRadialPresentation.resolve(0, false, false, EnumBloodFlow.DEAD, 0L);
        assertEquals(0xFF777777, state.iconTint());
        assertEquals(MuscleMemoryRadialPresentation.CrackSeverity.DEAD, state.cracks());
    }

    @Test
    void activeAndArmedStatesUseDifferentPulseCadence() {
        var activeA = MuscleMemoryRadialPresentation.resolve(6_000, true, false, EnumBloodFlow.STABLE, 0L);
        var activeB = MuscleMemoryRadialPresentation.resolve(6_000, true, false, EnumBloodFlow.STABLE, 10L);
        var armedA = MuscleMemoryRadialPresentation.resolve(6_000, true, true, EnumBloodFlow.STABLE, 0L);
        var armedB = MuscleMemoryRadialPresentation.resolve(6_000, true, true, EnumBloodFlow.STABLE, 4L);
        assertNotEquals(activeA.backgroundColor(), activeB.backgroundColor());
        assertNotEquals(armedA.backgroundColor(), armedB.backgroundColor());
        assertNotEquals(activeA.backgroundColor(), armedA.backgroundColor());
    }

    @Test
    void vascularFlowMapsToIncreasingCrackSeverity() {
        assertEquals(MuscleMemoryRadialPresentation.CrackSeverity.NONE,
                MuscleMemoryRadialPresentation.cracks(EnumBloodFlow.FLOWING));
        assertEquals(MuscleMemoryRadialPresentation.CrackSeverity.FINE,
                MuscleMemoryRadialPresentation.cracks(EnumBloodFlow.VARICOSE));
        assertEquals(MuscleMemoryRadialPresentation.CrackSeverity.DENSE,
                MuscleMemoryRadialPresentation.cracks(EnumBloodFlow.ClOTTED));
    }

    @Test
    void armedDeadAndEmptyChannelsRemainIndependentInOneCombinedState() {
        var state = MuscleMemoryRadialPresentation.resolve(0, true, true, EnumBloodFlow.DEAD, 4L);

        assertEquals(0xFF777777, state.iconTint());
        assertEquals(0xFFEF5050, state.backgroundColor());
        assertEquals(MuscleMemoryRadialPresentation.CrackSeverity.DEAD, state.cracks());
    }
}
