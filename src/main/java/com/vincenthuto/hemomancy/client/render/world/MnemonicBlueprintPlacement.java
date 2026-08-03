package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class MnemonicBlueprintPlacement {
	private MnemonicBlueprintPlacement() {
	}

	public static BlockPos worldPosition(BlockPos floorCenter, BlockPos local, Bounds bounds, Direction facing) {
		int dx = local.getX() - bounds.centerX();
		int dz = local.getZ() - bounds.centerZ();
		int rotatedX;
		int rotatedZ;
		switch (facing) {
			case EAST -> { rotatedX = -dz; rotatedZ = dx; }
			case SOUTH -> { rotatedX = -dx; rotatedZ = -dz; }
			case WEST -> { rotatedX = dz; rotatedZ = -dx; }
			default -> { rotatedX = dx; rotatedZ = dz; }
		}
		return floorCenter.offset(rotatedX, local.getY(), rotatedZ);
	}

	public record Bounds(int minX, int maxX, int minZ, int maxZ) {
		public int centerX() { return (minX + maxX) / 2; }
		public int centerZ() { return (minZ + maxZ) / 2; }
	}
}
