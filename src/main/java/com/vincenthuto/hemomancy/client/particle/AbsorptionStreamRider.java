package com.vincenthuto.hemomancy.client.particle;

import com.vincenthuto.hemomancy.client.render.world.CellHandStreams;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Carries a drop of drawn blood from where it was drained into the caster's palm as it is this frame. A drop that
 * {@code rides} follows the absorption stream the cast geometry draws to that body, inside it; one that does not (or
 * whose stream is not drawn) runs straight from its own source to the live palm.
 */
final class AbsorptionStreamRider {
    /** A drop picks its stream in its first ticks; after that it keeps to the path it set off on. */
    private static final int CHOOSING = 2;

    private final int caster;
    private final Vec3 source;
    private final boolean rides;
    private final double lane, laneAngle;
    private int slot = -1;

    AbsorptionStreamRider(int caster, Vec3 source, boolean rides, double lane, double laneAngle) {
        this.caster = caster;
        this.source = source;
        this.rides = rides;
        this.lane = lane;
        this.laneAngle = laneAngle;
    }

    /** Where the drop is {@code f} of the way (0 at the source, 1 at the palm), or null to leave it on its old path. */
    Vec3 at(ClientLevel level, double f, int age, float partial) {
        Entity entity = level.getEntity(caster);
        if (entity == null) return null;
        if (rides) {
            if (slot < 0 && age <= CHOOSING) slot = CellHandStreams.nearest(entity, source, partial);
            if (slot >= 0) {
                Vec3 inside = CellHandStreams.along(entity, slot, f, lane, laneAngle, partial);
                if (inside != null) return inside;
                slot = -1;
            }
        }
        Vec3 palm = CellHandStreams.livePalm(entity);
        return palm == null ? null : source.lerp(palm, Math.clamp(f, 0, 1));
    }
}
