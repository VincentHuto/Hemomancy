package com.vincenthuto.hemomancy.common.capability.player.harbinger.organs;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonEvents;
import com.vincenthuto.hemomancy.common.item.harbinger.OrganEchoItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class VisceralOrgansEvents {

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
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide) return;
		if (player.tickCount % 40 != 0) return;

		boolean bloodMoonActive = BloodMoonEvents.isBloodMoonActive(player.level());

		HemoCapabilityAccess.getVisceralOrgans(player).ifPresent(organs -> {
			// Spleen — increased max blood volume per modification level
			if (organs.isExtracted(EnumOrgan.SPLEEN)) {
				int level = organs.getOrganLevel(EnumOrgan.SPLEEN);
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
					double baseMax = 5000.0;
					double bonus = level * 1000.0; // +1000 per modification level
					double targetMax = baseMax + bonus;
					if (vol.getMaxBloodVolume() < targetMax) {
						vol.setMaxBloodVolume(targetMax);
						// Announce expansion so the player notices the new capacity
						player.displayClientMessage(
								Component.literal("Your blood reservoir has expanded \u2014 capacity now "
										+ (int) targetMax + ".")
										.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
								false);
					}
				});
			}

			// Liver — poison/toxin resistance
			if (organs.isExtracted(EnumOrgan.LIVER)) {
				int level = organs.getOrganLevel(EnumOrgan.LIVER);
				// Remove poison effect at level 2+
				if (player.hasEffect(MobEffects.POISON)) {
					if (level >= 2) {
						player.removeEffect(MobEffects.POISON);
					}
				}
				// At level 3, also suppress wither
				if (level >= 3 && player.hasEffect(MobEffects.WITHER)) {
					player.removeEffect(MobEffects.WITHER);
				}
			}

			// Lungs — enhanced underwater breathing
			if (organs.isExtracted(EnumOrgan.LUNGS)) {
				int level = organs.getOrganLevel(EnumOrgan.LUNGS);
				if (player.isUnderWater()) {
					player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
							100 * level, 0, true, false));
				}
			}

			// Kidneys — enhanced regeneration
			// During a Blood Moon the body is under more pressure; the kidney's
			// filtration overclocks — grant a higher amplifier when the moon is active.
			if (organs.isExtracted(EnumOrgan.KIDNEYS)) {
				int level = organs.getOrganLevel(EnumOrgan.KIDNEYS);
				if (!player.hasEffect(MobEffects.REGENERATION)) {
					int amplifier = bloodMoonActive ? level : (level - 1);
					player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
							60, amplifier, true, false));
				}
			}

			// Heart — Cardiac Autonomy
			// The player has removed their heart and sustains circulation through
			// sheer force of will, rhythmically commanding their muscles to
			// contract. This grants damage resistance but at the cost of occasional
			// blood drain from the effort.
			// At level 3 the autonomic rhythm is fully mastered: the player becomes
			// immune to Wither — the final reminder that they no longer have a
			// heart to stop.
			if (organs.isHeartless()) {
				int level = organs.getOrganLevel(EnumOrgan.HEART);
				// Grants damage resistance (the body's compensatory mechanism)
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
						60, Math.min(level - 1, 1), true, false));

				// Level 3: full Cardiac Autonomy — suppress Wither
				if (level >= 3 && player.hasEffect(MobEffects.WITHER)) {
					player.removeEffect(MobEffects.WITHER);
				}

				// Blood cost: sustaining circulation without a heart is taxing
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
					double cost = 10.0 / level; // Higher level = less cost
					vol.drain(cost);
					if (player instanceof ServerPlayer sp) {
						BloodVolumeEvents
								.syncVolume(sp, vol);
					}
				});
			}
		});
	}
}
