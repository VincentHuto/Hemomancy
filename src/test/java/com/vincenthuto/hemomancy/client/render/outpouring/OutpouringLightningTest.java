package com.vincenthuto.hemomancy.client.render.outpouring;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringLightningTest {
    private static final Vec3 ROOT = new Vec3(0, 1.2, 0);
    private static final List<Vec3> SPARKS = List.of(new Vec3(3, 0, 0), new Vec3(-2, 0, 1), new Vec3(1, 0, -2));

    @Test void everyStruckPointIsReached() {
        var tree = OutpouringAccents.tree(ROOT, SPARKS, 2, 7L);
        for (Vec3 spark : SPARKS)
            assertTrue(tree.stream().anyMatch(b -> b.to().distanceTo(spark) < 1e-9), "a branch reaches " + spark);
    }

    @Test void branchesGrowOutOfTheTree() {
        var tree = OutpouringAccents.tree(ROOT, SPARKS, 4, 11L);
        assertEquals(ROOT, tree.getFirst().from(), "the trunk starts at the exit");
        for (int i = 1; i < tree.size(); i++) {
            var branch = tree.get(i);
            assertTrue(tree.subList(0, i).stream().anyMatch(parent -> onSegment(branch.from(), parent.from(), parent.to())),
                    "branch " + i + " forks off an earlier branch");
        }
    }

    @Test void higherRanksBranchMore() {
        int low = OutpouringAccents.tree(ROOT, SPARKS, 0, 3L).size(), high = OutpouringAccents.tree(ROOT, SPARKS, 4, 3L).size();
        assertTrue(high > low + 3, "Perfectus strikes a fuller tree: " + low + " vs " + high);
        assertTrue(low >= SPARKS.size() + 2, "even Humilis twigs off its forks");
    }

    @Test void twigsAreThinnerThanTheTrunk() {
        var tree = OutpouringAccents.tree(ROOT, SPARKS, 4, 5L);
        for (int i = 1; i < tree.size(); i++) assertTrue(tree.get(i).weight() < tree.getFirst().weight());
    }

    private static boolean onSegment(Vec3 p, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double t = Math.clamp(p.subtract(a).dot(ab) / ab.lengthSqr(), 0, 1);
        return a.add(ab.scale(t)).distanceTo(p) < 1e-6;
    }
}
