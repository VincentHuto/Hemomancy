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

	private static final BlockState STEM = BlockInit.infected_stem.get().defaultBlockState();
	private static final BlockState HYPHAE = BlockInit.hyphae_block.get().defaultBlockState();
	private static final BlockState CONSCIOUS_MASS = BlockInit.conscious_mass.get().defaultBlockState();
	private static final BlockState CAP;
	private static final BlockState SPORITE = BlockInit.sporite_crystal.get().defaultBlockState();
	private static final BlockState VENOUS_STONE = BlockInit.venous_stone.get().defaultBlockState();
	private static final BlockState CALCIFIED = BlockInit.calcified_hyphae.get().defaultBlockState();

	static {
		BlockState capState = BlockInit.infected_cap.get().defaultBlockState();
		if (capState.hasProperty(HugeMushroomBlock.UP)) {
			capState = capState.setValue(HugeMushroomBlock.UP, Boolean.TRUE)
					.setValue(HugeMushroomBlock.DOWN, Boolean.FALSE);
		}
		CAP = capState;
	}

	public SporeNexusTowerFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		BlockPos groundPos = findGround(level, origin);
		if (groundPos == null) {
			return false;
		}

		if (!isValidPlacement(level, groundPos)) {
			return false;
		}

		int height = 30 + random.nextInt(21); // 30-50
		int baseRadius = 4 + random.nextInt(3); // 4-6
		int capRadius = 8 + random.nextInt(5); // 8-12

		// Build the tower
		buildTrunk(level, groundPos, height, baseRadius, random);
		buildCap(level, groundPos, height, capRadius, random);
		buildButtressRoots(level, groundPos, baseRadius, random);
		addHangingTendrils(level, groundPos, height, capRadius, random);
		addCrystalClusters(level, groundPos, height, baseRadius, random);
		addInternalChamber(level, groundPos, height, random);

		return true;
	}

	private BlockPos findGround(WorldGenLevel level, BlockPos start) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(start.getX(), start.getY(), start.getZ());
		for (int dy = 0; dy < 20; dy++) {
			BlockState below = level.getBlockState(mutable.below());
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

	private boolean isValidPlacement(WorldGenLevel level, BlockPos ground) {
		int baseY = ground.getY();
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				BlockPos check = ground.offset(dx, -1, dz);
				BlockState state = level.getBlockState(check);
				if (!state.isFaceSturdy(level, check, Direction.UP) || state.liquid()) {
					return false;
				}
				BlockPos surface = findGround(level, ground.offset(dx, 6, dz));
				if (surface == null || Math.abs(surface.getY() - baseY) > 3) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Build the main trunk/spire of the tower with a slight organic twist.
	 */
	private void buildTrunk(WorldGenLevel level, BlockPos base, int height, int baseRadius, RandomSource random) {
		// Twist parameters
		float twistRate = 0.05f + random.nextFloat() * 0.05f;
		float twistAmplitude = 1.0f + random.nextFloat() * 1.5f;

		for (int y = 0; y < height; y++) {
			float progress = (float) y / height;
			// Radius tapers from baseRadius to 1-2 at top
			float radius = baseRadius * (1.0f - progress * 0.75f);
			radius = Math.max(1.0f, radius);

			// Calculate organic twist offset
			float twistX = Mth.sin(y * twistRate) * twistAmplitude * progress;
			float twistZ = Mth.cos(y * twistRate * 1.3f) * twistAmplitude * progress;

			int intRadius = Mth.ceil(radius);
			for (int x = -intRadius; x <= intRadius; x++) {
				for (int z = -intRadius; z <= intRadius; z++) {
					float dist = Mth.sqrt(x * x + z * z);

					// Add surface noise
					float noise = (float) Math.sin(x * 0.8 + z * 1.2 + y * 0.4) * 0.4f;

					if (dist <= radius + noise) {
						int px = Mth.floor(x + twistX);
						int pz = Mth.floor(z + twistZ);
						BlockPos pos = base.offset(px, y, pz);
						if (!respectsCutoff(level, pos)) continue;

						BlockState block = chooseTrunkBlock(y, height, dist, radius, random);
						placeIfReplaceable(level, pos, block);
					}
				}
			}
		}

		// Fill foundation
		for (int x = -baseRadius; x <= baseRadius; x++) {
			for (int z = -baseRadius; z <= baseRadius; z++) {
				float dist = Mth.sqrt(x * x + z * z);
				if (dist <= baseRadius) {
					BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(
							base.getX() + x, base.getY() - 1, base.getZ() + z);
					for (int depth = 0; depth < 5; depth++) {
						BlockState existing = level.getBlockState(mutable);
						if (existing.isAir() || existing.canBeReplaced()) {
							if (respectsCutoff(level, mutable.immutable())) {
								level.setBlock(mutable, VENOUS_STONE, 2);
							}
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
	private void buildCap(WorldGenLevel level, BlockPos base, int height, int capRadius, RandomSource random) {
		int capBase = height - 2;
		int capHeight = 4 + random.nextInt(3); // 4-6 layers

		for (int y = 0; y < capHeight; y++) {
			float progress = (float) y / capHeight;
			// Cap shape: wide at bottom, narrowing to top
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
						BlockPos pos = base.offset(x, capBase + y, z);
						if (!respectsCutoff(level, pos)) continue;

						// Outer edge gets the cap block, inner gets conscious_mass
						if (dist > radius - 2) {
							placeIfReplaceable(level, pos, CAP);
						} else if (y == 0) {
							// Underside of cap
							if (random.nextInt(4) == 0) {
								placeIfReplaceable(level, pos, Blocks.SHROOMLIGHT.defaultBlockState());
							} else {
								placeIfReplaceable(level, pos, CONSCIOUS_MASS);
							}
						} else {
							placeIfReplaceable(level, pos, CAP);
						}
					}
				}
			}
		}
	}

	/**
	 * Add massive root buttresses extending from the base.
	 */
	private void buildButtressRoots(WorldGenLevel level, BlockPos base, int baseRadius, RandomSource random) {
		int rootCount = 3 + random.nextInt(4); // 3-6 roots

		for (int i = 0; i < rootCount; i++) {
			float angle = (float) i / rootCount * Mth.TWO_PI + random.nextFloat() * 0.5f;
			int rootLength = 6 + random.nextInt(8); // 6-13 blocks long
			int rootHeight = 4 + random.nextInt(6); // 4-9 blocks tall

			for (int step = 0; step < rootLength; step++) {
				float stepProgress = (float) step / rootLength;
				int rx = Mth.floor(Mth.cos(angle) * (baseRadius + step));
				int rz = Mth.floor(Mth.sin(angle) * (baseRadius + step));
				int ry = Mth.floor(rootHeight * (1.0f - stepProgress));

				// Build a column for this root segment
				int width = Math.max(1, Mth.floor(2 * (1.0f - stepProgress)));
				for (int dy = 0; dy <= ry; dy++) {
					for (int dx = -width; dx <= width; dx++) {
						for (int dz = -width; dz <= width; dz++) {
							if (Mth.sqrt(dx * dx + dz * dz) <= width) {
								BlockPos pos = base.offset(rx + dx, dy, rz + dz);
								if (respectsCutoff(level, pos)) {
									placeIfReplaceable(level, pos,
											random.nextInt(3) == 0 ? CALCIFIED : STEM);
								}
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
	private void addHangingTendrils(WorldGenLevel level, BlockPos base, int height, int capRadius, RandomSource random) {
		int capBase = height - 2;
		int tendrilCount = 10 + random.nextInt(15); // 10-24

		for (int i = 0; i < tendrilCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			float dist = 2 + random.nextFloat() * (capRadius - 3);
			int tx = Mth.floor(Mth.cos(angle) * dist);
			int tz = Mth.floor(Mth.sin(angle) * dist);

			int tendrilLength = 3 + random.nextInt(10); // 3-12 blocks
			for (int dy = 0; dy < tendrilLength; dy++) {
				BlockPos pos = base.offset(tx, capBase - 1 - dy, tz);
				if (!respectsCutoff(level, pos)) continue;

				BlockState existing = level.getBlockState(pos);
				if (existing.isAir()) {
					if (dy == tendrilLength - 1 && random.nextInt(3) == 0) {
						level.setBlock(pos, Blocks.SHROOMLIGHT.defaultBlockState(), 2);
					} else {
						level.setBlock(pos, HYPHAE, 2);
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
	private void addCrystalClusters(WorldGenLevel level, BlockPos base, int height, int baseRadius, RandomSource random) {
		int clusterCount = 6 + random.nextInt(8); // 6-13 clusters

		for (int i = 0; i < clusterCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int clusterY = 2 + random.nextInt(height - 4);
			float progress = (float) clusterY / height;
			float radius = baseRadius * (1.0f - progress * 0.75f);

			int cx = Mth.floor(Mth.cos(angle) * (radius + 1));
			int cz = Mth.floor(Mth.sin(angle) * (radius + 1));

			// Place a small cluster of sporite crystals
			int clusterSize = 1 + random.nextInt(3);
			for (int dx = -clusterSize; dx <= clusterSize; dx++) {
				for (int dy = -clusterSize; dy <= clusterSize; dy++) {
					for (int dz = -clusterSize; dz <= clusterSize; dz++) {
						if (Mth.sqrt(dx * dx + dy * dy + dz * dz) <= clusterSize
								&& random.nextInt(3) != 0) {
							BlockPos pos = base.offset(cx + dx, clusterY + dy, cz + dz);
							if (respectsCutoff(level, pos)) {
								BlockState existing = level.getBlockState(pos);
								if (existing.isAir()) {
									level.setBlock(pos, SPORITE, 2);
								}
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
	private void addInternalChamber(WorldGenLevel level, BlockPos base, int height, RandomSource random) {
		int chamberY = height / 3;
		int chamberRadius = 2;

		for (int x = -chamberRadius; x <= chamberRadius; x++) {
			for (int z = -chamberRadius; z <= chamberRadius; z++) {
				for (int y = 0; y < 4; y++) {
					float dist = Mth.sqrt(x * x + z * z);
					if (dist < chamberRadius) {
						BlockPos pos = base.offset(x, chamberY + y, z);
						if (respectsCutoff(level, pos)) {
							level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
						}
					}
				}
			}
		}

		// Place a shroomlight ceiling
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos pos = base.offset(x, chamberY + 4, z);
				if (respectsCutoff(level, pos)) {
					level.setBlock(pos, Blocks.SHROOMLIGHT.defaultBlockState(), 2);
				}
			}
		}
	}

	private void placeIfReplaceable(WorldGenLevel level, BlockPos pos, BlockState state) {
		BlockState existing = level.getBlockState(pos);
		if (existing.isAir() || existing.canBeReplaced()) {
			level.setBlock(pos, state, 2);
		}
	}

	private boolean respectsCutoff(WorldGenLevel level, BlockPos pos) {
		if (level instanceof WorldGenRegion region) {
			int i = SectionPos.blockToSectionCoord(pos.getX());
			int j = SectionPos.blockToSectionCoord(pos.getZ());
			ChunkPos chunkpos = region.getCenter();
			int k = Math.abs(chunkpos.x - i);
			int l = Math.abs(chunkpos.z - j);
			return k <= region.writeRadiusCutoff && l <= region.writeRadiusCutoff;
		}
		return true;
	}
}
