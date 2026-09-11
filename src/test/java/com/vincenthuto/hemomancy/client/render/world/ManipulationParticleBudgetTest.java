package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManipulationParticleBudgetTest {
    @Test void minimalDisablesDecorationAndDecreasedHalvesIt() {
        var budget=new ManipulationParticleBudget();
        assertEquals(0,budget.take(10,12,2));
        assertEquals(6,budget.take(10,12,1));
    }

    @Test void overlappingHitsShareABoundedBudgetWhichResetsNextTick() {
        var budget=new ManipulationParticleBudget();
        int total=0;
        for(int i=0;i<100;i++)total+=budget.take(10,12,0);
        assertEquals(48,total);
        assertEquals(12,budget.take(11,12,0));
    }

    @Test void duplicateTargetHitsAreQuietUntilTheShortCooldownExpires() {
        var budget=new ManipulationParticleBudget();
        assertTrue(budget.hit(24,100));
        assertFalse(budget.hit(24,101));
        assertTrue(budget.hit(25,101));
        assertTrue(budget.hit(24,105));
        budget.clear();
        assertTrue(budget.hit(24,1));
    }
}
