package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public final class VeinSpiderRules {
    public static final int MAX_RANGE_BLOCKS = 256;
    public static final int TRANSFER_INTERVAL_TICKS = 20;
    public static final int BLOOD_COST_PER_TRANSFER = 2;
    public static final int COURIER_VISIBLE_TICKS = 24;
    public static final int SIDE_DOWN = 0;
    public static final int SIDE_UP = 1;
    public static final int SIDE_NORTH = 2;
    public static final int SIDE_SOUTH = 3;
    public static final int SIDE_WEST = 4;
    public static final int SIDE_EAST = 5;
    public static final int SIDE_NULL = 6;
    private static final int[] TRANSFER_SIDE_PROBE_ORDER = {
            SIDE_DOWN, SIDE_UP, SIDE_NORTH, SIDE_SOUTH, SIDE_WEST, SIDE_EAST, SIDE_NULL
    };

    private VeinSpiderRules() {
    }

    public static boolean isValidLink(Link link) {
        return link != null
                && link.fromDimension().equals(link.toDimension())
                && distanceSquared(link.fromPackedPos(), link.toPackedPos()) <= MAX_RANGE_BLOCKS * MAX_RANGE_BLOCKS;
    }

    public static boolean shouldTransferAt(long gameTime) {
        return gameTime % TRANSFER_INTERVAL_TICKS == 0L;
    }

    public static EndpointAction endpointAction(boolean hasFrom, boolean hasTo, boolean sneaking) {
        if (!hasFrom) {
            return EndpointAction.STORE_FIRST;
        }
        if (!hasTo) {
            return EndpointAction.STORE_SECOND;
        }
        return sneaking ? EndpointAction.STORE_FIRST : EndpointAction.STORE_SECOND;
    }

    public static int[] transferSideProbeOrder() {
        return TRANSFER_SIDE_PROBE_ORDER.clone();
    }

    public static boolean isCourierExpired(int remainingTicks) {
        return remainingTicks <= 0;
    }

    public static float courierProgress(int remainingTicks, int visibleTicks, float partialTick) {
        int safeVisibleTicks = Math.max(1, visibleTicks);
        float elapsed = safeVisibleTicks - Math.max(0.0F, remainingTicks - partialTick);
        return Math.max(0.0F, Math.min(1.0F, elapsed / safeVisibleTicks));
    }

    public static long packPosition(int x, int y, int z) {
        return LastDeathMemory.packPosition(x, y, z);
    }

    public static double distanceSquared(long first, long second) {
        long dx = LastDeathMemory.unpackX(first) - LastDeathMemory.unpackX(second);
        long dy = LastDeathMemory.unpackY(first) - LastDeathMemory.unpackY(second);
        long dz = LastDeathMemory.unpackZ(first) - LastDeathMemory.unpackZ(second);
        return dx * dx + dy * dy + dz * dz;
    }

    public record Link(String fromDimension, long fromPackedPos, String toDimension, long toPackedPos) {
    }

    public enum EndpointAction {
        STORE_FIRST,
        STORE_SECOND
    }
}
