package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.circus.CinderBellowsRules;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

/** The Cinder Bellows' breath drawn with the Flammeus thermal flames instead of vanilla fire particles. */
public final class CinderBreathFlames {
    private static final Int2LongOpenHashMap LAST_TICK = new Int2LongOpenHashMap();
    static { LAST_TICK.defaultReturnValue(Long.MIN_VALUE); }
    private static ClientLevel world;

    private CinderBreathFlames() {}

    /** Emits at most once per game tick per Bellows; safe to call from every render frame. */
    public static void emit(CinderBellowsEntity bellows, float partialTicks) {
        if (!(bellows.level() instanceof ClientLevel level) || bellows.isInvisible()) return;
        if (world != level) { LAST_TICK.clear(); world = level; }
        int cycle = bellows.getBreathCycle();
        if (cycle == 0) return;
        long now = level.getGameTime();
        if (LAST_TICK.get(bellows.getId()) == now) return;
        LAST_TICK.put(bellows.getId(), now);
        Vec3 aim = bellows.getViewVector(partialTicks);
        Vec3 mouth = bellows.getEyePosition(partialTicks).add(aim.scale(.7));
        switch (CinderBellowsRules.phase(cycle)) {
            // A few banked wisps gather at the mouth while it draws breath.
            case INHALE -> { if (cycle % 5 == 0) ThermalParticles.emit(mouth, aim.scale(-1), false, 1, true); }
            // Fast licks fill the five-block, sixty-degree cone before burning out into smoke.
            case BREATHE -> ThermalParticles.jet(mouth, aim, 3, .3, .22);
            case RECOVER -> { if (cycle % 8 == 0) ThermalParticles.emit(mouth, aim, false, 1, true); }
        }
    }
}
