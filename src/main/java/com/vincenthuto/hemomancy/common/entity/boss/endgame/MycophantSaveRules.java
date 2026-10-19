package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * How a loaded Mycophant resumes. Saves from before the rebuild have no intro flag (they were mid-fight, so the intro
 * counts as played) and only cocoon keys, which are ignored: she always resumes idle with nothing held.
 */
public final class MycophantSaveRules {
	public record Resume(boolean introDone, boolean splitDone, int phase) {
	}

	private MycophantSaveRules() {
	}

	public static Resume resume(boolean enteredPhaseTwo, boolean hasIntroKey, boolean introDone, float health,
			float maxHealth) {
		boolean split = enteredPhaseTwo || MycophantCombatRules.phase(health, maxHealth) >= 2;
		return new Resume(!hasIntroKey || introDone, split, split ? 2 : 1);
	}
}
