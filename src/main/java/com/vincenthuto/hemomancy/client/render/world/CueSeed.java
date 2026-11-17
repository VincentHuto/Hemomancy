package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;

/**
 * The drawing seed of a shared-tier cue (VX-5). The source alone gives one caster the same look every cast, so each
 * cue is salted with the game tick this client first saw it (NQ-09-1). No payload carries it: two viewers may see
 * different variants of one cast. A salt of 0 leaves the source's seed as it was.
 */
final class CueSeed {
    private CueSeed() {}

    static int of(ManipulationVisualPacket packet, long salt) {
        int source = packet.entityId() >= 0 ? packet.entityId() : packet.from().hashCode();
        return salt == 0 ? source : source ^ (int) ((salt * 0x9E3779B97F4A7C15L) >>> 33);
    }
}
