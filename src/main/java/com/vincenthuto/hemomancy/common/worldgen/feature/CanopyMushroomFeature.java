package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public abstract class CanopyMushroomFeature extends AbstractHugeBloodMushroomFeature {
	private static final double TAU = Math.PI * 2.0D;

	private int lightsLeft;

	public CanopyMushroomFeature(Codec<HugeMushroomFeatureConfiguration> featureConfigurationCodec) {
		super(featureConfigurationCodec);
	}

	@Override
	public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> context) {
		RandomSource random = context.random();
		int height = this.getTreeHeight(random);
		BlockPos origin = context.origin();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		HugeMushroomFeatureConfiguration config = context.config();

		if (!this.isValidPosition(context.level(), origin, height, mutable, config)) {
			return false;
		}

		this.lightsLeft = random.nextInt(4);
		List<BlockPos> trunk = buildCrookedTrunk(context.level(), random, origin, config, height);
		if (trunk.isEmpty()) {
			return false;
		}

		BlockPos crown = trunk.get(trunk.size() - 1);
		this.makeCap(context.level(), random, crown, 0, mutable, config);
		buildBranchCluster(context.level(), random, origin, trunk, config);
		this.placeBulb(context, context.level(), random, origin, config, height, mutable);
		return true;
	}

	@Override
	protected int getTreeRadiusForHeight(int i, int i1, int foliageRadius, int treeHeight) {
		return treeHeight <= 2 ? 0 : foliageRadius * 2 + 2;
	}

	@Override
	protected void placeTrunk(LevelAccessor levelAccessor, RandomSource random, BlockPos pos,
			HugeMushroomFeatureConfiguration featureConfiguration, int height,
			BlockPos.MutableBlockPos mutableBlockPos) {
		buildCrookedTrunk(levelAccessor, random, pos, featureConfiguration, height);
	}

	private List<BlockPos> buildCrookedTrunk(LevelAccessor level, RandomSource random, BlockPos origin,
			HugeMushroomFeatureConfiguration config, int height) {
		List<BlockPos> trunk = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos().set(origin);
		int leanX = random.nextInt(3) - 1;
		int leanZ = random.nextInt(3) - 1;
		float leanChance = getLeanChance(random);

		for (int y = 0; y < height; y++) {
			BlockPos previous = cursor.immutable();
			if (y > 2 && random.nextFloat() < leanChance) {
				cursor.move(random.nextBoolean() ? leanX : random.nextInt(3) - 1, 0,
						random.nextBoolean() ? leanZ : random.nextInt(3) - 1);
			}
			cursor.setY(origin.getY() + y);
			BlockPos trunkPos = cursor.immutable();
			if (!canReplace(level, trunkPos)) {
				break;
			}

			if (y > 0 && (previous.getX() != trunkPos.getX() || previous.getZ() != trunkPos.getZ())) {
				placeTrunkConnector(level, random, previous, trunkPos, config);
			}
			setStem(level, random, trunkPos, config, true, true);
			trunk.add(trunkPos);

			if (this.lightsLeft > 0 && y > height / 3 && random.nextInt(13) == 0) {
				addShroomlight(level, trunkPos, random);
			}

			if (y > height / 2 && random.nextInt(9) == 0) {
				placeTrunkNodule(level, random, trunkPos);
			}
		}

		return trunk;
	}

	private void buildBranchCluster(LevelAccessor level, RandomSource random, BlockPos origin, List<BlockPos> trunk,
			HugeMushroomFeatureConfiguration config) {
		int branchCount = this.getBranches(random);
		int firstBranch = Math.max(1, trunk.size() / 3);
		for (int branch = 0; branch < branchCount; branch++) {
			int baseIndex = Mth.clamp(firstBranch + random.nextInt(Math.max(1, trunk.size() - firstBranch)),
					1, trunk.size() - 1);
			BlockPos base = trunk.get(baseIndex);
			double angle = random.nextDouble() * TAU + random.nextGaussian() * 0.2D;
			double length = this.getLength(random) * (0.65D + random.nextDouble() * 0.55D);
			int lift = random.nextInt(5) - 1;
			BlockPos tip = buildBranch(level, random, base, angle, length, lift, config);

			if (random.nextFloat() < 0.9F) {
				int radius = Math.max(2, config.foliageRadius - random.nextInt(2));
				this.makeCap(level, random, tip, 0, new BlockPos.MutableBlockPos(),
						new HugeMushroomFeatureConfiguration(config.capProvider, config.stemProvider, radius));
			}

			if (random.nextInt(5) == 0) {
				BlockPos shelf = tip.offset(random.nextInt(5) - 2, -1 - random.nextInt(2), random.nextInt(5) - 2);
				this.makeCap(level, random, shelf, 0, new BlockPos.MutableBlockPos(),
						new HugeMushroomFeatureConfiguration(config.capProvider, config.stemProvider, 2));
			}
		}
	}

	private BlockPos buildBranch(LevelAccessor level, RandomSource random, BlockPos base,
			double angle, double length, int lift, HugeMushroomFeatureConfiguration config) {
		BlockPos tip = base.offset(
				(int) Math.round(Math.sin(angle) * length),
				lift,
				(int) Math.round(Math.cos(angle) * length));
		BlockPos last = base;

		for (BlockPos branchPos : new VoxelBresenhamIterator(base, tip)) {
			if (canReplace(level, branchPos)) {
				setStem(level, random, branchPos, config, true, true);
				last = branchPos.immutable();
			}

			if (random.nextInt(5) == 0) {
				BlockPos bulge = branchPos.offset(random.nextInt(3) - 1, random.nextInt(3) == 0 ? 1 : 0,
						random.nextInt(3) - 1);
				if (!bulge.equals(branchPos) && canReplace(level, bulge)) {
					setStem(level, random, bulge, config, true, true);
				}
			}
		}

		if (this.lightsLeft > 0 && random.nextBoolean()) {
			addShroomlight(level, last, random);
		}
		return last;
	}

	private void placeTrunkConnector(LevelAccessor level, RandomSource random, BlockPos previous, BlockPos trunkPos,
			HugeMushroomFeatureConfiguration config) {
		BlockPos horizontalStep = new BlockPos(trunkPos.getX(), previous.getY(), trunkPos.getZ());
		BlockPos verticalStep = new BlockPos(previous.getX(), trunkPos.getY(), previous.getZ());
		if (canReplace(level, horizontalStep)) {
			setStem(level, random, horizontalStep, config, true, true);
		}
		if (canReplace(level, verticalStep)) {
			setStem(level, random, verticalStep, config, true, true);
		}
	}

	@Override
	protected int getTreeHeight(RandomSource random) {
		return 8 + random.nextInt(7);
	}

	protected float getLeanChance(RandomSource random) {
		return 0.18F + random.nextFloat() * 0.12F;
	}

	protected abstract int getBranches(RandomSource random);

	protected abstract double getLength(RandomSource random);

	protected float getCapHorizontalStretch(RandomSource random) {
		return 0.9F + random.nextFloat() * 0.8F;
	}

	protected float getCapVerticalStretch(RandomSource random) {
		return 0.85F + random.nextFloat() * 0.55F;
	}

	@Override
	protected void makeCap(LevelAccessor level, RandomSource random, BlockPos pos, int height,
			BlockPos.MutableBlockPos mutable, HugeMushroomFeatureConfiguration config) {
		int radius = Math.max(2, config.foliageRadius + random.nextInt(3) - 1);
		float stretchX = getCapHorizontalStretch(random);
		float stretchZ = getCapVerticalStretch(random);
		float phase = random.nextFloat() * 6.28F;
		float biteAngle = random.nextFloat() * 6.28F;
		float biteWidth = 0.35F + random.nextFloat() * 0.45F;
		int topLayer = 1 + random.nextInt(2);

		for (int dy = -1; dy <= topLayer; dy++) {
			int scanRadius = Mth.ceil(radius * Math.max(stretchX, stretchZ)) + 2;
			for (int x = -scanRadius; x <= scanRadius; x++) {
				for (int z = -scanRadius; z <= scanRadius; z++) {
					if (!isInsideLumpyCap(x, dy, z, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer)) {
						continue;
					}

					mutable.setWithOffset(pos, x, dy, z);
					if (!canReplace(level, mutable)) {
						continue;
					}

					BlockState cap = randomCapState(random, pos, config);
					cap = applyCapFaces(cap, x, dy, z, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer);
					this.setBlock(level, mutable, cap);
				}
			}
		}
	}

	private boolean isInsideLumpyCap(int x, int dy, int z, int radius, float stretchX, float stretchZ,
			float phase, float biteAngle, float biteWidth, int topLayer) {
		float layerScale = switch (dy) {
			case -1 -> 0.62F;
			case 0 -> 1.1F;
			case 1 -> topLayer > 1 ? 0.88F : 0.58F;
			default -> 0.48F;
		};
		double nx = x / (radius * stretchX * layerScale);
		double nz = z / (radius * stretchZ * layerScale);
		double dist = Math.sqrt(nx * nx + nz * nz);
		double wobble = 0.16D * Math.sin(x * 1.37D + phase) + 0.11D * Math.cos(z * 1.91D - phase)
				+ 0.08D * Math.sin((x + z) * 0.73D + dy);
		double angle = Math.atan2(z, x);
		double bite = Math.abs(Mth.wrapDegrees((float) Math.toDegrees(angle - biteAngle))) / 180.0D;

		if (dy == -1 && dist < 0.28D) {
			return false;
		}
		if (bite < biteWidth * 0.18D && dist > 0.48D && dy >= 0) {
			return false;
		}
		if (dist > 0.72D && Math.sin(x * 2.1D + z * 1.4D + phase) > 0.92D && dy == 0) {
			return false;
		}
		return dist <= 1.0D + wobble;
	}

	private BlockState randomCapState(RandomSource random, BlockPos pos, HugeMushroomFeatureConfiguration config) {
		if (random.nextFloat() < 0.28F) {
			return BlockInit.fruiting_infected_cap.get().defaultBlockState();
		}
		return config.capProvider.getState(random, pos);
	}

	private BlockState applyCapFaces(BlockState state, int x, int dy, int z, int radius, float stretchX, float stretchZ,
			float phase, float biteAngle, float biteWidth, int topLayer) {
		if (state.hasProperty(HugeMushroomBlock.UP)) {
			state = state.setValue(HugeMushroomBlock.UP,
					!isInsideLumpyCap(x, dy + 1, z, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer));
		}
		if (state.hasProperty(HugeMushroomBlock.DOWN)) {
			state = state.setValue(HugeMushroomBlock.DOWN,
					!isInsideLumpyCap(x, dy - 1, z, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer));
		}
		if (state.hasProperty(HugeMushroomBlock.WEST)) {
			state = state.setValue(HugeMushroomBlock.WEST,
					!isInsideLumpyCap(x - 1, dy, z, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer));
		}
		if (state.hasProperty(HugeMushroomBlock.EAST)) {
			state = state.setValue(HugeMushroomBlock.EAST,
					!isInsideLumpyCap(x + 1, dy, z, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer));
		}
		if (state.hasProperty(HugeMushroomBlock.NORTH)) {
			state = state.setValue(HugeMushroomBlock.NORTH,
					!isInsideLumpyCap(x, dy, z - 1, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer));
		}
		if (state.hasProperty(HugeMushroomBlock.SOUTH)) {
			state = state.setValue(HugeMushroomBlock.SOUTH,
					!isInsideLumpyCap(x, dy, z + 1, radius, stretchX, stretchZ, phase, biteAngle, biteWidth, topLayer));
		}
		return state;
	}

	private void setStem(LevelAccessor level, RandomSource random, BlockPos pos,
			HugeMushroomFeatureConfiguration config, boolean up, boolean down) {
		BlockState state = config.stemProvider.getState(random, pos);
		if (state.hasProperty(HugeMushroomBlock.UP)) {
			state = state.setValue(HugeMushroomBlock.UP, up);
		}
		if (state.hasProperty(HugeMushroomBlock.DOWN)) {
			state = state.setValue(HugeMushroomBlock.DOWN, down);
		}
		this.setBlock(level, pos, state);
	}

	protected void addShroomlight(LevelAccessor level, BlockPos pos, RandomSource random) {
		Direction direction = Direction.getRandom(random);
		if (direction.getAxis() == Direction.Axis.Y) {
			return;
		}
		BlockPos lightPos = pos.relative(direction);
		if (canReplace(level, lightPos)) {
			this.setBlock(level, lightPos, Blocks.SHROOMLIGHT.defaultBlockState());
			this.lightsLeft--;
		}
	}

	private void placeTrunkNodule(LevelAccessor level, RandomSource random, BlockPos pos) {
		Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		BlockPos nodulePos = pos.relative(direction);
		if (canReplace(level, nodulePos)) {
			this.setBlock(level, nodulePos, random.nextBoolean()
					? BlockInit.infected_cap.get().defaultBlockState()
					: BlockInit.fruiting_infected_cap.get().defaultBlockState());
		}
	}

	private boolean canReplace(LevelAccessor level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.canBeReplaced() || !state.isSolidRender(level, pos);
	}
}
