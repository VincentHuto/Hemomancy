package com.vincenthuto.hemomancy.common.block.unstained.plant;

import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class LetheanPoppyBlock extends FlowerBlock {

	public static final IntegerProperty STATE = IntegerProperty.create("state", 0, 1);
	public static final int BLOOMED = 0;
	public static final int DORMANT = 1;

	public LetheanPoppyBlock(Holder<MobEffect> effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(STATE, BLOOMED));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(STATE);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return state.getValue(STATE) == DORMANT;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(STATE) == DORMANT) {
			level.setBlock(pos, state.setValue(STATE, BLOOMED), 2);
		}
	}

	private InteractionResult handleUse(BlockState state, Level level, BlockPos pos) {
		if (state.getValue(STATE) == BLOOMED) {
			if (!level.isClientSide) {
				popResource(level, pos, new ItemStack(ItemInit.lethean_dew.get()));
				level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
				level.setBlock(pos, state.setValue(STATE, DORMANT), 2);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return handleUse(state, level, pos);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		InteractionResult result = handleUse(state, level, pos);
		return result == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}
}
