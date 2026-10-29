package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateLedger;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.common.util.TriState;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateForm;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AbhorrentGazeRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AbhorrentThoughtEntity;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPossession;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * A possession's server rules: damage spends Concentration instead of health, a lethal blow or leaving the Gardens
 * snaps the will home, Ambergris regenerates and liquids drain the forms that cannot bear them, and a possessed body
 * neither builds nor opens. Logout returns the will cleanly; a login mid-possession recovers it.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PossessionEvents {
	private PossessionEvents() {}

	private static boolean possessed(Player player) {
		return HemoCapabilityAccess.isPossessing(player);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !possessed(player)) return;
		event.setCanceled(true);
		if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			PossessionService.eject(player, PossessionRules.MIND_BROKEN_TICKS);
			return;
		}
		if (PossessionRules.ignoresHit(player.invulnerableTime, event.getSource().is(DamageTypeTags.BYPASSES_COOLDOWN))) return;
		player.invulnerableTime = 20;
		if (event.getSource().getEntity() instanceof AbhorrentThoughtEntity) {
			grab(player);
			return;
		}
		spend(player, PossessionRules.hitCost(event.getAmount()), true);
	}

	/** Spends Concentration; the shell answers first when {@code shellable}, the totem once before the break. */
	public static void spend(ServerPlayer player, float cost, boolean shellable) {
		Possession possession = HemoCapabilityAccess.getPossession(player);
		PossessionRules.Hit hit = PossessionRules.absorb(shellable ? possession.shell() : 0,
				possession.concentration(), cost);
		possession.setShell(shellable ? hit.shell() : possession.shell());
		possession.setConcentration(hit.concentration());
		if (!hit.broken()) return;
		if (possession.spendTotem()) {
			player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0,
					player.getZ(), 30, 0.4, 0.6, 0.4, 0.2);
			player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS,
					1f, 1f);
			return;
		}
		PossessionService.eject(player, PossessionRules.MIND_BROKEN_TICKS);
	}

	/** An Abhorrent Thought's grab breaks the will outright; an unspent totem saves the visit once. */
	private static void grab(ServerPlayer player) {
		Possession possession = HemoCapabilityAccess.getPossession(player);
		if (AbhorrentGazeRules.grab(possession.totem()) == AbhorrentGazeRules.Grab.TOTEM && possession.spendTotem()) {
			player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0,
					player.getZ(), 30, 0.4, 0.6, 0.4, 0.2);
			return;
		}
		PossessionService.eject(player, AbhorrentGazeRules.GRAB_MIND_BROKEN_TICKS);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !possessed(player)) return;
		event.setCanceled(true);
		player.setHealth(Math.max(1f, player.getHealth()));
		PossessionService.eject(player, PossessionRules.MIND_BROKEN_TICKS);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !possessed(player)) return;
		Possession possession = HemoCapabilityAccess.getPossession(player);
		if (!PossessionService.inGardens(player.level())) {
			PossessionService.eject(player, PossessionRules.MIND_BROKEN_TICKS);
			return;
		}
		if (player.tickCount % 100 == 0 && possession.regen()) {
			possession.setConcentration(possession.concentration() + PossessionRules.regenPer100(true));
		}
		SurrogateForm form = possession.form();
		if (form != null && form.waterHurts() && player.tickCount % PossessionRules.WATER_DRAIN_TICKS == 0
				&& (player.isInWater() || player.isInLava())) {
			spend(player, 1f, false);
		}
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && possessed(player)) PossessionService.returnClean(player);
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && possessed(player)) PossessionService.returnClean(player);
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof Player target && possessed(target)
				&& event.getEntity() instanceof ServerPlayer watcher) {
			PacketHandler.sendToPlayer(watcher, PacketSyncPossession.of(target.getId(),
					HemoCapabilityAccess.getPossession(target)));
		}
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (possessed(event.getPlayer())) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onPlace(BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() instanceof Player player && possessed(player)) event.setCanceled(true);
	}

	/** A possessed body opens nothing: with the Thread in hand only the item's own use runs, never the block's. */
	@SubscribeEvent
	public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
		if (!possessed(event.getEntity())) return;
		if (event.getItemStack().is(ItemInit.thread_of_will.get())) event.setUseBlock(TriState.FALSE);
		else event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onUseEntity(PlayerInteractEvent.EntityInteract event) {
		if (possessed(event.getEntity())) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onUseEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
		if (possessed(event.getEntity())) event.setCanceled(true);
	}

	/** A facsimile does not fight other players: a possessed will cannot be killed by them either. */
	@SubscribeEvent
	public static void onAttack(AttackEntityEvent event) {
		if (possessed(event.getEntity()) && event.getTarget() instanceof Player) event.setCanceled(true);
	}

	/** A surrogate whose visit ended while it was unloaded is released (or dies) the moment it loads again. */
	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent event) {
		if (event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof LivingEntity body
				&& !(body instanceof Player)) {
			SurrogateLedger ledger = SurrogateLedger.get(level.getServer());
			if (ledger.verdict(body.getUUID()) != null) PossessionService.settle(body, ledger);
		}
	}

	@SubscribeEvent
	public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
		if (possessed(event.getEntity()) && !event.getItemStack().is(ItemInit.thread_of_will.get())) {
			event.setCanceled(true);
		}
	}
}
