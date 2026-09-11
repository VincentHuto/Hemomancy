package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.network.particle.ManipulationFlowPacket;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Tracks moving anchors and freezes the last connection for a short dissolve when either disappears. */
final class LuxUmbraFlow {
    final ManipulationFlowPacket packet;
    final long born;
    Vec3 start, end;
    private long lostAt = Long.MAX_VALUE;

    LuxUmbraFlow(ManipulationFlowPacket packet, long born) {
        this.packet = packet;
        this.born = born;
    }

    boolean resolve(TendrilAnchor.EntityResolver resolver, long now) {
        if (lostAt != Long.MAX_VALUE) return false;
        var from = packet.start().resolve(resolver);
        var to = packet.end().resolve(resolver);
        if (from.isEmpty() || to.isEmpty() || !finite(from.get()) || !finite(to.get())) {
            lostAt = now;
            return false;
        }
        start = from.get(); end = to.get();
        return true;
    }

    float opacity(double now) {
        var config = packet.config();
        double age = now - born;
        float entrance = Mth.clamp((float) (age / Math.max(1, config.growTicks())), 0, 1);
        float exit = Mth.clamp((float) ((config.totalLifetime() - age) / Math.max(1, config.fadeTicks())), 0, 1);
        float lost = lostAt == Long.MAX_VALUE ? 1 : Mth.clamp((float) (1 - (now - lostAt) / 8), 0, 1);
        return entrance * exit * lost;
    }

    boolean finished(long now) {
        return now - born >= packet.config().totalLifetime() || (lostAt != Long.MAX_VALUE && now >= lostAt + 8);
    }

    private static boolean finite(Vec3 at) { return Double.isFinite(at.lengthSqr()); }
}
