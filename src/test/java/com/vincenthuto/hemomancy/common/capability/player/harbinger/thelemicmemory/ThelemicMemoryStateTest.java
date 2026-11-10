package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ThelemicMemoryStateTest {
    @Test
    void catalogueDefinesOnePrimaryMemoryForEveryTemper() {
        Set<EnumBloodTemper> primaries = Arrays.stream(ThelemicMemory.values())
                .map(ThelemicMemory::primaryTemper).collect(Collectors.toSet());
        assertEquals(8, ThelemicMemory.values().length);
        assertEquals(Set.of(EnumBloodTemper.values()), primaries);
    }

    @Test
    void dosesBuildAStoredReserveWithoutExpiringWhileInactive() {
        ThelemicMemoryState state = new ThelemicMemoryState();
        state.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 6_000);
        state.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 6_000);
        assertTrue(state.knows(ThelemicMemory.SANGUINE_FISTS));
        assertEquals(12_000, state.reserveTicks(ThelemicMemory.SANGUINE_FISTS));
        assertFalse(state.tickActiveReserves());
        assertEquals(12_000, state.reserveTicks(ThelemicMemory.SANGUINE_FISTS));
    }

    @Test
    void reserveCapsAtSixDoses() {
        ThelemicMemoryState state = new ThelemicMemoryState();
        state.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 60_000);
        assertEquals(ThelemicMemoryPrimingRules.MAX_RESERVE_TICKS,
                state.reserveTicks(ThelemicMemory.SANGUINE_FISTS));
    }

    @Test
    void activatingAnotherMemoryReplacesOnlyThatBodySection() {
        ThelemicMemoryState state = new ThelemicMemoryState();
        state.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 6_000);
        state.learnAndAddReserve(ThelemicMemory.LABORING_ARMS, 6_000);
        state.learnAndAddReserve(ThelemicMemory.COURSING_LEGS, 6_000);
        assertTrue(state.activate(ThelemicMemory.SANGUINE_FISTS));
        assertTrue(state.activate(ThelemicMemory.COURSING_LEGS));
        assertTrue(state.activate(ThelemicMemory.LABORING_ARMS));
        assertFalse(state.isEnabled(ThelemicMemory.SANGUINE_FISTS));
        assertTrue(state.isEnabled(ThelemicMemory.LABORING_ARMS));
        assertTrue(state.isEnabled(ThelemicMemory.COURSING_LEGS));
        assertEquals(6_000, state.reserveTicks(ThelemicMemory.SANGUINE_FISTS));
    }

    @Test
    void activeMemoriesDrainTheirOwnReserveAndDisableAtZero() {
        ThelemicMemoryState state = new ThelemicMemoryState();
        state.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 2);
        state.learnAndAddReserve(ThelemicMemory.COURSING_LEGS, 1);
        state.activate(ThelemicMemory.SANGUINE_FISTS);
        state.activate(ThelemicMemory.COURSING_LEGS);
        assertTrue(state.tickActiveReserves());
        assertEquals(1, state.reserveTicks(ThelemicMemory.SANGUINE_FISTS));
        assertEquals(0, state.reserveTicks(ThelemicMemory.COURSING_LEGS));
        assertFalse(state.isEnabled(ThelemicMemory.COURSING_LEGS));
        assertTrue(state.tickActiveReserves());
        assertEquals(0, state.reserveTicks(ThelemicMemory.SANGUINE_FISTS));
        assertFalse(state.isEnabled(ThelemicMemory.SANGUINE_FISTS));
    }

    @Test
    void armedOverexertionIsSingleUseAndExpires() {
        ThelemicMemoryState state = new ThelemicMemoryState();
        state.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 6_000);
        state.activate(ThelemicMemory.SANGUINE_FISTS);
        state.armOverexertion(ThelemicMemory.SANGUINE_FISTS, 1_000L);
        assertFalse(state.consumeOverexertion(ThelemicMemory.LABORING_ARMS, 1_001L));
        assertTrue(state.consumeOverexertion(ThelemicMemory.SANGUINE_FISTS, 1_199L));
        assertFalse(state.consumeOverexertion(ThelemicMemory.SANGUINE_FISTS, 1_199L));
        state.armOverexertion(ThelemicMemory.SANGUINE_FISTS, 2_000L);
        assertFalse(state.consumeOverexertion(ThelemicMemory.SANGUINE_FISTS, 2_200L));
    }

    @Test
    void saveRoundTripKeepsReserveActivationAndArmedState() {
        ThelemicMemoryState original = new ThelemicMemoryState();
        original.learnAndAddReserve(ThelemicMemory.HUSHED_GAIT, 6_000);
        original.activate(ThelemicMemory.HUSHED_GAIT);
        original.armOverexertion(ThelemicMemory.HUSHED_GAIT, 1_000L);
        ThelemicMemoryState restored = new ThelemicMemoryState();
        restored.deserializeNBT(null, original.serializeNBT(null));
        assertEquals(6_000, restored.reserveTicks(ThelemicMemory.HUSHED_GAIT));
        assertTrue(restored.isEnabled(ThelemicMemory.HUSHED_GAIT));
        assertTrue(restored.isOverexertionArmed(ThelemicMemory.HUSHED_GAIT, 1_199L));
    }

    @Test
    void deathCopyKeepsKnowledgeButClearsPreparedState() {
        ThelemicMemoryState original = new ThelemicMemoryState();
        original.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 6_000);
        original.activate(ThelemicMemory.SANGUINE_FISTS);
        original.armOverexertion(ThelemicMemory.SANGUINE_FISTS, 1_000L);
        ThelemicMemoryState respawned = original.copyForDeath();
        assertTrue(respawned.knows(ThelemicMemory.SANGUINE_FISTS));
        assertEquals(0, respawned.reserveTicks(ThelemicMemory.SANGUINE_FISTS));
        assertFalse(respawned.isEnabled(ThelemicMemory.SANGUINE_FISTS));
        assertFalse(respawned.isOverexertionArmed(ThelemicMemory.SANGUINE_FISTS, 1_001L));
    }
}
