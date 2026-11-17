package com.vincenthuto.hemomancy.common.manipulation.ferric;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The same local bounds drive placement, physical collision and rendering. */
public final class FerricConstructShapes {
    /**
     * Saved and synced by ordinal: append new kinds at the end. SEGMENT is one block of Ferric Rampart's swept wall
     * (core-loop round two P4), a wall in every way but its width.
     */
    public enum Kind {
        LEGACY, PILLAR, WALL, SPIKE, SEGMENT;

        /** A plate that stops shots: the rampart and each segment of a swept one. */
        public boolean isWall() { return this == WALL || this == SEGMENT; }
    }
    private FerricConstructShapes() {}

    public static AABB bounds(Kind kind, Direction facing, Vec3 origin) {
        double width = switch (kind) { case WALL -> 5; case SEGMENT -> 1; case SPIKE -> .72; default -> .75; };
        double depth = kind.isWall() ? .375 : kind == Kind.SPIKE ? .26 : width;
        double height = switch (kind) { case WALL, SEGMENT -> 3; case SPIKE -> 1.5; default -> 2.8; };
        boolean alongX = facing.getAxis() == Direction.Axis.Z;
        double x = (alongX ? width : depth) / 2, z = (alongX ? depth : width) / 2;
        return new AABB(-x, 0, -z, x, height, z).move(origin);
    }

    public static boolean canContact(long now, long previousHit) {
        return previousHit == Long.MIN_VALUE || now - previousHit >= 10;
    }
}
