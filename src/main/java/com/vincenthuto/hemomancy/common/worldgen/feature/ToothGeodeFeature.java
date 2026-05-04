package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Procedural world-gen feature that creates an underground bone/tooth geode.
 *
 * <p>Three-layer spherical structure, like a vanilla amethyst geode but made
 * of organic-horror materials:</p>
 * <ul>
 *   <li>Outer shell  — blackstone</li>
 *   <li>Middle shell — venous stone</li>
 *   <li>Inner wall   — scattered bone blocks (random axis)</li>
 *   <li>Interior     — hollow air pocket containing 2-5 ToothPeck mobs</li>
 * </ul>
 */
public class ToothGeodeFeature extends Feature<NoneFeatureConfiguration> {

	private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
	private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();

	public ToothGeodeFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		// Randomise geode size: outer radius 5-8
		int outerRadius = 5 + random.nextInt(4);
		int venousRadius = outerRadius - 2;  // 3-6: venous stone ring
		int boneRadius = venousRadius - 1;   // 2-5: bone-block ring (inner wall)
		int hollowRadius = boneRadius - 1;   // 1-4: air interior

		// Slight ellipsoidal squish on the Y axis for natural variation
		float scaleY = 0.75f + random.nextFloat() * 0.35f;

		BlockState venousStone = BlockInit.venous_stone.get().defaultBlockState();

		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int dx = -outerRadius; dx <= outerRadius; dx++) {
			for (int dy = -outerRadius; dy <= outerRadius; dy++) {
				for (int dz = -outerRadius; dz <= outerRadius; dz++) {
					double dist = Math.sqrt(dx * dx + (dy / scaleY) * (dy / scaleY) + dz * dz);
					if (dist > outerRadius) continue;

					mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);

					BlockState existing = level.getBlockState(mutable);
					if (!canReplace(existing)) continue;

					if (dist > venousRadius) {
						level.setBlock(mutable, BLACKSTONE, 2);
					} else if (dist > boneRadius) {
						level.setBlock(mutable, venousStone, 2);
					} else if (dist > hollowRadius) {
						// Inner bone wall — scattered tooth-like projections
						if (random.nextFloat() < 0.75f) {
							level.setBlock(mutable, randomBoneBlock(random), 2);
						} else {
							level.setBlock(mutable, venousStone, 2);
						}
					} else {
						// Hollow interior
						level.setBlock(mutable, AIR_STATE, 2);
					}
				}
			}
		}

		spawnInhabitants(level, origin, hollowRadius, random, mutable);

		return true;
	}

	/** Bone block with a random pillar axis to mimic tooth growth directions. */
	private BlockState randomBoneBlock(RandomSource random) {
		Direction.Axis axis = switch (random.nextInt(3)) {
			case 0 -> Direction.Axis.X;
			case 1 -> Direction.Axis.Z;
			default -> Direction.Axis.Y;
		};
		return Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
	}

	/**
	 * Returns true for blocks that the geode may displace.
	 * Only natural underground blocks are replaced; player-placed blocks and
	 * fluids are left untouched.
	 */
	private boolean canReplace(BlockState state) {
		return state.isAir()
				|| state.is(Blocks.STONE)
				|| state.is(Blocks.DEEPSLATE)
				|| state.is(Blocks.TUFF)
				|| state.is(Blocks.DIRT)
				|| state.is(Blocks.GRAVEL)
				|| state.is(Blocks.ANDESITE)
				|| state.is(Blocks.DIORITE)
				|| state.is(Blocks.GRANITE)
				|| state.is(Blocks.CALCITE)
				|| state.is(Blocks.SMOOTH_BASALT)
				|| state.is(Blocks.BLACKSTONE)
				|| state.canBeReplaced();
	}

	/**
	 * Spawns 2-5 ToothPeck mobs inside the hollow interior of the geode.
	 * Uses the same persistence pattern as {@link TermiteMoundFeature} so they
	 * do not despawn after the chunk has loaded.
	 */
	private void spawnInhabitants(WorldGenLevel level, BlockPos center, int hollowRadius,
			RandomSource random, BlockPos.MutableBlockPos mutable) {
		int count = 2 + random.nextInt(4);
		int cx = center.getX(), cy = center.getY(), cz = center.getZ();
		int r = Math.max(hollowRadius - 1, 1);

		for (int i = 0; i < count; i++) {
			for (int attempt = 0; attempt < 12; attempt++) {
				int dx = random.nextInt(r * 2 + 1) - r;
				int dy = random.nextInt(r * 2 + 1) - r;
				int dz = random.nextInt(r * 2 + 1) - r;
				mutable.set(cx + dx, cy + dy, cz + dz);
				if (level.getBlockState(mutable).isAir()) {
					spawnMob(level, EntityInit.tooth_pecks.get(), mutable.immutable(), random);
					break;
				}
			}
		}
	}

	private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos,
			RandomSource random) {
		T entity = type.create(level.getLevel());
		if (entity == null) return;
		entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
				random.nextFloat() * 360.0f, 0.0f);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}
}
