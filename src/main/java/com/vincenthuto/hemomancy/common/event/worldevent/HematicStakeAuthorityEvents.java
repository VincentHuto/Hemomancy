package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.HematicStakeBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class HematicStakeAuthorityEvents {
	private HematicStakeAuthorityEvents() {
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getLevel() instanceof ServerLevel level)
				|| !(event.getEntity() instanceof ServerPlayer player)
				|| event.getHand() != InteractionHand.MAIN_HAND
				|| !player.isShiftKeyDown()
				|| !player.getMainHandItem().isEmpty()) {
			return;
		}
		if (level.getBlockState(event.getPos()).is(BlockInit.consecrated_bloodwell.get())) {
			return;
		}

		BlockPos placePos = event.getPos().relative(event.getFace());
		if (HematicStakeBlock.manifestStake(level, player, placePos)) {
			player.displayClientMessage(Component.translatable("block.hemomancy.hematic_stake.manifested")
					.withStyle(ChatFormatting.DARK_RED), true);
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBreakStake(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level)
				|| !(event.getPlayer() instanceof ServerPlayer player)
				|| !event.getState().is(BlockInit.hematic_stake.get())) {
			return;
		}

		UUID owner = FoundingSanctumSavedData.get(level).findOwnerForStake(event.getPos());
		if (owner == null || owner.equals(player.getUUID())) {
			return;
		}

		player.displayClientMessage(Component.translatable("block.hemomancy.hematic_stake.not_owner")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		event.setCanceled(true);
	}
}
