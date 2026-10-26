package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VelorumVisualRulesTest {
    @Test void independentChannelsLayerDuringTheSharedAttackTickAndMartyrdom() {
        var phase=new VelorumVisualRules.Phase(100,15,160,19,80,17,30);
        var pose=VelorumVisualRules.pose(phase);
        assertTrue(pose.frost()>.99F && pose.veil()>.99F && pose.silence()>.99F && pose.martyr()>.99F);
        assertEquals(100,VelorumVisualRules.FROST_INTERVAL);
        assertEquals(160,VelorumVisualRules.VEIL_INTERVAL);
        assertEquals(80,VelorumVisualRules.DRAIN_INTERVAL);
    }
    @Test void anticipationAndRecoveryMeetWithoutAPoseJump() {
        var wound=VelorumVisualRules.pose(new VelorumVisualRules.Phase(1,0,160,0,80,0,0));
        var release=VelorumVisualRules.pose(new VelorumVisualRules.Phase(100,18,160,0,80,0,0));
        assertEquals(wound.gather(),release.gather(),.01F);
        assertEquals(0,VelorumVisualRules.pose(VelorumVisualRules.legacy(1,false,0)).frost());
        assertEquals(42,VelorumVisualRules.frost().total());
    }
    @Test void allMotionAndFrayStayFiniteAndBounded() {
        for(int t=-10;t<180;t++) {
            var p=VelorumVisualRules.pose(new VelorumVisualRules.Phase(t,t,t,t,t,t,t));
            for(float f:new float[]{p.gather(),p.frost(),p.veil(),p.silence(),p.martyr()})assertTrue(Float.isFinite(f) && f>=0 && f<=1.5);
        }
        assertEquals(1,VelorumVisualRules.fade(0,40));assertEquals(0,VelorumVisualRules.fade(40,40));
    }
}
