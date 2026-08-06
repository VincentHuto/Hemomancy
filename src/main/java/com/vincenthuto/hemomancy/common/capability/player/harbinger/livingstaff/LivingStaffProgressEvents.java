package com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingStaffProgressEvents {
	private LivingStaffProgressEvents() {
	}

	@SubscribeEvent
	public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			LivingStaffBondHelper.ensureConjureStaffKnown(player);
			LivingStaffBondHelper.ensureVesperSickleKnown(player);
			LivingStaffBondHelper.syncProgress(player);
		}
	}

	@SubscribeEvent
	public static void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			LivingStaffBondHelper.ensureConjureStaffKnown(player);
			LivingStaffBondHelper.ensureVesperSickleKnown(player);
			LivingStaffBondHelper.syncProgress(player);
		}
	}

	@SubscribeEvent
	public static void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			LivingStaffBondHelper.ensureConjureStaffKnown(player);
			LivingStaffBondHelper.ensureVesperSickleKnown(player);
			LivingStaffBondHelper.syncProgress(player);
		}
	}
}
