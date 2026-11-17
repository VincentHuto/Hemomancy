package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.common.world.answer.BloodSpilledLedger;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswerRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Suspended Vivianite as a ward (core-loop Track 10): while it hangs in a region, blood spilled there counts for half
 * and the count fades twice as fast, up to three wards ({@link WorldAnswerRules#MAX_WARDS}). Placing and removing it
 * records it in the region's {@link BloodSpilledLedger}; using it reads what it is holding back.
 */
public class SuspendedVivianiteBlock extends SuspendedCrystalBlock {
	public SuspendedVivianiteBlock(Properties properties, BlockEntityType.BlockEntitySupplier<? extends BlockEntity> crystal) {
		super(properties, crystal);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (level instanceof ServerLevel server && !oldState.is(this)) BloodSpilledLedger.addWard(server, pos);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (level instanceof ServerLevel server && !newState.is(this)) BloodSpilledLedger.removeWard(server, pos);
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult result) {
		if (level instanceof ServerLevel server) {
			int wards = WorldAnswerRules.clampWards(BloodSpilledLedger.wards(server, pos));
			int band = BloodSpilledLedger.band(server, pos);
			player.displayClientMessage(Component.translatable("block.hemomancy.suspended_vivianite.ward", wards,
					Component.translatable("world_answer.hemomancy.band." + band)).withStyle(ChatFormatting.DARK_AQUA), true);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
