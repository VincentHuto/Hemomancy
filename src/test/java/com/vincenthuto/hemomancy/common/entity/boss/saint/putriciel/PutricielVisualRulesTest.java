package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PutricielVisualRulesTest {
    @Test void novaBuildsBeforeReleaseWithoutChangingTheExistingCadence() {
        assertEquals(120,PutricielVisualRules.NOVA_INTERVAL);
        assertEquals(42,PutricielVisualRules.nova().total());
        assertEquals(0,PutricielVisualRules.warning(25));
        assertTrue(PutricielVisualRules.warning(1)>.99F);
        var gathered=PutricielVisualRules.pose(0,false,0,0,0,1);
        var release=PutricielVisualRules.pose(1,false,0,18,0,120);
        assertEquals(gathered.gather(),release.gather(),.01F);
        assertTrue(PutricielVisualRules.pose(1,false,0,15,0,117).release()>.99F);
    }
    @Test void absolutionHitAndNovaLayerOverTheOfferingWindow() {
        var open=PutricielVisualRules.pose(0,true,2,0,50,60);
        var hit=PutricielVisualRules.pose(2,true,2,10,50,60);
        var nova=PutricielVisualRules.pose(1,true,2,15,50,117);
        assertEquals(1,open.offering()); assertEquals(open.offering(),hit.offering());
        assertTrue(hit.flinch()>.5F); assertTrue(nova.offering()>.99F && nova.release()>.99F);
        assertEquals(0,PutricielVisualRules.progress(-1)); assertEquals(1,PutricielVisualRules.progress(6));
    }
    @Test void hitNovaAndOfferingCanAllPlayOnTheSameFrame() {
        var combined=PutricielVisualRules.pose(2,true,3,10,45,117,15);
        assertTrue(combined.release()>.99F && combined.flinch()>.5F && combined.offering()>.99F);
    }
    @Test void allChannelsAndRetirementStayFiniteAndBounded() {
        for(int visual=0;visual<=2;visual++) for(int t=-10;t<=130;t++) {
            var p=PutricielVisualRules.pose(visual,true,3,t,t,t);
            for(float f:new float[]{p.gather(),p.release(),p.offering(),p.flinch(),p.spores()})
                assertTrue(Float.isFinite(f) && Math.abs(f)<=1.01F);
        }
        assertEquals(1,PutricielVisualRules.fade(0,40));
        assertEquals(0,PutricielVisualRules.fade(40,40));
    }
}
