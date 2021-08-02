package com.huto.hemomancy.block;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huto.hemomancy.init.BlockInit;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockBefoulingAshTrail extends Block {
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap
			.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
	private static final VoxelShape BASE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
	private static final Map<Direction, VoxelShape> SIDE_TO_SHAPE = Maps
			.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D),
					Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST,
					Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST,
					Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
	private static final Map<Direction, VoxelShape> SIDE_TO_ASCENDING_SHAPE = Maps
			.newEnumMap(ImmutableMap.of(Direction.NORTH,
					Shapes.or(SIDE_TO_SHAPE.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)),
					Direction.SOUTH,
					Shapes.or(SIDE_TO_SHAPE.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)),
					Direction.EAST,
					Shapes.or(SIDE_TO_SHAPE.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)),
					Direction.WEST,
					Shapes.or(SIDE_TO_SHAPE.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
	private final Map<BlockState, VoxelShape> stateToShapeMap = Maps.newHashMap();
	private static final Vector3f[] powerRGB = new Vector3f[16];
	private final BlockState sideBaseState;
	private boolean canProvidePower = true;

	public BlockBefoulingAshTrail(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE)
				.setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE)
				.setValue(POWER, Integer.valueOf(0)));
		this.sideBaseState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE)
				.setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

		for (BlockState blockstate : this.getStateDefinition().getPossibleStates()) {
			if (blockstate.getValue(POWER) == 0) {
				this.stateToShapeMap.put(blockstate, this.getShapeForState(blockstate));
			}
		}

	}

	private VoxelShape getShapeForState(BlockState state) {
		VoxelShape voxelshape = BASE_SHAPE;

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.getValue(FACING_PROPERTY_MAP.get(direction));
			if (redstoneside == RedstoneSide.SIDE) {
				voxelshape = Shapes.or(voxelshape, SIDE_TO_SHAPE.get(direction));
			} else if (redstoneside == RedstoneSide.UP) {
				voxelshape = Shapes.or(voxelshape, SIDE_TO_ASCENDING_SHAPE.get(direction));
			}
		}

		return voxelshape;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return this.stateToShapeMap.get(state.setValue(POWER, Integer.valueOf(0)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.getUpdatedState(context.getLevel(), this.sideBaseState, context.getClickedPos());
	}

	private BlockState getUpdatedState(BlockGetter reader, BlockState state, BlockPos pos) {
		boolean flag = areAllSidesInvalid(state);
		state = this.recalculateFacingState(reader, this.defaultBlockState().setValue(POWER, state.getValue(POWER)),
				pos);
		if (flag && areAllSidesInvalid(state)) {
			return state;
		} else {
			boolean flag1 = state.getValue(NORTH).isConnected();
			boolean flag2 = state.getValue(SOUTH).isConnected();
			boolean flag3 = state.getValue(EAST).isConnected();
			boolean flag4 = state.getValue(WEST).isConnected();
			boolean flag5 = !flag1 && !flag2;
			boolean flag6 = !flag3 && !flag4;
			if (!flag4 && flag5) {
				state = state.setValue(WEST, RedstoneSide.SIDE);
			}

			if (!flag3 && flag5) {
				state = state.setValue(EAST, RedstoneSide.SIDE);
			}

			if (!flag1 && flag6) {
				state = state.setValue(NORTH, RedstoneSide.SIDE);
			}

			if (!flag2 && flag6) {
				state = state.setValue(SOUTH, RedstoneSide.SIDE);
			}

			return state;
		}
	}

	private BlockState recalculateFacingState(BlockGetter reader, BlockState state, BlockPos pos) {
		boolean flag = !reader.getBlockState(pos.above()).isRedstoneConductor(reader, pos);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!state.getValue(FACING_PROPERTY_MAP.get(direction)).isConnected()) {
				RedstoneSide redstoneside = this.recalculateSide(reader, pos, direction, flag);
				state = state.setValue(FACING_PROPERTY_MAP.get(direction), redstoneside);
			}
		}

		return state;
	}

	/**
	 * Update the provided state given the provided neighbor facing and neighbor
	 * state, returning a new state. For example, fences make their connections to
	 * the passed in state if possible, and wet concrete powder immediately returns
	 * its solidified counterpart. Note that this method should ideally consider
	 * only the specific face passed in.
	 */
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (facing == Direction.DOWN) {
			return stateIn;
		} else if (facing == Direction.UP) {
			return this.getUpdatedState(worldIn, stateIn, currentPos);
		} else {
			RedstoneSide redstoneside = this.getSide(worldIn, currentPos, facing);
			return redstoneside.isConnected() == stateIn.getValue(FACING_PROPERTY_MAP.get(facing)).isConnected()
					&& !areAllSidesValid(stateIn) ? stateIn.setValue(FACING_PROPERTY_MAP.get(facing), redstoneside)
							: this.getUpdatedState(worldIn, this.sideBaseState.setValue(POWER, stateIn.getValue(POWER))
									.setValue(FACING_PROPERTY_MAP.get(facing), redstoneside), currentPos);
		}
	}

	private static boolean areAllSidesValid(BlockState state) {
		return state.getValue(NORTH).isConnected() && state.getValue(SOUTH).isConnected()
				&& state.getValue(EAST).isConnected() && state.getValue(WEST).isConnected();
	}

	private static boolean areAllSidesInvalid(BlockState state) {
		return !state.getValue(NORTH).isConnected() && !state.getValue(SOUTH).isConnected()
				&& !state.getValue(EAST).isConnected() && !state.getValue(WEST).isConnected();
	}

	/**
	 * performs updates on diagonal neighbors of the target position and passes in
	 * the flags. The flags can be referenced from the docs for
	 * {@link ILevelWriter#setBlockState(IBlockState, BlockPos, int)}.
	 */
	@Override
	public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor worldIn, BlockPos pos, int flags,
			int recursionLeft) {
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			RedstoneSide redstoneside = state.getValue(FACING_PROPERTY_MAP.get(direction));
			if (redstoneside != RedstoneSide.NONE
					&& !worldIn.getBlockState(blockpos$mutable.setWithOffset(pos, direction)).is(this)) {
				blockpos$mutable.move(Direction.DOWN);
				BlockState blockstate = worldIn.getBlockState(blockpos$mutable);
				if (!blockstate.is(Blocks.OBSERVER)) {
					BlockPos blockpos = blockpos$mutable.relative(direction.getOpposite());
					BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(),
							worldIn.getBlockState(blockpos), worldIn, blockpos$mutable, blockpos);
					updateOrDestroy(blockstate, blockstate1, worldIn, blockpos$mutable, flags, recursionLeft);
				}

				blockpos$mutable.setWithOffset(pos, direction).move(Direction.UP);
				BlockState blockstate3 = worldIn.getBlockState(blockpos$mutable);
				if (!blockstate3.is(Blocks.OBSERVER)) {
					BlockPos blockpos1 = blockpos$mutable.relative(direction.getOpposite());
					BlockState blockstate2 = blockstate3.updateShape(direction.getOpposite(),
							worldIn.getBlockState(blockpos1), worldIn, blockpos$mutable, blockpos1);
					updateOrDestroy(blockstate3, blockstate2, worldIn, blockpos$mutable, flags, recursionLeft);
				}
			}
		}

	}

	private RedstoneSide getSide(BlockGetter worldIn, BlockPos pos, Direction face) {
		return this.recalculateSide(worldIn, pos, face,
				!worldIn.getBlockState(pos.above()).isRedstoneConductor(worldIn, pos));
	}

	private RedstoneSide recalculateSide(BlockGetter reader, BlockPos pos, Direction direction,
			boolean nonNormalCubeAbove) {
		BlockPos blockpos = pos.relative(direction);
		BlockState blockstate = reader.getBlockState(blockpos);
		if (nonNormalCubeAbove) {
			boolean flag = this.canPlaceOnTopOf(reader, blockpos, blockstate);
			if (flag && canConnectTo(reader.getBlockState(blockpos.above()), reader, blockpos.above(), null)) {
				if (blockstate.isFaceSturdy(reader, blockpos, direction.getOpposite())) {
					return RedstoneSide.UP;
				}

				return RedstoneSide.SIDE;
			}
		}

		return !canConnectTo(blockstate, reader, blockpos, direction)
				&& (blockstate.isRedstoneConductor(reader, blockpos)
						|| !canConnectTo(reader.getBlockState(blockpos.below()), reader, blockpos.below(), null))
								? RedstoneSide.NONE
								: RedstoneSide.SIDE;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		BlockPos blockpos = pos.below();
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return this.canPlaceOnTopOf(worldIn, blockpos, blockstate);
	}

	private boolean canPlaceOnTopOf(BlockGetter reader, BlockPos pos, BlockState state) {
		return state.isFaceSturdy(reader, pos, Direction.UP) || state.is(Blocks.HOPPER);
	}

	private void updatePower(Level world, BlockPos pos, BlockState state) {
		int i = this.getStrongestSignal(world, pos);
		if (state.getValue(POWER) != i) {
			if (world.getBlockState(pos) == state) {
				world.setBlock(pos, state.setValue(POWER, Integer.valueOf(i)), 2);
			}

			Set<BlockPos> set = Sets.newHashSet();
			set.add(pos);

			for (Direction direction : Direction.values()) {
				set.add(pos.relative(direction));
			}

			for (BlockPos blockpos : set) {
				world.updateNeighborsAt(blockpos, this);
			}
		}

	}

	private int getStrongestSignal(Level world, BlockPos pos) {
		this.canProvidePower = false;
		int i = world.getBestNeighborSignal(pos);
		this.canProvidePower = true;
		int j = 0;
		if (i < 15) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				BlockPos blockpos = pos.relative(direction);
				BlockState blockstate = world.getBlockState(blockpos);
				j = Math.max(j, this.getPower(blockstate));
				BlockPos blockpos1 = pos.above();
				if (blockstate.isRedstoneConductor(world, blockpos)
						&& !world.getBlockState(blockpos1).isRedstoneConductor(world, blockpos1)) {
					j = Math.max(j, this.getPower(world.getBlockState(blockpos.above())));
				} else if (!blockstate.isRedstoneConductor(world, blockpos)) {
					j = Math.max(j, this.getPower(world.getBlockState(blockpos.below())));
				}
			}
		}

		return Math.max(i, j - 1);
	}

	private int getPower(BlockState state) {
		return state.is(this) ? state.getValue(POWER) : 0;
	}

	/**
	 * Calls Level.notifyNeighborsOfStateChange() for all neighboring blocks, but
	 * only if the given block is a redstone wire.
	 */
	private void notifyWireNeighborsOfStateChange(Level worldIn, BlockPos pos) {
		if (worldIn.getBlockState(pos).is(this)) {
			worldIn.updateNeighborsAt(pos, this);

			for (Direction direction : Direction.values()) {
				worldIn.updateNeighborsAt(pos.relative(direction), this);
			}

		}
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!oldState.is(state.getBlock()) && !worldIn.isClientSide) {
			this.updatePower(worldIn, pos, state);

			for (Direction direction : Direction.Plane.VERTICAL) {
				worldIn.updateNeighborsAt(pos.relative(direction), this);
			}

			this.updateNeighboursStateChange(worldIn, pos);
		}
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!isMoving && !state.is(newState.getBlock())) {
			if (!worldIn.isClientSide) {
				for (Direction direction : Direction.values()) {
					worldIn.updateNeighborsAt(pos.relative(direction), this);
				}

				this.updatePower(worldIn, pos, state);
				this.updateNeighboursStateChange(worldIn, pos);
			}
		}
	}

	private void updateNeighboursStateChange(Level world, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			this.notifyWireNeighborsOfStateChange(world, pos.relative(direction));
		}

		for (Direction direction1 : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.relative(direction1);
			if (world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
				this.notifyWireNeighborsOfStateChange(world, blockpos.above());
			} else {
				this.notifyWireNeighborsOfStateChange(world, blockpos.below());
			}
		}

	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (!worldIn.isClientSide) {
			if (state.canSurvive(worldIn, pos)) {
				this.updatePower(worldIn, pos, state);
			} else {
				dropResources(state, worldIn, pos);
				worldIn.removeBlock(pos, false);
			}

		}
	}

	/**
	 * @deprecated call via
	 *             {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)}
	 *             whenever possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return !this.canProvidePower ? 0 : blockState.getSignal(blockAccess, pos, side);
	}

	/**
	 * @deprecated call via
	 *             {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)}
	 *             whenever possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (this.canProvidePower && side != Direction.DOWN) {
			int i = blockState.getValue(POWER);
			if (i == 0) {
				return 0;
			} else {
				return side != Direction.UP && !this.getUpdatedState(blockAccess, blockState, pos)
						.getValue(FACING_PROPERTY_MAP.get(side.getOpposite())).isConnected() ? 0 : i;
			}
		} else {
			return 0;
		}
	}

	protected static boolean canConnectTo(BlockState blockState, BlockGetter world, BlockPos pos,
			@Nullable Direction side) {
		if (blockState.getBlock() == BlockInit.befouling_ash_trail.get()) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change
	 * based on its state.
	 * 
	 * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible.
	 *             Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public boolean isSignalSource(BlockState state) {
		return this.canProvidePower;
	}

	@OnlyIn(Dist.CLIENT)
	public static int getRGBByPower(int power) {
		Vector3f vector3f = powerRGB[power];
		return Mth.color(vector3f.x(), vector3f.y(), vector3f.z());
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnPoweredParticle(Level world, Random rand, BlockPos pos, Vector3f rgbVector,
			Direction directionFrom, Direction directionTo, float minChance, float maxChance) {
		float f = maxChance - minChance;
		if (!(rand.nextFloat() >= 0.2F * f)) {
			float f2 = minChance + f * rand.nextFloat();
			double d0 = 0.5D + 0.4375F * directionFrom.getStepX()
					+ f2 * directionTo.getStepX();
			double d1 = 0.5D + 0.4375F * directionFrom.getStepY()
					+ f2 * directionTo.getStepY();
			double d2 = 0.5D + 0.4375F * directionFrom.getStepZ()
					+ f2 * directionTo.getStepZ();
			world.addParticle(new DustParticleOptions(new Vector3f(rgbVector.x(), rgbVector.y(), rgbVector.z()), 1.0F),
					pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
		}
	}

	/**
	 * Called periodically clientside on blocks near the player to show effects
	 * (like furnace fire particles). Note that this method is unrelated to
	 * {@link randomTick} and {@link #needsRandomTick}, and will always be called
	 * regardless of whether the block can receive random update ticks
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		int i = stateIn.getValue(POWER);
		if (i != 0) {
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				RedstoneSide redstoneside = stateIn.getValue(FACING_PROPERTY_MAP.get(direction));
				switch (redstoneside) {
				case UP:
					this.spawnPoweredParticle(worldIn, rand, pos, powerRGB[i], direction, Direction.UP, -0.5F, 0.5F);
				case SIDE:
					this.spawnPoweredParticle(worldIn, rand, pos, powerRGB[i], Direction.DOWN, direction, 0.0F, 0.5F);
					break;
				case NONE:
				default:
					this.spawnPoweredParticle(worldIn, rand, pos, powerRGB[i], Direction.DOWN, direction, 0.0F, 0.3F);
				}
			}

		}
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		switch (rot) {
		case CLOCKWISE_180:
			return state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST))
					.setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
		case COUNTERCLOCKWISE_90:
			return state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH))
					.setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
		case CLOCKWISE_90:
			return state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH))
					.setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
		default:
			return state;
		}
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		switch (mirrorIn) {
		case LEFT_RIGHT:
			return state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
		case FRONT_BACK:
			return state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
		default:
			return super.mirror(state, mirrorIn);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, POWER);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult hit) {
		if (!player.getAbilities().mayBuild) {
			return InteractionResult.PASS;
		} else {
			if (areAllSidesValid(state) || areAllSidesInvalid(state)) {
				BlockState blockstate = areAllSidesValid(state) ? this.defaultBlockState() : this.sideBaseState;
				blockstate = blockstate.setValue(POWER, state.getValue(POWER));
				blockstate = this.getUpdatedState(worldIn, blockstate, pos);
				if (blockstate != state) {
					worldIn.setBlock(pos, blockstate, 3);
					this.updateChangedConnections(worldIn, pos, state, blockstate);
					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.PASS;
		}
	}

	private void updateChangedConnections(Level world, BlockPos pos, BlockState prevState, BlockState newState) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos blockpos = pos.relative(direction);
			if (prevState.getValue(FACING_PROPERTY_MAP.get(direction)).isConnected() != newState
					.getValue(FACING_PROPERTY_MAP.get(direction)).isConnected()
					&& world.getBlockState(blockpos).isRedstoneConductor(world, blockpos)) {
				world.updateNeighborsAtExceptFromFacing(blockpos, newState.getBlock(), direction.getOpposite());
			}
		}

	}

	static {
		for (int i = 0; i <= 15; ++i) {
			float f = i / 15.0F;
			float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
			float f2 = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
			float f3 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
			powerRGB[i] = new Vector3f(f1, f2, f3);
		}

	}
}
