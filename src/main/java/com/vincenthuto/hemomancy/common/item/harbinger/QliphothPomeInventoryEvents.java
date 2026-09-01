package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class QliphothPomeInventoryEvents {
	private static final Component RESISTS_REMOVAL_MESSAGE = Component.literal(
			"The pome resists being taken away from you. Eat it; the next fruit will not grow until you do.")
			.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC);

	private QliphothPomeInventoryEvents() {
	}

	@SubscribeEvent
	public static void onItemToss(ItemTossEvent event) {
		ItemStack tossed = event.getEntity().getItem();
		if (!shouldProtect(tossed, event.getPlayer())) {
			return;
		}
		event.setCanceled(true);
		giveBackOrQueue(event.getPlayer(), tossed.copy(), true);
		event.getPlayer().displayClientMessage(RESISTS_REMOVAL_MESSAGE, true);
	}

	@SubscribeEvent
	public static void onLivingDrops(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide
				|| player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			return;
		}

		Iterator<ItemEntity> iterator = event.getDrops().iterator();
		while (iterator.hasNext()) {
			ItemEntity drop = iterator.next();
			ItemStack stack = drop.getItem();
			if (shouldProtect(stack, player)) {
				queueBoundPomeReturn(player, stack.copy());
				iterator.remove();
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		restoreQueuedBoundPomes(event.getEntity(), true);
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		restoreQueuedBoundPomes(event.getEntity(), true);
	}

	@SubscribeEvent
	public static void onItemStackedOnOther(ItemStackedOnOtherEvent event) {
		Player player = event.getPlayer();
		if (!shouldProtect(event.getCarriedItem(), player) || event.getSlot().container == player.getInventory()) {
			return;
		}
		event.setCanceled(true);
		player.displayClientMessage(RESISTS_REMOVAL_MESSAGE, true);
	}

	@SubscribeEvent
	public static void onContainerClose(PlayerContainerEvent.Close event) {
		restoreBoundPomesFromNonPlayerSlots(event.getEntity(), event.getContainer());
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) {
			return;
		}
		restoreQueuedBoundPomes(player, player.tickCount % 40 == 0);
		if (player.containerMenu != player.inventoryMenu) {
			restoreBoundPomesFromNonPlayerSlots(player, player.containerMenu);
		}
	}

	private static void restoreBoundPomesFromNonPlayerSlots(Player player, AbstractContainerMenu menu) {
		if (player.level().isClientSide || menu == null) {
			return;
		}
		boolean changed = false;
		for (Slot slot : menu.slots) {
			if (slot.container == player.getInventory()) {
				continue;
			}
			ItemStack stack = slot.getItem();
			if (!shouldProtect(stack, player)) {
				continue;
			}
			ItemStack returning = stack.copy();
			slot.set(ItemStack.EMPTY);
			slot.setChanged();
			giveBackOrQueue(player, returning, true);
			changed = true;
		}
		if (changed) {
			menu.broadcastChanges();
			player.displayClientMessage(RESISTS_REMOVAL_MESSAGE, true);
		}
	}

	private static boolean shouldProtect(ItemStack stack, Player player) {
		return QliphothPomeRules.shouldProtectBoundPome(QliphothPomeItem.isBoundPome(stack),
				player.getAbilities().instabuild);
	}

	private static void restoreQueuedBoundPomes(Player player, boolean notify) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		if (player.getAbilities().instabuild) {
			return;
		}
		QliphothBloomSavedData data = QliphothBloomSavedData.get(serverPlayer.server.overworld());
		List<ItemStack> returning = data.takePendingBoundPomeReturns(serverPlayer.getUUID());
		if (returning.isEmpty()) {
			return;
		}
		boolean stillWaiting = false;
		for (ItemStack stack : returning) {
			if (!giveBackOrQueue(player, stack, false)) {
				stillWaiting = true;
			}
		}
		if (stillWaiting && notify) {
			player.displayClientMessage(Component.literal("The pome clings close, but your inventory has no room.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		}
	}

	private static boolean giveBackOrQueue(Player player, ItemStack stack, boolean notifyWhenQueued) {
		if (stack.isEmpty()) {
			return true;
		}
		if (player.getInventory().add(stack)) {
			return true;
		}
		queueBoundPomeReturn(player, stack);
		if (notifyWhenQueued) {
			player.displayClientMessage(Component.literal("The pome clings close, but your inventory has no room.")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
		}
		return false;
	}

	private static void queueBoundPomeReturn(Player player, ItemStack stack) {
		if (player.level() instanceof ServerLevel serverLevel && !stack.isEmpty()) {
			QliphothBloomSavedData.get(serverLevel.getServer().overworld())
					.queueBoundPomeReturn(player.getUUID(), stack);
		}
	}
}
