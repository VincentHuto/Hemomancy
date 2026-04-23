package com.vincenthuto.hemomancy.common.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CrimsonFlameBlock extends BaseFireBlock {
	public static final MapCodec<CrimsonFlameBlock> CODEC = simpleCodec(CrimsonFlameBlock::new);
	public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
	public static final BooleanProperty NORTH = PipeBlock.NORTH;
	public static final BooleanProperty EAST = PipeBlock.EAST;
	public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
	public static final BooleanProperty WEST = PipeBlock.WEST;
	public static final BooleanProperty UP = PipeBlock.UP;
	private static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = PipeBlock.PROPERTY_BY_DIRECTION
			.entrySet()
			.stream()
			.filter(facingProperty -> facingProperty.getKey() != Direction.DOWN)
			.collect(Util.toMap());
	private static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
	private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
	private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
	private final Map<BlockState, VoxelShape> shapesCache;
	private final Object2IntMap<Block> encouragements = new Object2IntOpenHashMap<>();
	private final Object2IntMap<Block> flammabilities = new Object2IntOpenHashMap<>();

	public CrimsonFlameBlock() {
		this(Properties.of().mapColor(DyeColor.LIME).noOcclusion().noCollission().instabreak().lightLevel(state -> 15)
				.sound(SoundType.WOOL));
	}

	public CrimsonFlameBlock(Properties properties) {
		super(properties, 3.0F);
		this.registerDefaultState(
				this.stateDefinition
						.any()
						.setValue(AGE, 0)
						.setValue(NORTH, false)
						.setValue(EAST, false)
						.setValue(SOUTH, false)
						.setValue(WEST, false)
						.setValue(UP, false)
		);
		this.shapesCache = ImmutableMap.copyOf(
				this.stateDefinition
						.getPossibleStates()
						.stream()
						.filter(p_242674_0_ -> p_242674_0_.getValue(AGE) == 0)
						.collect(Collectors.toMap(Function.identity(), CrimsonFlameBlock::calculateShape))
		);
	}

	@Override
	protected MapCodec<? extends BaseFireBlock> codec() {
		return CODEC;
	}

	private static VoxelShape calculateShape(BlockState p_242673_0_) {
		VoxelShape voxelshape = Shapes.empty();
		if (p_242673_0_.getValue(UP)) {
			voxelshape = UP_AABB;
		}

		if (p_242673_0_.getValue(NORTH)) {
			voxelshape = Shapes.or(voxelshape, NORTH_AABB);
		}

		if (p_242673_0_.getValue(SOUTH)) {
			voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
		}

		if (p_242673_0_.getValue(EAST)) {
			voxelshape = Shapes.or(voxelshape, EAST_AABB);
		}

		if (p_242673_0_.getValue(WEST)) {
			voxelshape = Shapes.or(voxelshape, WEST_AABB);
		}

		return voxelshape.isEmpty() ? DOWN_AABB : voxelshape;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		return this.canSurvive(stateIn, worldIn, currentPos) ? this.getFireWithAge(worldIn, currentPos, stateIn.getValue(AGE)) : Blocks.AIR.defaultBlockState();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return this.shapesCache.get(state.setValue(AGE, 0));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
	}

	protected BlockState getStateForPlacement(BlockGetter blockReader, BlockPos pos) {
		BlockPos blockpos = pos.below();
		BlockState blockstate = blockReader.getBlockState(blockpos);
		if (!this.canCatchFire(blockReader, pos, Direction.UP) && !blockstate.isFaceSturdy(blockReader, blockpos, Direction.UP)) {
			BlockState blockstate1 = this.defaultBlockState();

			for (Direction direction : Direction.values()) {
				BooleanProperty booleanproperty = FACING_TO_PROPERTY_MAP.get(direction);
				if (booleanproperty != null) {
					blockstate1 = blockstate1.setValue(booleanproperty, this.canCatchFire(blockReader, pos.relative(direction), direction.getOpposite()));
				}
			}

			return blockstate1;
		} else {
			return this.defaultBlockState();
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		BlockPos blockpos = pos.below();
		return worldIn.getBlockState(blockpos).isFaceSturdy(worldIn, blockpos, Direction.UP) || this.areNeighborsFlammable(worldIn, pos);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
		worldIn.scheduleTick(pos, this, getTickCooldown(worldIn.random));
		if (worldIn.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
			if (!state.canSurvive(worldIn, pos)) {
				worldIn.removeBlock(pos, false);
			}

			BlockState blockstate = worldIn.getBlockState(pos.below());
			boolean flag = blockstate.isFireSource(worldIn, pos, Direction.UP);
			int i = state.getValue(AGE);
			if (!flag && worldIn.isRaining() && this.canDie(worldIn, pos) && rand.nextFloat() < 0.2F + i * 0.03F) {
				worldIn.removeBlock(pos, false);
			} else {
				int j = Math.min(15, i + rand.nextInt(3) / 2);
				if (i != j) {
					state = state.setValue(AGE, j);
					worldIn.setBlock(pos, state, 4);
				}

				if (!flag) {
					if (!this.areNeighborsFlammable(worldIn, pos)) {
						BlockPos blockpos = pos.below();
						if (!worldIn.getBlockState(blockpos).isFaceSturdy(worldIn, blockpos, Direction.UP) || i > 3) {
							worldIn.removeBlock(pos, false);
						}

						return;
					}

					if (i == 15 && rand.nextInt(4) == 0 && !this.canCatchFire(worldIn, pos.below(), Direction.UP)) {
						worldIn.removeBlock(pos, false);
						return;
					}
				}

				boolean flag1 = worldIn.getBiome(pos).is(BiomeTags.INCREASED_FIRE_BURNOUT);
				int k = flag1 ? -50 : 0;
				this.tryCatchFire(worldIn, pos.east(), 300 + k, rand, i, Direction.WEST);
				this.tryCatchFire(worldIn, pos.west(), 300 + k, rand, i, Direction.EAST);
				this.tryCatchFire(worldIn, pos.below(), 250 + k, rand, i, Direction.UP);
				this.tryCatchFire(worldIn, pos.above(), 250 + k, rand, i, Direction.DOWN);
				this.tryCatchFire(worldIn, pos.north(), 300 + k, rand, i, Direction.SOUTH);
				this.tryCatchFire(worldIn, pos.south(), 300 + k, rand, i, Direction.NORTH);
				BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

				for (int l = -1; l <= 1; l++) {
					for (int i1 = -1; i1 <= 1; i1++) {
						for (int j1 = -1; j1 <= 4; j1++) {
							if (l != 0 || j1 != 0 || i1 != 0) {
								int k1 = 100;
								if (j1 > 1) {
									k1 += (j1 - 1) * 100;
								}

								blockpos$mutable.setWithOffset(pos, l, j1, i1);
								int l1 = this.getNeighborEncouragement(worldIn, blockpos$mutable);
								if (l1 > 0) {
									int i2 = (l1 + 40 + worldIn.getDifficulty().getId() * 7) / (i + 30);
									if (flag1) {
										i2 /= 2;
									}

									if (i2 > 0 && rand.nextInt(k1) <= i2 && (!worldIn.isRaining() || !this.canDie(worldIn, blockpos$mutable))) {
										int j2 = Math.min(15, i + rand.nextInt(5) / 4);
										worldIn.setBlock(blockpos$mutable, this.getFireWithAge(worldIn, blockpos$mutable, j2), 3);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	protected boolean canDie(Level worldIn, BlockPos pos) {
		return false;
	}

	@Deprecated
	public int getFlammability(BlockState state) {
		return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)
				? 0
				: this.flammabilities.getInt(state.getBlock());
	}

	@Deprecated
	public int getFireSpreadSpeed(BlockState state) {
		return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)
				? 0
				: this.encouragements.getInt(state.getBlock()) * 2;
	}

	private void tryCatchFire(Level worldIn, BlockPos pos, int chance, RandomSource random, int age, Direction face) {
		int i = worldIn.getBlockState(pos).getFlammability(worldIn, pos, face);
		if (random.nextInt(chance) < i) {
			BlockState blockstate = worldIn.getBlockState(pos);
			if (random.nextInt(age + 10) < 5) {
				int j = Math.min(age + random.nextInt(5) / 4, 15);
				worldIn.setBlock(pos, this.getFireWithAge(worldIn, pos, j), 3);
			} else {
				worldIn.removeBlock(pos, false);
			}

			blockstate.onCaughtFire(worldIn, pos, face, null);
		}
	}

	private BlockState getFireWithAge(LevelAccessor world, BlockPos pos, int age) {
		return this.getStateForPlacement(world, pos).setValue(AGE, age);
	}

	private boolean areNeighborsFlammable(BlockGetter worldIn, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (this.canCatchFire(worldIn, pos.relative(direction), direction.getOpposite())) {
				return true;
			}
		}

		return false;
	}

	private int getNeighborEncouragement(LevelReader worldIn, BlockPos pos) {
		if (!worldIn.isEmptyBlock(pos)) {
			return 0;
		} else {
			int i = 0;

			for (Direction direction : Direction.values()) {
				BlockState blockstate = worldIn.getBlockState(pos.relative(direction));
				i = Math.max(blockstate.getFireSpreadSpeed(worldIn, pos.relative(direction), direction.getOpposite()), i);
			}

			return i;
		}
	}

	@Deprecated
	@Override
	protected boolean canBurn(BlockState state) {
		return this.getFireSpreadSpeed(state) > 0;
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);
		worldIn.scheduleTick(pos, this, getTickCooldown(worldIn.random));
	}

	private static int getTickCooldown(RandomSource rand) {
		return 5 + rand.nextInt(10);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
	}

	private void setFireInfo(Block blockIn, int encouragement, int flammability) {
		if (blockIn == Blocks.AIR) {
			throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
		} else {
			this.encouragements.put(blockIn, encouragement);
			this.flammabilities.put(blockIn, flammability);
		}
	}

	public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
		return world.getBlockState(pos).isFlammable(world, pos, face);
	}

}