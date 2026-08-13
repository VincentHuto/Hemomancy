package com.vincenthuto.hemomancy.common.worldgen.arbor;

import java.util.List;
import java.util.Optional;

/** Pure crosshair selection shared by the Arbor HUD and fruit interaction. */
public final class ArborFruitTargeting {
    private ArborFruitTargeting() { }

    public static Optional<Candidate> select(List<Candidate> candidates, ArborCanopyGeometry.Point eye,
            ArborCanopyGeometry.Point look, double reach) {
        ArborCanopyGeometry.Point direction = look.normalized();
        Candidate best = null;
        double bestAlong = reach + 1.0;
        for (Candidate candidate : candidates) {
            ArborCanopyGeometry.Point offset = candidate.position().subtract(eye);
            double along = offset.dot(direction);
            if (along < 0 || along > reach || along >= bestAlong) continue;
            double missSqr = offset.subtract(direction.scale(along)).dot(
                    offset.subtract(direction.scale(along)));
            if (missSqr <= candidate.radius() * candidate.radius()) {
                best = candidate;
                bestAlong = along;
            }
        }
        return Optional.ofNullable(best);
    }

    public record Candidate(int arborEntityId, int skillId, ArborCanopyGeometry.Point position, double radius) { }
}
