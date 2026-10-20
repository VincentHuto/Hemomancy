package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.FirstPersonHandRules.*;
import static org.junit.jupiter.api.Assertions.*;

class FirstPersonHandRulesTest {
    private static final HandPose P=HandPose.hands(.1,.2,.3,10,20,30,.4,.5,.6,40,50,60);
    @Test void leadIsMainArmAndMirrorsForLeftHanded() {
        assertEquals(P.lead(),handFor(P,true,false));
        assertEquals(P.support(),handFor(P,false,false));
        assertEquals(P.lead().mirror(),handFor(P,false,true));
        assertEquals(P.support().mirror(),handFor(P,true,true));
    }
    @Test void humilisAndAwakenedNeverTouchTheOffHand() {
        assertFalse(affectsOffHand(EnumManipulationRank.HUMILIS,null));
        assertFalse(affectsOffHand(null,EnumClarityStage.AWAKENED));
        assertTrue(affectsOffHand(EnumManipulationRank.MEDIOCRITAS,null));
        assertTrue(affectsOffHand(null,EnumClarityStage.DISCERNING));
        assertTrue(affectsOffHand(null,null));
    }
    @Test void bespokeStylesKeepTheirAuthoredSupportHandAtHumilis() {
        assertTrue(affectsOffHand(EnumManipulationRank.HUMILIS,null,com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle.BLOOD_RUSH));
        assertTrue(affectsOffHand(EnumManipulationRank.HUMILIS,null,com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle.DEADLY_GAZE));
        assertFalse(affectsOffHand(EnumManipulationRank.HUMILIS,null,com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle.SCHOOL));
        assertFalse(affectsOffHand(null,EnumClarityStage.AWAKENED,null));
    }
    @Test void translationIsClamped() {
        var h=clamp(new HandPose.Hand(1,-1,-1,0,0,0));
        assertEquals(.35F,h.tx()); assertEquals(-.25F,h.ty()); assertEquals(-.2F,h.tz());
        assertEquals(new HandPose.Hand(.1F,.1F,.1F,1,2,3),clamp(new HandPose.Hand(.1F,.1F,.1F,1,2,3)));
    }
    @Test void switchingHandsMidHoldKeepsTheSameLeadPose() {
        var r=handFor(P,true,false); var l=handFor(P,false,true);
        assertEquals(r.ty(),l.ty()); assertEquals(r.tz(),l.tz()); assertEquals(r.rx(),l.rx());
        assertEquals(-r.tx(),l.tx()); assertEquals(-r.ry(),l.ry()); assertEquals(-r.rz(),l.rz());
    }
    @Test void crosshairZoneIsTheCentralBand() {
        // The right hand rests at +0.64 blocks; moving 0.6 toward centre puts it in the central band.
        assertFalse(insideCrosshairZone(HandPose.Hand.ZERO,true));
        assertTrue(insideCrosshairZone(new HandPose.Hand(-.6F,.5F,0,0,0,0),true));
        assertFalse(insideCrosshairZone(new HandPose.Hand(-.3F,.1F,0,0,0,0),true));
        assertTrue(insideCrosshairZone(new HandPose.Hand(.6F,.5F,0,0,0,0),false));
    }
    @Test void crosshairZoneFollowsTheHandRotation() {
        // The hand is translated then rotated about the eye, so yaw swings its rest point across the frame.
        float yaw=(float)Math.toRadians(28);
        assertFalse(insideCrosshairZone(new HandPose.Hand(-.33F,.3F,0,0,0,0),true));
        assertTrue(insideCrosshairZone(new HandPose.Hand(-.33F,.3F,0,0,yaw,0),true));
        assertTrue(insideCrosshairZone(new HandPose.Hand(.33F,.3F,0,0,-yaw,0),false));
        // Yawing outward carries a translated-in hand back out of the band.
        assertFalse(insideCrosshairZone(new HandPose.Hand(-.6F,.5F,0,0,(float)Math.toRadians(-30),0),true));
        // Pitching the hand down drops it below the raised line.
        assertFalse(insideCrosshairZone(new HandPose.Hand(-.6F,.5F,0,(float)Math.toRadians(-40),0,0),true));
    }
    @Test void restIsOnScreenAndAPitchedDownHandIsNot() {
        assertTrue(onScreen(HandPose.Hand.ZERO,true)); assertTrue(onScreen(HandPose.Hand.ZERO,false));
        // Lux Mediocritas' first hold: lifting the hand while pitching it down 18 degrees sank the fist below the frame.
        assertFalse(onScreen(HandPose.hand(-.10,.15,-.07,-18,0,-6).lead(),true));
        // Pitching the same hand up instead keeps it in view.
        assertTrue(onScreen(HandPose.hand(-.10,.04,-.07,6,0,-6).lead(),true));
        // Swung far past the side of the frame.
        assertFalse(onScreen(HandPose.hand(.35,0,0,0,-40,0).lead(),true));
    }
    @Test void aRaisedHandMayNotShowItsCutEnd() {
        // Ironheart's first raised hammer: lifted 0.2 and pitched up 24 degrees, the shoulder end of the arm floats mid-frame.
        assertFalse(onScreen(HandPose.hand(-.08,.20,.04,24,0,-8).lead(),true));
        assertTrue(onScreen(HandPose.hand(-.04,.08,-.06,12,0,-8).lead(),true));
    }
    @Test void foldingBothFistsToTheMiddleCoversTheCrosshair() {
        // The rest point can stay low while the fist reaches the crosshair; the fist zone catches it.
        // Phoenix Debt's first "on" fold: low and inward, rolled 30 degrees, it met both fists at the crosshair.
        var fold=HandPose.hand(-.30,-.08,.08,10,20,30).lead();
        assertTrue(insideCrosshairZone(fold,true));
        assertTrue(insideCrosshairZone(fold.mirror(),false));
    }
}
