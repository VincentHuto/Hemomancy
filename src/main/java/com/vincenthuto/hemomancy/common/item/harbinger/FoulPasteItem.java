package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FoulPasteItem extends Item {

	public FoulPasteItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);

		if (level.isClientSide()) {
			if (isGrassLike(state) || isWoodLike(state)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		if (isGrassLike(state)) {
			level.setBlockAndUpdate(pos, BlockInit.erythrocytic_mycelium.get().defaultBlockState());
			level.playSound(null, pos, SoundEvents.FUNGUS_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
			if (!ctx.getPlayer().getAbilities().instabuild) {
				ctx.getItemInHand().shrink(1);
			}
			return InteractionResult.SUCCESS;
		}

		if (isWoodLike(state)) {
			level.setBlockAndUpdate(pos, BlockInit.infested_wood.get().defaultBlockState());
			level.playSound(null, pos, SoundEvents.FUNGUS_PLACE, SoundSource.BLOCKS, 1.0F, 0.6F);
			if (!ctx.getPlayer().getAbilities().instabuild) {
				ctx.getItemInHand().shrink(1);
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private boolean isGrassLike(BlockState state) {
		return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.MYCELIUM);
	}

	private boolean isWoodLike(BlockState state) {
		return state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS);
	}
}
