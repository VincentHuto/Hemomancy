package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

final class BloodStructureFeedBounds {
	private final int minX;
	private final int minY;
	private final int minZ;
	private final int maxX;
	private final int maxY;
	private final int maxZ;

	private BloodStructureFeedBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	static BloodStructureFeedBounds from(List<BlockPos> positions) {
		BlockPos first = positions.get(0);
		int minX = first.getX();
		int minY = first.getY();
		int minZ = first.getZ();
		int maxX = minX;
		int maxY = minY;
		int maxZ = minZ;

		for (BlockPos pos : positions) {
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}

		return new BloodStructureFeedBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}

	float centerX(Vec3 camera) {
		return (float) (worldCenterX() - camera.x);
	}

	float centerY(Vec3 camera) {
		return (float) (worldCenterY() - camera.y);
	}

	float centerZ(Vec3 camera) {
		return (float) (worldCenterZ() - camera.z);
	}

	double worldCenterX() {
		return (minX + maxX + 1.0D) * 0.5D;
	}

	double worldCenterY() {
		return (minY + maxY + 1.0D) * 0.5D;
	}

	double worldCenterZ() {
		return (minZ + maxZ + 1.0D) * 0.5D;
	}

	double bottomY() {
		return minY;
	}

	float bottomY(Vec3 camera) {
		return (float) (bottomY() - camera.y);
	}

	double height() {
		return maxY - minY + 1.0D;
	}

	double radius() {
		double width = maxX - minX + 1.0D;
		double depth = maxZ - minZ + 1.0D;
		return Math.max(1.65D, Math.max(width, depth) * 0.5D + 0.85D);
	}

	float seed() {
		long value = 0xcbf29ce484222325L;
		value ^= minX;
		value *= 0x100000001b3L;
		value ^= minY;
		value *= 0x100000001b3L;
		value ^= minZ;
		value *= 0x100000001b3L;
		value ^= maxX;
		value *= 0x100000001b3L;
		value ^= maxY;
		value *= 0x100000001b3L;
		value ^= maxZ;
		value ^= value >>> 33;
		value *= 0xff51afd7ed558ccdL;
		value ^= value >>> 33;
		return (float) ((value & 0xFFFFL) / 65535.0D);
	}
}
