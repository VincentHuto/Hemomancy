package com.vincenthuto.hemomancy.client.data;

import net.minecraft.core.BlockPos;

public final class VesperFightClientData {
	private static boolean active;
	private static BlockPos center = BlockPos.ZERO;

	private VesperFightClientData() {
	}

	public static void activate(BlockPos arenaCenter) {
		active = true;
		center = arenaCenter.immutable();
	}

	public static void clear() {
		active = false;
		center = BlockPos.ZERO;
	}

	public static boolean isActive() {
		return active;
	}

	public static BlockPos center() {
		return center;
	}
}
