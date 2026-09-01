package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.WarpChairBlock;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ChamberVisitEvents {
	private ChamberVisitEvents() {
	}

	@SubscribeEvent
	public static void onWake(PlayerWakeUpEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ChamberVisitService.onCompletedSleep(player, !event.wakeImmediately());
		}
	}

	@SubscribeEvent
	public static void onTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player) ChamberVisitService.tick(player);
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ChamberVisitService.clearInterruptedChairSleep(player);
		if (!ChamberVisitService.isAttuned(player)
				&& com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 6
				&& com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player,
						com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED)) {
			ChamberVisitService.attune(player);
		}
		if (ChamberVisitService.isActive(player)
				&& !player.level().dimension().equals(com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.CHAMBER_OF_WILL)) {
			ChamberVisitService.recoverOutsideChamber(player);
		} else {
			ChamberVisitService.sync(player);
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			ChamberVisitService.clearInterruptedChairSleep(player);
			if (ChamberVisitService.isActive(player)) ChamberVisitService.returnFromVisit(player);
		}
	}

	@SubscribeEvent
	public static void onDamage(LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && ChamberVisitService.isProtected(player)) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && ChamberVisitService.isProtected(player)) {
			event.setCanceled(true);
			player.setHealth(1.0F);
			ChamberVisitService.returnFromVisit(player);
		}
	}

	@SubscribeEvent
	public static void onAttack(AttackEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && ChamberVisitService.isDream(player)
				&& !(event.getTarget() instanceof ArborOfWillEntity)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getEntity() instanceof ServerPlayer player && ChamberVisitService.isDream(player)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntity() instanceof ServerPlayer player && ChamberVisitService.isDream(player)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getEntity() instanceof ServerPlayer player && ChamberVisitService.isDream(player)
				&& !(event.getTarget() instanceof ArborOfWillEntity)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player
				&& (ChamberVisitService.isDream(player) || WarpChairBlock.isPaired(event.getLevel(), event.getPos()))) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlace(BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && ChamberVisitService.isDream(player)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		event.getAffectedBlocks().removeIf(pos -> WarpChairBlock.isPaired(event.getLevel(), pos));
	}

	@SubscribeEvent
	public static void onToss(ItemTossEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player && ChamberVisitService.isDream(player)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onPickup(ItemEntityPickupEvent.Pre event) {
		if (event.getPlayer() instanceof ServerPlayer player && ChamberVisitService.isDream(player)) {
			event.setCanPickup(TriState.FALSE);
		}
	}
}
