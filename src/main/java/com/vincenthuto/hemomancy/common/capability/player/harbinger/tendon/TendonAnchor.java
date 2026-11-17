package com.vincenthuto.hemomancy.common.capability.player.harbinger.tendon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Where a player's Tendon Line is rooted: a block face in one dimension, or nothing. Transient player state (never
 * saved, not kept through death), synced to the owner so the line can be drawn and the haul's claim agrees on both
 * sides. Read and written through {@code HemoCapabilityAccess.getTendonAnchor}.
 */
public final class TendonAnchor {
	private BlockPos pos;
	private Direction face = Direction.UP;
	private ResourceKey<Level> dimension;

	public boolean isSet() {
		return pos != null;
	}

	public boolean isSetIn(ResourceKey<Level> level) {
		return pos != null && (dimension == null || dimension.equals(level));
	}

	public BlockPos pos() {
		return pos;
	}

	public Direction face() {
		return face;
	}

	public ResourceKey<Level> dimension() {
		return dimension;
	}

	public void set(BlockPos pos, Direction face, ResourceKey<Level> dimension) {
		this.pos = pos.immutable();
		this.face = face == null ? Direction.UP : face;
		this.dimension = dimension;
	}

	public void clear() {
		pos = null;
		dimension = null;
		face = Direction.UP;
	}

	/** The point the line pulls toward: just off the anchored face. */
	public Vec3 target() {
		return target(pos, face);
	}

	public static Vec3 target(BlockPos pos, Direction face) {
		return Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.58D));
	}
}
