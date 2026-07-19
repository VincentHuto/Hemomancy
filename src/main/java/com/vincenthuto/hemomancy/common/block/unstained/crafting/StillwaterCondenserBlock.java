package com.vincenthuto.hemomancy.common.block.unstained.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A quiet, environment-driven Unstained station that condenses Lethean dew. */
public class StillwaterCondenserBlock extends Block {
	public StillwaterCondenserBlock(Properties properties) { super(properties); }

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(Items.GLASS_BOTTLE)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (level.isClientSide) return ItemInteractionResult.SUCCESS;
		if (!(player instanceof ServerPlayer serverPlayer)) return ItemInteractionResult.FAIL;
		boolean eligible = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> progress.hasBegunPurification() && progress.getPurity() >= 50f).orElse(false);
		if (!eligible) {
			player.displayClientMessage(Component.literal("The condenser remains still until Purity reaches 50.")
					.withStyle(ChatFormatting.GRAY), true);
			return ItemInteractionResult.FAIL;
		}
		if (!hasStillWater(level, pos) || !hasGhostPipe(level, pos)) {
			player.displayClientMessage(Component.literal("It needs source water beneath it and living Ghost Pipe nearby.")
					.withStyle(ChatFormatting.AQUA), true);
			return ItemInteractionResult.FAIL;
		}
		if (player.getCooldowns().isOnCooldown(asItem())) {
			player.displayClientMessage(Component.literal("The stillwater has not settled yet.").withStyle(ChatFormatting.GRAY), true);
			return ItemInteractionResult.FAIL;
		}
		if (!player.getAbilities().instabuild) stack.shrink(1);
		int output = VerdigrisLatticeBlock.hasActiveLattice(level, pos, 5) ? 2 : 1;
		ItemStack dew = new ItemStack(ItemInit.lethean_dew.get(), output);
		if (!player.addItem(dew)) player.drop(dew, false);
		player.getCooldowns().addCooldown(asItem(), output == 2 ? 100 : 200);
		level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8f, 1.3f);
		return ItemInteractionResult.SUCCESS;
	}

	private static boolean hasStillWater(Level level, BlockPos pos) {
		for (BlockPos check : new BlockPos[] { pos.below(), pos.north(), pos.south(), pos.east(), pos.west() }) {
			if (level.getFluidState(check).isSource()) return true;
		}
		return false;
	}

	private static boolean hasGhostPipe(Level level, BlockPos pos) {
		for (BlockPos check : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 2, 4))) {
			if (level.getBlockState(check).is(BlockInit.ghost_pipe.get())) return true;
		}
		return false;
	}
}
