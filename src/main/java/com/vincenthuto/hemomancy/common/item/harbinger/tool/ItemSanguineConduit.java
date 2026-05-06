package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ItemSanguineConduit extends BlockItem {

	public ItemSanguineConduit(Block block, Properties prop) {
		super(block, prop.stacksTo(1));
	}

	// ── In-air use: open Harbinger skill-tree screen ──────────────────────────

	@Override
	public InteractionResultHolder<ItemStack> use(Level lvl, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (lvl.isClientSide) {
			HarbingerProgressScreen.openScreen();
		}
		return InteractionResultHolder.sidedSuccess(stack, lvl.isClientSide());
	}

	// ── Right-click on surface: place the conduit block if degree ≥ 5 ─────────

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);

		if (degree < 5) {
			if (!context.getLevel().isClientSide()) {
				player.displayClientMessage(
						Component.translatable("hemomancy.item.sanguine_conduit.place_locked")
								.withStyle(ChatFormatting.DARK_RED),
						true);
			}
			return InteractionResult.FAIL;
		}

		return super.useOn(context);
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

