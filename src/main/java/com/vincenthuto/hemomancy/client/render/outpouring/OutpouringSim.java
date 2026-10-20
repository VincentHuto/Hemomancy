package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * The caster's blood after it leaves the body: motes that fall, rise, whip, orbit or roll by tendency, collide with blocks
 * one axis at a time (so nothing tunnels into a wall), and report where they touched so the world can take a fading
 * mark. Pure apart from the tendency enum; the world comes in through {@link Blocks}. Client thread only.
 */
public final class OutpouringSim {
    public static final int GLOBAL_CAP = 1200, PER_CAST_CAP = 220;
    private static final double GRAVITY = .04;
    /** Motes per tick at full volume and strength: a steady flow that a 40-tick life keeps well under the per-cast cap. */
    private static final double FLOW = .9;
    /** Direction ordinals, so contacts map straight onto block faces. */
    static final int DOWN = 0, UP = 1, NORTH = 2, SOUTH = 3, WEST = 4, EAST = 5;
    private static int live;

    public interface Blocks {
        boolean solid(int x, int y, int z);
        boolean liquid(int x, int y, int z);
    }

    public enum Kind { DROP, STRAND, SHARD, PLATE, SPIKE, WISP, ORB, PUFF, FLAME, BLOB }

    public static final class Mote {
        public double x, y, z, vx, vy, vz, prevX, prevY, prevZ;
        public float size;
        public int age, life, seed, face = -1, settled;
        public Kind kind;
        public boolean stuck, secondary;
        /** The last six positions, newest first, for ribbons and trails. */
        public final double[] trail = new double[18];
        public int trailCount;

        void remember() {
            System.arraycopy(trail, 0, trail, 3, 15);
            trail[0] = x; trail[1] = y; trail[2] = z;
            trailCount = Math.min(6, trailCount + 1);
        }
    }

    public record Contact(int bx, int by, int bz, int face, double hx, double hy, double hz, float size, boolean spark) {}

    private final EnumBloodTendency school;
    private final EnumBloodTendency secondaryTendency;
    private final Random random;
    private final List<Mote> motes = new ArrayList<>();
    private double[] exits = new double[0];
    private int exitCount, rank;
    private double reach = 1, wildness;
    private float pending;
    private boolean discarded;

    public OutpouringSim(EnumBloodTendency school, EnumBloodTendency secondary, long seed) {
        this.school = school;
        this.secondaryTendency = secondary;
        this.random = new Random(seed);
    }

    /** Lux buds many small orbs rather than a few large ones. */
    static int orbCap(int rank) { return 4 + 4 * rank; }

    public EnumBloodTendency school() { return school; }
    public EnumBloodTendency secondary() { return secondaryTendency; }
    public List<Mote> motes() { return motes; }
    public int size() { return motes.size(); }
    public boolean finished() { return motes.isEmpty(); }
    static int live() { return live; }

    /** Lets the global budget go when the cast is dropped before its blood has settled. */
    public void discard() {
        if (discarded) return;
        discarded = true;
        live -= motes.size();
        motes.clear();
    }

    /**
     * Blood leaves at {@code exits} (xyz triples) along {@code outward}. A burst throws the release volume at once;
     * otherwise the flow is the profile's volume at this strength. Wound-only spells bring their own blood, so only a trickle.
     */
    public void emit(double[] exits, int exitCount, double[] outward, float strength, OutpouringRules.Profile profile,
                     int rank, boolean burst, boolean woundOnly) {
        if (discarded || exitCount <= 0) return;
        this.exits = exits.clone();
        this.exitCount = exitCount;
        this.rank = rank;
        this.reach = profile.reach();
        this.wildness = profile.wildness();
        float rate = (float) (profile.volume() * strength * FLOW);
        if (woundOnly) rate *= .15f;
        pending += rate;
        if (burst && !woundOnly) pending += (float) profile.volume() * (school == EnumBloodTendency.DUCTILIS ? 16 : 40);
        while (pending >= 1) {
            pending--;
            if (motes.size() >= PER_CAST_CAP || live >= GLOBAL_CAP) { pending = 0; break; }
            if (school == EnumBloodTendency.LUX && count(Kind.ORB) >= orbCap(rank)) { pending = 0; break; }
            int i = random.nextInt(exitCount);
            spawn(exits[i * 3], exits[i * 3 + 1], exits[i * 3 + 2], outward[i * 3], outward[i * 3 + 1], outward[i * 3 + 2]);
        }
    }

    private long count(Kind kind) { return motes.stream().filter(m -> m.kind == kind).count(); }

    private void spawn(double x, double y, double z, double ox, double oy, double oz) {
        Mote m = new Mote();
        m.x = m.prevX = x + jitter(.03); m.y = m.prevY = y + jitter(.03); m.z = m.prevZ = z + jitter(.03);
        m.seed = random.nextInt(1 << 16);
        m.secondary = secondaryTendency != null && random.nextFloat() < .2f;
        double w = wildness, spread = .25 + .6 * w;
        double dx = ox + jitter(spread), dy = oy + jitter(spread), dz = oz + jitter(spread);
        double len = Math.max(1e-6, Math.sqrt(dx * dx + dy * dy + dz * dz));
        dx /= len; dy /= len; dz /= len;
        double speed;
        switch (school) {
            case ANIMUS -> { m.kind = Kind.DROP; speed = .12 + .1 * w; m.size = (float) (.06 + .04 * w); m.life = life(30, 50); }
            case FLAMMEUS -> { m.kind = Kind.FLAME; speed = .05; dy = Math.abs(dy) + .5; m.size = .18f; m.life = life(14, 24); }
            case DUCTILIS -> { m.kind = Kind.STRAND; speed = .25 + .3 * w; m.size = .03f; m.life = life(8, 14); }
            case LUX -> { m.kind = Kind.ORB; speed = .03; m.size = (float) (.05 + .015 * rank); m.life = life(40, 70); }
            case MORTEM -> { m.kind = Kind.BLOB; speed = .015 + .015 * w; m.size = (float) (.1 + .06 * w); m.life = life(40, 80); }
            case CONGEATIO -> { m.kind = Kind.DROP; speed = .1 + .1 * w; m.size = (float) (.05 + .04 * w); m.life = life(40, 60); }
            case FERRIC -> {
                // Most of the iron is thrown down to erupt as spikes from the ground; the rest hardens into plates where it lands.
                m.kind = random.nextFloat() < .6f ? Kind.SPIKE : Kind.PLATE;
                if (m.kind == Kind.SPIKE) dy = -Math.abs(dy) - .6;
                speed = .25 + .2 * w; m.size = m.kind == Kind.PLATE ? .09f : (float) (.2 + .1 * w); m.life = life(40, 60);
            }
            default -> { m.kind = Kind.PUFF; speed = .02; m.size = .18f; m.life = life(45, 80); }
        }
        m.vx = dx * speed; m.vy = dy * speed; m.vz = dz * speed;
        m.remember();
        motes.add(m);
        live++;
    }

    private int life(int min, int max) { return min + random.nextInt(max - min + 1); }
    private double jitter(double amount) { return (random.nextDouble() * 2 - 1) * amount; }

    /** Advances one tick and returns up to {@code markBudget} new contacts. */
    public List<Contact> step(Blocks blocks, double cx, double cy, double cz, boolean withdraw, int markBudget) {
        List<Contact> contacts = new ArrayList<>();
        for (Iterator<Mote> it = motes.iterator(); it.hasNext(); ) {
            Mote m = it.next();
            m.prevX = m.x; m.prevY = m.y; m.prevZ = m.z;
            m.age++;
            boolean dead = withdraw ? withdraw(m) : advance(m, blocks, cx, cy, cz, contacts, markBudget);
            double dx = m.x - cx, dy = m.y - (cy + 1), dz = m.z - cz;
            if (!dead && !withdraw && (m.age >= m.life || dx * dx + dy * dy + dz * dz > (reach + 2) * (reach + 2))) dead = true;
            if (dead) { it.remove(); live--; } else m.remember();
        }
        return contacts;
    }

    private boolean withdraw(Mote m) {
        double best = Double.MAX_VALUE, tx = m.x, ty = m.y, tz = m.z;
        for (int i = 0; i < exitCount; i++) {
            double ex = exits[i * 3], ey = exits[i * 3 + 1], ez = exits[i * 3 + 2];
            double d = (ex - m.x) * (ex - m.x) + (ey - m.y) * (ey - m.y) + (ez - m.z) * (ez - m.z);
            if (d < best) { best = d; tx = ex; ty = ey; tz = ez; }
        }
        m.stuck = false;
        m.x += (tx - m.x) * .3; m.y += (ty - m.y) * .3; m.z += (tz - m.z) * .3;
        double dx = tx - m.x, dy = ty - m.y, dz = tz - m.z;
        return dx * dx + dy * dy + dz * dz < .2 * .2;
    }

    /** Returns true when the mote dies this tick. */
    private boolean advance(Mote m, Blocks blocks, double cx, double cy, double cz, List<Contact> contacts, int budget) {
        if (blocks.solid(cell(m.x), cell(m.y), cell(m.z))) return true;
        if (m.stuck) {
            m.settled++;
            // Sludge creeps down walls; frozen and hardened blood stays put.
            if (m.kind == Kind.BLOB && m.face >= NORTH && !blocks.solid(cell(m.x), cell(m.y - .005), cell(m.z))) m.y -= .005;
            return false;
        }
        double w = wildness;
        switch (m.kind) {
            case DROP -> { m.vy -= GRAVITY; drag(m, school == EnumBloodTendency.CONGEATIO ? .92 : .96); }
            case FLAME -> {
                m.vy += .03; drag(m, .9);
                m.vx += jitter(w * .02); m.vz += jitter(w * .02);
                m.size = (float) Math.min(m.size * 1.05, .7);
            }
            case STRAND -> {
                if (m.age % (1 + m.seed % 2) == 0) snap(m, Math.toRadians(35 + 55 * w));
            }
            case ORB -> {
                double angle = m.seed * 2.4 + m.age * (.05 + (m.seed & 7) * .01), radius = .5 + .2 * rank + (m.seed & 3) * .12;
                double tx = cx + Math.cos(angle) * radius, ty = cy + 1.3 + .15 * Math.sin(m.age * .1 + m.seed);
                double tz = cz + Math.sin(angle) * radius;
                m.vx = (tx - m.x) * .15; m.vy = (ty - m.y) * .15; m.vz = (tz - m.z) * .15;
            }
            case BLOB -> { m.vy -= GRAVITY * .35; drag(m, .8); }
            case PLATE, SPIKE -> { m.vy -= GRAVITY; drag(m, .98); }
            case PUFF -> {
                // Heavy fog: it pours down off the body, then rolls slowly outward along the ground.
                double rx = m.x - cx, rz = m.z - cz, r = Math.max(.05, Math.hypot(rx, rz));
                boolean grounded = blocks.solid(cell(m.x), cell(m.y - .12), cell(m.z));
                m.vy = Math.max(-.09, m.vy - .022);
                double roll = grounded ? .006 * (1 + w) : .0015;
                m.vx = m.vx * .88 + rx / r * roll;
                m.vz = m.vz * .88 + rz / r * roll;
                m.size = (float) Math.min(m.size * 1.04, .6 + reach * .15);
            }
            case WISP -> {
                // A funnel of blood mist: it turns round the caster, widening and climbing as it goes.
                double rx = m.x - cx, rz = m.z - cz, r = Math.max(.05, Math.hypot(rx, rz));
                double ring = .45 + reach * .3 + Math.max(0, m.y - cy) * .25, swirl = .1 + .12 * w;
                m.vx = -rz / r * swirl + rx / r * (ring - r) * .12;
                m.vz = rx / r * swirl + rz / r * (ring - r) * .12;
                m.vy = Math.min(.05, m.vy + .004);
                m.size = (float) Math.min(m.size * 1.02, .5);
            }
            default -> {}
        }
        // One axis at a time, vertical first, so the blocked axis names the face that was hit.
        if (move(m, 1, blocks, contacts, budget)) return true;
        if (m.stuck) return false;
        if (move(m, 0, blocks, contacts, budget)) return true;
        if (m.stuck) return false;
        return move(m, 2, blocks, contacts, budget);
    }

    private static void drag(Mote m, double drag) { m.vx *= drag; m.vy *= drag; m.vz *= drag; }

    private void snap(Mote m, double maxAngle) {
        double speed = Math.sqrt(m.vx * m.vx + m.vy * m.vy + m.vz * m.vz);
        if (speed < 1e-6) return;
        double dx = m.vx / speed, dy = m.vy / speed, dz = m.vz / speed;
        double rx = jitter(1), ry = jitter(1), rz = jitter(1), along = rx * dx + ry * dy + rz * dz;
        rx -= dx * along; ry -= dy * along; rz -= dz * along;
        double len = Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (len < 1e-6) return;
        double a = random.nextDouble() * maxAngle, c = Math.cos(a), s = Math.sin(a) / len;
        m.vx = (dx * c + rx * s) * speed; m.vy = (dy * c + ry * s) * speed; m.vz = (dz * c + rz * s) * speed;
    }

    /** Moves along one axis (0 x, 1 y, 2 z). Returns true when the mote dies. */
    private boolean move(Mote m, int axis, Blocks blocks, List<Contact> contacts, int budget) {
        double v = axis == 0 ? m.vx : axis == 1 ? m.vy : m.vz;
        if (v == 0) return false;
        double nx = m.x + (axis == 0 ? v : 0), ny = m.y + (axis == 1 ? v : 0), nz = m.z + (axis == 2 ? v : 0);
        int bx = cell(nx), by = cell(ny), bz = cell(nz);
        if (!blocks.solid(bx, by, bz)) {
            if (blocks.liquid(bx, by, bz) && (m.kind == Kind.DROP || m.kind == Kind.FLAME || m.kind == Kind.PUFF)) return true;
            m.x = nx; m.y = ny; m.z = nz;
            return false;
        }
        int face = axis == 0 ? (v > 0 ? WEST : EAST) : axis == 1 ? (v > 0 ? DOWN : UP) : (v > 0 ? NORTH : SOUTH);
        // The hit point sits on the face plane of the block that stopped the mote.
        double hx = axis == 0 ? (v > 0 ? bx : bx + 1) : m.x, hy = axis == 1 ? (v > 0 ? by : by + 1) : m.y;
        double hz = axis == 2 ? (v > 0 ? bz : bz + 1) : m.z;
        switch (m.kind) {
            case ORB, PUFF, WISP -> zero(m, axis);
            case STRAND -> {
                contact(contacts, budget, bx, by, bz, face, hx, hy, hz, m.size * 4, true);
                if (axis == 0) m.vx = -m.vx; else if (axis == 1) m.vy = -m.vy; else m.vz = -m.vz;
            }
            case FLAME -> {
                contact(contacts, budget, bx, by, bz, face, hx, hy, hz, m.size, false);
                return true;
            }
            case DROP -> {
                contact(contacts, budget, bx, by, bz, face, hx, hy, hz, m.size * 3, false);
                if (school == EnumBloodTendency.CONGEATIO) { stick(m, face, hx, hy, hz, Kind.SHARD); }
                else if (face == UP) {
                    // The drop feeds the pool and its spray is caught up in the whirlwind.
                    m.kind = Kind.WISP; m.y = hy + .05; m.vy = .02; m.size = .22f; m.life = m.age + life(30, 50);
                } else zero(m, axis);
            }
            case BLOB, PLATE, SPIKE -> {
                contact(contacts, budget, bx, by, bz, face, hx, hy, hz, m.size * 2, false);
                stick(m, face, hx, hy, hz, m.kind);
            }
            default -> zero(m, axis);
        }
        return false;
    }

    private static void zero(Mote m, int axis) { if (axis == 0) m.vx = 0; else if (axis == 1) m.vy = 0; else m.vz = 0; }

    private static void stick(Mote m, int face, double hx, double hy, double hz, Kind kind) {
        // Rest a hair off the face, on the open side, so the stuck mote never reads as inside the block.
        double lift = .01;
        m.x = hx + (face == WEST ? -lift : face == EAST ? lift : 0);
        m.y = hy + (face == DOWN ? -lift : face == UP ? lift : 0);
        m.z = hz + (face == NORTH ? -lift : face == SOUTH ? lift : 0);
        m.kind = kind; m.face = face; m.stuck = true;
    }

    private static void contact(List<Contact> contacts, int budget, int bx, int by, int bz, int face, double hx, double hy,
                                double hz, float size, boolean spark) {
        if (contacts.size() < budget) contacts.add(new Contact(bx, by, bz, face, hx, hy, hz, size, spark));
    }

    private static int cell(double value) { return (int) Math.floor(value); }
}
