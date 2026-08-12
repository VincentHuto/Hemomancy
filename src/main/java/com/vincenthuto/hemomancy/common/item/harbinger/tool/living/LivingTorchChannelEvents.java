package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Explicit channel cleanup at lifecycle seams which can remove the caster from tracking. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LivingTorchChannelEvents {
	private LivingTorchChannelEvents() { }

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) stopIfBreathing(player);
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) stopIfBreathing(player);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) stopIfBreathing(player);
	}

	private static void stopIfBreathing(Player player) {
		if (player.isUsingItem() && player.getUseItem().getItem() instanceof LivingTorchItem
				&& player instanceof ServerPlayer serverPlayer) {
			LivingTorchItem.stopChannel(serverPlayer, player.getUsedItemHand());
		}
	}
}
