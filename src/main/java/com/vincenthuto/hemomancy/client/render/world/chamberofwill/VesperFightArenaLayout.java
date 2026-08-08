package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public record VesperFightArenaLayout(BlockPos center, List<Tile> tiles, List<HorizonTile> horizonTiles,
		List<HorizonWound> horizonWounds, List<Rock> rocks, List<Fissure> fissures) {
	public static final int HALF_SIZE = 25;
	public static final int HORIZON_RADIUS = 64;
	public static final int WOUND_RADIUS = 50;
	public static final int SAFE_MIN = -17;
	public static final int SAFE_MAX = 16;
	public static final int MACRO_SIZE = 4;

	public VesperFightArenaLayout {
		center = center.immutable();
		tiles = List.copyOf(tiles);
		horizonTiles = List.copyOf(horizonTiles);
		horizonWounds = List.copyOf(horizonWounds);
		rocks = List.copyOf(rocks);
		fissures = List.copyOf(fissures);
	}

	public static VesperFightArenaLayout generate(BlockPos center) {
		long seed = mix(center.asLong() ^ 0x715E5F10C4A6B29DL);
		Random random = new Random(seed);
		int checkerPhase = (int) (seed & 1L);
		List<Tile> tiles = new ArrayList<>(50 * 50);
		for (int x = -HALF_SIZE; x < HALF_SIZE; x++) {
			for (int z = -HALF_SIZE; z < HALF_SIZE; z++) {
				boolean outerBand = x < SAFE_MIN || x > SAFE_MAX || z < SAFE_MIN || z > SAFE_MAX;
				int edgeDepth = Math.max(Math.abs(x), Math.abs(z)) - 16;
				float damageChance = outerBand ? 0.22F + edgeDepth * 0.055F : 0.0F;
				boolean severe = outerBand && random.nextFloat() < damageChance;
				boolean missing = severe && random.nextFloat() < 0.045F + edgeDepth * 0.012F;
				float height = outerBand
						? (random.nextFloat() - 0.54F) * (severe ? 0.48F : 0.12F)
						: (random.nextFloat() - 0.5F) * 0.05F;
				float tiltX = severe ? (random.nextFloat() - 0.5F) * 0.28F : 0.0F;
				float tiltZ = severe ? (random.nextFloat() - 0.5F) * 0.28F : 0.0F;
				int macroX = Math.floorDiv(x + HALF_SIZE, MACRO_SIZE);
				int macroZ = Math.floorDiv(z + HALF_SIZE, MACRO_SIZE);
				Material material = ((macroX + macroZ + checkerPhase) & 1) == 0
						? Material.BASALT : Material.BONE_BRICK;
				SurfaceVariant variant = SurfaceVariant.values()[random.nextInt(SurfaceVariant.values().length)];
				tiles.add(new Tile(x, z, material, variant, missing, severe, height, tiltX, tiltZ,
						random.nextFloat() * 0.018F));
			}
		}

		Random horizonRandom = new Random(mix(seed ^ 0x4F1BBCDCBFA54001L));
		List<HorizonTile> horizonTiles = new ArrayList<>(7000);
		for (int x = -HORIZON_RADIUS; x < HORIZON_RADIUS; x++) {
			for (int z = -HORIZON_RADIUS; z < HORIZON_RADIUS; z++) {
				if (x >= -HALF_SIZE && x < HALF_SIZE && z >= -HALF_SIZE && z < HALF_SIZE) continue;
				int distance = Math.max(Math.abs(x), Math.abs(z));
				float fade = Math.min(1.0F, (distance - HALF_SIZE) / (float) (HORIZON_RADIUS - HALF_SIZE));
				float smoothFade = fade * fade * (3.0F - 2.0F * fade);
				float density = 0.90F - smoothFade * 0.82F;
				if (horizonRandom.nextFloat() > density) continue;
				int alpha = Math.round(255.0F * (1.0F - smoothFade));
				if (alpha < 4) continue;
				int macroX = Math.floorDiv(x + HALF_SIZE, MACRO_SIZE);
				int macroZ = Math.floorDiv(z + HALF_SIZE, MACRO_SIZE);
				Material material = ((macroX + macroZ + checkerPhase) & 1) == 0
						? Material.BASALT : Material.BONE_BRICK;
				float height = -smoothFade * 1.85F + (horizonRandom.nextFloat() - 0.5F) * 0.10F;
				float chip = 0.008F + horizonRandom.nextFloat() * 0.022F;
				float damageChance = 0.52F + smoothFade * 0.20F;
				HorizonDamage damage = HorizonDamage.INTACT;
				if (horizonRandom.nextFloat() < damageChance) {
					float damageRoll = horizonRandom.nextFloat();
					float shardThreshold = 0.08F + fade * 0.22F;
					float fractureThreshold = shardThreshold + 0.28F + fade * 0.12F;
					damage = damageRoll < shardThreshold ? HorizonDamage.SHARD
							: damageRoll < fractureThreshold ? HorizonDamage.FRACTURED
							: HorizonDamage.CHIPPED_CORNER;
				}
				horizonTiles.add(new HorizonTile(x, z, material, height, chip, alpha,
						damage, horizonRandom.nextInt(4)));
			}
		}

		List<HorizonWound> horizonWounds = new ArrayList<>(7000);
		for (int x = -WOUND_RADIUS; x < WOUND_RADIUS; x++) {
			for (int z = -WOUND_RADIUS; z < WOUND_RADIUS; z++) {
				if (x >= -HALF_SIZE && x < HALF_SIZE && z >= -HALF_SIZE && z < HALF_SIZE) continue;
				int distance = Math.max(Math.abs(x), Math.abs(z));
				float woundFade = Math.min(1.0F,
						(distance - HALF_SIZE) / (float) (WOUND_RADIUS - HALF_SIZE));
				float smoothFade = woundFade * woundFade * (3.0F - 2.0F * woundFade);
				int alpha = Math.round(255.0F * (1.0F - smoothFade));
				if (alpha < 4) continue;
				float horizonFade = Math.min(1.0F,
						(distance - HALF_SIZE) / (float) (HORIZON_RADIUS - HALF_SIZE));
				float smoothHorizonFade = horizonFade * horizonFade * (3.0F - 2.0F * horizonFade);
				float height = -0.15F - smoothHorizonFade * 1.85F;
				horizonWounds.add(new HorizonWound(x, z, height, alpha));
			}
		}

		List<Rock> rocks = new ArrayList<>(22);
		for (int i = 0; i < 22; i++) {
			double angle = Math.PI * 2.0 * i / 22.0 + (random.nextDouble() - 0.5) * 0.16;
			float radius = 26.2F + random.nextFloat() * 5.8F;
			float directionX = (float) Math.cos(angle);
			float directionZ = (float) Math.sin(angle);
			float squareScale = radius / Math.max(Math.abs(directionX), Math.abs(directionZ));
			rocks.add(new Rock(directionX * squareScale, directionZ * squareScale,
					1.3F + random.nextFloat() * 3.2F, 1.8F + random.nextFloat() * 4.8F,
					random.nextFloat() * 360.0F));
		}

		List<Fissure> fissures = new ArrayList<>(32);
		for (int i = 0; i < 32; i++) {
			List<FissurePoint> points = new ArrayList<>();
			int pointCount = 4 + random.nextInt(3);
			float x = -14 + random.nextInt(29);
			float z = -14 + random.nextInt(29);
			for (int point = points.size(); point < pointCount; point++) {
				points.add(new FissurePoint(x, z));
				int step = 1 + random.nextInt(3);
				if (random.nextBoolean()) {
					x += random.nextBoolean() ? step : -step;
				} else {
					z += random.nextBoolean() ? step : -step;
				}
				x = Math.max(-16.0F, Math.min(16.0F, x));
				z = Math.max(-16.0F, Math.min(16.0F, z));
			}
			fissures.add(new Fissure(points, 0.38F + random.nextFloat() * 0.35F,
					0.035F + random.nextFloat() * 0.055F));
		}

		return new VesperFightArenaLayout(center, tiles, horizonTiles, horizonWounds, rocks, fissures);
	}

	public Tile tileAt(int x, int z) {
		if (x < -HALF_SIZE || x >= HALF_SIZE || z < -HALF_SIZE || z >= HALF_SIZE) {
			throw new IllegalArgumentException("Tile lies outside the 50x50 ordeal floor");
		}
		return tiles.get((x + HALF_SIZE) * (HALF_SIZE * 2) + z + HALF_SIZE);
	}

	private static long mix(long value) {
		value ^= value >>> 33;
		value *= 0xff51afd7ed558ccdl;
		value ^= value >>> 33;
		value *= 0xc4ceb9fe1a85ec53l;
		return value ^ value >>> 33;
	}

	public enum Material {
		BASALT,
		BONE_BRICK
	}

	public enum SurfaceVariant {
		CHIPPED,
		STAINED,
		CRACKED
	}

	public enum HorizonDamage {
		INTACT,
		CHIPPED_CORNER,
		FRACTURED,
		SHARD
	}

	public record Tile(int x, int z, Material material, SurfaceVariant variant, boolean missing,
			boolean severeDamage, float heightOffset, float tiltX, float tiltZ, float chipAmount) {
	}

	public record HorizonTile(int x, int z, Material material, float height, float chipAmount, int alpha,
			HorizonDamage damage, int damageRotation) {
		public Tile asInteriorTile() {
			SurfaceVariant variant = switch (damage) {
				case INTACT -> SurfaceVariant.STAINED;
				case CHIPPED_CORNER, SHARD -> SurfaceVariant.CHIPPED;
				case FRACTURED -> SurfaceVariant.CRACKED;
			};
			return new Tile(x, z, material, variant, false, damage != HorizonDamage.INTACT,
					height, 0.0F, 0.0F, chipAmount);
		}
	}

	public record HorizonWound(int x, int z, float height, int alpha) {
	}

	public record Rock(float x, float z, float width, float height, float rotationDegrees) {
	}

	public record Fissure(List<FissurePoint> points, float intensity, float width) {
		public Fissure {
			points = List.copyOf(points);
		}
	}

	public record FissurePoint(float x, float z) {
	}
}
