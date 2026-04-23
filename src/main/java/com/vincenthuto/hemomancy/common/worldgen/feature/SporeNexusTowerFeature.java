package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Procedural world-gen feature that builds a massive fungal spore nexus tower.
 * A towering organic spire of infected stem, hyphae, and conscious mass topped
 * with a colossal mushroom cap and dangling hyphae tendrils. Spawns sporite
 * crystal clusters along the sides and shroomlight lanterns.
 *
 * <p>Shape: a twisted, tapered fungal tower 30-50 blocks tall with a wide cap,
 * buttress roots at the base, and hanging tendrils from the canopy.</p>
 */
public class SporeNexusTowerFeature extends Feature<NoneFeatureConfiguration> {

	// Lazily resolved – RegistryObjects are not available at class-load time.
	private static BlockState STEM;
	private static BlockState HYPHAE;
	private static BlockState CONSCIOUS_MASS;
	private static BlockState CAP;
	private static BlockState SPORITE;
	private static BlockState VENOUS_STONE;
	private static BlockState CALCIFIED;
	private static BlockState SHROOMLIGHT_STATE;
	private static BlockState AIR_STATE;
	private static boolean resolved = false;

	private static void ensureResolved() {
		if (resolved) return;
		STEM = BlockInit.infected_stem.get().defaultBlockState();
		HYPHAE = BlockInit.hyphae_block.get().defaultBlockState();
		CONSCIOUS_MASS = BlockInit.conscious_mass.get().defaultBlockState();
		SPORITE = BlockInit.sporite_crystal.get().defaultBlockState();
		VENOUS_STONE = BlockInit.venous_stone.get().defaultBlockState();
		CALCIFIED = BlockInit.calcified_hyphae.get().defaultBlockState();
		SHROOMLIGHT_STATE = Blocks.SHROOMLIGHT.defaultBlockState();
		AIR_STATE = Blocks.AIR.defaultBlockState();

		BlockState capState = BlockInit.infected_cap.get().defaultBlockState();
		if (capState.hasProperty(HugeMushroomBlock.UP)) {
			capState = capState.setValue(HugeMushroomBlock.UP, Boolean.TRUE)
					.setValue(HugeMushroomBlock.DOWN, Boolean.FALSE);
		}
		CAP = capState;
		resolved = true;
	}

	public SporeNexusTowerFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		ensureResolved();
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		// Single shared mutable pos – reused across all helper methods
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		BlockPos groundPos = findGround(level, origin, mutable);
		if (groundPos == null) {
			return false;
		}

		if (!isValidPlacement(level, groundPos, mutable)) {
			return false;
		}

		// Cache cutoff info once instead of re-deriving every block
		int writeRadiusCutoff = -1;
		int centerCX = 0, centerCZ = 0;
		if (level instanceof WorldGenRegion region) {
			ChunkPos cp = region.getCenter();
			centerCX = cp.x;
			centerCZ = cp.z;
		}

		int height = 30 + random.nextInt(21); // 30-50
		int baseRadius = 4 + random.nextInt(3); // 4-6
		int capRadius = 8 + random.nextInt(5); // 8-12

		// Build the tower
		buildTrunk(level, groundPos, height, baseRadius, random, mutable, writeRadiusCutoff, centerCX, centerCZ);
		buildCap(level, groundPos, height, capRadius, random, mutable, writeRadiusCutoff, centerCX, centerCZ);
		buildButtressRoots(level, groundPos, baseRadius, random, mutable, writeRadiusCutoff, centerCX, centerCZ);
		addHangingTendrils(level, groundPos, height, capRadius, random, mutable, writeRadiusCutoff, centerCX, centerCZ);
		addCrystalClusters(level, groundPos, height, baseRadius, random, mutable, writeRadiusCutoff, centerCX, centerCZ);
		addInternalChamber(level, groundPos, height, mutable, writeRadiusCutoff, centerCX, centerCZ);

		return true;
	}

	private BlockPos findGround(WorldGenLevel level, BlockPos start, BlockPos.MutableBlockPos mutable) {
		mutable.set(start.getX(), start.getY(), start.getZ());
		for (int dy = 0; dy < 20; dy++) {
			mutable.move(Direction.DOWN); // peek below
			BlockState below = level.getBlockState(mutable);
			mutable.move(Direction.UP);   // back to current
			if (below.isFaceSturdy(level, mutable.below(), Direction.UP) && !below.liquid()) {
				BlockState at = level.getBlockState(mutable);
				if (at.isAir() || at.canBeReplaced()) {
					return mutable.immutable();
				}
			}
			mutable.move(Direction.DOWN);
		}
		return null;
	}

	private boolean isValidPlacement(WorldGenLevel level, BlockPos ground, BlockPos.MutableBlockPos mutable) {
		int baseY = ground.getY();
		int failures = 0;
		BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(); // second mutable for findGround calls
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				mutable.set(ground.getX() + dx, ground.getY() - 1, ground.getZ() + dz);
				BlockState state = level.getBlockState(mutable);
				if (!state.isFaceSturdy(level, mutable, Direction.UP) || state.liquid()) {
					failures++;
					if (failures > 2) return false;
					continue;
				}
				probe.set(ground.getX() + dx, ground.getY() + 6, ground.getZ() + dz);
				BlockPos surface = findGround(level, probe, probe);
				if (surface == null || Math.abs(surface.getY() - baseY) > 5) {
					failures++;
					if (failures > 2) return false;
				}
			}
		}
		return true;
	}

	/** Fast cutoff check using pre-cached values. Returns true if outside write radius. */
	private static boolean outOfBounds(int blockX, int blockZ, int writeRadiusCutoff, int centerCX, int centerCZ) {
		if (writeRadiusCutoff < 0) return false; // not a WorldGenRegion
		int cx = SectionPos.blockToSectionCoord(blockX);
		int cz = SectionPos.blockToSectionCoord(blockZ);
		return Math.abs(centerCX - cx) > writeRadiusCutoff || Math.abs(centerCZ - cz) > writeRadiusCutoff;
	}

	/**
	 * Build the main trunk/spire of the tower with a slight organic twist.
	 */
	private void buildTrunk(WorldGenLevel level, BlockPos base, int height, int baseRadius, RandomSource random,
							BlockPos.MutableBlockPos mutable, int cutoff, int ccx, int ccz) {
		int bx = base.getX(), by = base.getY(), bz = base.getZ();
		// Twist parameters
		float twistRate = 0.05f + random.nextFloat() * 0.05f;
		float twistAmplitude = 1.0f + random.nextFloat() * 1.5f;

		for (int y = 0; y < height; y++) {
			float progress = (float) y / height;
			float radius = baseRadius * (1.0f - progress * 0.75f);
			radius = Math.max(1.0f, radius);

			float twistX = Mth.sin(y * twistRate) * twistAmplitude * progress;
			float twistZ = Mth.cos(y * twistRate * 1.3f) * twistAmplitude * progress;

			int intRadius = Mth.ceil(radius);
			for (int x = -intRadius; x <= intRadius; x++) {
				for (int z = -intRadius; z <= intRadius; z++) {
					float dist = Mth.sqrt(x * x + z * z);
					float noise = (float) Math.sin(x * 0.8 + z * 1.2 + y * 0.4) * 0.4f;

					if (dist <= radius + noise) {
						int px = bx + Mth.floor(x + twistX);
						int pz = bz + Mth.floor(z + twistZ);
						if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;

						mutable.set(px, by + y, pz);
						BlockState block = chooseTrunkBlock(y, height, dist, radius, random);
						placeIfReplaceable(level, mutable, block);
					}
				}
			}
		}

		// Fill foundation – reuse mutable
		for (int x = -baseRadius; x <= baseRadius; x++) {
			for (int z = -baseRadius; z <= baseRadius; z++) {
				float dist = Mth.sqrt(x * x + z * z);
				if (dist <= baseRadius) {
					int fx = bx + x, fz = bz + z;
					if (outOfBounds(fx, fz, cutoff, ccx, ccz)) continue;
					mutable.set(fx, by - 1, fz);
					for (int depth = 0; depth < 5; depth++) {
						BlockState existing = level.getBlockState(mutable);
						if (existing.isAir() || existing.canBeReplaced()) {
							level.setBlock(mutable, VENOUS_STONE, 2);
						}
						mutable.move(Direction.DOWN);
					}
				}
			}
		}
	}

	private BlockState chooseTrunkBlock(int y, int height, float dist, float maxRadius, RandomSource random) {
		float heightProgress = (float) y / height;
		int roll = random.nextInt(10);

		// Core is always infected_stem
		if (dist < maxRadius * 0.5f) {
			if (roll < 7) return STEM;
			return HYPHAE;
		}

		// Outer shell: more hyphae and calcified
		if (heightProgress > 0.7f) {
			if (roll < 4) return STEM;
			if (roll < 7) return HYPHAE;
			return CALCIFIED;
		}

		if (roll < 5) return STEM;
		if (roll < 8) return HYPHAE;
		return CALCIFIED;
	}

	/**
	 * Build the massive mushroom cap on top.
	 */
	private void buildCap(WorldGenLevel level, BlockPos base, int height, int capRadius, RandomSource random,
						  BlockPos.MutableBlockPos mutable, int cutoff, int ccx, int ccz) {
		int bx = base.getX(), by = base.getY(), bz = base.getZ();
		int capBase = height - 2;
		int capHeight = 4 + random.nextInt(3); // 4-6 layers

		for (int y = 0; y < capHeight; y++) {
			float progress = (float) y / capHeight;
			float radius;
			if (y == 0) {
				radius = capRadius;
			} else if (y < capHeight - 1) {
				radius = capRadius * (1.0f - progress * 0.6f);
			} else {
				radius = capRadius * 0.3f;
			}

			int intRadius = Mth.ceil(radius);
			for (int x = -intRadius; x <= intRadius; x++) {
				for (int z = -intRadius; z <= intRadius; z++) {
					float dist = Mth.sqrt(x * x + z * z);
					float noise = (float) Math.sin(x * 0.5 + z * 0.7) * 0.5f;

					if (dist <= radius + noise) {
						int px = bx + x, pz = bz + z;
						if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
						mutable.set(px, by + capBase + y, pz);

						if (dist > radius - 2) {
							placeIfReplaceable(level, mutable, CAP);
						} else if (y == 0) {
							if (random.nextInt(4) == 0) {
								placeIfReplaceable(level, mutable, SHROOMLIGHT_STATE);
							} else {
								placeIfReplaceable(level, mutable, CONSCIOUS_MASS);
							}
						} else {
							placeIfReplaceable(level, mutable, CAP);
						}
					}
				}
			}
		}
	}

	/**
	 * Add massive root buttresses extending from the base.
	 */
	private void buildButtressRoots(WorldGenLevel level, BlockPos base, int baseRadius, RandomSource random,
									BlockPos.MutableBlockPos mutable, int cutoff, int ccx, int ccz) {
		int bx = base.getX(), by = base.getY(), bz = base.getZ();
		int rootCount = 3 + random.nextInt(4); // 3-6 roots

		for (int i = 0; i < rootCount; i++) {
			float angle = (float) i / rootCount * Mth.TWO_PI + random.nextFloat() * 0.5f;
			int rootLength = 6 + random.nextInt(8);
			int rootHeight = 4 + random.nextInt(6);

			for (int step = 0; step < rootLength; step++) {
				float stepProgress = (float) step / rootLength;
				int rx = Mth.floor(Mth.cos(angle) * (baseRadius + step));
				int rz = Mth.floor(Mth.sin(angle) * (baseRadius + step));
				int ry = Mth.floor(rootHeight * (1.0f - stepProgress));

				int width = Math.max(1, Mth.floor(2 * (1.0f - stepProgress)));
				for (int dy = 0; dy <= ry; dy++) {
					for (int dx = -width; dx <= width; dx++) {
						for (int dz = -width; dz <= width; dz++) {
							if (Mth.sqrt(dx * dx + dz * dz) <= width) {
								int px = bx + rx + dx, pz = bz + rz + dz;
								if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
								mutable.set(px, by + dy, pz);
								placeIfReplaceable(level, mutable,
										random.nextInt(3) == 0 ? CALCIFIED : STEM);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Add hanging hyphae tendrils from the cap underside.
	 */
	private void addHangingTendrils(WorldGenLevel level, BlockPos base, int height, int capRadius, RandomSource random,
									BlockPos.MutableBlockPos mutable, int cutoff, int ccx, int ccz) {
		int bx = base.getX(), by = base.getY(), bz = base.getZ();
		int capBase = height - 2;
		int tendrilCount = 10 + random.nextInt(15);

		for (int i = 0; i < tendrilCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			float dist = 2 + random.nextFloat() * (capRadius - 3);
			int tx = Mth.floor(Mth.cos(angle) * dist);
			int tz = Mth.floor(Mth.sin(angle) * dist);

			int px = bx + tx, pz = bz + tz;
			if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;

			int tendrilLength = 3 + random.nextInt(10);
			for (int dy = 0; dy < tendrilLength; dy++) {
				mutable.set(px, by + capBase - 1 - dy, pz);

				BlockState existing = level.getBlockState(mutable);
				if (existing.isAir()) {
					if (dy == tendrilLength - 1 && random.nextInt(3) == 0) {
						level.setBlock(mutable, SHROOMLIGHT_STATE, 2);
					} else {
						level.setBlock(mutable, HYPHAE, 2);
					}
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Add sporite crystal clusters along the trunk and at the base.
	 */
	private void addCrystalClusters(WorldGenLevel level, BlockPos base, int height, int baseRadius, RandomSource random,
									BlockPos.MutableBlockPos mutable, int cutoff, int ccx, int ccz) {
		int bx = base.getX(), by = base.getY(), bz = base.getZ();
		int clusterCount = 6 + random.nextInt(8);

		for (int i = 0; i < clusterCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int clusterY = 2 + random.nextInt(height - 4);
			float progress = (float) clusterY / height;
			float radius = baseRadius * (1.0f - progress * 0.75f);

			int cx = Mth.floor(Mth.cos(angle) * (radius + 1));
			int cz = Mth.floor(Mth.sin(angle) * (radius + 1));

			int clusterSize = 1 + random.nextInt(3);
			for (int dx = -clusterSize; dx <= clusterSize; dx++) {
				for (int dy = -clusterSize; dy <= clusterSize; dy++) {
					for (int dz = -clusterSize; dz <= clusterSize; dz++) {
						if (Mth.sqrt(dx * dx + dy * dy + dz * dz) <= clusterSize
								&& random.nextInt(3) != 0) {
							int px = bx + cx + dx, pz = bz + cz + dz;
							if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
							mutable.set(px, by + clusterY + dy, pz);
							BlockState existing = level.getBlockState(mutable);
							if (existing.isAir()) {
								level.setBlock(mutable, SPORITE, 2);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Carve an internal chamber inside the trunk, about 1/3 of the way up.
	 */
	private void addInternalChamber(WorldGenLevel level, BlockPos base, int height,
									BlockPos.MutableBlockPos mutable, int cutoff, int ccx, int ccz) {
		int bx = base.getX(), by = base.getY(), bz = base.getZ();
		int chamberY = height / 3;
		int chamberRadius = 2;

		for (int x = -chamberRadius; x <= chamberRadius; x++) {
			for (int z = -chamberRadius; z <= chamberRadius; z++) {
				float dist = Mth.sqrt(x * x + z * z);
				if (dist < chamberRadius) {
					int px = bx + x, pz = bz + z;
					if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
					for (int y = 0; y < 4; y++) {
						mutable.set(px, by + chamberY + y, pz);
						level.setBlock(mutable, AIR_STATE, 2);
					}
				}
			}
		}

		// Place a shroomlight ceiling
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				int px = bx + x, pz = bz + z;
				if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
				mutable.set(px, by + chamberY + 4, pz);
				level.setBlock(mutable, SHROOMLIGHT_STATE, 2);
			}
		}
	}

	private static void placeIfReplaceable(WorldGenLevel level, BlockPos.MutableBlockPos pos, BlockState state) {
		BlockState existing = level.getBlockState(pos);
		if (existing.isAir() || existing.canBeReplaced()) {
			level.setBlock(pos, state, 2);
		}
	}
}
