package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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

	public TermiteMoundFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		// Find the ground surface
		BlockPos groundPos = findGround(level, origin);
		if (groundPos == null) {
			return false;
		}

		// Validate the ground is solid and mostly flat
		if (!isValidPlacement(level, groundPos)) {
			return false;
		}

		// Randomize mound dimensions
		int baseRadius = 4 + random.nextInt(4); // 4-7
		int height = 10 + random.nextInt(7); // 10-16
		int chimneyHeight = 2 + random.nextInt(3); // 2-4

		// Build the mound
		buildMound(level, groundPos, baseRadius, height, chimneyHeight, random);

		// Carve internal tunnels / chambers
		carveTunnels(level, groundPos, baseRadius, height, random);

		// Spawn termites and queen
		spawnInhabitants(level, groundPos, height, baseRadius, random);

		return true;
	}

	/**
	 * Scans downward from origin to find solid ground.
	 */
	private BlockPos findGround(WorldGenLevel level, BlockPos start) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(start.getX(), start.getY(), start.getZ());
		for (int dy = 0; dy < 16; dy++) {
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

	/**
	 * Check that placement is roughly flat and on solid ground.
	 */
	private boolean isValidPlacement(WorldGenLevel level, BlockPos ground) {
		int baseY = ground.getY();
		// Check a 5x5 area for ground stability
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				BlockPos check = ground.offset(dx, -1, dz);
				BlockState state = level.getBlockState(check);
				if (!state.isFaceSturdy(level, check, Direction.UP) || state.liquid()) {
					return false;
				}
				// Check height variance: ground should be within 2 blocks
				BlockPos surface = findGround(level, ground.offset(dx, 4, dz));
				if (surface == null || Math.abs(surface.getY() - baseY) > 2) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Builds the solid mound structure.
	 */
	private void buildMound(WorldGenLevel level, BlockPos base, int baseRadius, int height,
			int chimneyHeight, RandomSource random) {

		int totalHeight = height + chimneyHeight;

		// Build the main conical body
		for (int y = 0; y < height; y++) {
			// Radius shrinks as we go up, with some noise
			float progress = (float) y / height;
			float radius = baseRadius * (1.0f - progress * 0.85f);

			for (int x = -baseRadius; x <= baseRadius; x++) {
				for (int z = -baseRadius; z <= baseRadius; z++) {
					float dist = Mth.sqrt(x * x + z * z);

					// Add surface noise for organic look
					float noise = (float) Math.sin(x * 0.7 + z * 1.1 + y * 0.5) * 0.6f
							+ (float) Math.cos(x * 1.3 - z * 0.9) * 0.4f;

					if (dist <= radius + noise) {
						BlockPos pos = base.offset(x, y, z);
						BlockState block = chooseMoundBlock(y, height, dist, radius, random);
						placeIfReplaceable(level, pos, block);
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
						BlockPos pos = base.offset(x, height + y, z);
						placeIfReplaceable(level, pos, TERRACOTTA);
					}
				}
			}
		}

		// Add random bumps/buttresses around the base
		int bumpCount = 3 + random.nextInt(4);
		for (int i = 0; i < bumpCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int bx = Mth.floor(Math.cos(angle) * (baseRadius - 1));
			int bz = Mth.floor(Math.sin(angle) * (baseRadius - 1));
			int bumpHeight = 2 + random.nextInt(4);
			int bumpRadius = 1 + random.nextInt(2);

			for (int y = 0; y < bumpHeight; y++) {
				float progress = (float) y / bumpHeight;
				float r = bumpRadius * (1.0f - progress * 0.7f);
				for (int dx = -bumpRadius; dx <= bumpRadius; dx++) {
					for (int dz = -bumpRadius; dz <= bumpRadius; dz++) {
						if (Mth.sqrt(dx * dx + dz * dz) <= r) {
							BlockPos pos = base.offset(bx + dx, y, bz + dz);
							placeIfReplaceable(level, pos, chooseMoundBlock(y, bumpHeight, 0, r, random));
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
					BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(
							base.getX() + x, base.getY() - 1, base.getZ() + z);
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
			RandomSource random) {

		// Central chamber at ~1/3 height
		int chamberY = height / 3;
		int chamberRadius = Math.max(2, baseRadius / 2);
		for (int x = -chamberRadius; x <= chamberRadius; x++) {
			for (int z = -chamberRadius; z <= chamberRadius; z++) {
				for (int y = 0; y < 3; y++) {
					float dist = Mth.sqrt(x * x + z * z);
					if (dist < chamberRadius) {
						BlockPos pos = base.offset(x, chamberY + y, z);
						setAir(level, pos);
					}
				}
			}
		}

		// Carve entrance tunnels from exterior into central chamber
		int tunnelCount = 1 + random.nextInt(3);
		for (int t = 0; t < tunnelCount; t++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			carveHorizontalTunnel(level, base, chamberY + 1, angle, baseRadius, random);
		}

		// Vertical shaft from chamber up to chimney area
		for (int y = chamberY + 3; y < height - 1; y++) {
			BlockPos pos = base.offset(0, y, 0);
			setAir(level, pos);
			// Occasionally widen the shaft
			if (random.nextInt(3) == 0) {
				Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
				setAir(level, pos.relative(dir));
			}
		}
	}

	/**
	 * Carves a horizontal tunnel from the mound exterior toward the center.
	 */
	private void carveHorizontalTunnel(WorldGenLevel level, BlockPos base, int y, float angle,
			int baseRadius, RandomSource random) {
		int dx = Mth.floor(Math.cos(angle));
		int dz = Mth.floor(Math.sin(angle));

		// Ensure at least one axis moves
		if (dx == 0 && dz == 0) {
			dx = 1;
		}

		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		for (int step = 0; step <= baseRadius + 2; step++) {
			int px = Mth.floor(Math.cos(angle) * step);
			int pz = Mth.floor(Math.sin(angle) * step);
			mutable.set(base.getX() + px, base.getY() + y, base.getZ() + pz);

			// Carve a 2-high, 1-wide tunnel
			setAir(level, mutable.immutable());
			setAir(level, mutable.above().immutable());
		}
	}

	/**
	 * Sets a block to air only if it's a mound block (not natural terrain).
	 */
	private void setAir(WorldGenLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.is(Blocks.MUD) || state.is(Blocks.DIRT) || state.is(Blocks.TERRACOTTA)
				|| state.is(Blocks.PACKED_MUD)) {
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
		}
	}

	/**
	 * Only places a block if the position is air or otherwise replaceable.
	 */
	private void placeIfReplaceable(WorldGenLevel level, BlockPos pos, BlockState state) {
		BlockState existing = level.getBlockState(pos);
		if (existing.isAir() || existing.canBeReplaced()) {
			level.setBlock(pos, state, 2);
		}
	}

	/**
	 * Spawn chthonian termites around and inside the mound, with at least one
	 * guaranteed queen in the central chamber.
	 * <p>
	 * Note: During world-gen, the level is a {@code WorldGenRegion}, not a
	 * {@code ServerLevel}, so we cannot use {@code EntityType.spawn(ServerLevel...)}.
	 * Instead we manually create each entity and add it via
	 * {@code addFreshEntityWithPassengers}.
	 */
	private void spawnInhabitants(WorldGenLevel level, BlockPos base, int height, int baseRadius,
			RandomSource random) {

		int chamberY = height / 3;

		// --- Guarantee at least 1 queen inside the central chamber ---
		boolean queenSpawned = false;
		// Try the exact center first, then search nearby air blocks
		for (int attempt = 0; attempt < 16 && !queenSpawned; attempt++) {
			int dx = (attempt == 0) ? 0 : random.nextInt(5) - 2;
			int dy = (attempt == 0) ? 0 : random.nextInt(3) - 1;
			int dz = (attempt == 0) ? 0 : random.nextInt(5) - 2;
			BlockPos queenPos = base.offset(dx, chamberY + 1 + dy, dz);
			if (level.getBlockState(queenPos).isAir()) {
				spawnMob(level, EntityInit.chthonian_queen.get(), queenPos);
				queenSpawned = true;
			}
		}
		// Last resort: force-clear the center and spawn the queen there
		if (!queenSpawned) {
			BlockPos queenPos = base.offset(0, chamberY + 1, 0);
			level.setBlock(queenPos, Blocks.AIR.defaultBlockState(), 2);
			spawnMob(level, EntityInit.chthonian_queen.get(), queenPos);
		}

		// --- Spawn 3-5 chthonians inside the mound tunnels/chambers ---
		int innerCount = 3 + random.nextInt(3);
		for (int i = 0; i < innerCount; i++) {
			for (int attempt = 0; attempt < 8; attempt++) {
				int dx = random.nextInt(5) - 2;
				int dy = random.nextInt(height / 2);
				int dz = random.nextInt(5) - 2;
				BlockPos spawnPos = base.offset(dx, chamberY + dy, dz);
				if (level.getBlockState(spawnPos).isAir()) {
					spawnMob(level, EntityInit.chthonian.get(), spawnPos);
					break;
				}
			}
		}

		// --- Spawn 4-7 chthonians around the outside of the mound ---
		int outerCount = 4 + random.nextInt(4);
		for (int i = 0; i < outerCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int distance = baseRadius + 1 + random.nextInt(3); // just outside the base
			int ox = Mth.floor(Math.cos(angle) * distance);
			int oz = Mth.floor(Math.sin(angle) * distance);
			// Find ground level at this position
			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(
					base.getX() + ox, base.getY() + 4, base.getZ() + oz);
			for (int scan = 0; scan < 10; scan++) {
				if (level.getBlockState(mutable).isAir()
						&& level.getBlockState(mutable.below()).isFaceSturdy(level, mutable.below(), Direction.UP)) {
					spawnMob(level, EntityInit.chthonian.get(), mutable.immutable());
					break;
				}
				mutable.move(Direction.DOWN);
			}
		}
	}

	/**
	 * Manually creates and adds a mob entity during world generation.
	 * Uses {@code EntityType.create()} + {@code addFreshEntityWithPassengers()}
	 * because the standard {@code EntityType.spawn()} requires a {@code ServerLevel}
	 * which is not available during feature placement (only {@code WorldGenRegion}).
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
