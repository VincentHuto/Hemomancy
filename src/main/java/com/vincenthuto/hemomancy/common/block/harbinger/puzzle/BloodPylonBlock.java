package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.BloodPylonBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BloodPylonBlock extends BaseEntityBlock {
  public static final MapCodec<BloodPylonBlock> CODEC = simpleCodec(BloodPylonBlock::new);

    public BloodPylonBlock(Properties props) {
        super(props);
    }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodPylonBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, BlockEntityInit.blood_pylon.get(), BloodPylonBlockEntity::serverTick);
    }

    private InteractionResult handleUse(Level level, Player player) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        player.displayClientMessage(
            Component.translatable("message.hemomancy.blood_pylon.inspect")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
            false);
        return InteractionResult.CONSUME;
    }

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return handleUse(level, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		handleUse(level, player);
		return ItemInteractionResult.SUCCESS;
	}
}
