package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Pulse;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Stage;
import com.vincenthuto.hemomancy.client.render.world.OutpouringBrushes;
import net.minecraft.world.phys.Vec3;
import java.util.List;

/**
 * The school particles that ride on the outpouring, spawned from the client tick: flammeus licks and flames, frost
 * powder where Congeatio lands, rot and blood spray, light and umbral mist, and Ductilis lightning that branches from the
 * exit out to everything its strands struck.
 */
final class OutpouringAccents {
    /** Ticks between Ductilis strikes; one branching tree per strike rather than a spray of separate bolts. */
    static final int BOLT_EVERY = 4;

    private OutpouringAccents() {}

    /** {@code exits} is null when the cast is no longer emitting. */
    static void tick(OutpouringTracker.Cast cast, double[] exits, double[] outward, Pulse pulse,
                     List<OutpouringSim.Contact> contacts, long now, boolean reduced) {
        int rank = cast.rank.ordinal(), every = reduced ? 2 : 1;
        boolean burst = pulse.stage() == Stage.PEAK && !cast.accentBurst;
        if (burst) cast.accentBurst = true;
        int exitCount = exits == null ? 0 : exits.length / 3;
        switch (cast.school) {
            case FLAMMEUS -> {
                for (int i = 0; i < exitCount; i++) {
                    Vec3 at = point(exits, i), out = point(outward, i);
                    if (now % (2L * every) == 0) OutpouringBrushes.lick(at, out.scale(.04).add(0, .07, 0), (int) now + i, 14, .12 + .03 * rank);
                    if (burst) OutpouringBrushes.flames(at, out, Math.min(10, (4 + 2 * rank) / every), false);
                }
            }
            case CONGEATIO -> {
                for (int i = 0; i < Math.min(3, contacts.size()); i++) {
                    var c = contacts.get(i);
                    OutpouringBrushes.frost(new Vec3(c.hx(), c.hy(), c.hz()), OutpouringGeometry.faceNormal(c.face()).scale(.02), 2, true);
                }
                if (burst) for (int i = 0; i < exitCount; i++) OutpouringBrushes.frost(point(exits, i), point(outward, i), 4 + rank, true);
            }
            case MORTEM -> {
                if (now % (3L * every) == 0) for (int i = 0; i < exitCount; i++)
                    OutpouringBrushes.blood(point(exits, i), point(outward, i), true, 1, true);
            }
            case ANIMUS -> {
                if (burst) for (int i = 0; i < exitCount; i++) OutpouringBrushes.blood(point(exits, i), point(outward, i), false, 3 + rank, false);
            }
            case LUX -> {
                if (cast.sim != null && now % (3L * every) == 0) for (var m : cast.sim.motes())
                    if (m.kind == OutpouringSim.Kind.ORB && (m.seed + now) % 4 == 0)
                        OutpouringBrushes.mist(new Vec3(m.x, m.y, m.z), new Vec3(0, .01, 0), false, .08f, 16);
            }
            case TENEBRIS -> {
                if (now % (2L * every) == 0) for (int i = 0; i < exitCount; i++)
                    OutpouringBrushes.mist(point(exits, i).add(point(outward, i).scale(.3)), point(outward, i).scale(.02).add(0, -.035, 0), true, .16f + .03f * rank, 40, cast.entityId);
            }
            case DUCTILIS -> lightning(cast, exits, contacts, now, rank, reduced);
            default -> {}
        }
    }

    /** One bolt of a strike: {@code weight} 1 for the trunk, less for each generation of fork and twig. */
    record Branch(Vec3 from, Vec3 to, float weight) {}

    private static final com.vincenthuto.hutoslib.client.particle.util.ParticleColor SPARK =
            new com.vincenthuto.hutoslib.client.particle.util.ParticleColor(255, 232, 120),
            HOT = new com.vincenthuto.hutoslib.client.particle.util.ParticleColor(255, 250, 230);

    /**
     * A branching strike: a trunk from the exit to the farthest point the strands hit, a fork to every other hit, twigs off
     * every branch (more with rank) and, from Magister, twigs off the twigs. Each branch grows out of an earlier one.
     */
    static List<Branch> tree(Vec3 root, List<Vec3> sparks, int rank, long seed) {
        var random = new java.util.Random(seed);
        Vec3 end = sparks.stream().max(java.util.Comparator.comparingDouble(root::distanceToSqr)).orElseThrow();
        List<Branch> tree = new java.util.ArrayList<>();
        tree.add(new Branch(root, end, 1));
        for (Vec3 spark : sparks)
            if (spark != end) tree.add(new Branch(root.lerp(end, .25 + .5 * random.nextDouble()), spark, .65F));
        twig(tree, List.copyOf(tree), 1 + (rank + 1) / 2, random);
        if (rank >= 3) twig(tree, List.copyOf(tree.subList(1 + sparks.size() - 1, tree.size())), 1, random);
        return tree;
    }

    private static void twig(List<Branch> tree, List<Branch> parents, int each, java.util.Random random) {
        for (Branch parent : parents) for (int i = 0; i < each; i++) {
            Vec3 axis = parent.to().subtract(parent.from());
            double length = axis.length();
            if (length < 1e-6) continue;
            Vec3 dir = axis.scale(1 / length);
            Vec3 jitter = new Vec3(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5);
            Vec3 side = jitter.subtract(dir.scale(jitter.dot(dir)));
            side = side.lengthSqr() < 1e-8 ? new Vec3(0, 1, 0) : side.normalize();
            Vec3 from = parent.from().lerp(parent.to(), .3 + .6 * random.nextDouble());
            Vec3 to = from.add(dir.scale(.5).add(side.scale(.8)).normalize().scale(length * (.2 + .25 * random.nextDouble())));
            tree.add(new Branch(from, to, parent.weight() * .5F));
        }
    }

    /** One branching tree per strike, with sparks spraying from every branch's end. */
    private static void lightning(OutpouringTracker.Cast cast, double[] exits, List<OutpouringSim.Contact> contacts, long now,
                                  int rank, boolean reduced) {
        for (var c : contacts) if (c.spark() && cast.sparks.size() < 8) cast.sparks.add(new Vec3(c.hx(), c.hy(), c.hz()));
        if (exits == null || exits.length < 3 || cast.sparks.isEmpty() || now - cast.lastBolt < BOLT_EVERY) return;
        long seed = cast.sequence * 7919 + now;
        float figure = .8F + .15F * rank;
        var tree = tree(point(exits, 0), cast.sparks, rank, seed);
        var level = net.minecraft.client.Minecraft.getInstance().level;
        var random = new java.util.Random(seed);
        for (int i = 0; i < tree.size(); i++) {
            var branch = tree.get(i);
            float w = branch.weight();
            OutpouringBrushes.bolt(branch.from(), branch.to(), seed + i, w > .9F ? 8 : w > .5F ? 6 : 4, .09F * figure * w,
                    .014F * figure * Math.max(.45F, w));
            if (level == null) continue;
            int count = (w > .5F ? 4 + rank : 2) / (reduced ? 2 : 1);
            Vec3 at = branch.to();
            for (int k = 0; k < count; k++)
                level.addParticle(com.vincenthuto.hemomancy.common.particle.HemoParticleData.emberSpark(random.nextInt(3) == 0 ? HOT : SPARK,
                        .05F + .03F * w, .95F, 10 + random.nextInt(8)), at.x, at.y, at.z,
                        (random.nextDouble() - .5) * .24, .05 + random.nextDouble() * .12, (random.nextDouble() - .5) * .24);
        }
        cast.sparks.clear();
        cast.lastBolt = now;
    }

    private static Vec3 point(double[] xyz, int i) { return new Vec3(xyz[i * 3], xyz[i * 3 + 1], xyz[i * 3 + 2]); }
}
