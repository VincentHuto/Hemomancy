package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

/**
 * Marimo's floating sign (Track 8): a gold Sunburst bloom on the water over reef and coast, standing in the air cell
 * above the top water block the way a lily pad does. Set dressing placed by the Court sign feature and round the
 * Lighthouse Garden; no drop. Its item places it on water like a lily pad. {@link Variant} picks the model and {@code turn} spins it, so a field of blooms
 * does not tile. Models from tools/model_export/sunburst_bloom.py.
 */
public class SunburstBloomBlock extends WaterlilyBlock {
	public enum Variant implements StringRepresentable {
		PAD, PAIR, BLOOM;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);
	public static final IntegerProperty TURN = IntegerProperty.create("turn", 0, 3);
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 1.5, 15);

	public SunburstBloomBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(VARIANT, Variant.PAD).setValue(TURN, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(VARIANT, TURN);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/** Placed by hand, a bloom picks its look from its position the way the sign feature's blooms do. */
	@Override
	public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
		long hash = net.minecraft.util.Mth.getSeed(context.getClickedPos());
		int pick = (int) Math.floorMod(hash >>> 16, 5);
		Variant variant = pick == 0 ? Variant.BLOOM : pick <= 2 ? Variant.PAD : Variant.PAIR;
		return defaultBlockState().setValue(VARIANT, variant).setValue(TURN, (int) Math.floorMod(hash >>> 20, 4));
	}

	/** The light the model's glow implies: the frond bloom shows most. */
	public static int light(BlockState state) {
		return state.getValue(VARIANT) == Variant.BLOOM ? 9 : 6;
	}
}
