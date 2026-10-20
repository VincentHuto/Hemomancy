package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.player.tracks.AimLayers;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPurpose;
import org.junit.jupiter.api.Test;
import static com.vincenthuto.hemomancy.client.player.AimLayerRules.*;
import static com.vincenthuto.hemomancy.client.player.CastingPose.*;
import static org.junit.jupiter.api.Assertions.*;

class AimLayerRulesTest {
    @Test void weightRampsHoldsAndFades() {
        assertEquals(0,weight(CastPhase.OPENING,5,6,14));
        assertEquals(0,weight(CastPhase.HOLD,50,6,14));
        assertEquals(0,weight(CastPhase.RELEASE,0,6,14));
        assertEquals(.5F,weight(CastPhase.RELEASE,1,6,14),1e-6);
        assertEquals(1,weight(CastPhase.RELEASE,3,6,14));
        assertEquals(1,weight(CastPhase.RECOVERY,0,6,14));
        assertEquals(.5F,weight(CastPhase.RECOVERY,7,6,14),1e-6);
        assertEquals(0,weight(CastPhase.RECOVERY,14,6,14));
        assertEquals(0,weight(CastPhase.CANCEL,2,6,14));
    }
    @Test void applyReplacesLeadArmAndHeadOnly() {
        CastingPose base=pose(2,3,4,-50,-10,-60,20,5), aim=pose(-8,0,0,-90,0,0,0,0);
        var out=apply(base,aim,1);
        assertEquals(aim.rightArm(),out.rightArm()); assertEquals(aim.head(),out.head());
        assertEquals(base.leftArm(),out.leftArm()); assertEquals(base.body(),out.body());
        assertEquals(base.rightLeg(),out.rightLeg()); assertEquals(base.leftLeg(),out.leftLeg());
        assertEquals(base.pitch(),out.pitch());
        assertEquals(base,apply(base,aim,0));
    }
    @Test void applyFirstReplacesLeadHandOnly() {
        HandPose base=HandPose.hands(.1,.1,.1,10,10,10,.2,.2,.2,20,20,20), aim=HandPose.hand(.3,0,0,30,0,0);
        var out=applyFirst(base,aim,1);
        assertEquals(aim.lead(),out.lead()); assertEquals(base.support(),out.support());
    }
    @Test void everyPurposeHasAnAimLayerThatPointsSomewhere() {
        for(var purpose:CastPurpose.values()) {
            var layer=AimLayers.purpose(purpose);
            assertNotNull(layer,purpose.name());
            assertNotEquals(CastingPose.Rotation.ZERO,layer.third().last(CastingPose.ZERO).rightArm(),purpose.name());
            assertNotEquals(HandPose.Hand.ZERO,layer.first().last(HandPose.ZERO).lead(),purpose.name());
        }
    }
}
