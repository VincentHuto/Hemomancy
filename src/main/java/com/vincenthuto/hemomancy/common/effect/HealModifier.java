package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodInjectionRules;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationStatusRules;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

/**
 * Every reduction to incoming healing, in the order {@code ManipulationStatusEvents.onLivingHeal} applies them at
 * NORMAL priority. The multipliers stack. Unstained restraint purity reads the requested amount at HIGH priority,
 * before this table.
 */
public enum HealModifier {
	/** Necrosis, unless Insatiable Hunger or Hemophagy already starve the heal. */
	NECROSIS(0.75F, entity -> entity.hasEffect(EffectInit.necrosis)
			&& !entity.hasEffect(EffectInit.insatiable_hunger) && !entity.hasEffect(EffectInit.hemophagy)),
	INSATIABLE_HUNGER(ManipulationStatusRules.INSATIABLE_HEAL_MULTIPLIER,
			entity -> entity.hasEffect(EffectInit.insatiable_hunger)),
	/** Hemorath's Empty Pulse; Insatiable Hunger replaces it. */
	HEMOPHAGY(0.25F, entity -> entity.hasEffect(EffectInit.hemophagy) && !entity.hasEffect(EffectInit.insatiable_hunger)),
	/** A blood injection's Impaired Recovery. */
	IMPAIRED_RECOVERY(BloodInjectionRules.HEALING_MULTIPLIER, entity -> entity.hasEffect(EffectInit.impaired_recovery));

	private final float multiplier;
	private final Predicate<LivingEntity> applies;

	HealModifier(float multiplier, Predicate<LivingEntity> applies) {
		this.multiplier = multiplier;
		this.applies = applies;
	}

	public float multiplier() {
		return multiplier;
	}

	public boolean appliesTo(LivingEntity entity) {
		return applies.test(entity);
	}
}
