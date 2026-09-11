package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManipulationLifecycleTest {
    @Test void verdictFlashesPromptlyThenFadesAcrossTheSecondSecond() {
        var life=ManipulationLifecycle.afterglow(100,40);
        assertEquals(0,life.presence(100));
        assertEquals(1,life.presence(101));
        assertTrue(life.presence(120)>.9, "The beam should still be clear one second after firing");
        assertTrue(life.presence(130)>.3, "The afterglow should remain readable halfway through its fade");
        float previous=1;
        for(int tick=116;tick<=140;tick++) {
            float opacity=life.presence(tick);
            assertTrue(opacity<=previous && previous-opacity<.07, "The trail must fade without a sudden cutoff");
            previous=opacity;
        }
        assertEquals(0,life.presence(140));
        life.retire(140);
        assertEquals(0,life.retiredOpacity(), "Expiry must not flash the beam back into view");
    }

    @Test void aShortEffectFormsAndDissolvesWithinItsExistingLifetime() {
        var life=new ManipulationLifecycle(100,8,false);
        assertEquals(0,life.formation(100));
        assertEquals(1,life.formation(102));
        assertEquals(1,life.presence(104));
        assertTrue(life.presence(107)<life.presence(106));
        assertEquals(0,life.presence(108));
    }

    @Test void sustainedRefreshDoesNotRestartFormationOrPulseTheOpacity() {
        var life=new ManipulationLifecycle(100,12,true);
        assertEquals(1,life.presence(111.9));
        life.refresh(110,12);
        assertEquals(1,life.formation(112));
        assertEquals(1,life.presence(121.9));
        assertFalse(life.expired(121));
        assertTrue(life.expired(122));
    }

    @Test void cancellationHasOneBoundedDecorativeTail() {
        var life=new ManipulationLifecycle(100,200,true);
        assertTrue(life.retire(120));
        assertFalse(life.retire(121));
        assertEquals(1,life.residue(120));
        assertTrue(life.residue(124)>0);
        assertEquals(0,life.residue(128));
        assertTrue(life.finished(128));
    }

    @Test void everySpatialSampleFinishesFormingAndFullyDissolves() {
        for(float y:new float[]{-5,0,.5F,3,20}) {
            assertEquals(0,ManipulationLifecycle.reveal(0,.3F,y,.8F));
            assertEquals(1,ManipulationLifecycle.reveal(1,.3F,y,.8F));
            float previous=0;
            for(int i=0;i<=100;i++) {
                float next=ManipulationLifecycle.reveal(i/100F,.3F,y,.8F);
                assertTrue(next>=previous); previous=next;
            }
        }
    }

    @Test void naturalExpiryDoesNotResurrectAnAlreadyFadedSubject() {
        var life=new ManipulationLifecycle(100,20,false);
        life.retire(120);
        assertEquals(0,life.retiredOpacity());
    }
}
