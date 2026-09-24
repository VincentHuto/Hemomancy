package com.vincenthuto.hemomancy.common.entity.mob.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/** Geometry shared by wall path selection and its focused tests. */
public final class MortarboundWallRules {
    private MortarboundWallRules() {}
    public record Step(BlockPos cell, Direction face) {}

    public static boolean isValidStep(Direction face, Direction step, boolean support, boolean clear) {
        return face.getAxis().isHorizontal() && step.getAxis() != face.getAxis() && support && clear;
    }

    public static boolean canTurn(Direction face, Direction nextFace, boolean support, boolean clear) {
        return face.getAxis().isHorizontal() && nextFace.getAxis().isHorizontal()
                && face.getAxis() != nextFace.getAxis() && support && clear;
    }

    public static Step next(BlockPos cell, Direction face, Vec3 target,
                            BiPredicate<BlockPos, Direction> anchored, Predicate<BlockPos> clear) {
        Step best = null;
        double score = cell.getCenter().distanceToSqr(target) - .05;
        for (Direction travel : Direction.values()) {
            if (travel.getAxis() == face.getAxis()) continue;
            BlockPos adjacent = cell.relative(travel);
            if (!clear.test(adjacent)) continue;
            for (Direction nextFace : Direction.Plane.HORIZONTAL) {
                if (!anchored.test(adjacent, nextFace)) continue;
                if (nextFace == face && !isValidStep(face, travel, true, true)) continue;
                if (nextFace != face && !canTurn(face, nextFace, true, true)) continue;
                double candidate = adjacent.getCenter().distanceToSqr(target);
                if (candidate < score) { score = candidate; best = new Step(adjacent, nextFace); }
            }
            if (!travel.getAxis().isHorizontal()) continue;
            BlockPos corner = adjacent.relative(face.getOpposite());
            if (clear.test(corner) && anchored.test(corner, travel) && canTurn(face, travel, true, true)) {
                double candidate = corner.getCenter().distanceToSqr(target);
                if (candidate < score) { score = candidate; best = new Step(corner, travel); }
            }
        }
        return best;
    }
}
