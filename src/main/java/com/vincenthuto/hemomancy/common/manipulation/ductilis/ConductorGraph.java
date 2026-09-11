package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class ConductorGraph {
    public static final int MAX_NODES = 256;
    public static final int RADIUS = 12;
    private ConductorGraph() { }

    public static List<BlockPos> collect(BlockPos seed, Predicate<BlockPos> loaded,
            Predicate<BlockPos> conductor) {
        List<BlockPos> nodes = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        var queue = new ArrayDeque<BlockPos>();
        queue.add(seed.immutable()); seen.add(seed.immutable());
        while (!queue.isEmpty() && nodes.size() < MAX_NODES) {
            BlockPos pos = queue.removeFirst();
            if (pos.distSqr(seed) > RADIUS*RADIUS || !loaded.test(pos) || !conductor.test(pos)) continue;
            nodes.add(pos);
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (seen.add(next)) queue.addLast(next);
            }
        }
        return List.copyOf(nodes);
    }
}
