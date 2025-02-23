package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.common.block.EngramBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class EngramStampItem extends Item {

	public EngramStampItem(Properties prop) {
		super(prop);
	}

	public InteractionResult useOn(UseOnContext ctx) {
		if (!ctx.getLevel().isClientSide()) {
			if (ctx.getLevel().getBlockState(ctx.getClickedPos()).isFaceSturdy(ctx.getLevel(), ctx.getClickedPos(),
					Direction.UP) && ctx.getLevel().isEmptyBlock(ctx.getClickedPos().above())) {
				ctx.getLevel().setBlockAndUpdate(ctx.getClickedPos().above(),
						(BlockState) (BlockInit.engram_block.get()).defaultBlockState()
								.setValue(EngramBlock.CHARACTERINDEX, (int) Math.floor(Math.random()
										* (double) (EngramBlock.CHARACTERINDEX.getPossibleValues().size() - 1))));
				ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), (player) -> {
					player.broadcastBreakEvent(ctx.getHand());
				});
			} else 
				//Cycle Engrams
				if (ctx.getLevel().getBlockState(ctx.getClickedPos()).getBlock() == BlockInit.engram_block.get()) {
				ctx.getLevel().setBlockAndUpdate(ctx.getClickedPos(),
						(BlockState) (BlockInit.engram_block.get()).defaultBlockState()
								.setValue(EngramBlock.CHARACTERINDEX, (int) Math.floor(Math.random()
										* (double) (EngramBlock.CHARACTERINDEX.getPossibleValues().size() - 1))));
				ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), (player) -> {
					player.broadcastBreakEvent(ctx.getHand());
				});
				return InteractionResult.SUCCESS;

			} else {
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.SUCCESS;
	}

}
