package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class CardinalRiteFogGeometry {
	private static final float RENDER_MARGIN = 0.0F;
	private static final int MINIMUM_PUFFS = 165;
	private static final int MAXIMUM_PUFFS = 2150;
	private static final float PUFFS_PER_RADIUS = 12.25F;

	private CardinalRiteFogGeometry() {
	}

	public static float perimeterRadius(float footprintRadius, int riteSize,
			int completedRings, boolean legacy) {
		if (footprintRadius > 0.0F) {
			return footprintRadius;
		}
		float completedBoundary = CardinalRiteBoundaryGeometry.exteriorRadius(
				riteSize, completedRings, legacy);
		return completedBoundary > 0.0F
				? completedBoundary
				: (float) (riteSize / 2.0D + 1.0D);
	}

	public static boolean isWithinRenderDistance(BlockPos center, Vec3 camera,
			float radius, int renderDistanceChunks) {
		double dx = center.getX() + 0.5D - camera.x;
		double dz = center.getZ() + 0.5D - camera.z;
		double limit = Math.max(1, renderDistanceChunks) * 16.0D + radius + RENDER_MARGIN;
		return dx * dx + dz * dz <= limit * limit;
	}

	public static List<FogPuff> puffs(float seed, float radius) {
		int puffCount = Math.max(MINIMUM_PUFFS, Math.min(MAXIMUM_PUFFS,
				Math.round(Math.max(0.0F, radius) * (float) (Math.PI * 2.0D) * PUFFS_PER_RADIUS)));
		int scatterCount = Math.max(90, Math.round(puffCount * 1.65F));
		double rotation = puffNoise(seed, 0, 0.37F) * Math.PI * 2.0D;
		int clusterCount = Math.max(8, Math.min(14, Math.round(Math.max(1.0F, radius) * 02.78F)));
		double[] clusterAngles = new double[clusterCount];
		float[] clusterWeights = new float[clusterCount];
		float[] clusterSpreads = new float[clusterCount];
		float[] clusterRadiusOffsets = new float[clusterCount];
		float[] clusterHeightOffsets = new float[clusterCount];
		float[] clusterScales = new float[clusterCount];
		float totalClusterWeight = 0.0F;
		for (int cluster = 0; cluster < clusterCount; cluster++) {
			clusterAngles[cluster] = normalizeAngle(
					rotation + puffNoise(seed, cluster, 0.83F) * Math.PI * 2.0D);
			clusterWeights[cluster] = 0.25F + puffNoise(seed, cluster, 1.47F) * 1.75F;
			clusterSpreads[cluster] = 0.09F + puffNoise(seed, cluster, 2.09F) * 0.13F;
			clusterRadiusOffsets[cluster] = -0.95F
					+ puffNoise(seed, cluster, 2.83F) * 1.90F;
			clusterHeightOffsets[cluster] = 0.55F
					+ puffNoise(seed, cluster, 3.53F) * 0.55F;
			clusterScales[cluster] = 0.65F
					+ puffNoise(seed, cluster, 4.07F) * 0.70F;
			totalClusterWeight += clusterWeights[cluster];
		}

		List<FogPuff> puffs = new ArrayList<>(puffCount + scatterCount);
		for (int index = 0; index < puffCount; index++) {
			int cluster = index < clusterCount
					? index
					: selectCluster(seed, index, clusterWeights, totalClusterWeight);
			float localAngle = puffNoise(seed, index, 5.11F)
					+ puffNoise(seed, index, 5.79F) - 1.0F;
			double angle = normalizeAngle(
					clusterAngles[cluster] + localAngle * clusterSpreads[cluster]);
			float radialOffset = Math.max(-1.40F, Math.min(1.40F,
					clusterRadiusOffsets[cluster]
							+ (puffNoise(seed, index, 6.37F) - 0.5F) * 0.90F));
			float heightOffset = Math.max(0.34F, Math.min(1.50F,
					clusterHeightOffsets[cluster]
							+ (puffNoise(seed, index, 6.97F) - 0.5F) * 0.50F));
			float halfWidth = (0.56F + puffNoise(seed, index, 5.83F) * 1.38F)
					* clusterScales[cluster];
			float halfHeight = (0.38F + puffNoise(seed, index, 7.31F) * 0.88F)
					* clusterScales[cluster];
			float opacity = 0.13F + puffNoise(seed, index, 9.07F) * 0.14F;
			float rollRadians = (puffNoise(seed, index, 10.43F) * 2.0F - 1.0F)
					* (float) Math.PI;
			float phase = puffNoise(seed, index, 12.17F) * (float) (Math.PI * 2.0D);
			float driftSpeed = 0.0045F + puffNoise(seed, index, 13.91F) * 0.0035F;
			float orbitSpeed = orbitSpeed(seed, index, 14.63F);
			float fadeSpeed = 0.010F + puffNoise(seed, index, 14.97F) * 0.018F;
			float radialDrift = 0.08F + puffNoise(seed, index, 15.37F) * 0.06F;
			float tangentialDrift = 0.07F + puffNoise(seed, index, 17.03F) * 0.06F;
			float verticalDrift = 0.06F + puffNoise(seed, index, 18.79F) * 0.05F;
			float variantSeed = puffNoise(seed, index, 20.21F);
			float crimsonNoise = puffNoise(seed, index, 21.67F);
			float crimsonWeight = crimsonNoise < 0.72F
					? 0.06F + puffNoise(seed, index, 23.11F) * 0.27F
					: 0.52F + puffNoise(seed, index, 23.11F) * 0.38F;
			float brightness = 0.72F + puffNoise(seed, index, 25.43F) * 0.26F;
			puffs.add(new FogPuff(angle, radialOffset, heightOffset, halfWidth, halfHeight,
					opacity, rollRadians, phase, driftSpeed, orbitSpeed, fadeSpeed, radialDrift, tangentialDrift,
					verticalDrift, variantSeed, crimsonWeight, brightness, false));
		}

		for (int scatter = 0; scatter < scatterCount; scatter++) {
			int index = puffCount + scatter;
			double angle = normalizeAngle(rotation
					+ puffNoise(seed, index, 31.17F) * Math.PI * 2.0D);
			float radialNoise = puffNoise(seed, index, 32.03F);
			float radialOffset;
			if (scatter % 5 == 0) {
				radialOffset = 2.40F + radialNoise * 1.20F;
			} else if (scatter % 7 == 0) {
				radialOffset = -2.20F + radialNoise * 0.60F;
			} else {
				radialOffset = -1.80F + radialNoise * 4.40F;
			}
			int layer = scatter % 6;
			float layerBase = 0.10F + layer * 0.30F;
			float heightOffset = layerBase + puffNoise(seed, index, 33.29F) * 0.58F;
			float halfWidth = 0.58F + puffNoise(seed, index, 34.41F) * 1.12F;
			float halfHeight = 0.34F + puffNoise(seed, index, 35.57F) * 0.82F;
			float opacity = 0.085F + puffNoise(seed, index, 36.73F) * 0.105F;
			float rollRadians = (puffNoise(seed, index, 37.89F) * 2.0F - 1.0F)
					* (float) Math.PI;
			float phase = puffNoise(seed, index, 39.01F) * (float) (Math.PI * 2.0D);
			float driftSpeed = 0.0045F + puffNoise(seed, index, 40.19F) * 0.0035F;
			float orbitSpeed = orbitSpeed(seed, index, 40.83F);
			float fadeSpeed = 0.014F + puffNoise(seed, index, 41.07F) * 0.024F;
			float radialDrift = 0.10F + puffNoise(seed, index, 41.31F) * 0.10F;
			float tangentialDrift = 0.11F + puffNoise(seed, index, 42.47F) * 0.12F;
			float verticalDrift = 0.08F + puffNoise(seed, index, 43.61F) * 0.10F;
			float variantSeed = puffNoise(seed, index, 44.83F);
			float crimsonWeight = 0.08F + puffNoise(seed, index, 45.97F) * 0.24F;
			float brightness = 0.65F + puffNoise(seed, index, 47.09F) * 0.25F;
			puffs.add(new FogPuff(angle, radialOffset, heightOffset, halfWidth, halfHeight,
					opacity, rollRadians, phase, driftSpeed, orbitSpeed, fadeSpeed, radialDrift, tangentialDrift,
					verticalDrift, variantSeed, crimsonWeight, brightness, true));
		}
		return List.copyOf(puffs);
	}

	private static int selectCluster(float seed, int puff, float[] weights, float totalWeight) {
		float selection = puffNoise(seed, puff, 4.61F) * totalWeight;
		for (int cluster = 0; cluster < weights.length; cluster++) {
			selection -= weights[cluster];
			if (selection <= 0.0F) {
				return cluster;
			}
		}
		return weights.length - 1;
	}

	public static PuffPosition position(FogPuff puff, float radius, float time) {
		float wave = time * puff.driftSpeed() + puff.phase();
		float radialDistance = radius + puff.radialOffset()
				+ (float) Math.sin(wave) * puff.radialDrift();
		float tangentialOffset = (float) Math.cos(
				wave * 0.73F + puff.phase() * 0.37F) * puff.tangentialDrift();
		float orbitAngle = (float) (puff.angle() + time * puff.orbitSpeed());
		float cosine = (float) Math.cos(orbitAngle);
		float sine = (float) Math.sin(orbitAngle);
		float x = cosine * radialDistance - sine * tangentialOffset;
		float z = sine * radialDistance + cosine * tangentialOffset;
		float billow = (float) Math.sin(wave * 0.81F + puff.phase() * 0.53F)
				* puff.verticalDrift() * 1.65F;
		float stormBob = (float) Math.sin(time * (0.012F + puff.driftSpeed() * 2.8F)
				+ puff.phase() * 1.37F) * (0.10F + puff.verticalDrift() * 0.65F);
		float y = puff.heightOffset() + billow + stormBob;
		return new PuffPosition(x, y, z);
	}

	public static float opacityMultiplier(FogPuff puff, float time) {
		float cycle = 0.5F + 0.5F * (float) Math.sin(
				time * puff.fadeSpeed() + puff.phase() * 1.73F);
		float smoothCycle = cycle * cycle * (3.0F - 2.0F * cycle);
		return 0.14F + smoothCycle * 0.86F;
	}

	private static float orbitSpeed(float seed, int puff, float salt) {
		float magnitude = 0.0035F + puffNoise(seed, puff, salt) * 0.0035F;
		return puffNoise(seed, puff, salt + 0.71F) < 0.18F ? -magnitude : magnitude;
	}

	private static double normalizeAngle(double angle) {
		double normalized = angle % (Math.PI * 2.0D);
		return normalized < 0.0D ? normalized + Math.PI * 2.0D : normalized;
	}

	private static float puffNoise(float seed, int puff, float salt) {
		double value = Math.sin(seed * 12.9898D
				+ puff * 3.719D + salt * 19.19D) * 43758.5453D;
		return (float) (value - Math.floor(value));
	}

	public record FogPuff(
			double angle,
			float radialOffset,
			float heightOffset,
			float halfWidth,
			float halfHeight,
			float opacity,
			float rollRadians,
			float phase,
			float driftSpeed,
			float orbitSpeed,
			float fadeSpeed,
			float radialDrift,
			float tangentialDrift,
			float verticalDrift,
			float variantSeed,
			float crimsonWeight,
			float brightness,
			boolean scattered) {
	}

	public record PuffPosition(float x, float y, float z) {
	}
}
