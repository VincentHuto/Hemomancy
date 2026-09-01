package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScaleGripEvents {
	private static final Map<UUID, List<ItemStack>> PENDING_RETURNS = new ConcurrentHashMap<>();

	private ScaleGripEvents() {
	}

	@SubscribeEvent
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
			PENDING_RETURNS.put(player.getUUID(), savedStacks);
			player.displayClientMessage(Component.literal("Scale Grip coils around "
					+ savedStacks.size() + " bound item" + (savedStacks.size() == 1 ? "" : "s") + ".")
					.withStyle(ChatFormatting.DARK_GREEN), true);
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		List<ItemStack> savedStacks = PENDING_RETURNS.remove(player.getUUID());
		if (savedStacks == null || savedStacks.isEmpty()) {
			return;
		}
		for (ItemStack saved : savedStacks) {
			if (!saved.isEmpty() && !player.getInventory().add(saved)) {
				player.drop(saved, false);
			}
		}
	}

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		if (isProtected(event.getItemStack())) {
			event.getToolTip().add(Component.literal("Scale Grip: prevents death loss once")
					.withStyle(ChatFormatting.DARK_GREEN));
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
