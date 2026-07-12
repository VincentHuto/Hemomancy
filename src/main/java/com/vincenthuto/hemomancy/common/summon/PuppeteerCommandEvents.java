package com.vincenthuto.hemomancy.common.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PuppeteerCommandEvents {
	private PuppeteerCommandEvents() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onAttackEntity(AttackEntityEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| !(event.getTarget() instanceof LivingEntity target)
				|| !(target instanceof Enemy)) {
			return;
		}
		ItemStack crossbar = player.getMainHandItem();
		if (crossbar.getItem() instanceof MarionetteCrossbarItem) {
			MarionetteCrossbarItem.focusTarget(player, crossbar, target);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		rotateSession(event);
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		rotateSession(event);
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		rotateSession(event);
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		rotateSession(event);
	}

	private static void rotateSession(PlayerEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			BoundSummonBehavior.rotateOwnerSession(player);
		}
	}
}
