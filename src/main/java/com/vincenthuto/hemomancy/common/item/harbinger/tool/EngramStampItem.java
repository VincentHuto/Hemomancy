package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.block.harbinger.EngramBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EngramStampItem extends Item {

	public EngramStampItem(Properties prop) {
		super(prop);
	}

	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos clickedPos = ctx.getClickedPos();
		BlockState clickedState = level.getBlockState(clickedPos);

		if (clickedState.getBlock() == BlockInit.engram_block.get()) {
			if (!level.isClientSide()) {
				level.setBlockAndUpdate(clickedPos,
						clickedState.setValue(EngramBlock.CHARACTERINDEX, EngramBlock.randomCharacterIndex()));
				damageStamp(ctx);
			}
			return InteractionResult.SUCCESS;
		}

		Direction clickedFace = ctx.getClickedFace();
		if (clickedFace == Direction.DOWN) {
			return InteractionResult.FAIL;
		}

		Direction engramFacing = clickedFace.getAxis().isHorizontal() ? clickedFace : Direction.UP;
		BlockPos targetPos = clickedFace == Direction.UP ? clickedPos.above() : clickedPos.relative(clickedFace);
		if (!clickedState.isFaceSturdy(level, clickedPos, clickedFace) || !level.isEmptyBlock(targetPos)) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			BlockState engramState = BlockInit.engram_block.get().defaultBlockState()
					.setValue(EngramBlock.CHARACTERINDEX, EngramBlock.randomCharacterIndex())
					.setValue(EngramBlock.FACING, engramFacing)
					.setValue(EngramBlock.LIT, false);
			level.setBlockAndUpdate(targetPos, engramState);
			damageStamp(ctx);
		}
		return InteractionResult.SUCCESS;
	}

	private static void damageStamp(UseOnContext ctx) {
		if (ctx.getPlayer() != null) {
			ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), LivingEntity.getSlotForHand(ctx.getHand()));
		}
	}

}
