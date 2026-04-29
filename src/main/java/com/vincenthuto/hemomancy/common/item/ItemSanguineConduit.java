package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;

public class ItemSanguineConduit extends Item {

	public ItemSanguineConduit(Properties prop) {
		super(prop.stacksTo(1));
	}

	// ── In-air use: open Harbinger skill-tree screen ──────────────────────────

	@Override
	public InteractionResultHolder<ItemStack> use(Level lvl, Player player, InteractionHand hand) {
		if (lvl.isClientSide) {
			HarbingerProgressScreen.openScreen();
		}
		return super.use(lvl, player, hand);
	}

	// ── Right-click on surface: place the conduit block if degree ≥ 5 ─────────

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);

		if (context.getLevel().isClientSide()) {
			// Optimistic client feedback: fail immediately so no ghost placement occurs
			return degree >= 5 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		}

		// Server: enforce the degree gate
		if (degree < 5) {
			player.displayClientMessage(
					Component.translatable("hemomancy.item.sanguine_conduit.place_locked")
							.withStyle(ChatFormatting.DARK_RED),
					true);
			return InteractionResult.FAIL;
		}

		BlockPlaceContext placeContext = new BlockPlaceContext(context);
		Block conduitBlock = BlockInit.sanguine_conduit.get();
		BlockState placementState = conduitBlock.getStateForPlacement(placeContext);
		if (placementState == null) {
			return InteractionResult.FAIL;
		}

		Level level = context.getLevel();
		BlockPos pos = placeContext.getClickedPos();

		if (!level.isInWorldBounds(pos)) {
			return InteractionResult.FAIL;
		}
		if (!level.getBlockState(pos).canBeReplaced(placeContext)) {
			return InteractionResult.FAIL;
		}

		level.setBlock(pos, placementState, Block.UPDATE_ALL);

		SoundType soundType = placementState.getSoundType(level, pos, player);
		level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS,
				(soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);

		if (!player.isCreative()) {
			context.getItemInHand().shrink(1);
		}

		return InteractionResult.SUCCESS;
	}

	// ── Tooltip: degree-gated messaging ──────────────────────────────────────

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);

		// appendHoverText is only ever called client-side; Minecraft instance is safe.
		Player viewer = Minecraft.getInstance().player;
		int degree = (viewer != null) ? HemoCapabilityAccess.getPlayerDegreeNumber(viewer) : 0;

		if (degree >= 5) {
			tooltip.add(Component.translatable("hemomancy.item.sanguine_conduit.tooltip.unlocked")
					.withStyle(ChatFormatting.DARK_RED));
			tooltip.add(Component.translatable("hemomancy.item.sanguine_conduit.tooltip.unlocked_hint")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		} else {
			tooltip.add(Component.translatable("hemomancy.item.sanguine_conduit.tooltip.locked")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
		}
	}
}

