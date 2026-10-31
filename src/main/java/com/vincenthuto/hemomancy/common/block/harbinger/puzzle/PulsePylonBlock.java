package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.StackedSetPieceBlock;
import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.PulsePylonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A Repose wing's pylon, three tall: the pulse that tests the still ring fires from its head. Before each pulse the
 * Chapel focus winds {@link #CHARGE} up on the head (the PYLON mark) and its charge ring flares; the head's block
 * entity only feeds that ring's renderer. {@link #STYLE} picks the cut: the venous pylon with its crimson eye,
 * Seraphae's anchor (a chained calcite anchor with a pale Lux lens and a white-gold ring), or Putriciel's pyre (a bone
 * pyre on a charred crib with no ring: its charge feeds a Flammeus fire on its ember bed, and each pulse is drawn as a
 * purge of fire sweeping out over the floor), or Velorum's frost pillar (deepslate brick and packed ice under rime and
 * icicles, a cold blue-white head and a pale ring). Unbreakable. Models from tools/model_export/pulse_pylon.py.
 */
public class PulsePylonBlock extends StackedSetPieceBlock implements EntityBlock {
	public enum Part implements StringRepresentable {
		LOWER, MIDDLE, UPPER;

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}

	/** The pylon's cut; the charge ring (or, on the pyre, its fire) follows it. */
	public enum Style implements StringRepresentable {
		VENOUS, ANCHOR, PYRE, FROST;

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
	public static final EnumProperty<Style> STYLE = EnumProperty.create("style", Style.class);
	public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 3);
	public static final int MAX_CHARGE = 3;
	private static final Map<Direction, VoxelShape> LOWER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {1, 0, 1, 15, 3, 15}, new double[] {2.5, 3, 2.5, 13.5, 6, 13.5},
					new double[] {2.25, 6, 2.25, 13.75, 7, 13.75}, new double[] {4, 7, 3.75, 12, 16, 12}));
	private static final Map<Direction, VoxelShape> MIDDLE = ModelShapes.horizontal(
			ModelShapes.of(new double[] {4, 0, 3.75, 12, 8, 12}, new double[] {3.5, 8, 3.5, 12.5, 9.5, 12.5},
					new double[] {4.5, 9.5, 4.25, 11.5, 16, 11.5}));
	private static final Map<Direction, VoxelShape> UPPER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {4, 0, 4, 12, 13, 12}, new double[] {5.5, 7.5, 3.25, 10.5, 12, 4},
					new double[] {0.5, 5.25, 0.5, 15.5, 6.75, 15.5}, new double[] {5, 13, 5, 11, 14.5, 11},
					new double[] {6.5, 14.5, 6.5, 9.5, 16, 9.5}));
	private static final Map<Direction, VoxelShape> ANCHOR_LOWER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {1, 0, 1, 15, 2.5, 15}, new double[] {3.5, 2.5, 3.5, 12.5, 4.5, 12.5},
					new double[] {4.25, 4.5, 4.25, 11.75, 8.75, 11.75}, new double[] {4.75, 8.75, 4.75, 11.25, 16, 11.25}));
	private static final Map<Direction, VoxelShape> ANCHOR_MIDDLE = ModelShapes.horizontal(
			ModelShapes.of(new double[] {4.75, 0, 4.75, 11.25, 16, 11.25}));
	private static final Map<Direction, VoxelShape> ANCHOR_UPPER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {4.75, 0, 4.75, 11.25, 7, 11.25}, new double[] {4.25, 7, 4.25, 11.75, 13.5, 11.75},
					new double[] {5.25, 8.25, 3.25, 10.75, 13.5, 4.25}, new double[] {1, 9.25, 6.75, 15, 11.75, 9.25},
					new double[] {6.75, 9.25, 11.75, 9.25, 11.75, 15}, new double[] {0.5, 5.25, 0.5, 15.5, 6.75, 15.5},
					new double[] {5, 13.5, 5, 11, 14.5, 11}, new double[] {6.75, 14.5, 6.75, 9.25, 16, 9.25}));
	private static final Map<Direction, VoxelShape> PYRE_LOWER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {0.5, 0, 0.5, 15.5, 2.5, 15.5}, new double[] {1.75, 2.5, 1.75, 14.25, 5.5, 14.25},
					new double[] {2.75, 5.5, 2.75, 13.25, 8, 13.25}, new double[] {3.5, 8, 3.5, 12.5, 16, 12.5}));
	private static final Map<Direction, VoxelShape> PYRE_MIDDLE = ModelShapes.horizontal(
			ModelShapes.of(new double[] {3.75, 0, 3.75, 12.25, 16, 12.25}));
	private static final Map<Direction, VoxelShape> PYRE_UPPER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {4.5, 0, 4.5, 11.5, 2, 11.5}, new double[] {5.75, 0, 2.25, 10.25, 1.75, 4.5},
					new double[] {3.75, 2, 3.75, 12.25, 3.25, 12.25}, new double[] {2.5, 3.25, 2.5, 13.5, 11.5, 13.5}));
	private static final Map<Direction, VoxelShape> FROST_LOWER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {1, 0, 1, 15, 3, 15}, new double[] {2.5, 3, 2.5, 13.5, 6, 13.5},
					new double[] {2.25, 6, 2.25, 13.75, 7, 13.75}, new double[] {4, 7, 4, 12, 16, 12}));
	private static final Map<Direction, VoxelShape> FROST_MIDDLE = ModelShapes.horizontal(
			ModelShapes.of(new double[] {4, 0, 4, 12, 16, 12}, new double[] {3.5, 8, 3.5, 12.5, 9.5, 12.5}));
	private static final Map<Direction, VoxelShape> FROST_UPPER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {4, 0, 4, 12, 13, 12}, new double[] {0.5, 5.25, 0.5, 15.5, 6.75, 15.5},
					new double[] {5, 13, 5, 11, 14.5, 11}, new double[] {6.5, 14.5, 6.5, 9.5, 16, 9.5}));

	public PulsePylonBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, Part.LOWER)
				.setValue(CHARGE, 0).setValue(STYLE, Style.VENOUS));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, PART, CHARGE, STYLE);
	}

	@Override
	protected int storey(BlockState state) {
		return state.getValue(PART).ordinal();
	}

	@Override
	protected int storeys() {
		return 3;
	}

	@Override
	protected BlockState withStorey(BlockState state, int storey) {
		return state.setValue(PART, Part.values()[storey]);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Part part = state.getValue(PART);
		return (switch (state.getValue(STYLE)) {
			case VENOUS -> part == Part.LOWER ? LOWER : part == Part.MIDDLE ? MIDDLE : UPPER;
			case ANCHOR -> part == Part.LOWER ? ANCHOR_LOWER : part == Part.MIDDLE ? ANCHOR_MIDDLE : ANCHOR_UPPER;
			case PYRE -> part == Part.LOWER ? PYRE_LOWER : part == Part.MIDDLE ? PYRE_MIDDLE : PYRE_UPPER;
			case FROST -> part == Part.LOWER ? FROST_LOWER : part == Part.MIDDLE ? FROST_MIDDLE : FROST_UPPER;
		}).get(state.getValue(FACING));
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(PART) == Part.UPPER ? new PulsePylonBlockEntity(pos, state) : null;
	}
}
