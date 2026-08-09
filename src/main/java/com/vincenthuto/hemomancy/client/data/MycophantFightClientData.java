package com.vincenthuto.hemomancy.client.data;

import net.minecraft.core.BlockPos;

public final class MycophantFightClientData {
	private static boolean active;
	private static BlockPos center = BlockPos.ZERO;
	private MycophantFightClientData() {}
	public static void activate(BlockPos value) { active = true; center = value.immutable(); }
	public static void clear() { active = false; center = BlockPos.ZERO; }
	public static boolean isActive() { return active; }
	public static BlockPos center() { return center; }
}
