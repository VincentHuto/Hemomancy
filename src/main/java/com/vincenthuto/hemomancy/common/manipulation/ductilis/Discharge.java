package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One cast and its conductor relays share this ledger; each later pulse gets fresh hits. */
public final class Discharge {
    private static final java.util.Map<UUID, Discharge> ROOTS = new java.util.WeakHashMap<>();

    public Discharge() {
        var hit = com.vincenthuto.hemomancy.common.damage.SchoolDamage.current();
        if (hit != null) ROOTS.putIfAbsent(hit.rootAttack(), this);
    }

    public static Discharge current() {
        var hit = com.vincenthuto.hemomancy.common.damage.SchoolDamage.current();
        return hit == null ? null : ROOTS.get(hit.rootAttack());
    }

    private final com.vincenthuto.hemomancy.common.damage.SchoolHitContext origin =
            com.vincenthuto.hemomancy.common.damage.SchoolDamage.current();
    public com.vincenthuto.hemomancy.common.damage.SchoolHitContext pulseContext(net.minecraft.world.entity.LivingEntity owner) {
        var hit = origin == null ? com.vincenthuto.hemomancy.common.damage.SchoolHitContext.direct(
                com.vincenthuto.hemomancy.Hemomancy.rloc("conductor_pulse"),
                com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.DUCTILIS, null, owner) : origin;
        if (pulseTick != owner.level().getGameTime()) {
            pulseTick = owner.level().getGameTime();
            pulseHit = hit.newAttack().child(com.vincenthuto.hemomancy.common.damage.SchoolHitContext.Kind.PERIODIC);
        }
        return pulseHit;
    }
    private final Set<UUID> victims = new HashSet<>();
    private long tick = Long.MIN_VALUE;
    private int limit = 64;
    private boolean reactiveArcs = true;
    private boolean deferReactions;
    private final java.util.List<Runnable> reactions = new java.util.ArrayList<>();
    private com.vincenthuto.hemomancy.common.damage.SchoolHitContext pulseHit;
    private long pulseTick = Long.MIN_VALUE;
    public void deferReactions() { deferReactions = true; }
    public boolean enqueueReaction(Runnable reaction) {
        if (!deferReactions) return false;
        reactions.add(reaction);
        return true;
    }
    public void flushReactions() {
        deferReactions = false;
        for (Runnable reaction : java.util.List.copyOf(reactions)) reaction.run();
        reactions.clear();
    }
    public void suppressReactiveArcs() { reactiveArcs = false; }
    public boolean allowsReactiveArcs() { return reactiveArcs; }

    private void prepare(long now) {
        if (tick != now) { victims.clear(); tick = now; limit = 64; }
    }

    public void beginPulse(long now) {
        prepare(now);
        limit = Math.min(limit,3);
    }

    public void finishChain(long now) {
        prepare(now);
        limit = 0;
    }

    public boolean canClaim(UUID victim, long now) {
        prepare(now);
        return victims.size() < limit && !victims.contains(victim);
    }

    public boolean claim(UUID victim, long now) {
        prepare(now);
        return victims.size() < limit && victims.add(victim);
    }

    public boolean hasHit(UUID victim, long now) {
        return tick == now && victims.contains(victim);
    }
}
