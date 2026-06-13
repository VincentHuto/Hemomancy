package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ScaleGripEvents {
	private static final Map<UUID, ItemStack> PENDING_RETURNS = new ConcurrentHashMap<>();

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
		int gripSlot = findScaleGripSlot(player);
		if (gripSlot < 0) {
			return;
		}

		OptionalInt protectedSlot = ScaleGripRules.selectProtectedSlot(toCandidates(player));
		if (protectedSlot.isEmpty()) {
			return;
		}

		ItemStack saved = player.getInventory().getItem(protectedSlot.getAsInt()).copy();
		if (saved.isEmpty()) {
			return;
		}
		player.getInventory().setItem(protectedSlot.getAsInt(), ItemStack.EMPTY);
		player.getInventory().getItem(gripSlot).shrink(1);
		PENDING_RETURNS.put(player.getUUID(), saved);
		player.displayClientMessage(Component.literal("Scale Grip coils around " + saved.getHoverName().getString() + ".")
				.withStyle(ChatFormatting.DARK_GREEN), true);
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		ItemStack saved = PENDING_RETURNS.remove(player.getUUID());
		if (saved == null || saved.isEmpty()) {
			return;
		}
		if (!player.getInventory().add(saved)) {
			player.drop(saved, false);
		}
	}

	private static int findScaleGripSlot(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			if (player.getInventory().getItem(slot).is(ItemInit.scale_grip.get())) {
				return slot;
			}
		}
		return -1;
	}

	private static List<ScaleGripRules.Candidate> toCandidates(ServerPlayer player) {
		List<ScaleGripRules.Candidate> candidates = new ArrayList<>();
		for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
			ItemStack stack = player.getInventory().items.get(slot);
			candidates.add(new ScaleGripRules.Candidate(slot, itemId(stack), stack.isEmpty(),
					stack.is(ItemInit.scale_grip.get())));
		}
		return candidates;
	}

	private static String itemId(ItemStack stack) {
		if (stack.isEmpty()) {
			return "minecraft:air";
		}
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		return id == null ? "minecraft:air" : id.toString();
	}
}
