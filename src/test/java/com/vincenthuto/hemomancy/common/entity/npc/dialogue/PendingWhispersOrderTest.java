package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

class PendingWhispersOrderTest {
    private static ListTag inbox(boolean... decisions) {
        ListTag list = new ListTag();
        for (boolean decision : decisions) {
            CompoundTag entry = new CompoundTag();
            entry.putBoolean("decision", decision);
            list.add(entry);
        }
        return list;
    }

    @Test
    void theOldestDecisionWhisperOpensBeforeOlderMemos() {
        assertEquals(2, PendingWhispers.firstToOpen(inbox(false, false, true, false, true)));
    }

    @Test
    void withNoDecisionTheOldestWhisperOpens() {
        assertEquals(0, PendingWhispers.firstToOpen(inbox(false, false, false)));
        assertEquals(0, PendingWhispers.firstToOpen(inbox(true, false)));
    }
}
