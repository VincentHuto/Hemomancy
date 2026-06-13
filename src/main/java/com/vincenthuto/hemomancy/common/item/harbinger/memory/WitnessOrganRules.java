package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import java.util.List;

public final class WitnessOrganRules {
    public static final int MAX_SOUNDS = 16;
    public static final int BLOOD_COST_PER_PLAYBACK = 10;
    public static final int RECORD_COOLDOWN_TICKS = 5;
    public static final int RECORD_RADIUS_BLOCKS = 8;

    private WitnessOrganRules() {
    }

    public static void append(List<SoundMemory> memories, SoundMemory memory) {
        memories.add(memory);
        while (memories.size() > MAX_SOUNDS) {
            memories.remove(0);
        }
    }

    public static long packPosition(int x, int y, int z) {
        return LastDeathMemory.packPosition(x, y, z);
    }

    public static boolean shouldRecordNote(long notePackedPosition, long organPackedPosition) {
        int dx = LastDeathMemory.unpackX(notePackedPosition) - LastDeathMemory.unpackX(organPackedPosition);
        int dy = LastDeathMemory.unpackY(notePackedPosition) - LastDeathMemory.unpackY(organPackedPosition);
        int dz = LastDeathMemory.unpackZ(notePackedPosition) - LastDeathMemory.unpackZ(organPackedPosition);
        return dx * dx + dy * dy + dz * dz <= RECORD_RADIUS_BLOCKS * RECORD_RADIUS_BLOCKS;
    }

    public static float notePitch(int vanillaNoteId) {
        return (float) Math.pow(2.0D, (vanillaNoteId - 12) / 12.0D);
    }

    public enum Mode {
        ONCE(true),
        LOOP(false);

        private final boolean stopAfterPass;

        Mode(boolean stopAfterPass) {
            this.stopAfterPass = stopAfterPass;
        }

        public boolean shouldStopAfterPass() {
            return stopAfterPass;
        }
    }

    public record SoundMemory(String sound, long packedPosition, float volume, float pitch) {
    }
}
