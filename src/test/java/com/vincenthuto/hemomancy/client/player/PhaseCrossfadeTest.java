package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.CastingPose.pose;
import static org.junit.jupiter.api.Assertions.*;

class PhaseCrossfadeTest {
    private static final CastingPose A=pose(0,0,0,-40,-10,0,0,0), B=pose(0,0,0,-80,-30,0,0,0), C=pose(0,0,0,-20,0,0,0,0);
    @Test void blendStartsAtThePreviousPoseAndArrivesSmoothly() {
        assertEquals(A,PhaseCrossfade.blend(A,B,0,3));
        assertEquals(B,PhaseCrossfade.blend(B,B,1,3));
        assertEquals(B,PhaseCrossfade.blend(A,B,3,3));
        assertEquals(B,PhaseCrossfade.blend(A,B,5,3));
        assertEquals(A.blend(B,.5F),PhaseCrossfade.blend(A,B,1.5F,3));
        assertTrue(PhaseCrossfade.amount(.5F,3)<.5F/3);
    }
    @Test void phaseOrSequenceChangesFadeFromTheLastShownValue() {
        var fade=new PhaseCrossfade<CastingPose>(CastingPose::blend);
        assertEquals(A,fade.apply(1,CastPhase.OPENING,10,A));
        assertEquals(B,fade.apply(1,CastPhase.OPENING,11,B));
        assertEquals(B,fade.apply(1,CastPhase.CANCEL,11.5F,C));
        assertEquals(C,fade.apply(1,CastPhase.CANCEL,14.5F,C));
        assertEquals(C,fade.apply(2,CastPhase.CANCEL,15,A));
        assertEquals(A,fade.apply(2,CastPhase.CANCEL,18,A));
    }
    @Test void aValueNotShownRecentlyIsNotFadedFrom() {
        var fade=new PhaseCrossfade<CastingPose>(CastingPose::blend);
        fade.apply(1,CastPhase.HOLD,10,A);
        assertEquals(B,fade.apply(1,CastPhase.RELEASE,40,B));
    }
    @Test void scalarsFadeToo() {
        var fade=new PhaseCrossfade<Float>((a,b,t) -> a+(b-a)*t);
        fade.apply(1,CastPhase.RECOVERY,10,1F);
        assertEquals(1F,fade.apply(2,CastPhase.OPENING,10,1/3F));
        assertEquals(1F,fade.apply(2,CastPhase.OPENING,13,1F));
    }
}
