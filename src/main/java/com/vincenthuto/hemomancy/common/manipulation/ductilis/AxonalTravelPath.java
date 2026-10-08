package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A signal follows cell centres. Every crossed edge is checked, including at full travel speed. */
public final class AxonalTravelPath {
    public static final double SPEED = 32.0 / 20.0;
    public static final double PRECISE_SPEED = 6.0 / 20.0;
    private static final double AIM_EPSILON = 1.0E-5;
    public interface Network {
        boolean contains(BlockPos pos);
        boolean node(BlockPos pos);
    }
    public enum Result { MOVING, STOPPED, NODE, LOST }
    private BlockPos anchor;
    private BlockPos previous;
    private BlockPos next;
    private double progress;
    private boolean departed;

    public AxonalTravelPath(BlockPos source) { anchor = source.immutable(); }
    public BlockPos anchor() { return anchor; }
    public Vec3 position() {
        Vec3 start = Vec3.atCenterOf(anchor);
        return next == null ? start : start.lerp(Vec3.atCenterOf(next), progress);
    }
    public boolean atExit(Network network) { return departed && next == null && network.node(anchor); }

    public Result advance(Network network, double distance, int input, Vec3 look) {
        if (!network.contains(anchor)) return Result.LOST;
        if (next != null && !neighbors(network, anchor).contains(next)) {
            next = null;
            progress = 0;
            return Result.STOPPED;
        }
        if (input == 0 || distance <= 0) return atExit(network) ? Result.NODE : Result.STOPPED;
        Vec3 aim = look.scale(input > 0 ? 1 : -1);
        if (next != null && score(anchor, next, aim) < -AIM_EPSILON) {
            BlockPos oldAnchor = anchor;
            anchor = next;
            next = oldAnchor;
            progress = 1.0 - progress;
            previous = null;
        } else if (atExit(network)) {
            if (previous != null && neighbors(network, anchor).contains(previous)
                    && score(anchor, previous, aim) > AIM_EPSILON) {
                next = previous;
                previous = null;
            } else return Result.NODE;
        }

        double remaining = distance;
        while (remaining > 1.0E-7) {
            if (next == null) {
                next = choose(network, anchor, previous, aim);
                if (next == null) return Result.STOPPED;
            }
            if (!neighbors(network, anchor).contains(next)) {
                next = null;
                progress = 0;
                return Result.STOPPED;
            }
            double length = Math.sqrt(anchor.distSqr(next));
            double step = Math.min(remaining, (1.0 - progress) * length);
            progress += step / length;
            remaining -= step;
            if (progress < 1.0 - 1.0E-7) return Result.MOVING;
            previous = anchor;
            anchor = next;
            next = null;
            progress = 0;
            departed = true;
            if (network.node(anchor)) return Result.NODE;
        }
        return Result.MOVING;
    }

    public static List<BlockPos> neighbors(Network network, BlockPos origin) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
            if (!pos.equals(origin) && network.contains(pos)) candidates.add(pos.immutable());
        }
        // A diagonal is an edge only when it does not skip a nearer cell on the same run.
        return candidates.stream().filter(target -> candidates.stream().noneMatch(via ->
                !via.equals(target) && origin.distSqr(via) < origin.distSqr(target)
                        && between(origin, target, via))).toList();
    }

    private static boolean between(BlockPos start, BlockPos end, BlockPos via) {
        return via.getX() >= Math.min(start.getX(), end.getX()) && via.getX() <= Math.max(start.getX(), end.getX())
                && via.getY() >= Math.min(start.getY(), end.getY()) && via.getY() <= Math.max(start.getY(), end.getY())
                && via.getZ() >= Math.min(start.getZ(), end.getZ()) && via.getZ() <= Math.max(start.getZ(), end.getZ());
    }

    private static BlockPos choose(Network network, BlockPos origin, BlockPos previous, Vec3 aim) {
        List<BlockPos> options = neighbors(network, origin).stream()
                .sorted(Comparator.comparingDouble((BlockPos pos) -> score(origin, pos, aim)).reversed()).toList();
        if (options.isEmpty()) return null;
        if (options.size() == 1) return score(origin, options.getFirst(), aim) >= -AIM_EPSILON ? options.getFirst() : null;
        // Follow an unambiguous bend, but let looking back select the incoming fiber.
        if (options.size() == 2 && options.contains(previous) && score(origin, previous, aim) <= AIM_EPSILON) {
            BlockPos onward = options.getFirst().equals(previous) ? options.get(1) : options.getFirst();
            if (score(origin, onward, aim) >= -AIM_EPSILON) return onward;
        }
        double best = score(origin, options.getFirst(), aim);
        double second = score(origin, options.get(1), aim);
        return best >= 0.45 && best - second >= 0.2 ? options.getFirst() : null;
    }

    private static double score(BlockPos origin, BlockPos candidate, Vec3 aim) {
        return Vec3.atCenterOf(candidate).subtract(Vec3.atCenterOf(origin)).normalize().dot(aim.normalize());
    }
}
