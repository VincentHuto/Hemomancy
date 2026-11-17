package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Chunk 15 T10 (D-43): the columns a troupe site's terrain gate samples. The four corners and the four edge midpoints
 * first, so a steep or wet side is refused after a few queries, then a coarse grid {@code step} blocks apart that
 * always takes the far edges. Every column is asked once. A site brings its own ground, so the columns between samples
 * are left to the template's skirt and the terrain adaptation.
 */
public final class TroupeFootprintScan {
    @FunctionalInterface
    public interface ColumnCheck { boolean test(int x, int z); }

    private TroupeFootprintScan() {
    }

    public static boolean matches(int minX, int maxX, int minZ, int maxZ, int step, ColumnCheck check) {
        for (long column : columns(minX, maxX, minZ, maxZ, step))
            if (!check.test((int) (column >> 32), (int) column)) return false;
        return true;
    }

    /** The sampled columns in the order they are asked, packed x in the high half and z in the low half. */
    public static Set<Long> columns(int minX, int maxX, int minZ, int maxZ, int step) {
        Set<Long> columns = new LinkedHashSet<>();
        for (int x : new int[]{minX, maxX}) for (int z : new int[]{minZ, maxZ}) columns.add(pack(x, z));
        int[] xs = edges(minX, maxX), zs = edges(minZ, maxZ);
        for (int x : xs) for (int z : zs) columns.add(pack(x, z));
        for (int x : grid(minX, maxX, step)) for (int z : grid(minZ, maxZ, step)) columns.add(pack(x, z));
        return columns;
    }

    private static int[] edges(int min, int max) {
        return new int[]{min, max, min + (max - min) / 2};
    }

    private static int[] grid(int min, int max, int step) {
        int count = (max - min) / step + 1;
        boolean tail = min + (count - 1) * step != max;
        int[] values = new int[count + (tail ? 1 : 0)];
        for (int i = 0; i < count; i++) values[i] = min + i * step;
        if (tail) values[count] = max;
        return values;
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }

	/** The centred square of side {@code core} inside {@code box}, or the whole box when {@code core} is 0. */
	public static net.minecraft.world.level.levelgen.structure.BoundingBox gateCore(
			net.minecraft.world.level.levelgen.structure.BoundingBox box, int core) {
		if (core <= 0) return box;
		int insetX = Math.max(0, (box.getXSpan() - core) / 2), insetZ = Math.max(0, (box.getZSpan() - core) / 2);
		return new net.minecraft.world.level.levelgen.structure.BoundingBox(box.minX() + insetX, box.minY(),
				box.minZ() + insetZ, box.maxX() - insetX, box.maxY(), box.maxZ() - insetZ);
	}
}
