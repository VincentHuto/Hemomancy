package com.vincenthuto.hemomancy.compat.mna.spell;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.config.HemoMnAConfig;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Helper class for the MnA Spell ↔ Manipulation combo system. Called from
 * {@link com.vincenthuto.hemomancy.common.manipulation.BloodManipulation#performAction}
 * when a manipulation is successfully used and MnA is loaded.
 * <p>
 * This is in the mna compat package so that it can safely reference MnA-gated
 * config values without causing class-loading issues when MnA is absent.
 */
public class ManipComboHelper {

	/**
	 * Called after a Hemomancy manipulation is successfully used.
	 * Grants Sanguine Clarity (next MnA spell costs less mana) and
	 * consumes Arcane Resonance if it was active (it already reduced
	 * this manipulation's blood cost via the cost calculation in performAction).
	 */
	public static void onManipulationUsed(Player player) {
		if (!HemoMnAConfig.SPELL_MANIP_COMBO_ENABLED.get()) return;

		// Grant Sanguine Clarity — reduces the mana cost of the next MnA spell
		int clarityDuration = HemoMnAConfig.SANGUINE_CLARITY_DURATION.get();
		player.addEffect(new MobEffectInstance(
				EffectInit.sanguine_clarity,
				clarityDuration, 0, false, true));

		// Consume Arcane Resonance if active (the cost reduction was already applied
		// in BloodManipulation.performAction via the effectiveCost calculation)
		if (player.hasEffect(EffectInit.arcane_resonance)) {
			player.removeEffect(EffectInit.arcane_resonance);
		}
	}
}
