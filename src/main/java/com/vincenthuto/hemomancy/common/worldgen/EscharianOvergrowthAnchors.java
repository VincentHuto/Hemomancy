package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Bounds;
import com.vincenthuto.hemomancy.common.worldgen.EscharianOvergrowthLayout.Cell;
import net.minecraft.core.Direction;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.BiPredicate;

public final class EscharianOvergrowthAnchors {
    public static final int OVERHEAD_ICHOR_REACH = 48;
    public record Anchor(Cell cell, Direction normal, long order) {}
    private EscharianOvergrowthAnchors() {}
    public static List<Anchor> find(Bounds bounds, Predicate<Cell> air, BiPredicate<Cell, Direction> support,
                                     long seed, int budget) {
        return find(bounds, air, support, seed, budget, 8);
    }
    public static List<Anchor> find(Bounds bounds, Predicate<Cell> air, BiPredicate<Cell, Direction> support,
                                     long seed, int budget, int groundChance) {
        List<Anchor> walls = new ArrayList<>(), ceilings = new ArrayList<>(), floors = new ArrayList<>();
        boolean groundFirst = Math.floorMod(seed, groundChance) == 0;
        for (int x = bounds.minX(); x <= bounds.maxX(); x++)
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++)
                for (int y = bounds.minY(); y <= bounds.maxY(); y++) {
                    Cell cell = new Cell(x, y, z);
                    if (!air.test(cell)) continue;
                    for (Direction normal : Direction.values()) {
                        if (!support.test(cell, normal)) continue;
                        long order = PhlegethonticRules.seed(seed ^ y, x, z, normal.ordinal());
                        var anchor = new Anchor(cell, normal, order);
                        (normal == Direction.UP ? floors : normal == Direction.DOWN ? ceilings : walls).add(anchor);
                    }
                }
        for (var group : List.of(walls, ceilings, floors)) group.sort(Comparator.comparingLong(Anchor::order));
        // Give the chosen habitat several contour fits before falling back to easier faces.
        boolean ceilingFirst = Math.floorMod(seed, 3) == 0;
        var preferred = ceilingFirst ? ceilings : walls;
        var other = ceilingFirst ? walls : ceilings;
        List<Anchor> result = new ArrayList<>();
        int p = 0, o = 0;
        if (groundFirst) result.addAll(floors.subList(0, Math.min(floors.size(), Math.max(1, budget / 2))));
        int surfaceBudget = budget - (groundFirst ? 0 : Math.min(floors.size(), Math.min(32, budget / 4)));
        for (int n = 0; n < Math.min(32, budget / 2) && p < preferred.size() && result.size() < surfaceBudget; n++) result.add(preferred.get(p++));
        // Interleave the remaining surfaces so neither can exhaust the other's search budget.
        while (result.size() < surfaceBudget && (p < preferred.size() || o < other.size())) {
            for (int n = 0; n < 2 && p < preferred.size() && result.size() < surfaceBudget; n++) result.add(preferred.get(p++));
            if (o < other.size() && result.size() < surfaceBudget) result.add(other.get(o++));
        }
        for (var floor : floors) {
            if (result.size() >= budget) break;
            if (!result.contains(floor)) result.add(floor);
        }
        return List.copyOf(result);
    }
}
