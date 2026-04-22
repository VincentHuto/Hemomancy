package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Marked by Canon — a persistent debuff applied when a Saint's sarcophagus
 * rejects the player during extraction. While active:
 * - Future mausoleum extraction attempts have reduced success
 * - Movement speed is slightly reduced
 * - The mark stacks: each additional rejection increases amplifier
 */
public class MarkedByCanonEffect extends MobEffect {

	public MarkedByCanonEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
				net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "marked_by_canon_movement_speed"),
				-0.05F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		// Passive debuff — the main penalty is reduced extraction success
		// and the attribute modifier on movement speed.
		// At higher amplifiers, could apply periodic minor damage.
		if (amplifier >= 2 && entity.level().getGameTime() % 100 == 0) {
			entity.hurt(entity.damageSources().magic(), 1.0f);
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Marked by Canon");
	}

	@Override
	public boolean isBeneficial() {
		return false;
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return amplifier >= 2;
	}
}
