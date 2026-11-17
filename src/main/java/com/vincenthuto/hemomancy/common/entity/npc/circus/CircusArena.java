package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The ring, as one record shared by the acts and the finale (chunk 15 T13): the carousel's hub, the ring's radius, the
 * leash a holder may stray within, and the grace a finale gives them outside it before the site lets them go. The troupe
 * is the finale cast whose home lies on this ring's ground, wherever a chase has taken them.
 */
public record CircusArena(BlockPos origin, double ringRadius, double leash, int graceTicks) {
	public static final double RING_RADIUS = 7.0D;
	public static final double LEASH = 24.0D;
	public static final int GRACE_TICKS = 200;

	public static CircusArena of(BlockPos origin) {
		return new CircusArena(origin, RING_RADIUS, LEASH, GRACE_TICKS);
	}

	public Vec3 centre() {
		return Vec3.atBottomCenterOf(origin);
	}

	/** Whether a holder at {@code position} is on the leash. */
	public boolean holds(Vec3 position) {
		return position.distanceToSqr(centre()) <= leash * leash;
	}

	/** Whether a performer belongs to this ring's finale: the original cast, homed on its ground. */
	public boolean isMember(CircusPerformerEntity performer) {
		return performer.participatesInFinale() && holds(Vec3.atBottomCenterOf(performer.getHome()));
	}

	public List<CircusPerformerEntity> troupe(Level level) {
		return level.getEntitiesOfClass(CircusPerformerEntity.class, new AABB(origin).inflate(leash + 16.0D), this::isMember);
	}
}
