package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;

/**
 * A standing venous-stone basin banded in hematic iron, Hemorath's dry or brimming hall basin. {@link #FACING} is
 * the side its pouring spout faces; {@link #FILL} as the gutter's. Structures set the fill.
 * <p>
 * A brimming basin (fill 3, the blood standing in the spout) whose spout overhangs a {@link BloodGutterBlock} on the
 * floor in front of it pours into it: {@link #POURING} draws the stream (model from tools/model_export/venous_basin.py)
 * and splashes, and the blood runs on through the connected gutters, level or downhill, wetting each one at least to
 * the stream's level for its distance from the spout ({@link #flow}). Pouring only ever raises a gutter's fill.
 */
public class VenousBasinBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty FILL = BloodGutterBlock.FILL;
	public static final BooleanProperty POURING = BooleanProperty.create("pouring");
	/** The fill at which the blood reaches the spout's notch. */
	public static final int SPILL_FILL = 3;
	/** Every this many gutters from the spout, the stream runs one fill level lower. */
	public static final int FALLOFF = 4;
	/** The most gutters one basin wets. */
	public static final int MAX_REACH = 64;
	private static final ParticleColor BLOOD = new ParticleColor(150, 10, 18);
	private static final ParticleColor BLOOD_BRIGHT = new ParticleColor(196, 24, 30);
	/** Plinth, pedestal and bowl, traced from the north-facing model (venous_basin.py). */
	private static final Map<Direction, VoxelShape> SHAPES = ModelShapes.horizontal(ModelShapes.of(
			new double[] {2, 0, 2, 14, 2, 14},
			new double[] {5, 2, 5, 11, 5, 11},
			new double[] {3, 5, 3, 13, 6, 13},
			new double[] {1.5, 6, 1.5, 14.5, 14, 14.5},
			new double[] {6, 10.5, 0, 10, 13, 2}));

	public VenousBasinBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FILL, 0)
				.setValue(POURING, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FILL, POURING);
	}

	/** The spout faces the player who sets the basin down. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		return withPouring(context.getLevel(), context.getClickedPos(), state);
	}

	/** The gutter a basin at {@code pos} pours into: the one in the floor just in front of its spout. */
	public static BlockPos spoutTarget(BlockPos pos, BlockState state) {
		return pos.relative(state.getValue(FACING)).below();
	}

	public static boolean pours(BlockGetter level, BlockPos pos, BlockState state) {
		return state.getValue(FILL) >= SPILL_FILL
				&& level.getBlockState(spoutTarget(pos, state)).getBlock() instanceof BloodGutterBlock;
	}

	public static BlockState withPouring(BlockGetter level, BlockPos pos, BlockState state) {
		return state.setValue(POURING, pours(level, pos, state));
	}

	/** Called when a gutter appears or goes under this basin's spout. */
	public static void refresh(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof VenousBasinBlock)) return;
		BlockState updated = withPouring(level, pos, state);
		if (updated != state) level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
		if (updated.getValue(POURING) && level instanceof ServerLevel server) flow(server, pos, updated);
	}

	/**
	 * Runs the stream on from the spout through every connected gutter at the same height or lower, the gutters
	 * {@code d} steps from the spout wetted to at least {@code SPILL_FILL - d / FALLOFF}.
	 */
	public static void flow(LevelAccessor level, BlockPos pos, BlockState basin) {
		BlockPos start = spoutTarget(pos, basin);
		var queue = new ArrayDeque<BlockPos>();
		var distance = new java.util.HashMap<BlockPos, Integer>();
		var seen = new HashSet<BlockPos>();
		queue.add(start);
		distance.put(start, 0);
		seen.add(start);
		while (!queue.isEmpty() && seen.size() <= MAX_REACH) {
			BlockPos at = queue.poll();
			int d = distance.get(at);
			int stream = basin.getValue(FILL) - d / FALLOFF;
			if (stream <= 0) continue;
			BlockState gutter = level.getBlockState(at);
			if (!(gutter.getBlock() instanceof BloodGutterBlock)) continue;
			if (gutter.getValue(BloodGutterBlock.FILL) < stream) {
				level.setBlock(at, gutter.setValue(BloodGutterBlock.FILL, stream), Block.UPDATE_CLIENTS);
			}
			for (Direction side : Direction.Plane.HORIZONTAL) {
				for (BlockPos next : new BlockPos[] {at.relative(side), at.relative(side).below()}) {
					if (seen.add(next) && level.getBlockState(next).getBlock() instanceof BloodGutterBlock) {
						distance.put(next, d + 1);
						queue.add(next);
					}
				}
			}
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		super.onPlace(state, level, pos, old, moved);
		BloodGutterBlock.refreshSide(level, spoutTarget(pos, state), state.getValue(FACING).getOpposite());
		if (!level.isClientSide && state.getValue(POURING)) flow(level, pos, state);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
		super.onRemove(state, level, pos, replacement, moved);
		if (!replacement.is(this) || replacement.getValue(FACING) != state.getValue(FACING)) {
			BloodGutterBlock.refreshSide(level, spoutTarget(pos, state), state.getValue(FACING).getOpposite());
		}
	}

	/** Basins a structure placed bring their pouring state and their stream up to date as their chunk ticks. */
	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(FILL) >= SPILL_FILL || state.getValue(POURING);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		refresh(level, pos);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(POURING)) return;
		Direction facing = state.getValue(FACING);
		// Where the stream meets the gutter's channel, just past the spout's lip.
		double x = pos.getX() + 0.5 + facing.getStepX() * 0.55;
		double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.55;
		double y = pos.getY() - 0.1;
		if (random.nextFloat() < 0.6f) {
			level.addParticle(HemoParticleData.bloodCell(random.nextFloat() < 0.3f ? BLOOD_BRIGHT : BLOOD),
					x + (random.nextDouble() - 0.5) * 0.15, y + 0.05, z + (random.nextDouble() - 0.5) * 0.15,
					(random.nextDouble() - 0.5) * 0.03, 0.02 + random.nextDouble() * 0.03,
					(random.nextDouble() - 0.5) * 0.03);
		}
		if (random.nextFloat() < 0.25f) {
			// A drop leaving the lip.
			double lx = pos.getX() + 0.5 + facing.getStepX() * 0.5;
			double lz = pos.getZ() + 0.5 + facing.getStepZ() * 0.5;
			level.addParticle(HemoParticleData.bloodCell(BLOOD), lx, pos.getY() + 0.7, lz, 0, -0.05, 0);
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
