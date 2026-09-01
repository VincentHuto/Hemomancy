package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MuscleMemoryStateTest {
    @Test
    void catalogueDefinesOnePrimaryMemoryForEveryTendency() {
        Set<EnumBloodTendency> primaries = Arrays.stream(MuscleMemory.values())
                .map(MuscleMemory::primaryTendency).collect(Collectors.toSet());
        assertEquals(8, MuscleMemory.values().length);
        assertEquals(Set.of(EnumBloodTendency.values()), primaries);
    }

    @Test
    void dosesBuildAStoredReserveWithoutExpiringWhileInactive() {
        MuscleMemoryState state = new MuscleMemoryState();
        state.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 6_000);
        state.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 6_000);
        assertTrue(state.knows(MuscleMemory.SANGUINE_FISTS));
        assertEquals(12_000, state.reserveTicks(MuscleMemory.SANGUINE_FISTS));
        assertFalse(state.tickActiveReserves());
        assertEquals(12_000, state.reserveTicks(MuscleMemory.SANGUINE_FISTS));
    }

    @Test
    void reserveCapsAtSixDoses() {
        MuscleMemoryState state = new MuscleMemoryState();
        state.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 60_000);
        assertEquals(MuscleMemoryPrimingRules.MAX_RESERVE_TICKS,
                state.reserveTicks(MuscleMemory.SANGUINE_FISTS));
    }

    @Test
    void activatingAnotherMemoryReplacesOnlyThatBodySection() {
        MuscleMemoryState state = new MuscleMemoryState();
        state.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 6_000);
        state.learnAndAddReserve(MuscleMemory.LABORING_ARMS, 6_000);
        state.learnAndAddReserve(MuscleMemory.COURSING_LEGS, 6_000);
        assertTrue(state.activate(MuscleMemory.SANGUINE_FISTS));
        assertTrue(state.activate(MuscleMemory.COURSING_LEGS));
        assertTrue(state.activate(MuscleMemory.LABORING_ARMS));
        assertFalse(state.isEnabled(MuscleMemory.SANGUINE_FISTS));
        assertTrue(state.isEnabled(MuscleMemory.LABORING_ARMS));
        assertTrue(state.isEnabled(MuscleMemory.COURSING_LEGS));
        assertEquals(6_000, state.reserveTicks(MuscleMemory.SANGUINE_FISTS));
    }

    @Test
    void activeMemoriesDrainTheirOwnReserveAndDisableAtZero() {
        MuscleMemoryState state = new MuscleMemoryState();
        state.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 2);
        state.learnAndAddReserve(MuscleMemory.COURSING_LEGS, 1);
        state.activate(MuscleMemory.SANGUINE_FISTS);
        state.activate(MuscleMemory.COURSING_LEGS);
        assertTrue(state.tickActiveReserves());
        assertEquals(1, state.reserveTicks(MuscleMemory.SANGUINE_FISTS));
        assertEquals(0, state.reserveTicks(MuscleMemory.COURSING_LEGS));
        assertFalse(state.isEnabled(MuscleMemory.COURSING_LEGS));
        assertTrue(state.tickActiveReserves());
        assertEquals(0, state.reserveTicks(MuscleMemory.SANGUINE_FISTS));
        assertFalse(state.isEnabled(MuscleMemory.SANGUINE_FISTS));
    }

    @Test
    void armedOverexertionIsSingleUseAndExpires() {
        MuscleMemoryState state = new MuscleMemoryState();
        state.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 6_000);
        state.activate(MuscleMemory.SANGUINE_FISTS);
        state.armOverexertion(MuscleMemory.SANGUINE_FISTS, 1_000L);
        assertFalse(state.consumeOverexertion(MuscleMemory.LABORING_ARMS, 1_001L));
        assertTrue(state.consumeOverexertion(MuscleMemory.SANGUINE_FISTS, 1_199L));
        assertFalse(state.consumeOverexertion(MuscleMemory.SANGUINE_FISTS, 1_199L));
        state.armOverexertion(MuscleMemory.SANGUINE_FISTS, 2_000L);
        assertFalse(state.consumeOverexertion(MuscleMemory.SANGUINE_FISTS, 2_200L));
    }

    @Test
    void saveRoundTripKeepsReserveActivationAndArmedState() {
        MuscleMemoryState original = new MuscleMemoryState();
        original.learnAndAddReserve(MuscleMemory.HUSHED_GAIT, 6_000);
        original.activate(MuscleMemory.HUSHED_GAIT);
        original.armOverexertion(MuscleMemory.HUSHED_GAIT, 1_000L);
        MuscleMemoryState restored = new MuscleMemoryState();
        restored.deserializeNBT(null, original.serializeNBT(null));
        assertEquals(6_000, restored.reserveTicks(MuscleMemory.HUSHED_GAIT));
        assertTrue(restored.isEnabled(MuscleMemory.HUSHED_GAIT));
        assertTrue(restored.isOverexertionArmed(MuscleMemory.HUSHED_GAIT, 1_199L));
    }

    @Test
    void deathCopyKeepsKnowledgeButClearsPreparedState() {
        MuscleMemoryState original = new MuscleMemoryState();
        original.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 6_000);
        original.activate(MuscleMemory.SANGUINE_FISTS);
        original.armOverexertion(MuscleMemory.SANGUINE_FISTS, 1_000L);
        MuscleMemoryState respawned = original.copyForDeath();
        assertTrue(respawned.knows(MuscleMemory.SANGUINE_FISTS));
        assertEquals(0, respawned.reserveTicks(MuscleMemory.SANGUINE_FISTS));
        assertFalse(respawned.isEnabled(MuscleMemory.SANGUINE_FISTS));
        assertFalse(respawned.isOverexertionArmed(MuscleMemory.SANGUINE_FISTS, 1_001L));
    }
}
