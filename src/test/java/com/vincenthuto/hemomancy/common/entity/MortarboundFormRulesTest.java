package com.vincenthuto.hemomancy.common.entity;

import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundFormRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundFormRules.Form;
import com.vincenthuto.hemomancy.common.entity.mob.monster.MortarboundFormRules.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MortarboundFormRulesTest {
    @Test void alertGrowsEmbeddedCreatureOutOfWallBeforeItCanHunt() {
        State state = new State(Form.EMBEDDED, 0);
        state = MortarboundFormRules.tick(state, true, false);
        assertEquals(Form.EMERGING, state.form());
        assertFalse(MortarboundFormRules.canHunt(state));
        for (int i = 0; i < MortarboundFormRules.TRANSITION_TICKS; i++)
            state = MortarboundFormRules.tick(state, true, false);
        assertEquals(Form.ACTIVE, state.form());
        assertTrue(MortarboundFormRules.canHunt(state));
        assertEquals(1F, MortarboundFormRules.emergence(state), .001F);
    }

    @Test void quietCreatureMeltsBackAndCanReverseWhenAlerted() {
        State retreating = MortarboundFormRules.tick(new State(Form.ACTIVE, 0), false, true);
        assertEquals(Form.RETREATING, retreating.form());
        for (int i = 0; i < 7; i++) retreating = MortarboundFormRules.tick(retreating, false, true);
        float before = MortarboundFormRules.emergence(retreating);
        State reversing = MortarboundFormRules.tick(retreating, true, false);
        assertEquals(Form.EMERGING, reversing.form());
        assertEquals(before, MortarboundFormRules.emergence(reversing), .001F);
        for (int i = 0; i < MortarboundFormRules.TRANSITION_TICKS; i++)
            retreating = MortarboundFormRules.tick(retreating, false, true);
        assertEquals(Form.EMBEDDED, retreating.form());
        assertEquals(0F, MortarboundFormRules.emergence(retreating), .001F);
    }
}
