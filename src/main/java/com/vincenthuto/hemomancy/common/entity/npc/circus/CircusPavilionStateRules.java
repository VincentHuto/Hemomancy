package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CircusPavilionStateRules {
	/** Session state: each bystander's last refusal, by the act's holder. Cleared by the transient-state hub. */
	private static final Map<UUID, UUID> BYSTANDERS_TOLD = new HashMap<>();

	private CircusPavilionStateRules() {
	}

	public static boolean canBegin(UUID activeOwner, Outcome outcome) {
		return activeOwner == null && outcome == Outcome.NEUTRAL;
	}

	public static boolean canAct(UUID activeOwner, UUID player) {
		return activeOwner != null && activeOwner.equals(player);
	}

	/** The line a refused claim gets: a settled site never plays again, a held one frees up when its act ends. */
	public static String refusalKey(Outcome outcome) {
		return outcome == Outcome.NEUTRAL ? "hemomancy.circus.finale.busy" : "hemomancy.circus.finale.settled";
	}

	/**
	 * D-34: the player {@code entity} acts for. A player acts for themself; a non-trial bound body and a Blood Cur act
	 * for the owner who holds them; a projectile acts for whoever its shooter acts for. Null for anything else.
	 */
	public static UUID actingFor(Entity entity) {
		if (entity instanceof Player player) return player.getUUID();
		if (entity instanceof Projectile projectile) return actingFor(projectile.getOwner());
		if (entity instanceof SanguineHoundEntity hound && hound.isBloodCur()) return hound.curOwner();
		if (entity instanceof BoundPuppeteerSummon bound && !bound.hemomancy$isTrialSummon()) return bound.hemomancy$getOwnerUUID();
		return null;
	}

	/** The player a damage source acts for: its causing entity, or its direct one when nothing caused it. */
	public static UUID actingFor(DamageSource source) {
		return actingFor(source.getEntity() != null ? source.getEntity() : source.getDirectEntity());
	}

	/**
	 * The finale stays solo: a player (or a body they hold) striking the troupe during someone else's act or finale is
	 * told once per act, on the action bar, however many troupe members they strike.
	 */
	public static void refuseBystander(Entity troupe, UUID actor, UUID holder) {
		if (actor == null || holder == null || !(troupe.level() instanceof ServerLevel server)
				|| !firstRefusal(BYSTANDERS_TOLD, actor, holder)) return;
		if (server.getPlayerByUUID(actor) instanceof ServerPlayer player) player.displayClientMessage(
				Component.translatable("hemomancy.circus.finale.solo").withStyle(ChatFormatting.DARK_GRAY), true);
	}

	/** Records the refusal; true when {@code actor} has not yet been told during {@code holder}'s act. */
	static boolean firstRefusal(Map<UUID, UUID> told, UUID actor, UUID holder) {
		return !holder.equals(told.put(actor, holder));
	}

	/** A new act by {@code holder} begins, so its bystanders are told afresh. */
	public static void beginAct(UUID holder) {
		BYSTANDERS_TOLD.values().removeIf(holder::equals);
	}

	public static void clearSessionState() {
		BYSTANDERS_TOLD.clear();
	}

	public static void logout(Player player) {
		BYSTANDERS_TOLD.remove(player.getUUID());
	}

	public static Phase resetPhase(Outcome outcome) {
		return outcome == Outcome.NEUTRAL ? Phase.IDLE : Phase.COMPLETE;
	}

	public enum Outcome { NEUTRAL, SUCCESSION, RUIN }
	/**
	 * CURTAIN is the closing beat after the last blow (chunk 15 T14): Succession's yield, Liberation's collapse. The win
	 * is earned by then, so a death, a relog or an unload during it does not release the site; the beat settles it.
	 */
	public enum Phase { IDLE, PERFORMANCE, RAFTERS, CAROUSEL, DESCENT, CURTAIN, COMPLETE }
}
