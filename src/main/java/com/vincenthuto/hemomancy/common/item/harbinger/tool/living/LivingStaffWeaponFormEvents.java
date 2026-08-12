package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingStaffWeaponFormEvents {
	private LivingStaffWeaponFormEvents() {
	}

	@SubscribeEvent
	public static void onItemToss(ItemTossEvent event) {
		ItemStack tossed = event.getEntity().getItem();
		if (LivingFlailDeployment.isDeployed(tossed)) {
			LivingFlailDeployment.reconcileForRestoration(tossed, event.getPlayer());
		}
		if (LivingSicklePruning.isTemporarySickle(tossed)) {
			Player player = event.getPlayer();
			ItemStack restored = LivingSicklePruning.restoredWeaponStack(tossed, player.registryAccess());
			event.getEntity().setItem(LivingStaffWeaponFormHelper.isTransformedStaffWeapon(restored)
					? LivingStaffWeaponFormHelper.restoredStaffStack(restored, player.registryAccess())
					: restored);
			LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		} else if (LivingStaffWeaponFormHelper.isTransformedStaffWeapon(tossed)) {
			Player player = event.getPlayer();
			LivingStaffWeaponFormHelper.removePairedOffhandClaw(player, tossed);
			event.getEntity().setItem(LivingStaffWeaponFormHelper.restoredStaffStack(tossed, player.registryAccess()));
			LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		}
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof Player player) {
			LivingFlailDeployment.reconcile(player);
			LivingStaffWeaponFormHelper.restoreMainHand(player);
			LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		}
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			LivingFlailDeployment.reconcile(player);
			LivingStaffWeaponFormHelper.restoreMainHand(player);
			LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		}
	}

	@SubscribeEvent
	public static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		LivingFlailDeployment.reconcile(event.getEntity());
		LivingStaffWeaponFormHelper.restoreMainHand(event.getEntity());
		LivingArsenalInventoryGuard.sanitizePlayerInventory(event.getEntity());
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		LivingFlailDeployment.reconcile(event.getEntity());
		LivingStaffWeaponFormHelper.restoreMainHand(event.getEntity());
		LivingArsenalInventoryGuard.sanitizePlayerInventory(event.getEntity());
	}

	@SubscribeEvent
	public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		LivingFlailDeployment.reconcile(event.getEntity());
		LivingStaffWeaponFormHelper.restoreMainHand(event.getEntity());
		LivingArsenalInventoryGuard.sanitizePlayerInventory(event.getEntity());
	}

	@SubscribeEvent
	public static void onContainerClose(PlayerContainerEvent.Close event) {
		Player player = event.getEntity();
		LivingArsenalInventoryGuard.restoreLivingArsenalFromNonPlayerSlots(player, event.getContainer());
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide || player.tickCount % 10 != 0) {
			return;
		}
		if (player.containerMenu != player.inventoryMenu) {
			LivingArsenalInventoryGuard.restoreLivingArsenalFromNonPlayerSlots(player, player.containerMenu);
		}
		LivingArsenalInventoryGuard.sanitizePlayerInventory(player);
		if (player instanceof ServerPlayer serverPlayer) LivingFlailDeployment.reconcileMissingProjectile(serverPlayer);
	}
}
