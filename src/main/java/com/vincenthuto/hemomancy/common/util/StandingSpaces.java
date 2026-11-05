package com.vincenthuto.hemomancy.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Where a landed player can stand: a dry, collision-free, in-border column the size of a player. */
public final class StandingSpaces {
	private StandingSpaces() {
	}

	/** Whether a player can stand with their feet at {@code feet}. */
	public static boolean safe(Entity entity, ServerLevel level, Vec3 feet) {
		AABB box = new AABB(feet.x - 0.3, feet.y, feet.z - 0.3, feet.x + 0.3, feet.y + 1.8, feet.z + 0.3);
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
				BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
			if (!level.hasChunkAt(pos) || !level.getFluidState(pos).isEmpty()) return false;
		}
		return level.getWorldBorder().isWithinBounds(box) && level.noCollision(entity, box);
	}

	/**
	 * A standing space above {@code node}, else a floored cell within two blocks of it, else null. The familiar
	 * space above the node comes first so a returning traveller lands where they expect.
	 */
	public static Vec3 near(Entity entity, ServerLevel level, BlockPos node) {
		Vec3 above = Vec3.atBottomCenterOf(node.above());
		if (safe(entity, level, above)) return above;
		for (BlockPos pos : BlockPos.betweenClosed(node.offset(-2, -1, -2), node.offset(2, 2, 2))) {
			Vec3 candidate = Vec3.atBottomCenterOf(pos);
			if (level.hasChunkAt(pos.below())
					&& !level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty()
					&& safe(entity, level, candidate)) return candidate;
		}
		return null;
	}
}
