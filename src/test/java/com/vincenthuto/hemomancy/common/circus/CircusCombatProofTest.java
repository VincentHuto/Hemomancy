package com.vincenthuto.hemomancy.common.circus;

import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetRules;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CircusCombatProofTest {
    @Test void memoryKeepsItsOriginalAttackerWhenClampedAndConsumed() {
        UUID target = UUID.randomUUID(), attacker = UUID.randomUUID();
        var memories = new ArrayList<MnemonistPuppetRules.AttackMemory>();
        MnemonistPuppetRules.record(memories, new MnemonistPuppetRules.AttackMemory(target, 50, 10, attacker));
        var memory = MnemonistPuppetRules.pollReplayMemory(memories, target, 20);
        assertEquals(attacker, memory.attackerId());
        assertEquals(16, memory.damage());
        assertTrue(memories.isEmpty());
    }
    @Test void threeDaggersKeepTheOriginalRoundedProjectileBudget() {
        for (double attack : new double[]{2, 5, 6.25, 9})
            for (double speed : new double[]{1.7, 1.5, 1.2})
                assertEquals(Math.ceil(speed * attack),
                        MarrowJugglerRules.impactDamage(MarrowJugglerRules.daggerDamage(attack), speed) * 3, .0001);
    }
}
