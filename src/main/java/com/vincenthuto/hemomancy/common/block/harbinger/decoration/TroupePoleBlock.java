package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * A big top's king pole or quarter pole, one block per section: an iron-shod foot, the lacquered shaft, an iron
 * collar where the canvas is laced on, and a bone finial at the top. Templates write each section. Placed by hand,
 * a section set on the same pole is shaft and any other is a foot. A section walled in on all four sides (where the pole
 * runs through the roof canvas) is {@link #SEALED}: a full laced-canvas block that keeps out light and sky.
 */
public class TroupePoleBlock extends Block {
	public enum Section implements StringRepresentable {
		FOOT, SHAFT, COLLAR, FINIAL;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	public static final EnumProperty<Section> SECTION = EnumProperty.create("section", Section.class);
	public static final BooleanProperty SEALED = BooleanProperty.create("sealed");
	private final Map<Section, VoxelShape> shapes = new EnumMap<>(Section.class);

	/** {@code half} is the shaft's half width in pixels; the foot and collar stand two pixels proud of it. */
	public TroupePoleBlock(Properties properties, double half) {
		super(properties);
		double proud = Math.min(8, half + 2);
		shapes.put(Section.FOOT, box(8 - proud, 0, 8 - proud, 8 + proud, 16, 8 + proud));
		shapes.put(Section.SHAFT, box(8 - half, 0, 8 - half, 8 + half, 16, 8 + half));
		shapes.put(Section.COLLAR, box(8 - proud, 0, 8 - proud, 8 + proud, 16, 8 + proud));
		shapes.put(Section.FINIAL, box(8 - half, 0, 8 - half, 8 + half, 14, 8 + half));
		registerDefaultState(stateDefinition.any().setValue(SECTION, Section.SHAFT).setValue(SEALED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SECTION, SEALED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
		return defaultBlockState().setValue(SECTION, below.is(this) ? Section.SHAFT : Section.FOOT)
				.setValue(SEALED, walledIn(context.getLevel(), context.getClickedPos()));
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		return direction.getAxis().isHorizontal() ? state.setValue(SEALED, walledIn(level, pos)) : state;
	}

	/** Whether every horizontal neighbour is a full face toward the pole, as the roof canvas is. */
	private static boolean walledIn(BlockGetter level, BlockPos pos) {
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos at = pos.relative(side);
			if (!level.getBlockState(at).isFaceSturdy(level, at, side.getOpposite())) return false;
		}
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(SEALED) ? Shapes.block() : shapes.get(state.getValue(SECTION));
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(SEALED) ? Shapes.block() : Shapes.empty();
	}

	@Override
	protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(SEALED) ? 15 : 0;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return !state.getValue(SEALED);
	}
}
