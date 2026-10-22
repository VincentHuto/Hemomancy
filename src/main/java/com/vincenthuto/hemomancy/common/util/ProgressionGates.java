package com.vincenthuto.hemomancy.common.util;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import net.minecraft.world.entity.player.Player;

/**
 * The player's place on the two progression paths, asked one way. Harbinger degree and Unstained stage both run 0-8;
 * recipe and rite requirements build on these in {@code RecipeDegreeGates}. Compare a degree through here rather than
 * against a literal, so a gate reads as "has reached D3" instead of {@code > 2} or {@code >= 3}.
 */
public final class ProgressionGates {
	private ProgressionGates() {
	}

	/** Whether the player has reached Harbinger degree {@code degree} or beyond. */
	public static boolean hasDegree(Player player, int degree) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) >= degree;
	}

	/** Whether the player stands at exactly Harbinger degree {@code degree}. */
	public static boolean isDegree(Player player, int degree) {
		return HemoCapabilityAccess.getPlayerDegreeNumber(player) == degree;
	}

	/** {@link #hasDegree(Player, int)} for degree state already in hand. */
	public static boolean hasDegree(IInitiatoryDegree state, int degree) {
		return state.getDegreeNumber() >= degree;
	}

	/** {@link #isDegree(Player, int)} for degree state already in hand. */
	public static boolean isDegree(IInitiatoryDegree state, int degree) {
		return state.getDegreeNumber() == degree;
	}

	/** Whether the player has opted into blood magic. */
	public static boolean isBloodActive(Player player) {
		return HemoCapabilityAccess.getBloodVolume(player).isActive();
	}

	/**
	 * The player's Unstained stage, 0-8, which gates Unstained rites the way degree gates Harbinger ones.
	 *
	 * <pre>
	 *  0 - not begun purification
	 *  1 - begun, purity &lt; 25 %  (Corrupted stage, path started)
	 *  2 - Tainted    (purity &ge; 25 %)
	 *  3 - Cleansing  (purity &ge; 50 %)
	 *  4 - Absolved   (purity &ge; 75 %)
	 *  5 - Purified   (purity &ge; 100 %)
	 *  6 - Discerning (clarity &ge; 25 %)
	 *  7 - Vigilant   (clarity &ge; 50 %)
	 *  8 - Enlightened(clarity &ge; 100 %)
	 * </pre>
	 */
	public static int unstainedStage(Player player) {
		return unstainedStage(HemoCapabilityAccess.getUnstainedProgress(player));
	}

	/** {@link #unstainedStage(Player)} for progress already in hand. */
	public static int unstainedStage(UnstainedProgress progress) {
		if (progress.hasClarityUnlocked()) {
			float clarity = progress.getClarity();
			if (clarity >= 100f) return 8;
			if (clarity >= 50f) return 7;
			if (clarity >= 25f) return 6;
			return 5;
		}
		if (!progress.hasBegunPurification()) return 0;
		if (!progress.isPurified()) {
			float purity = progress.getPurity();
			if (purity < 25f) return 1;
			if (purity < 50f) return 2;
			if (purity < 75f) return 3;
			return 4;
		}
		return 5;
	}
}
