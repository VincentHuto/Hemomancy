package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * A follower of Marimo gone all the way into his moss: the true petrification on the lowest walls of the Lighthouse
 * Garden's undercroft. Set dressing, not a resident; nothing here can withdraw. {@link Moss} picks how far the moss
 * has closed over the figure. Model from tools/model_export/petrified_follower.py.
 */
public class PetrifiedFollowerBlock extends CourtFacingFixtureBlock {
	public enum Moss implements StringRepresentable {
		PARTIAL, WHOLE;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	public static final EnumProperty<Moss> MOSS = EnumProperty.create("moss", Moss.class);
	public static final double[][] OUTLINE = {{2, 0, 3, 14, 16, 15}};

	public PetrifiedFollowerBlock(Properties properties) {
		super(OUTLINE, properties);
		registerDefaultState(defaultBlockState().setValue(MOSS, Moss.PARTIAL));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(MOSS);
	}
}
