package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * A Saint's phases, chosen by health fraction (falling through each phase's {@code at}), by meter fraction (rising
 * through it), or by the Saint itself (Putriciel's rot and cleansing). A change waits for the controller's recovery
 * boundary unless it is cut in immediately; {@link #advance} forces a phase that the source cannot take back. A
 * {@link #ratchet} table never goes back down. A phase change sets the bar's colour and name and plays its card once.
 */
public final class SaintPhaseTable {
	public enum Source { HEALTH, METER, SAINT }

	/** The card's length, during which the presence pauses. */
	public static final int CARD_TICKS = 50;

	/**
	 * @param number  1 upward
	 * @param nameKey the bar's title key in this phase
	 * @param at      HEALTH: entered at or below this health fraction; METER: at or above this meter fraction
	 * @param card    a {@code BossScenes} scene played once on entry, or null
	 */
	public record Phase(int number, String nameKey, BossEvent.BossBarColor color, float at,
			@Nullable ResourceLocation card) {
	}

	private final Source source;
	private final List<Phase> phases;
	@Nullable
	private final ToIntFunction<SaintTrialEntity> bySaint;
	private boolean ratchet;
	private int phase;
	private int pending;
	private SaintCombatController.Cut pendingCut = SaintCombatController.Cut.QUEUE;
	private int forcedFloor;
	private long cardsPlayed;
	private int cardTicks;
	private int ticksInPhase;

	private SaintPhaseTable(Source source, @Nullable ToIntFunction<SaintTrialEntity> bySaint, Phase... phases) {
		if (phases.length == 0) throw new IllegalArgumentException("a phase table needs a phase");
		this.source = source;
		this.bySaint = bySaint;
		this.phases = Arrays.stream(phases).sorted(Comparator.comparingInt(Phase::number)).toList();
		this.phase = this.phases.get(0).number();
	}

	public static SaintPhaseTable byHealth(Phase... phases) {
		return new SaintPhaseTable(Source.HEALTH, null, phases);
	}

	public static SaintPhaseTable byMeter(Phase... phases) {
		return new SaintPhaseTable(Source.METER, null, phases);
	}

	public static SaintPhaseTable bySaint(ToIntFunction<SaintTrialEntity> phase, Phase... phases) {
		return new SaintPhaseTable(Source.SAINT, phase, phases);
	}

	/** One phase that never changes: what a legacy Saint carries. */
	public static SaintPhaseTable single(String nameKey, BossEvent.BossBarColor color) {
		return byHealth(new Phase(1, nameKey, color, 1.0F, null));
	}

	/** A phase once reached is kept (Seraphae). */
	public SaintPhaseTable ratchet() {
		ratchet = true;
		return this;
	}

	public Source source() {
		return source;
	}

	public List<Phase> phases() {
		return phases;
	}

	public int phase() {
		return phase;
	}

	public Phase current() {
		return phase(phase);
	}

	public Phase phase(int number) {
		for (Phase candidate : phases) if (candidate.number() == number) return candidate;
		return phases.get(0);
	}

	/** True while a change waits for the recovery boundary. */
	public boolean pending() {
		return pending != 0 && pending != phase;
	}

	/** Ticks left of the last phase card; the presence pauses while it plays. */
	public int cardTicks() {
		return cardTicks;
	}

	public int ticksInPhase() {
		return ticksInPhase;
	}

	/** The phase the source asks for from a health fraction, a meter fraction or the Saint's own number. */
	public int target(float health, float meter, int saintPhase) {
		int wanted = phases.get(0).number();
		switch (source) {
			case HEALTH -> {
				for (Phase candidate : phases) if (health <= candidate.at()) wanted = candidate.number();
			}
			case METER -> {
				for (Phase candidate : phases) if (meter >= candidate.at()) wanted = candidate.number();
			}
			case SAINT -> wanted = saintPhase;
		}
		wanted = Math.max(wanted, forcedFloor);
		if (ratchet) wanted = Math.max(wanted, phase);
		return wanted;
	}

	/** One kit tick: asks the source, queues a change and applies it at the controller's boundary. */
	public void tick(SaintTrialEntity saint, SaintCombatController controller) {
		// an immediate advance (Hemorath's first full meter) cuts the active move: the one path besides the ending
		if (pendingCut == SaintCombatController.Cut.IMMEDIATE && pending()) controller.cancel();
		int saintPhase = bySaint == null ? phase : bySaint.applyAsInt(saint);
		float health = saint.getMaxHealth() <= 0.0F ? 1.0F : saint.getHealth() / saint.getMaxHealth();
		int wanted = target(health, saint.meter().fraction(), saintPhase);
		int from = phase;
		if (update(wanted, controller.interruptible())) changed(saint, from);
	}

	/**
	 * The pure step: records {@code wanted} as pending and applies it when {@code atBoundary} or when an immediate cut
	 * is pending. Returns true when the phase changed this call.
	 */
	public boolean update(int wanted, boolean atBoundary) {
		if (cardTicks > 0) cardTicks--;
		ticksInPhase++;
		if (wanted != phase) {
			pending = wanted;
		} else if (pending != 0 && pending != phase && pendingCut == SaintCombatController.Cut.QUEUE) {
			pending = 0;
		}
		if (!pending()) return false;
		if (!atBoundary && pendingCut != SaintCombatController.Cut.IMMEDIATE) return false;
		phase = pending;
		pending = 0;
		pendingCut = SaintCombatController.Cut.QUEUE;
		ticksInPhase = 0;
		return true;
	}

	/**
	 * Forces {@code number} (Hemorath's first full meter): the source cannot take it back. {@code IMMEDIATE} cancels
	 * the active move now; {@code QUEUE} waits for the boundary.
	 */
	public void advance(int number, SaintCombatController.Cut cut) {
		forcedFloor = Math.max(forcedFloor, number);
		pending = Math.max(number, phase);
		pendingCut = cut;
	}

	/** Whether {@code number}'s card has been played; marks it played. */
	public boolean playCardOnce(int number) {
		long bit = 1L << Math.min(63, Math.max(0, number));
		if ((cardsPlayed & bit) != 0) return false;
		cardsPlayed |= bit;
		cardTicks = CARD_TICKS;
		return true;
	}

	private void changed(SaintTrialEntity saint, int from) {
		Phase entered = current();
		boolean card = entered.card() != null && playCardOnce(entered.number());
		saint.phaseEntered(from, entered, card);
	}

	public void save(CompoundTag tag) {
		CompoundTag table = new CompoundTag();
		table.putInt("phase", phase);
		table.putInt("pending", pending);
		table.putString("cut", pendingCut.name());
		table.putInt("forced", forcedFloor);
		table.putLong("cards", cardsPlayed);
		table.putInt("card_ticks", cardTicks);
		table.putInt("in_phase", ticksInPhase);
		tag.put("SaintPhases", table);
	}

	public void load(CompoundTag tag) {
		CompoundTag table = tag.getCompound("SaintPhases");
		if (table.contains("phase")) phase = table.getInt("phase");
		pending = table.getInt("pending");
		pendingCut = "IMMEDIATE".equals(table.getString("cut")) ? SaintCombatController.Cut.IMMEDIATE
				: SaintCombatController.Cut.QUEUE;
		forcedFloor = table.getInt("forced");
		cardsPlayed = table.getLong("cards");
		cardTicks = table.getInt("card_ticks");
		ticksInPhase = table.getInt("in_phase");
	}
}
