package com.vincenthuto.hemomancy.common.entity.summon;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class MnemonistPuppetRules {
    public static final int MAX_MEMORIES = 6;
    public static final int MEMORY_EXPIRY_TICKS = 160;
    public static final int REPLAY_INTERVAL_TICKS = 40;
    public static final float MAX_STORED_DAMAGE = 16.0F;
    public static final float REPLAY_DAMAGE_FRACTION = 0.45F;
    public static final float MAX_REPLAY_DAMAGE = 8.0F;

    private MnemonistPuppetRules() {
    }

    public static void record(List<AttackMemory> memories, AttackMemory memory) {
        if (memories == null || memory == null) {
            return;
        }
        float damage = Math.max(0.0F, Math.min(MAX_STORED_DAMAGE, memory.damage()));
        memories.add(new AttackMemory(memory.targetId(), damage, memory.gameTime()));
        while (memories.size() > MAX_MEMORIES) {
            memories.remove(0);
        }
    }

    public static void pruneExpired(List<AttackMemory> memories, long gameTime) {
        if (memories == null) {
            return;
        }
        memories.removeIf(memory -> isExpired(memory, gameTime));
    }

    public static boolean canRecord(UUID expectedTarget, UUID damagedTarget, float damage, boolean replayDamage) {
        return !replayDamage
                && expectedTarget != null
                && expectedTarget.equals(damagedTarget)
                && damage > 0.0F;
    }

    public static float replayDamage(float storedDamage) {
        return Math.max(0.0F, Math.min(MAX_REPLAY_DAMAGE, storedDamage * REPLAY_DAMAGE_FRACTION));
    }

    public static AttackMemory pollReplayMemory(List<AttackMemory> memories, UUID targetId, long gameTime) {
        if (memories == null || targetId == null) {
            return null;
        }
        pruneExpired(memories, gameTime);
        Iterator<AttackMemory> iterator = memories.iterator();
        while (iterator.hasNext()) {
            AttackMemory memory = iterator.next();
            if (targetId.equals(memory.targetId())) {
                iterator.remove();
                return memory;
            }
        }
        return null;
    }

    private static boolean isExpired(AttackMemory memory, long gameTime) {
        return memory == null || gameTime - memory.gameTime() > MEMORY_EXPIRY_TICKS;
    }

    public record AttackMemory(UUID targetId, float damage, long gameTime) {
    }
}
