package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.VenousBasinBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A standing venous-stone basin banded in hematic iron, Hemorath's dry or brimming hall basin. {@link #FACING} is
 * the side its pouring spout faces; {@link #FILL} as the gutter's. Structures set the fill.
 * <p>
 * A brimming basin (fill 3, the blood standing in the spout) whose spout overhangs a {@link BloodGutterBlock} on the
 * floor in front of it pours into it: {@link #POURING} draws the stream (model from tools/model_export/venous_basin.py)
 * and splashes, and the blood runs on through the connected gutters, level or downhill, wetting each one at least to
 * the stream's level for its distance from the spout ({@link #flow}). Pouring only ever raises a gutter's fill.
 * <p>
 * A basin a player sets down is {@link #WORKED} (core-loop Track 6): it keeps a real tank in a
 * {@link VenousBasinBlockEntity}, its fill follows the blood in it, and its stream runs only down worked gutters,
 * raising and lowering them with its level ({@link #flowWorked}) and feeding what they end at. A structure's basin is
 * not worked and keeps the arena's behaviour above, with no block entity.
 */
public class VenousBasinBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty FILL = BloodGutterBlock.FILL;
	public static final BooleanProperty POURING = BooleanProperty.create("pouring");
	/** Set down by a player: a real tank that feeds worked gutters. Structures leave it false. */
	public static final BooleanProperty WORKED = BloodGutterBlock.WORKED;
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
				.setValue(POURING, false).setValue(WORKED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FILL, POURING, WORKED);
	}

	/** The spout faces the player who sets the basin down; a placed basin is worked and starts empty. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WORKED, true);
	}

	/** Only a worked basin keeps a tank. */
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(WORKED) ? new VenousBasinBlockEntity(pos, state) : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide || !state.getValue(WORKED) ? null
				: HemoTickers.create(type, BlockEntityInit.venous_basin.get(), VenousBasinBlockEntity::serverTick);
	}

	/** Whether a gutter lies under the spout of the basin at {@code pos}. */
	public static boolean overGutter(BlockGetter level, BlockPos pos, BlockState state) {
		return level.getBlockState(spoutTarget(pos, state)).getBlock() instanceof BloodGutterBlock;
	}

	/**
	 * The worked gutters a worked basin's stream reaches, and the machines and farmland they end at; {@code streams} is
	 * the stream this basin alone gives each gutter, and {@code others} the other worked basins pouring into the same
	 * network.
	 */
	public record Run(List<BlockPos> gutters, List<BlockPos> machines, List<BlockPos> farmland, Map<BlockPos, Integer> streams,
			List<BlockPos> others) {
		public static final Run EMPTY = new Run(List.of(), List.of(), List.of(), Map.of(), List.of());
	}

	/**
	 * A worked basin's stream at {@code fill}: every worked gutter connected to its spout, level or downhill, is set to
	 * the stream's level for its distance ({@link GroundRunRules#streamAt}), raised or lowered, so the gutters dry when
	 * the basin does. Where other worked basins pour into the same network a gutter shows the highest stream any of them
	 * gives it ({@link GroundRunRules#sharedFill}), so two basins never overwrite each other's fill. Gutters a structure
	 * placed are never touched. Returns what this basin's own wet gutters end at (a block entity that keeps a tank beside
	 * or just above one, and farmland beside one), its own stream per gutter, and the other basins it found.
	 */
	public static Run flowWorked(ServerLevel level, BlockPos pos, BlockState basin, int fill) {
		BlockPos start = spoutTarget(pos, basin);
		var queue = new ArrayDeque<BlockPos>();
		var distance = new java.util.HashMap<BlockPos, Integer>();
		var seen = new HashSet<BlockPos>();
		List<BlockPos> gutters = new ArrayList<>();
		List<BlockPos> machines = new ArrayList<>();
		List<BlockPos> farmland = new ArrayList<>();
		var targets = new HashSet<BlockPos>();
		Map<BlockPos, Integer> streams = new HashMap<>();
		if (isWorkedGutter(level.getBlockState(start))) {
			queue.add(start);
			distance.put(start, 0);
			seen.add(start);
		}
		List<VenousBasinBlockEntity> others = networkBasins(level, pos, start);
		while (!queue.isEmpty() && gutters.size() < MAX_REACH) {
			BlockPos at = queue.poll();
			int d = distance.get(at);
			BlockState gutter = level.getBlockState(at);
			int stream = GroundRunRules.streamAt(fill, d);
			gutters.add(at);
			streams.put(at, stream);
			int shown = GroundRunRules.sharedFill(stream, others.stream().mapToInt(other -> other.streamAt(at)).toArray());
			if (gutter.getValue(BloodGutterBlock.FILL) != shown) {
				level.setBlock(at, gutter.setValue(BloodGutterBlock.FILL, shown), Block.UPDATE_CLIENTS);
			}
			if (stream > 0) collectTargets(level, pos, at, machines, farmland, targets);
			for (Direction side : Direction.Plane.HORIZONTAL) {
				for (BlockPos next : new BlockPos[] {at.relative(side), at.relative(side).below()}) {
					if (seen.add(next) && isWorkedGutter(level.getBlockState(next))) {
						distance.put(next, d + 1);
						queue.add(next);
					}
				}
			}
		}
		return new Run(List.copyOf(gutters), List.copyOf(machines), List.copyOf(farmland), Map.copyOf(streams),
				others.stream().map(BlockEntity::getBlockPos).toList());
	}

	/** The worked basins other than {@code self} whose spouts pour into the worked gutter at {@code gutter}. */
	private static List<VenousBasinBlockEntity> spoutingInto(Level level, BlockPos self, BlockPos gutter) {
		List<VenousBasinBlockEntity> found = new ArrayList<>(1);
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos spout = gutter.above().relative(side);
			if (spout.equals(self)) continue;
			BlockState basin = level.getBlockState(spout);
			if (basin.getBlock() instanceof VenousBasinBlock && basin.getValue(WORKED) && spoutTarget(spout, basin).equals(gutter)
					&& level.getBlockEntity(spout) instanceof VenousBasinBlockEntity tank) found.add(tank);
		}
		return found;
	}

	/**
	 * The other worked basins on {@code start}'s gutter network: walks every connected worked gutter, uphill, level and
	 * downhill (at most {@code 2 * MAX_REACH} gutters), for spouts that pour into them. Both directions, so two basins
	 * on separate branches that join downhill still find each other.
	 */
	private static List<VenousBasinBlockEntity> networkBasins(Level level, BlockPos self, BlockPos start) {
		List<VenousBasinBlockEntity> found = new ArrayList<>();
		if (!isWorkedGutter(level.getBlockState(start))) return found;
		var queue = new ArrayDeque<BlockPos>();
		var seen = new HashSet<BlockPos>();
		queue.add(start.immutable());
		seen.add(start.immutable());
		int walked = 0;
		while (!queue.isEmpty() && walked < 2 * MAX_REACH) {
			BlockPos at = queue.poll();
			walked++;
			for (VenousBasinBlockEntity tank : spoutingInto(level, self, at)) {
				if (!found.contains(tank)) found.add(tank);
			}
			for (Direction side : Direction.Plane.HORIZONTAL) {
				for (BlockPos next : new BlockPos[] {at.relative(side), at.relative(side).above(), at.relative(side).below()}) {
					if (seen.add(next) && isWorkedGutter(level.getBlockState(next))) queue.add(next);
				}
			}
		}
		return found;
	}

	/**
	 * The worked basin whose stream wets the worked gutter at {@code gutter}: walks the connected worked gutters (level
	 * or uphill, the way the stream came) for one whose spout pours into them, preferring the nearest that holds fresh
	 * blood over a dry one on the same network. Null when none does.
	 */
	@Nullable
	public static VenousBasinBlockEntity feedingBasin(Level level, BlockPos gutter) {
		if (!isWorkedGutter(level.getBlockState(gutter))) return null;
		var queue = new ArrayDeque<BlockPos>();
		var seen = new HashSet<BlockPos>();
		queue.add(gutter.immutable());
		seen.add(gutter.immutable());
		VenousBasinBlockEntity dry = null;
		int walked = 0;
		while (!queue.isEmpty() && walked < MAX_REACH) {
			BlockPos at = queue.poll();
			walked++;
			for (Direction side : Direction.Plane.HORIZONTAL) {
				BlockPos spout = at.above().relative(side);
				BlockState basin = level.getBlockState(spout);
				if (basin.getBlock() instanceof VenousBasinBlock && basin.getValue(WORKED) && spoutTarget(spout, basin).equals(at)
						&& level.getBlockEntity(spout) instanceof VenousBasinBlockEntity tank) {
					if (tank.configure().getBloodVolume() > 0.0D) return tank;
					if (dry == null) dry = tank;
				}
				for (BlockPos next : new BlockPos[] {at.relative(side), at.relative(side).above()}) {
					if (seen.add(next) && isWorkedGutter(level.getBlockState(next))) queue.add(next);
				}
			}
		}
		return dry;
	}

	private static boolean isWorkedGutter(BlockState state) {
		return state.getBlock() instanceof BloodGutterBlock && state.getValue(BloodGutterBlock.WORKED);
	}

	private static void collectTargets(ServerLevel level, BlockPos basin, BlockPos gutter, List<BlockPos> machines,
			List<BlockPos> farmland, java.util.Set<BlockPos> seen) {
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos beside = gutter.relative(side);
			if (level.getBlockState(beside).getBlock() instanceof FarmBlock && seen.add(beside)) farmland.add(beside.immutable());
			for (BlockPos candidate : new BlockPos[] {beside, beside.above()}) {
				if (!candidate.equals(basin) && level.getBlockEntity(candidate) instanceof IBloodReservoir reservoir
						&& reservoir.holdsBlood() && seen.add(candidate)) machines.add(candidate.immutable());
			}
		}
		BlockPos above = gutter.above();
		if (!above.equals(basin) && level.getBlockEntity(above) instanceof IBloodReservoir reservoir && reservoir.holdsBlood()
				&& seen.add(above)) machines.add(above.immutable());
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
		if (state.getValue(WORKED)) {
			if (level.getBlockEntity(pos) instanceof VenousBasinBlockEntity basin) basin.markStale();
			return;
		}
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
		if (!level.isClientSide && state.getValue(POURING) && !state.getValue(WORKED)) flow(level, pos, state);
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
		return !state.getValue(WORKED) && (state.getValue(FILL) >= SPILL_FILL || state.getValue(POURING));
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
