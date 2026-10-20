package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StrainDisplayTest {
    @Test void risingChargeIsFollowedAtOnce() {
        var s=new StrainDisplay();
        assertEquals(.3F,s.sample(.3F,10));
        assertEquals(.6F,s.sample(.6F,10.5F));
        assertEquals(1,s.sample(1,11));
    }
    @Test void droppingChargeEasesBackOverFourTicks() {
        var s=new StrainDisplay();
        s.sample(.8F,10);
        assertEquals(.8F,s.sample(0,10));
        assertEquals(.6F,s.sample(0,11),1e-6);
        assertEquals(.4F,s.sample(0,12),1e-6);
        assertEquals(.2F,s.sample(0,13),1e-6);
        assertEquals(0,s.sample(0,14));
        // A small drop takes the same four ticks rather than snapping.
        s.sample(.5F,20);
        s.sample(.25F,20);
        assertTrue(s.sample(.25F,22.9F)>.25F);
        assertEquals(.25F,s.sample(.25F,24));
    }
    @Test void samplingTwiceInOneFrameChangesNothing() {
        var s=new StrainDisplay();
        s.sample(.8F,10);
        s.sample(.2F,11);
        float shown=s.sample(.2F,12);
        assertEquals(shown,s.sample(.2F,12));
    }
    @Test void regainedChargeOverridesTheEase() {
        var s=new StrainDisplay();
        s.sample(.8F,10);
        s.sample(.2F,11);
        assertEquals(.9F,s.sample(.9F,12));
    }
    @Test void aLatchedStrainHoldsUntilReleasedOrReset() {
        var s=new StrainDisplay();
        s.sample(.5F,10);
        s.latch();
        assertEquals(.5F,s.sample(0,11));
        assertEquals(.5F,s.sample(0,30));
        s.unlatch();
        assertEquals(.7F,s.sample(.7F,31));
        s.latch(); s.reset();
        assertEquals(0,s.sample(0,32));
    }
    @Test void onlyASustainedCastEndingInTheSameSequenceLatches() {
        assertTrue(StrainDisplay.latches(true,CastPhase.HOLD,CastPhase.RELEASE));
        assertTrue(StrainDisplay.latches(true,CastPhase.OPENING,CastPhase.CANCEL));
        assertFalse(StrainDisplay.latches(false,CastPhase.HOLD,CastPhase.RELEASE));
        assertFalse(StrainDisplay.latches(true,CastPhase.HOLD,CastPhase.HOLD));
        assertFalse(StrainDisplay.latches(true,CastPhase.RELEASE,CastPhase.CANCEL));
    }
}
