package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VicarContinuingLessonsTest {
    @Test
    void unfinishedConsecrationRemainsAvailableAfterAdvancement() {
        for (int degree : new int[]{6, 7, 8}) {
            DialogueTree base = HarbingerVicarDialogueTrees.forDegree(degree, 42, false);
            DialogueTree continued = HarbingerVicarDialogueTrees.withContinuingConsecration(base, degree, false);
            assertTrue(continued.getStartNode().options().stream()
                    .anyMatch(option -> "armature_consecration".equals(option.nextNodeId())));
            assertTrue(continued.getNode("armature_consecration").options().stream()
                    .anyMatch(option -> HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT.equals(option.eventId())));
            assertSame(base, HarbingerVicarDialogueTrees.withContinuingConsecration(base, degree, true));
        }
    }
}
