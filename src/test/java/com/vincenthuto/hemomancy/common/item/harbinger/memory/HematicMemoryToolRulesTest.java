package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import java.util.ArrayList;
import java.util.List;

public final class HematicMemoryToolRulesTest {
    private static final String OVERWORLD = "minecraft:overworld";
    private static final String NETHER = "minecraft:the_nether";

    private HematicMemoryToolRulesTest() {
    }

    public static void main(String[] args) {
        lastDeathMemoryRoundTripsDimensionAndPosition();
        memoryThreadCapsAndThinsRoutePoints();
        veinSpiderRejectsInvalidLinks();
        witnessOrganCapsRecordingsAndModes();
        huskEffigyProfilesAreBoundedProtoPuppets();
    }

    private static void lastDeathMemoryRoundTripsDimensionAndPosition() {
        LastDeathMemory memory = new LastDeathMemory();
        memory.remember(OVERWORLD, LastDeathMemory.packPosition(12, 64, -8), 12345L);

        java.util.Map<String, String> tag = memory.serialize();
        LastDeathMemory restored = new LastDeathMemory();
        restored.deserialize(tag);

        assertTrue(restored.hasMemory(), "death memory should be present");
        assertEquals(OVERWORLD, restored.dimensionId(), "dimension id");
        assertEquals(LastDeathMemory.packPosition(12, 64, -8), restored.packedPosition(), "position");
        assertEquals(12345L, restored.gameTime(), "game time");
        assertTrue(restored.isSameDimension(OVERWORLD), "same dimension");
        assertFalse(restored.isSameDimension(NETHER), "cross dimension");
    }

    private static void memoryThreadCapsAndThinsRoutePoints() {
        List<Long> points = new ArrayList<>();
        for (int i = 0; i < MemoryThreadRules.MAX_POINTS + 20; i++) {
            MemoryThreadRules.appendRoutePoint(points, MemoryThreadRules.packPosition(i * 3, 64, 0));
        }

        assertEquals(MemoryThreadRules.MAX_POINTS, points.size(), "route cap");
        assertEquals(MemoryThreadRules.packPosition(60, 64, 0), points.get(0).longValue(), "oldest points trimmed");
        assertFalse(MemoryThreadRules.shouldRecord(MemoryThreadRules.packPosition(346, 64, 0), points.get(points.size() - 1)),
                "nearby point rejected");
        assertTrue(MemoryThreadRules.shouldRecord(MemoryThreadRules.packPosition(348, 64, 0), points.get(points.size() - 1)),
                "distant point accepted");
    }

    private static void veinSpiderRejectsInvalidLinks() {
        VeinSpiderRules.Link sameDimension = new VeinSpiderRules.Link(OVERWORLD, VeinSpiderRules.packPosition(0, 64, 0),
                OVERWORLD, VeinSpiderRules.packPosition(64, 64, 0));
        VeinSpiderRules.Link crossDimension = new VeinSpiderRules.Link(OVERWORLD, VeinSpiderRules.packPosition(0, 64, 0),
                NETHER, VeinSpiderRules.packPosition(64, 64, 0));
        VeinSpiderRules.Link tooFar = new VeinSpiderRules.Link(OVERWORLD, VeinSpiderRules.packPosition(0, 64, 0),
                OVERWORLD, VeinSpiderRules.packPosition(257, 64, 0));

        assertTrue(VeinSpiderRules.isValidLink(sameDimension), "same-dimension link in range");
        assertFalse(VeinSpiderRules.isValidLink(crossDimension), "cross-dimension rejected");
        assertFalse(VeinSpiderRules.isValidLink(tooFar), "over range rejected");
        assertTrue(VeinSpiderRules.shouldTransferAt(200L), "transfer cadence hit");
        assertFalse(VeinSpiderRules.shouldTransferAt(201L), "transfer cadence miss");
    }

    private static void witnessOrganCapsRecordingsAndModes() {
        List<WitnessOrganRules.SoundMemory> memories = new ArrayList<>();
        for (int i = 0; i < WitnessOrganRules.MAX_SOUNDS + 4; i++) {
            WitnessOrganRules.append(memories,
                    new WitnessOrganRules.SoundMemory("hemomancy:test_sound_" + i,
                            WitnessOrganRules.packPosition(i, 64, 0), 1.0F, 1.0F));
        }

        assertEquals(WitnessOrganRules.MAX_SOUNDS, memories.size(), "sound cap");
        assertEquals("hemomancy:test_sound_4", memories.get(0).sound(), "oldest sound trimmed");
        assertTrue(WitnessOrganRules.Mode.ONCE.shouldStopAfterPass(), "once mode stops");
        assertFalse(WitnessOrganRules.Mode.LOOP.shouldStopAfterPass(), "loop mode repeats");
        assertTrue(WitnessOrganRules.shouldRecordNote(WitnessOrganRules.packPosition(3, 64, 0),
                WitnessOrganRules.packPosition(0, 64, 0)), "nearby note block is heard");
        assertFalse(WitnessOrganRules.shouldRecordNote(WitnessOrganRules.packPosition(9, 64, 0),
                WitnessOrganRules.packPosition(0, 64, 0)), "distant note block is ignored");
        assertEquals(1.0F, WitnessOrganRules.notePitch(12), "middle F sharp pitch");
        assertTrue(WitnessOrganRules.notePitch(24) > WitnessOrganRules.notePitch(0), "higher note has higher pitch");
    }

    private static void huskEffigyProfilesAreBoundedProtoPuppets() {
        assertEquals(3, HuskEffigyRules.Profile.values().length, "v1 profile count");
        assertEquals(20 * 25, HuskEffigyRules.Profile.HUSK.getLifetimeTicks(), "husk lifetime");
        assertTrue(HuskEffigyRules.Profile.SPIDER.getBloodCost() > HuskEffigyRules.Profile.ZOMBIE.getBloodCost(),
                "spider profile costs more than zombie");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " got " + actual);
        }
    }

    private static void assertEquals(long expected, long actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " got " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " got " + actual);
        }
    }

    private static void assertEquals(float expected, float actual, String label) {
        if (Math.abs(expected - actual) > 0.0001F) {
            throw new AssertionError(label + ": expected " + expected + " got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean value, String label) {
        if (value) {
            throw new AssertionError(label);
        }
    }
}
