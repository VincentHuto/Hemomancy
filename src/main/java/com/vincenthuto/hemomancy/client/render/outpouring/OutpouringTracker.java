package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Mode;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Pulse;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Stage;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastLane;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.network.CastingAnimationPacket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One outpouring per caster, fed by the casting packets. A recast or a new manipulation starts a fresh cast; the old one
 * finishes its aftermath in {@link #fading()} without emitting more blood. Pure apart from the packet record, so the
 * lifecycle is pinned by JVM tests; the registry lookups come in through the resolver.
 */
final class OutpouringTracker {
    /** Silent sustained casts end like the casting animation does: 40 ticks without a heartbeat. */
    static final int SILENCE = 40;
    private static final java.util.concurrent.atomic.AtomicInteger CASTS = new java.util.concurrent.atomic.AtomicInteger();

    interface Resolver {
        EnumVeinSections section(String id);
        EnumManipulationType type(CastingAnimationPacket packet);
    }

    static final class Cast {
        final UUID player;
        final int entityId;
        final String id;
        final long sequence;
        final EnumBloodTendency school, secondary;
        final EnumManipulationRank rank;
        final EnumVeinSections section;
        final EnumManipulationType type;
        final Mode mode;
        /** Each cast grows its own vein pattern, even a repeated preview. */
        final int seed;
        private CastPhase phase;
        private long phaseStart, lastUpdate;
        private int held, required;
        private float carried;
        /** The simulated blood; null until the first client tick after the packet. */
        OutpouringSim sim;
        /** The release burst is thrown once per cast. */
        boolean burst;
        /** Marks this cast has laid, against the rank's budget. */
        int marks;
        /** Where the caster last stood, so blood keeps settling after they leave tracking range. */
        double lastX, lastY, lastZ;
        /** The caster's entity has been resolved at least once, so its absence now means it left. */
        boolean seen;
        /** The release's one-off particle burst has been thrown. */
        boolean accentBurst;
        /** Where Ductilis strands struck since the last bolt, and when that bolt fell. */
        final java.util.List<net.minecraft.world.phys.Vec3> sparks = new java.util.ArrayList<>();
        long lastBolt = Long.MIN_VALUE / 2;

        Cast(CastingAnimationPacket p, long now, Resolver resolver) {
            player = p.playerId(); entityId = p.entityId(); id = p.manipulation(); sequence = p.sequence();
            school = p.school(); secondary = p.secondary(); rank = p.rank() == null ? EnumManipulationRank.HUMILIS : p.rank();
            section = resolver.section(id); type = resolver.type(p); mode = OutpouringRules.mode(id, type);
            seed = Math.floorMod((int) (sequence * 0x9E3779B9L ^ now * 31 ^ entityId * 17) + CASTS.getAndIncrement() * 7919, 1000);
            phase = p.phase(); phaseStart = lastUpdate = now; held = p.held(); required = p.required();
            // A toggle-off arrives on its own sequence; it still draws visible blood back in.
            carried = phase == CastPhase.CANCEL && mode == Mode.TOGGLE ? OutpouringRules.PASSIVE_SCALE : 0;
        }

        EnumVeinSections section() { return section; }

        void update(CastingAnimationPacket p, long now) {
            CastPhase next = p.phase();
            // The server flips OPENING to HOLD after a few ticks; that is one continuous build, not a new phase.
            if (phase.sustained() != next.sustained() || !next.sustained() && phase != next) {
                carried = pulse(now, 0).strength();
                phaseStart = now;
                phase = next;
            }
            held = p.held(); required = p.required(); lastUpdate = now;
        }

        Pulse pulse(long now, float partial) {
            if (phase.sustained() && now - lastUpdate > SILENCE) {
                long silent = lastUpdate + SILENCE;
                float last = OutpouringRules.pulse(type, phase, charge(silent, 0), silent - phaseStart, carried, rank).strength();
                return OutpouringRules.pulse(type, CastPhase.CANCEL, 0, now - silent + partial, last, rank);
            }
            return OutpouringRules.pulse(type, phase, charge(now, partial), now - phaseStart + partial, carried, rank);
        }

        private float charge(long now, float partial) {
            if (required <= 0) return 1;
            return Math.clamp((held + (phase.sustained() ? now - lastUpdate + partial : 0)) / required, 0f, 1f);
        }

        /** The caster died or left: whatever is still flowing draws back in. */
        void end(long now) {
            var pulse = pulse(now, 0);
            if (pulse.stage() == Stage.DONE || pulse.stage() == Stage.WITHDRAW) return;
            carried = pulse.strength();
            phase = CastPhase.CANCEL;
            phaseStart = lastUpdate = now;
        }

        boolean emitting(long now) {
            Stage stage = pulse(now, 0).stage();
            return stage == Stage.BUILD || stage == Stage.PEAK;
        }

        boolean finished(long now) {
            return pulse(now, 0).stage() == Stage.DONE && (sim == null || sim.finished());
        }
    }

    private final Resolver resolver;
    private final Map<UUID, Cast> current = new HashMap<>();
    private final List<Cast> fading = new ArrayList<>();

    OutpouringTracker(Resolver resolver) { this.resolver = resolver; }

    void accept(CastingAnimationPacket p, long now) {
        if (p.lane() == CastLane.UNSTAINED || p.school() == null) return;
        Cast old = current.get(p.playerId());
        if (old != null && old.sequence == p.sequence() && old.id.equals(p.manipulation()) && old.school == p.school()
                && old.rank == p.rank() && old.emitting(now)) {
            old.update(p, now);
            return;
        }
        if (old != null) fading.add(old);
        current.put(p.playerId(), new Cast(p, now, resolver));
    }

    void end(UUID player, long now) {
        Cast cast = current.get(player);
        if (cast != null) cast.end(now);
    }

    void tick(long now) {
        current.values().removeIf(cast -> release(cast, cast.finished(now)));
        fading.removeIf(cast -> release(cast, cast.finished(now)));
    }

    private static boolean release(Cast cast, boolean finished) {
        if (finished && cast.sim != null) cast.sim.discard();
        return finished;
    }

    void clear() {
        for (Cast cast : all()) release(cast, true);
        current.clear();
        fading.clear();
    }

    /** Current and fading casts together, each once. */
    List<Cast> all() {
        List<Cast> all = new ArrayList<>(current.values());
        all.addAll(fading);
        return all;
    }

    Cast current(UUID player) { return current.get(player); }
    Collection<Cast> casts() { return current.values(); }
    List<Cast> fading() { return fading; }
}
