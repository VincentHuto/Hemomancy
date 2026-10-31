package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSeatSearch;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * The Voyagers' Relay Chart: a waybill that points to the nearest Lighthouse Garden. Its search runs through
 * {@link CourtSeatSearch}, off the server thread and bounded to {@link CourtSeatSearch#MAX_RINGS} Seat cells, so
 * issuing or using a chart never stalls the server. A chart that finds nothing says so in its tooltip.
 */
public final class RelayChartItem extends CovenantWaybillItem {
	private static final String KEY = "item.hemomancy.relay_chart";
	private static final String NONE = "RelayChartNone";

	public RelayChartItem(Properties properties) {
		super(properties, Hemomancy.rloc("brined_court"), KEY, 0);
	}

	@Override
	protected void attune(ItemStack stack, ServerLevel level, Player player, boolean showFeedback) {
		if (!(player instanceof ServerPlayer serverPlayer)) return;
		if (!CourtSeatSearch.begin(player.getUUID())) {
			if (showFeedback) tell(player, ".searching", ChatFormatting.GRAY);
			return;
		}
		if (showFeedback) tell(player, ".searching", ChatFormatting.GRAY);
		CourtSeatSearch.nearest(level, player.blockPosition()).whenComplete((found, error) -> {
			CourtSeatSearch.finish(player.getUUID());
			if (error != null) {
				Hemomancy.LOGGER.warn("Relay Chart search failed", error);
				return;
			}
			if (serverPlayer.isRemoved() || serverPlayer.level() != level) return;
			ItemStack chart = heldOrCarried(serverPlayer, stack);
			if (chart.isEmpty()) return;
			BlockPos target = found.orElse(null);
			record(chart, level, serverPlayer, target, showFeedback);
			CompoundTag tag = chart.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (target == null) tag.putBoolean(NONE, true);
			else tag.remove(NONE);
			chart.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		});
	}

	/** Never called: {@link #attune} replaces the synchronous search. */
	@Override
	protected BlockPos findTarget(ServerLevel level, Player player) {
		return null;
	}

	/** The chart that asked, if the player still has it; otherwise any unaimed chart they carry. */
	private static ItemStack heldOrCarried(ServerPlayer player, ItemStack asked) {
		var inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot) == asked) return asked;
		}
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.getItem() instanceof RelayChartItem && target(stack, player.level()) == null) return stack;
		}
		return ItemStack.EMPTY;
	}

	public static boolean foundNothing(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(NONE);
	}

	private static void tell(Player player, String suffix, ChatFormatting style) {
		player.displayClientMessage(Component.translatable(KEY + suffix).withStyle(style, ChatFormatting.ITALIC), false);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		if (foundNothing(stack)) {
			tooltip.add(Component.translatable(KEY + ".tooltip.none").withStyle(ChatFormatting.DARK_GRAY,
					ChatFormatting.ITALIC));
		}
	}
}
