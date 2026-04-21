package com.vincenthuto.hemomancy.common.capability.player.visceral;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.OrganEchoItem;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Handles capability attachment, persistence across death, and per-tick
 * effects of organ echoes for the Visceral Mirror system.
 *
 * <p>Also enforces the rule that organ echo items dissolve when placed
 * into any non-player container (chest, barrel, hopper output, etc.).</p>
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class VisceralOrgansEvents {

	@SubscribeEvent
	public static void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			event.addCapability(Hemomancy.rloc("visceral_organs"), new VisceralOrgansProvider());
		}
	}

	@SubscribeEvent
	public static void playerDeath(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			Player original = event.getOriginal();
			Player newPlayer = event.getEntity();
			original.reviveCaps();
			IVisceralOrgans oldOrgans = original.getCapability(VisceralOrgansProvider.ORGANS_CAPA)
					.orElseThrow(IllegalStateException::new);
			IVisceralOrgans newOrgans = newPlayer.getCapability(VisceralOrgansProvider.ORGANS_CAPA)
					.orElseThrow(IllegalStateException::new);
			for (EnumOrgan organ : EnumOrgan.values()) {
				newOrgans.setOrganLevel(organ, oldOrgans.getOrganLevel(organ));
			}
			original.invalidateCaps();
		}
	}

	// ========================== ECHO DISSOLVE ==========================

	/**
	 * When a player closes a container, scan every non-player slot for
	 * organ echo items and dissolve them. This prevents echoes from
	 * persisting in chests, barrels, or similar storage.
	 */
	@SubscribeEvent
	public static void onContainerClose(PlayerContainerEvent.Close event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) return;

		AbstractContainerMenu menu = event.getContainer();
		for (Slot slot : menu.slots) {
			// Skip slots that belong to the player's own inventory
			if (slot.container == player.getInventory()) continue;

			ItemStack stack = slot.getItem();
			if (!stack.isEmpty() && stack.getItem() instanceof OrganEchoItem) {
				slot.set(ItemStack.EMPTY);
			}
		}
	}

	// ========================== ORGAN TICK EFFECTS ==========================

	/**
	 * Applies passive organ modification effects each tick.
	 * Only runs server-side every 40 ticks (2 seconds) to avoid spam.
	 */
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Player player = event.player;
		if (player.level().isClientSide) return;
		if (player.tickCount % 40 != 0) return;

		player.getCapability(VisceralOrgansProvider.ORGANS_CAPA).ifPresent(organs -> {
			// Spleen — increased max blood volume per modification level
			if (organs.isExtracted(EnumOrgan.SPLEEN)) {
				int level = organs.getOrganLevel(EnumOrgan.SPLEEN);
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
					double baseMax = 5000.0;
					double bonus = level * 1000.0; // +1000 per modification level
					double targetMax = baseMax + bonus;
					if (vol.getMaxBloodVolume() < targetMax) {
						vol.setMaxBloodVolume(targetMax);
					}
				});
			}

			// Liver — poison/toxin resistance
			if (organs.isExtracted(EnumOrgan.LIVER)) {
				int level = organs.getOrganLevel(EnumOrgan.LIVER);
				// Remove poison effect and grant brief resistance
				if (player.hasEffect(MobEffects.POISON)) {
					if (level >= 2) {
						player.removeEffect(MobEffects.POISON);
					}
				}
				// At level 3, also remove wither
				if (level >= 3 && player.hasEffect(MobEffects.WITHER)) {
					player.removeEffect(MobEffects.WITHER);
				}
			}

			// Lungs — enhanced underwater breathing
			if (organs.isExtracted(EnumOrgan.LUNGS)) {
				int level = organs.getOrganLevel(EnumOrgan.LUNGS);
				if (player.isUnderWater()) {
					// Grant water breathing effect scaled to level
					player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
							100 * level, 0, true, false));
				}
			}

			// Kidneys — enhanced regeneration
			if (organs.isExtracted(EnumOrgan.KIDNEYS)) {
				int level = organs.getOrganLevel(EnumOrgan.KIDNEYS);
				// Grant regeneration briefly at intervals
				if (!player.hasEffect(MobEffects.REGENERATION)) {
					player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
							60, level - 1, true, false));
				}
			}

			// Heart — Cardiac Autonomy
			// The player has removed their heart and sustains circulation through
			// sheer force of will, rhythmically commanding their muscles to
			// contract. This grants damage resistance but at the cost of occasional
			// blood drain from the effort.
			if (organs.isHeartless()) {
				int level = organs.getOrganLevel(EnumOrgan.HEART);
				// Grants damage resistance (the body's compensatory mechanism)
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						60, Math.min(level - 1, 1), true, false));

				// Blood cost: sustaining circulation without a heart is taxing
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
					double cost = 10.0 / level; // Higher level = less cost
					vol.drain(cost);
				});
			}
		});
	}
}
