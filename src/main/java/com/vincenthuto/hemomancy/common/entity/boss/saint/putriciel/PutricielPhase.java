package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

import javax.annotation.Nullable;

/**
 * Putriciel's phases, chosen by the fire cycle and not by health (design section 3). The numbers are the kit's
 * {@code SaintPhaseTable} numbers and what {@code SaintMoveInputs.phase} carries to the client. Each phase holds its
 * own pace: the decision delay (resolution B-7), the Kindling's creep (D-6), the presence radius and gain, the
 * shamble's step and the turn rate.
 */
public enum PutricielPhase {
	ROT_I(1, "boss.hemomancy.putriciel.phase.befouled", BossEvent.BossBarColor.GREEN, null, 20, 12, 7.0D, 1.0F, 5.0F, 16),
	INTERVAL(2, "boss.hemomancy.putriciel.phase.purifier", BossEvent.BossBarColor.PINK,
			BossPresentationIds.SCENE_PUTRICIEL_PURIFIER_CARD, 10, 0, 4.0D, 0.0F, 10.0F, 14),
	ROT_II(3, "boss.hemomancy.putriciel.phase.rot_returns", BossEvent.BossBarColor.GREEN,
			BossPresentationIds.SCENE_PUTRICIEL_ROT_RETURNS_CARD, 16, 20, 8.0D, 1.5F, 6.0F, 16),
	ROT_III(4, "boss.hemomancy.putriciel.phase.blight", BossEvent.BossBarColor.YELLOW, null, 12, 15, 9.0D, 2.0F, 8.0F, 12);

	/** The wind-up the rot moves lose in Rot III, never under the kind's floor. */
	public static final int ROT_III_WINDUP_CUT = 3;

	private final int number;
	private final String nameKey;
	private final BossEvent.BossBarColor color;
	@Nullable
	private final ResourceLocation card;
	private final int decisionDelay;
	private final int creepTicks;
	private final double presenceRadius;
	private final float colonisationGain;
	private final float turnRate;
	private final int stepCycle;

	PutricielPhase(int number, String nameKey, BossEvent.BossBarColor color, @Nullable ResourceLocation card,
			int decisionDelay, int creepTicks, double presenceRadius, float colonisationGain, float turnRate, int stepCycle) {
		this.number = number;
		this.nameKey = nameKey;
		this.color = color;
		this.card = card;
		this.decisionDelay = decisionDelay;
		this.creepTicks = creepTicks;
		this.presenceRadius = presenceRadius;
		this.colonisationGain = colonisationGain;
		this.turnRate = turnRate;
		this.stepCycle = stepCycle;
	}

	public int number() {
		return number;
	}

	public String nameKey() {
		return nameKey;
	}

	public BossEvent.BossBarColor color() {
		return color;
	}

	@Nullable
	public ResourceLocation card() {
		return card;
	}

	/** Ticks between a move's end and the next pick: 20, 16, 12 in the rot, 10 in the fire. */
	public int decisionDelay() {
		return decisionDelay;
	}

	/** Ticks per point of the Kindling's natural creep; 0 in the interval, which burns it down instead. */
	public int creepTicks() {
		return creepTicks;
	}

	/** The rot presence's horizontal radius; in the interval, the purging heat's. */
	public double presenceRadius() {
		return presenceRadius;
	}

	/** Colonisation each rot presence pulse adds. */
	public float colonisationGain() {
		return colonisationGain;
	}

	/** Degrees a tick it turns. */
	public float turnRate() {
		return turnRate;
	}

	/** The shamble's (or the stride's) step cycle in ticks. */
	public int stepCycle() {
		return stepCycle;
	}

	public boolean rot() {
		return this != INTERVAL;
	}

	/** The rot phase that follows {@code intervalsEnded} ended intervals: I before any, II after one, III after two. */
	public static PutricielPhase rotAfter(int intervalsEnded) {
		if (intervalsEnded <= 0) return ROT_I;
		return intervalsEnded == 1 ? ROT_II : ROT_III;
	}

	public static PutricielPhase byNumber(int number) {
		for (PutricielPhase phase : values()) if (phase.number == number) return phase;
		return ROT_I;
	}

	public static boolean isRot(int number) {
		return byNumber(number).rot();
	}

	/** The kit phase row: health and meter thresholds are unused, the Saint names the phase. */
	public SaintPhaseTable.Phase row() {
		return new SaintPhaseTable.Phase(number, nameKey, color, 1.0F, card);
	}

	public static SaintPhaseTable.Phase[] rows() {
		PutricielPhase[] all = values();
		SaintPhaseTable.Phase[] rows = new SaintPhaseTable.Phase[all.length];
		for (int i = 0; i < all.length; i++) rows[i] = all[i].row();
		return rows;
	}
}
