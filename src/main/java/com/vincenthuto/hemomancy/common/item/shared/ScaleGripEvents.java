package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScaleGripEvents {
	/** The bound stacks a death parked, handed back to the living player once they really land (F-4). */
	public static final PendingDelivery.Claim RETURNS = new PendingDelivery.Claim("scale_grip_returns",
			player -> HemoCapabilityAccess.scaleGripReturns(player).owed(), player -> true,
			(player, left) -> HemoCapabilityAccess.scaleGripReturns(player).settle(left));

	private ScaleGripEvents() {
	}

	/** Lowest priority, so a death another handler cancels never strips the bound items. */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onLivingDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			return;
		}
		List<ItemStack> savedStacks = new ArrayList<>();
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.isEmpty() || !isProtected(stack)) {
				continue;
			}
			ItemStack saved = stack.copy();
			clearProtected(saved);
			player.getInventory().setItem(slot, ItemStack.EMPTY);
			savedStacks.add(saved);
		}
		if (!savedStacks.isEmpty()) {
			HemoCapabilityAccess.scaleGripReturns(player).park(savedStacks);
			player.displayClientMessage(HemoText.translatable("message.hemomancy.scale_grip_events.scale_grip_coils_around_bound_item",
					savedStacks.size(),
					(savedStacks.size() == 1 ? "" : "s")).withStyle(ChatFormatting.DARK_GREEN), true);
		}
	}

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		if (isProtected(event.getItemStack())) {
			event.getToolTip().add(Component.translatable("message.hemomancy.scale_grip_events.scale_grip_prevents_death_loss_once").withStyle(ChatFormatting.DARK_GREEN));
		}
	}

	public static boolean isProtected(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
				.getBoolean(ScaleGripRules.TAG_PROTECTED);
	}

	public static void markProtected(ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(ScaleGripRules.TAG_PROTECTED, true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void clearProtected(ItemStack stack) {
		if (stack.isEmpty() || !stack.has(DataComponents.CUSTOM_DATA)) {
			return;
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.remove(ScaleGripRules.TAG_PROTECTED);
		if (tag.isEmpty()) {
			stack.remove(DataComponents.CUSTOM_DATA);
		} else {
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}

}
