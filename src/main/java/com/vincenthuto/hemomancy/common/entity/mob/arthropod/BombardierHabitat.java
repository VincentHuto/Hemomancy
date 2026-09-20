package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class BombardierHabitat {
    private static final Comparator<BlockPos> ANCHOR_ORDER = Comparator.comparingInt((BlockPos pos) -> pos.getY())
            .thenComparingInt(pos -> pos.getZ()).thenComparingInt(pos -> pos.getX());

    private BombardierHabitat() {}

    public static boolean isGrowth(LevelReader level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(BlockInit.escharian_overgrowth.get()) || state.is(BlockInit.escharian_overgrowth_rim.get());
    }

    public static boolean isValidSupport(LevelReader level, BlockPos feetPos) {
        BlockPos support = feetPos.below();
        var floor = level.getBlockState(support).getCollisionShape(level, support);
        return isGrowth(level, support) && !floor.isEmpty()
                && floor.max(net.minecraft.core.Direction.Axis.Y) >= .5
                && level.getBlockState(feetPos).getCollisionShape(level, feetPos).isEmpty()
                && level.getBlockState(feetPos.above()).getCollisionShape(level, feetPos.above()).isEmpty();
    }

    public static Optional<BlockPos> findNearestSupport(LevelReader level, BlockPos origin,
                                                        int horizontalRange, int verticalRange) {
        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int dy = -verticalRange; dy <= verticalRange; dy++)
            for (int dx = -horizontalRange; dx <= horizontalRange; dx++)
                for (int dz = -horizontalRange; dz <= horizontalRange; dz++) {
                    int distance = dx * dx + dz * dz + dy * dy;
                    if (distance > bestDistance) continue;
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (!level.hasChunk(candidate.getX() >> 4, candidate.getZ() >> 4)
                            || !isValidSupport(level, candidate)) continue;
                    if (distance < bestDistance || ANCHOR_ORDER.compare(candidate, best) < 0) {
                        best = candidate.immutable();
                        bestDistance = distance;
                    }
                }
        return Optional.ofNullable(best);
    }

    public static Optional<BlockPos> findNearestGrowth(LevelReader level, BlockPos origin,
                                                       int horizontalRange, int verticalRange) {
        BlockPos best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int dy = -verticalRange; dy <= verticalRange; dy++)
            for (int dx = -horizontalRange; dx <= horizontalRange; dx++)
                for (int dz = -horizontalRange; dz <= horizontalRange; dz++) {
                    int distance = dx * dx + dz * dz + dy * dy;
                    if (distance > bestDistance) continue;
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (!level.hasChunk(candidate.getX() >> 4, candidate.getZ() >> 4)
                            || !isGrowth(level, candidate)) continue;
                    if (distance < bestDistance || ANCHOR_ORDER.compare(candidate, best) < 0) {
                        best = candidate.immutable();
                        bestDistance = distance;
                    }
                }
        return Optional.ofNullable(best);
    }

    public static Optional<BlockPos> resolveComponentAnchor(LevelReader level, BlockPos growthPos, int visitLimit) {
        return resolveComponentAnchor(pos -> level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)
                && isGrowth(level, pos), growthPos, visitLimit);
    }

    static Optional<BlockPos> resolveComponentAnchor(Predicate<BlockPos> growth, BlockPos start, int visitLimit) {
        if (visitLimit <= 0 || !growth.test(start)) return Optional.empty();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(start.immutable());
        BlockPos anchor = start.immutable();
        while (!pending.isEmpty()) {
            BlockPos current = pending.removeFirst();
            if (!growth.test(current) || !visited.add(current)) continue;
            if (visited.size() > visitLimit) return Optional.empty();
            if (ANCHOR_ORDER.compare(current, anchor) < 0) anchor = current;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!visited.contains(neighbor) && growth.test(neighbor)) pending.addLast(neighbor.immutable());
            }
        }
        return Optional.of(anchor);
    }

    public static boolean sharesOutcropping(PhlegethonticBombardier first,
                                             PhlegethonticBombardier second) {
        return first.getTerritoryAnchor().isPresent()
                && first.getTerritoryAnchor().equals(second.getTerritoryAnchor());
    }
}
