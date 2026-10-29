package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bracket fungus stepping out of a wall in Putriciel's overgrown bands: shelf caps whose gills glow a faint green.
 * {@link #FACING} is the way the caps grow; the block hangs on the face behind them, which must be sturdy.
 * {@link #BLOOM} is the bright phase (light 11, brighter gills); otherwise the shelf gives 5. Models from
 * tools/model_export/putriciel_materials.py.
 */
public class SporeShelfBlock extends Block {
	public static final MapCodec<SporeShelfBlock> CODEC = simpleCodec(SporeShelfBlock::new);
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty BLOOM = BooleanProperty.create("bloom");
	public static final int DIM_LIGHT = 5;
	public static final int BLOOM_LIGHT = 11;
	private static final ParticleColor SPORE = new ParticleColor(132, 176, 96);
	// The model's extent for facing=north (putriciel_materials.py prints it): x 1-15, y 1.5-15, z 8-16.
	private static final VoxelShape NORTH = box(1, 1.5, 8, 15, 15, 16);
	private static final VoxelShape SOUTH = box(1, 1.5, 0, 15, 15, 8);
	private static final VoxelShape WEST = box(8, 1.5, 1, 16, 15, 15);
	private static final VoxelShape EAST = box(0, 1.5, 1, 8, 15, 15);

	public SporeShelfBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BLOOM, false));
	}

	/** The light a shelf gives: the bloom's brighter glow, or the faint one. */
	public static int lightLevel(BlockState state) {
		return state.getValue(BLOOM) ? BLOOM_LIGHT : DIM_LIGHT;
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, BLOOM);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SOUTH;
			case WEST -> WEST;
			case EAST -> EAST;
			default -> NORTH;
		};
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos support = pos.relative(facing.getOpposite());
		return level.getBlockState(support).isFaceSturdy(level, support, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction side, BlockState neighbour, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		return side == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, side, neighbour, level, pos, neighbourPos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState();
		LevelReader level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		for (Direction looking : context.getNearestLookingDirections()) {
			if (looking.getAxis().isHorizontal()) {
				BlockState placed = state.setValue(FACING, looking.getOpposite());
				if (placed.canSurvive(level, pos)) {
					return placed;
				}
			}
		}
		return null;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/** A slow faint drift of spores from under the caps; more of them in bloom. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextFloat() >= (state.getValue(BLOOM) ? 0.3f : 0.06f)) {
			return;
		}
		Direction facing = state.getValue(FACING);
		Direction side = facing.getClockWise();
		double out = 0.5 - (2 + random.nextDouble() * 5) / 16.0;      // 2-7 px out from the wall
		double across = (random.nextDouble() - 0.5) * 0.7;
		double x = pos.getX() + 0.5 - facing.getStepX() * out + side.getStepX() * across;
		double z = pos.getZ() + 0.5 - facing.getStepZ() * out + side.getStepZ() * across;
		double y = pos.getY() + (2 + random.nextDouble() * 9) / 16.0;
		level.addParticle(HemoParticleData.sporiticSpore(SPORE), x, y, z, facing.getStepX() * 0.003, -0.004,
				facing.getStepZ() * 0.003);
	}
}
