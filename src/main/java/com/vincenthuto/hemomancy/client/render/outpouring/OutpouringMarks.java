package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Detail;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Mode;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Where the blood touched the world: frost, scorch, corrosion, iron crust, soot or a quivering pool on a block face.
 * Client-only and never saved; they grow in, hold, and fade. The oldest give way past {@link #CAP}.
 */
public final class OutpouringMarks {
    public static final int CAP = 256;
    /** Marks within this many blocks of the caster's feet survive Decreased particles and passive toggles. */
    static final double FEET = 1.5;
    static final OutpouringMarks CLIENT = new OutpouringMarks();

    /** {@code u, v} place the hit on the face (0..1 along {@link OutpouringGeometry#faceAxes}). */
    public record Mark(int bx, int by, int bz, int face, double u, double v, EnumBloodTendency school, float size, long born,
                       int life, int seed, boolean pool, boolean spark) {}

    private final Deque<Mark> marks = new ArrayDeque<>();

    void add(Mark mark) {
        if (mark.pool() && feed(mark)) return;
        if (marks.size() >= CAP) marks.pollFirst();
        marks.addLast(mark);
    }

    /** Blood landing in an existing pool on the same floor feeds it: the pool grows and lasts from the latest drop. */
    private boolean feed(Mark drop) {
        double x = drop.bx() + drop.u(), z = drop.bz() + drop.v();
        for (var it = marks.iterator(); it.hasNext(); ) {
            Mark pool = it.next();
            if (!pool.pool() || pool.by() != drop.by() || pool.face() != drop.face()) continue;
            double dx = pool.bx() + pool.u() - x, dz = pool.bz() + pool.v() - z, reach = .45 + pool.size() * .5;
            if (dx * dx + dz * dz > reach * reach) continue;
            it.remove();
            long end = Math.max(pool.born() + pool.life(), drop.born() + drop.life());
            marks.addLast(new Mark(pool.bx(), pool.by(), pool.bz(), pool.face(), pool.u(), pool.v(), pool.school(),
                    Math.min(.9f, pool.size() + drop.size() * .2f), pool.born(), (int) (end - pool.born()), pool.seed(), true, false));
            return true;
        }
        return false;
    }

    /** Whether the floor column at (x, z) carries a pool at the pool's level: solid underneath, open above. */
    public interface Floor { boolean open(int x, int z); }

    /** The pool's render radius for a mark of this size. */
    static double poolRadius(Mark m) { return m.size() * .38; }

    /** A pool's centre, not clipped to its landing block. */
    static double[] poolCentre(Mark m, long now, float partial) { return poolCentre(m, now, partial, (x, z) -> true); }

    /** Where a pool lies: where the blood landed. It quivers at its edge (in the shader) but never slides. */
    static double[] poolCentre(Mark m, long now, float partial, Floor floor) {
        return new double[]{m.bx() + m.u(), m.by() + 1, m.bz() + m.v()};
    }

    /** The largest radius up to {@code want} whose disc lies wholly on open floor (0 when the centre itself is not). */
    static double fit(double cx, double cz, double want, Floor floor) {
        for (int i = 0; ; i++) {
            double r = want - i * .02;
            if (r <= 1e-9) return 0;
            if (clear(cx, cz, r, floor)) return r;
        }
    }

    private static boolean clear(double cx, double cz, double r, Floor floor) {
        if (!floor.open((int) Math.floor(cx), (int) Math.floor(cz))) return false;
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8;
            for (double ring : new double[]{r, r * .5})
                if (!floor.open((int) Math.floor(cx + Math.cos(a) * ring), (int) Math.floor(cz + Math.sin(a) * ring))) return false;
        }
        return true;
    }

    void prune(long now) { marks.removeIf(m -> now >= m.born() + m.life()); }
    void clear() { marks.clear(); }
    List<Mark> live() { return new ArrayList<>(marks); }

    static boolean exposed(int face, boolean neighbourSolid) { return !neighbourSolid; }

    /** The contacts a viewer keeps: all at full detail, only those at the feet when reduced or for a passive toggle. */
    static List<OutpouringSim.Contact> keep(List<OutpouringSim.Contact> contacts, Detail detail, Mode mode, double fx, double fy, double fz) {
        if (detail == Detail.VEINS || detail == Detail.NONE) return List.of();
        boolean feetOnly = detail == Detail.REDUCED || mode == Mode.TOGGLE;
        List<OutpouringSim.Contact> kept = new ArrayList<>();
        for (var c : contacts) {
            double dx = c.hx() - fx, dy = c.hy() - fy, dz = c.hz() - fz;
            if (!feetOnly || dx * dx + dy * dy + dz * dz <= FEET * FEET) kept.add(c);
        }
        return kept;
    }

    /** How far a mark has faded: nothing for the first 70% of its life, then a smooth fall to gone. */
    static double fade(double age, int life) {
        double start = life * .7, t = Math.clamp((age - start) / (life - start), 0, 1);
        return t * t * (3 - 2 * t);
    }

    /** Marks within 48 blocks whose block is still there and whose face still meets open air (not fluid). */
    static List<Mark> visible(List<Mark> marks, net.minecraft.world.level.Level level, Vec3 camera) {
        List<Mark> shown = new ArrayList<>();
        var pos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (var m : marks) {
            if (camera.distanceToSqr(m.bx() + .5, m.by() + .5, m.bz() + .5) > 48 * 48) continue;
            pos.set(m.bx(), m.by(), m.bz());
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos).blocksMotion()) continue;
            var n = OutpouringGeometry.faceNormal(m.face());
            pos.move((int) n.x, (int) n.y, (int) n.z);
            var neighbour = level.getBlockState(pos);
            if (exposed(m.face(), neighbour.blocksMotion() || !neighbour.getFluidState().isEmpty())) shown.add(m);
        }
        return shown;
    }

    static double[] faceUv(OutpouringSim.Contact c) {
        Vec3[] axes = OutpouringGeometry.faceAxes(OutpouringGeometry.faceNormal(c.face()));
        double lx = c.hx() - c.bx(), ly = c.hy() - c.by(), lz = c.hz() - c.bz();
        return new double[]{HemoEasing.clamp01(lx * axes[0].x + ly * axes[0].y + lz * axes[0].z),
                HemoEasing.clamp01(lx * axes[1].x + ly * axes[1].y + lz * axes[1].z)};
    }

    /** Lays this tick's contacts for a cast into the client store. Returns how many were laid. */
    static int lay(List<OutpouringSim.Contact> contacts, OutpouringTracker.Cast cast, Detail detail, long now) {
        if (contacts.isEmpty()) return 0;
        var kept = keep(contacts, detail, cast.mode, cast.lastX, cast.lastY, cast.lastZ);
        var profile = OutpouringRules.profile(cast.rank);
        for (var c : kept) {
            double[] uv = faceUv(c);
            boolean pool = cast.school == EnumBloodTendency.ANIMUS && c.face() == OutpouringSim.UP && !c.spark();
            float size = (float) ((.25 + .35 * profile.wildness()) * (c.spark() ? .6 : 1));
            int seed = (int) ((c.bx() * 73856093L ^ c.by() * 19349663L ^ c.bz() * 83492791L ^ now) & 0xFFFF);
            CLIENT.add(new Mark(c.bx(), c.by(), c.bz(), c.face(), uv[0], uv[1], cast.school, size, now, profile.markLife(),
                    seed, pool, c.spark()));
        }
        return kept.size();
    }

    static void pruneClient(long now) { CLIENT.prune(now); }
    static void clearClient() { CLIENT.clear(); }
    static List<Mark> client() { return CLIENT.live(); }

    /**
     * Four corners (x, y, z, u, v each) of the decal, clamped to the face square and lifted 0.004 off it. Pools drift in a
     * slow loop across their face.
     */
    static double[] quad(Mark m, long now, float partial) {
        Vec3 normal = OutpouringGeometry.faceNormal(m.face());
        Vec3[] axes = OutpouringGeometry.faceAxes(normal);
        double age = now - m.born() + partial;
        double cu = m.u(), cv = m.v();
        double half = m.size() / 2;
        double u0 = HemoEasing.clamp01(cu - half), u1 = HemoEasing.clamp01(cu + half), v0 = HemoEasing.clamp01(cv - half), v1 = HemoEasing.clamp01(cv + half);
        // The face plane: the block's near or far side along the normal, nudged outward.
        double ox = m.bx() + (normal.x > 0 ? 1 : 0), oy = m.by() + (normal.y > 0 ? 1 : 0), oz = m.bz() + (normal.z > 0 ? 1 : 0);
        ox += normal.x * .004; oy += normal.y * .004; oz += normal.z * .004;
        double[][] corners = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
        double[] out = new double[20];
        for (int i = 0; i < 4; i++) {
            double fu = corners[i][0], fv = corners[i][1];
            out[i * 5] = ox + axes[0].x * fu + axes[1].x * fv;
            out[i * 5 + 1] = oy + axes[0].y * fu + axes[1].y * fv;
            out[i * 5 + 2] = oz + axes[0].z * fu + axes[1].z * fv;
            out[i * 5 + 3] = (fu - (cu - half)) / m.size();
            out[i * 5 + 4] = (fv - (cv - half)) / m.size();
        }
        return out;
    }
}
