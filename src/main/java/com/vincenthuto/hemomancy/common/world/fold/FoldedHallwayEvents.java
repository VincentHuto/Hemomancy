package com.vincenthuto.hemomancy.common.world.fold;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class FoldedHallwayEvents {
	private FoldedHallwayEvents() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		FoldedHallwayManager.tick(event.getEntity());
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (isFolded(event.getEntity())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
		}
	}

	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (isFolded(event.getEntity())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (isFolded(event.getPlayer())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() instanceof Player player && isFolded(player)) {
			event.setCanceled(true);
		}
	}

	private static boolean isFolded(Player player) {
		return player != null && FoldedHallwayManager.isActive(player);
	}
}
