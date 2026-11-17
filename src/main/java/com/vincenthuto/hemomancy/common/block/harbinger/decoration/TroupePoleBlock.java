package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

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
 * a section set on the same pole is shaft and any other is a foot.
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
	private final Map<Section, VoxelShape> shapes = new EnumMap<>(Section.class);

	/** {@code half} is the shaft's half width in pixels; the foot and collar stand two pixels proud of it. */
	public TroupePoleBlock(Properties properties, double half) {
		super(properties);
		double proud = Math.min(8, half + 2);
		shapes.put(Section.FOOT, box(8 - proud, 0, 8 - proud, 8 + proud, 16, 8 + proud));
		shapes.put(Section.SHAFT, box(8 - half, 0, 8 - half, 8 + half, 16, 8 + half));
		shapes.put(Section.COLLAR, box(8 - proud, 0, 8 - proud, 8 + proud, 16, 8 + proud));
		shapes.put(Section.FINIAL, box(8 - half, 0, 8 - half, 8 + half, 14, 8 + half));
		registerDefaultState(stateDefinition.any().setValue(SECTION, Section.SHAFT));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SECTION);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
		return defaultBlockState().setValue(SECTION, below.is(this) ? Section.SHAFT : Section.FOOT);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes.get(state.getValue(SECTION));
	}
}
