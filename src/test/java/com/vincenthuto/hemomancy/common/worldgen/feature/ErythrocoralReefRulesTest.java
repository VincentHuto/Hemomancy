package com.vincenthuto.hemomancy.common.worldgen.feature;

final class ErythrocoralReefRulesTest {
    public static void main(String[] args) {
        rejectsDryOrShallowPositions();
        acceptsStableSubmergedFloors();
        keepsClusterShapeCompact();
    }

    private static void rejectsDryOrShallowPositions() {
        assertFalse(ErythrocoralReefRules.canUseFloor(false, true, true, false, ErythrocoralReefRules.MIN_WATER_DEPTH),
                "reefs require water at the placement origin");
        assertFalse(ErythrocoralReefRules.canUseFloor(true, false, true, false, ErythrocoralReefRules.MIN_WATER_DEPTH),
                "reefs require water above the placement origin");
        assertFalse(ErythrocoralReefRules.canUseFloor(true, true, true, false, ErythrocoralReefRules.MIN_WATER_DEPTH - 1),
                "reefs should not generate in shallow shelf puddles");
    }

    private static void acceptsStableSubmergedFloors() {
        assertTrue(ErythrocoralReefRules.canUseFloor(true, true, true, false, ErythrocoralReefRules.MIN_WATER_DEPTH),
                "stable underwater floors are valid reef anchors");
        assertFalse(ErythrocoralReefRules.canUseFloor(true, true, false, false, ErythrocoralReefRules.MIN_WATER_DEPTH),
                "reefs should not float over non-sturdy floor blocks");
        assertFalse(ErythrocoralReefRules.canUseFloor(true, true, true, true, ErythrocoralReefRules.MIN_WATER_DEPTH),
                "reefs should not anchor onto liquid blocks");
    }

    private static void keepsClusterShapeCompact() {
        assertTrue(ErythrocoralReefRules.isInsideReefCluster(0, 0, 3), "cluster center is always included");
        assertTrue(ErythrocoralReefRules.isInsideReefCluster(2, 1, 3), "nearby offsets are included");
        assertFalse(ErythrocoralReefRules.isInsideReefCluster(4, 4, 3), "far offsets are outside compact reef clusters");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
