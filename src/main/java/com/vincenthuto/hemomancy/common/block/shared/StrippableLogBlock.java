package com.vincenthuto.hemomancy.common.block.shared;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.function.Supplier;

/** A log or bark block an axe strips to its stripped form, keeping its axis. */
public class StrippableLogBlock extends RotatedPillarBlock {
	private final Supplier<? extends Block> stripped;

	public StrippableLogBlock(Properties properties, Supplier<? extends Block> stripped) {
		super(properties);
		this.stripped = stripped;
	}

	@Override
	public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
		if (itemAbility == ItemAbilities.AXE_STRIP) {
			return stripped.get().defaultBlockState().setValue(AXIS, state.getValue(AXIS));
		}
		return super.getToolModifiedState(state, context, itemAbility, simulate);
	}
}
