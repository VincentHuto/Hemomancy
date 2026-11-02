package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

/**
 * How {@link PlaythroughClock#rite(PlaythroughPlayer, net.minecraft.core.BlockPos, RitePlan)} plays a Cardinal Rite.
 * Start from {@link #standard()} and change what the rite needs:
 *
 * <pre>{@code
 * PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().fightThreats()
 *         .onPhase(CardinalRitePhase.INSCRIPTION, (rite, ticks) -> { if (ticks == 0) p.interactOn(vicar); }))
 * }</pre>
 *
 * @param maxTicks       fail after this many ticks; 0 picks {@link PlaythroughRites#defaultTicks} from the recipe
 * @param extraAim       a target between the anchors and the daemon; null means {@link PlaythroughRites#supportSigilNodes}
 * @param plant          false takes up a rite already running (after a relog) instead of planting the held Staff
 * @param pauseWhen      once true (checked each tick while the rite runs) the step lets go and ends, rite still running
 * @param fightsThreats   while a wave is live the player attacks the nearest live {@code getRiteThreats()} entry each
 *                       tick (anchors needing blood are refilled first)
 * @param releasesOnStill let go of Projection during {@code STILL_INTERVAL} (the Covenant Vigil's still intervals) once
 *                     every anchor is whole again; the lull is for repairs
 * @param onPhase        a hook run each tick spent in a phase, given the rite and the ticks spent in that phase so far
 * @param attacksTrialPuppet    for a Puppeteer trial, attack the manifested trial body until it is defeated
 * @param weapon         wielded for attacks when the player carries it (null keeps whatever is in hand)
 * @param tracesFirst    during ORDEAL, a non-null {@code extraAim} (a demanded sigil) is projected at before the
 *                       anchors are refilled, as a player answers a covenant_pressure wave: its drain stops only once
 *                       the sigil is traced
 */
public record RitePlan(int maxTicks, @Nullable BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> extraAim,
		boolean plant, BooleanSupplier pauseWhen, boolean fightsThreats, boolean releasesOnStill,
		Map<CardinalRitePhase, BiConsumer<ActiveCardinalRite, Integer>> onPhase, boolean attacksTrialPuppet,
		@Nullable ItemLike weapon, boolean tracesFirst) {
	public RitePlan {
		onPhase = Map.copyOf(onPhase);
	}

	/** Plant the Staff, feed anchors, the support sigils, then the daemon; the recipe's default timeout. */
	public static RitePlan standard() {
		return new RitePlan(0, null, true, () -> false, false, false, Map.of(), false, null, false);
	}

	public RitePlan maxTicks(int ticks) {
		return new RitePlan(ticks, extraAim, plant, pauseWhen, fightsThreats, releasesOnStill, onPhase, attacksTrialPuppet, weapon, tracesFirst);
	}

	public RitePlan extraAim(BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> aim) {
		return new RitePlan(maxTicks, aim, plant, pauseWhen, fightsThreats, releasesOnStill, onPhase, attacksTrialPuppet, weapon, tracesFirst);
	}

	/** No extra target at all (not even the support sigils). */
	public RitePlan noExtraAim() {
		return extraAim((rite, recipe) -> null);
	}

	/** Takes up the player's running rite instead of planting the Staff. */
	public RitePlan takeUp() {
		return new RitePlan(maxTicks, extraAim, false, pauseWhen, fightsThreats, releasesOnStill, onPhase, attacksTrialPuppet, weapon, tracesFirst);
	}

	public RitePlan pauseWhen(BooleanSupplier when) {
		return new RitePlan(maxTicks, extraAim, plant, when, fightsThreats, releasesOnStill, onPhase, attacksTrialPuppet, weapon, tracesFirst);
	}

	public RitePlan fightThreats() {
		return new RitePlan(maxTicks, extraAim, plant, pauseWhen, true, releasesOnStill, onPhase, attacksTrialPuppet, weapon, tracesFirst);
	}

	public RitePlan releaseOnStill() {
		return new RitePlan(maxTicks, extraAim, plant, pauseWhen, fightsThreats, true, onPhase, attacksTrialPuppet, weapon, tracesFirst);
	}

	/** Adds (or replaces) the hook for {@code phase}. */
	public RitePlan onPhase(CardinalRitePhase phase, BiConsumer<ActiveCardinalRite, Integer> hook) {
		Map<CardinalRitePhase, BiConsumer<ActiveCardinalRite, Integer>> hooks = new EnumMap<>(CardinalRitePhase.class);
		hooks.putAll(onPhase);
		hooks.put(phase, hook);
		return new RitePlan(maxTicks, extraAim, plant, pauseWhen, fightsThreats, releasesOnStill, hooks, attacksTrialPuppet, weapon, tracesFirst);
	}

	public RitePlan trialPuppet() {
		return new RitePlan(maxTicks, extraAim, plant, pauseWhen, fightsThreats, releasesOnStill, onPhase, true, weapon, tracesFirst);
	}

	/** During ORDEAL, project at the extra aim (a demanded sigil) before refilling anchors. */
	public RitePlan traceFirst() {
		return new RitePlan(maxTicks, extraAim, plant, pauseWhen, fightsThreats, releasesOnStill, onPhase, attacksTrialPuppet, weapon, true);
	}

	public RitePlan weapon(ItemLike item) {
		return new RitePlan(maxTicks, extraAim, plant, pauseWhen, fightsThreats, releasesOnStill, onPhase, attacksTrialPuppet, item, tracesFirst);
	}
}
