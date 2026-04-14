package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Procedural world-gen feature that builds a large termite mound in savannah
 * biomes. The mound is constructed from mud, dirt, and terracotta, and spawns
 * chthonian termites plus their queen inside.
 *
 * <p>Shape: a roughly conical/dome structure ~7-11 blocks wide at the base and
 * ~10-16 blocks tall, with irregular surface bumps and internal tunnels.</p>
 */
public class TermiteMoundFeature extends Feature<NoneFeatureConfiguration> {

	/** Blocks used to build the mound exterior and interior. */
	private static final BlockState MUD = Blocks.MUD.defaultBlockState();
	private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
	private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
	private static final BlockState PACKED_MUD = Blocks.PACKED_MUD.defaultBlockState();
	private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();

	public TermiteMoundFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		// Single shared mutable positions – reused across all helper methods
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos mutable2 = new BlockPos.MutableBlockPos();

		// Find the ground surface
		BlockPos groundPos = findGround(level, origin, mutable);
		if (groundPos == null) {
			return false;
		}

		// Validate the ground is solid and mostly flat
		if (!isValidPlacement(level, groundPos, mutable, mutable2)) {
			return false;
		}

		// Cache cutoff info once
		int writeRadiusCutoff = -1;
		int centerCX = 0, centerCZ = 0;
		if (level instanceof WorldGenRegion region) {
			ChunkPos cp = region.getCenter();
			centerCX = cp.x;
			centerCZ = cp.z;
			writeRadiusCutoff = region.writeRadiusCutoff;
		}

		// Randomize mound dimensions
		int baseRadius = 4 + random.nextInt(4); // 4-7
		int height = 10 + random.nextInt(7); // 10-16
		int chimneyHeight = 2 + random.nextInt(3); // 2-4

		// Build the mound
		buildMound(level, groundPos, baseRadius, height, chimneyHeight, random, mutable, writeRadiusCutoff, centerCX, centerCZ);

		// Carve internal tunnels / chambers
		carveTunnels(level, groundPos, baseRadius, height, random, mutable, writeRadiusCutoff, centerCX, centerCZ);

		// Spawn termites and queen
		spawnInhabitants(level, groundPos, height, baseRadius, random, mutable);

		return true;
	}

	/**
	 * Scans downward from origin to find solid ground.
	 */
	private BlockPos findGround(WorldGenLevel level, BlockPos start, BlockPos.MutableBlockPos mutable) {
		mutable.set(start.getX(), start.getY(), start.getZ());
		for (int dy = 0; dy < 16; dy++) {
			int belowY = mutable.getY() - 1;
			mutable.setY(belowY);
			BlockState below = level.getBlockState(mutable);
			mutable.setY(belowY + 1); // back to current
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

	/**
	 * Check that placement is roughly flat and on solid ground.
	 */
	private boolean isValidPlacement(WorldGenLevel level, BlockPos ground,
									 BlockPos.MutableBlockPos mutable, BlockPos.MutableBlockPos probe) {
		int baseY = ground.getY();
		int gx = ground.getX(), gz = ground.getZ();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				mutable.set(gx + dx, baseY - 1, gz + dz);
				BlockState state = level.getBlockState(mutable);
				if (!state.isFaceSturdy(level, mutable, Direction.UP) || state.liquid()) {
					return false;
				}
				// Check height variance: ground should be within 2 blocks
				probe.set(gx + dx, baseY + 4, gz + dz);
				BlockPos surface = findGround(level, probe, probe);
				if (surface == null || Math.abs(surface.getY() - baseY) > 2) {
					return false;
				}
			}
		}
		return true;
	}

	/** Returns true if the block coords are outside the writable region. */
	private static boolean outOfBounds(int blockX, int blockZ, int writeRadiusCutoff, int centerCX, int centerCZ) {
		if (writeRadiusCutoff < 0) return false;
		int cx = SectionPos.blockToSectionCoord(blockX);
		int cz = SectionPos.blockToSectionCoord(blockZ);
		return Math.abs(centerCX - cx) > writeRadiusCutoff || Math.abs(centerCZ - cz) > writeRadiusCutoff;
	}

	/**
	 * Builds the solid mound structure.
	 */
	private void buildMound(WorldGenLevel level, BlockPos base, int baseRadius, int height,
							int chimneyHeight, RandomSource random, BlockPos.MutableBlockPos mutable,
							int cutoff, int ccx, int ccz) {

		int bx = base.getX(), by = base.getY(), bz = base.getZ();

		// Build the main conical body
		for (int y = 0; y < height; y++) {
			float progress = (float) y / height;
			float radius = baseRadius * (1.0f - progress * 0.85f);

			for (int x = -baseRadius; x <= baseRadius; x++) {
				for (int z = -baseRadius; z <= baseRadius; z++) {
					float dist = Mth.sqrt(x * x + z * z);

					float noise = (float) Math.sin(x * 0.7 + z * 1.1 + y * 0.5) * 0.6f
							+ (float) Math.cos(x * 1.3 - z * 0.9) * 0.4f;

					if (dist <= radius + noise) {
						int px = bx + x, pz = bz + z;
						if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
						mutable.set(px, by + y, pz);
						BlockState block = chooseMoundBlock(y, height, dist, radius, random);
						placeIfReplaceable(level, mutable, block);
					}
				}
			}
		}

		// Build chimney/spire on top
		for (int y = 0; y < chimneyHeight; y++) {
			float chimneyRadius = 1.5f - (float) y / chimneyHeight * 1.0f;
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					float dist = Mth.sqrt(x * x + z * z);
					if (dist <= chimneyRadius) {
						int px = bx + x, pz = bz + z;
						if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
						mutable.set(px, by + height + y, pz);
						placeIfReplaceable(level, mutable, TERRACOTTA);
					}
				}
			}
		}

		// Add random bumps/buttresses around the base
		int bumpCount = 3 + random.nextInt(4);
		for (int i = 0; i < bumpCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int bmpx = Mth.floor(Math.cos(angle) * (baseRadius - 1));
			int bmpz = Mth.floor(Math.sin(angle) * (baseRadius - 1));
			int bumpHeight = 2 + random.nextInt(4);
			int bumpRadius = 1 + random.nextInt(2);

			for (int y = 0; y < bumpHeight; y++) {
				float bprogress = (float) y / bumpHeight;
				float r = bumpRadius * (1.0f - bprogress * 0.7f);
				for (int dx = -bumpRadius; dx <= bumpRadius; dx++) {
					for (int dz = -bumpRadius; dz <= bumpRadius; dz++) {
						if (Mth.sqrt(dx * dx + dz * dz) <= r) {
							int px = bx + bmpx + dx, pz = bz + bmpz + dz;
							if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
							mutable.set(px, by + y, pz);
							placeIfReplaceable(level, mutable, chooseMoundBlock(y, bumpHeight, 0, r, random));
						}
					}
				}
			}
		}

		// Fill foundation below the mound to prevent floating
		for (int x = -baseRadius; x <= baseRadius; x++) {
			for (int z = -baseRadius; z <= baseRadius; z++) {
				float dist = Mth.sqrt(x * x + z * z);
				if (dist <= baseRadius) {
					int fx = bx + x, fz = bz + z;
					if (outOfBounds(fx, fz, cutoff, ccx, ccz)) continue;
					mutable.set(fx, by - 1, fz);
					for (int depth = 0; depth < 4; depth++) {
						BlockState existing = level.getBlockState(mutable);
						if (existing.isAir() || existing.canBeReplaced()) {
							level.setBlock(mutable, DIRT, 2);
						}
						mutable.move(Direction.DOWN);
					}
				}
			}
		}
	}

	/**
	 * Choose block type based on position within the mound.
	 */
	private BlockState chooseMoundBlock(int y, int totalHeight, float dist, float maxRadius,
			RandomSource random) {
		float heightProgress = (float) y / totalHeight;
		int roll = random.nextInt(10);

		// Upper portion: more terracotta
		if (heightProgress > 0.7f) {
			if (roll < 5) return TERRACOTTA;
			if (roll < 8) return PACKED_MUD;
			return MUD;
		}

		// Middle: mixed
		if (heightProgress > 0.3f) {
			if (roll < 3) return TERRACOTTA;
			if (roll < 6) return PACKED_MUD;
			if (roll < 8) return MUD;
			return DIRT;
		}

		// Lower portion: more dirt and mud
		if (roll < 3) return DIRT;
		if (roll < 6) return MUD;
		if (roll < 8) return PACKED_MUD;
		return TERRACOTTA;
	}

	/**
	 * Carve tunnels and a central chamber inside the mound.
	 */
	private void carveTunnels(WorldGenLevel level, BlockPos base, int baseRadius, int height,
							  RandomSource random, BlockPos.MutableBlockPos mutable,
							  int cutoff, int ccx, int ccz) {

		int bx = base.getX(), by = base.getY(), bz = base.getZ();

		// Central chamber at ~1/3 height
		int chamberY = height / 3;
		int chamberRadius = Math.max(2, baseRadius / 2);
		for (int x = -chamberRadius; x <= chamberRadius; x++) {
			for (int z = -chamberRadius; z <= chamberRadius; z++) {
				float dist = Mth.sqrt(x * x + z * z);
				if (dist < chamberRadius) {
					int px = bx + x, pz = bz + z;
					if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;
					for (int y = 0; y < 3; y++) {
						mutable.set(px, by + chamberY + y, pz);
						setAir(level, mutable);
					}
				}
			}
		}

		// Carve entrance tunnels from exterior into central chamber
		int tunnelCount = 1 + random.nextInt(3);
		for (int t = 0; t < tunnelCount; t++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			carveHorizontalTunnel(level, base, chamberY + 1, angle, baseRadius, mutable, cutoff, ccx, ccz);
		}

		// Vertical shaft from chamber up to chimney area
		if (!outOfBounds(bx, bz, cutoff, ccx, ccz)) {
			for (int y = chamberY + 3; y < height - 1; y++) {
				mutable.set(bx, by + y, bz);
				setAir(level, mutable);
				// Occasionally widen the shaft
				if (random.nextInt(3) == 0) {
					Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
					int nx = bx + dir.getStepX(), nz = bz + dir.getStepZ();
					if (!outOfBounds(nx, nz, cutoff, ccx, ccz)) {
						mutable.set(nx, by + y, nz);
						setAir(level, mutable);
					}
				}
			}
		}
	}

	/**
	 * Carves a horizontal tunnel from the mound exterior toward the center.
	 */
	private void carveHorizontalTunnel(WorldGenLevel level, BlockPos base, int y, float angle,
									   int baseRadius, BlockPos.MutableBlockPos mutable,
									   int cutoff, int ccx, int ccz) {

		int bx = base.getX(), by = base.getY(), bz = base.getZ();

		for (int step = 0; step <= baseRadius + 2; step++) {
			int px = bx + Mth.floor(Math.cos(angle) * step);
			int pz = bz + Mth.floor(Math.sin(angle) * step);
			if (outOfBounds(px, pz, cutoff, ccx, ccz)) continue;

			// Carve a 2-high, 1-wide tunnel
			mutable.set(px, by + y, pz);
			setAir(level, mutable);
			mutable.set(px, by + y + 1, pz);
			setAir(level, mutable);
		}
	}

	/**
	 * Sets a block to air only if it's a mound block (not natural terrain).
	 */
	private void setAir(WorldGenLevel level, BlockPos.MutableBlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.is(Blocks.MUD) || state.is(Blocks.DIRT) || state.is(Blocks.TERRACOTTA)
				|| state.is(Blocks.PACKED_MUD)) {
			level.setBlock(pos, AIR_STATE, 2);
		}
	}

	/**
	 * Only places a block if the position is air or otherwise replaceable.
	 */
	private static void placeIfReplaceable(WorldGenLevel level, BlockPos.MutableBlockPos pos, BlockState state) {
		BlockState existing = level.getBlockState(pos);
		if (existing.isAir() || existing.canBeReplaced()) {
			level.setBlock(pos, state, 2);
		}
	}

	/**
	 * Spawn chthonian termites around and inside the mound, with at least one
	 * guaranteed queen in the central chamber.
	 */
	private void spawnInhabitants(WorldGenLevel level, BlockPos base, int height, int baseRadius,
								  RandomSource random, BlockPos.MutableBlockPos mutable) {

		int bx = base.getX(), by = base.getY(), bz = base.getZ();
		int chamberY = height / 3;

		// --- Guarantee at least 1 queen inside the central chamber ---
		boolean queenSpawned = false;
		for (int attempt = 0; attempt < 16 && !queenSpawned; attempt++) {
			int dx = (attempt == 0) ? 0 : random.nextInt(5) - 2;
			int dy = (attempt == 0) ? 0 : random.nextInt(3) - 1;
			int dz = (attempt == 0) ? 0 : random.nextInt(5) - 2;
			mutable.set(bx + dx, by + chamberY + 1 + dy, bz + dz);
			if (level.getBlockState(mutable).isAir()) {
				spawnMob(level, EntityInit.chthonian_queen.get(), mutable.immutable());
				queenSpawned = true;
			}
		}
		// Last resort: force-clear the center and spawn the queen there
		if (!queenSpawned) {
			mutable.set(bx, by + chamberY + 1, bz);
			level.setBlock(mutable, AIR_STATE, 2);
			spawnMob(level, EntityInit.chthonian_queen.get(), mutable.immutable());
		}

		// --- Spawn 3-5 chthonians inside the mound tunnels/chambers ---
		int innerCount = 3 + random.nextInt(3);
		for (int i = 0; i < innerCount; i++) {
			for (int attempt = 0; attempt < 8; attempt++) {
				int dx = random.nextInt(5) - 2;
				int dy = random.nextInt(height / 2);
				int dz = random.nextInt(5) - 2;
				mutable.set(bx + dx, by + chamberY + dy, bz + dz);
				if (level.getBlockState(mutable).isAir()) {
					spawnMob(level, EntityInit.chthonian.get(), mutable.immutable());
					break;
				}
			}
		}

		// --- Spawn 4-7 chthonians around the outside of the mound ---
		int outerCount = 4 + random.nextInt(4);
		for (int i = 0; i < outerCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int distance = baseRadius + 1 + random.nextInt(3);
			int ox = Mth.floor(Math.cos(angle) * distance);
			int oz = Mth.floor(Math.sin(angle) * distance);
			mutable.set(bx + ox, by + 4, bz + oz);
			for (int scan = 0; scan < 10; scan++) {
				if (level.getBlockState(mutable).isAir()) {
					mutable.setY(mutable.getY() - 1);
					boolean sturdy = level.getBlockState(mutable).isFaceSturdy(level, mutable, Direction.UP);
					mutable.setY(mutable.getY() + 1);
					if (sturdy) {
						spawnMob(level, EntityInit.chthonian.get(), mutable.immutable());
						break;
					}
				}
				mutable.move(Direction.DOWN);
			}
		}
	}

	/**
	 * Manually creates and adds a mob entity during world generation.
	 */
	private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos) {
		T entity = type.create(level.getLevel());
		if (entity == null) return;
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}
}
