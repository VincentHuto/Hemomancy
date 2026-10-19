package com.vincenthuto.hemomancy.common.rite.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * The shape and lifetime of a rite space: the N×N×N cube above a consecrated floor (where its braziers counted),
 * kept only while the very floor the rite completed on stays formed.
 */
public final class RiteSpaceRules {
	private RiteSpaceRules() {}

	/** Whether {@code pos} lies in the cube above a floor {@code half} blocks from its Focus to its edge. */
	public static boolean covers(BlockPos focus, int half, BlockPos pos) {
		int dy = pos.getY() - focus.getY();
		return Math.abs(pos.getX() - focus.getX()) <= half && Math.abs(pos.getZ() - focus.getZ()) <= half
				&& dy >= 1 && dy <= 2 * half;
	}

	/**
	 * Whether a space consecrated on {@code spaceFloor} ends now that the Focus reports {@code formedFloor}
	 * (null when no floor is formed). A floor grown to a larger tier is a different floor.
	 */
	public static boolean shouldRelease(ResourceLocation spaceFloor, ResourceLocation formedFloor) {
		return !Objects.equals(spaceFloor, formedFloor);
	}
}
