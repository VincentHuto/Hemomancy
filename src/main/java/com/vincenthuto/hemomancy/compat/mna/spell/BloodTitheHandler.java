package com.vincenthuto.hemomancy.compat.mna.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.events.CalculatingManaCostEvent;
import com.mna.api.events.SpellCastEvent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.compat.mna.faction.HarbingerEventHandler;
import com.vincenthuto.hemomancy.config.HemoMnAConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Handles cross-system spell mechanics that bridge MnA and Hemomancy:
 * <ul>
 *   <li><b>Blood Tithe:</b> Harbinger faction members casting blood-affinity spells
 *       have a portion of their mana cost converted to blood cost instead.</li>
 *   <li><b>Spell → Manipulation Combos:</b> Casting blood-affinity MnA spells grants
 *       Arcane Resonance (reduces next manipulation blood cost). Using manipulations
 *       grants Sanguine Clarity (reduces next spell mana cost).</li>
 * </ul>
 * <p>
 * Registered on the Forge event bus when MnA is loaded.
 */
public class BloodTitheHandler {

	/**
	 * Listens to MnA's mana cost calculation event. If the caster is a Harbinger
	 * faction member and the spell contains Blood-affinity components, reduce the
	 * mana cost by the configured percentage. The "saved" mana will be drained as
	 * blood in {@link #onSpellCast}.
	 * <p>
	 * Also applies Sanguine Clarity cost reduction if the effect is active.
	 */
	@SubscribeEvent
	public static void onCalculateManaCost(CalculatingManaCostEvent event) {
		LivingEntity caster = event.getCaster();
		if (!(caster instanceof Player player)) return;

		float currentCost = event.getManaCost();

		// ─── Blood Tithe ───
		if (HemoMnAConfig.BLOOD_TITHE_ENABLED.get()) {
			IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
			if (progression != null && progression.getAlliedFaction() != null
					&& progression.getAlliedFaction().is(HarbingerEventHandler.FACTION_HARBINGERS_ID)) {

				ISpellDefinition spell = event.getSpell();
				if (spell.getAffinity() != null && spell.getAffinity().containsKey(Affinity.BLOOD)) {

					IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
					if (blood != null && blood.isActive()) {
						float reduction = (float) (currentCost * HemoMnAConfig.BLOOD_TITHE_MANA_REDUCTION.get());
						double bloodCost = reduction * HemoMnAConfig.BLOOD_TITHE_BLOOD_PER_MANA.get();

						// Only reduce if the player has enough blood to pay
						if (!blood.wouldOverstrain(bloodCost)) {
							currentCost -= reduction;
						}
					}
				}
			}
		}

		// ─── Sanguine Clarity (combo system) ───
		if (HemoMnAConfig.SPELL_MANIP_COMBO_ENABLED.get()
				&& player.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.sanguine_clarity)) {
			float clarityReduction = (float) (currentCost * HemoMnAConfig.SANGUINE_CLARITY_MANA_REDUCTION.get());
			currentCost -= clarityReduction;
		}

		event.setManaCost(Math.max(0, currentCost));
	}

	/**
	 * Listens to MnA's spell cast event. If Blood Tithe was active, drain the
	 * corresponding blood. Also grants the Arcane Resonance buff if the spell
	 * contained Blood affinity components (for the combo system).
	 */
	@SubscribeEvent
	public static void onSpellCast(SpellCastEvent event) {
		if (event.getSource() == null || !event.getSource().isPlayerCaster()) return;
		Player player = event.getSource().getPlayer();
		if (player == null) return;

		ISpellDefinition spell = event.getSpell();
		boolean hasBloodAffinity = spell.getAffinity() != null && spell.getAffinity().containsKey(Affinity.BLOOD);

		// ─── Blood Tithe: drain blood for the mana saved ───
		if (HemoMnAConfig.BLOOD_TITHE_ENABLED.get() && hasBloodAffinity) {
			IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
			if (progression != null && progression.getAlliedFaction() != null
					&& progression.getAlliedFaction().is(HarbingerEventHandler.FACTION_HARBINGERS_ID)) {

				IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
				if (blood != null && blood.isActive()) {
					float originalManaCost = spell.getManaCost();
					// Recalculate what the original cost would have been before our reduction
					float reductionRatio = HemoMnAConfig.BLOOD_TITHE_MANA_REDUCTION.get().floatValue();
					// originalManaCost is already reduced, so: reducedCost = original * (1 - ratio)
					// therefore original = reducedCost / (1 - ratio)
					float estimatedOriginal = originalManaCost / (1.0F - reductionRatio);
					float manaSaved = estimatedOriginal * reductionRatio;
					double bloodCost = manaSaved * HemoMnAConfig.BLOOD_TITHE_BLOOD_PER_MANA.get();

					if (!blood.wouldOverstrain(bloodCost)) {
						blood.drain(bloodCost);
					}
				}
			}
		}

		// ─── Spell → Manipulation Combo: grant Arcane Resonance ───
		if (HemoMnAConfig.SPELL_MANIP_COMBO_ENABLED.get() && hasBloodAffinity) {
			int duration = HemoMnAConfig.ARCANE_RESONANCE_DURATION.get();
			if (com.vincenthuto.hemomancy.common.init.EffectInit.arcane_resonance != null) {
				player.addEffect(new MobEffectInstance(
						com.vincenthuto.hemomancy.common.init.EffectInit.arcane_resonance,
						duration, 0, false, true));
			}
		}

		// ─── Consume Sanguine Clarity if it was active (combo used) ───
		if (HemoMnAConfig.SPELL_MANIP_COMBO_ENABLED.get()
				&& player.hasEffect(com.vincenthuto.hemomancy.common.init.EffectInit.sanguine_clarity)) {
			player.removeEffect(com.vincenthuto.hemomancy.common.init.EffectInit.sanguine_clarity);
		}
	}
}
