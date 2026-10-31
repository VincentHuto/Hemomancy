package com.vincenthuto.hemomancy.common.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * The status icon an equipped Morphling strain keeps up while worn. It carries no behaviour of its
 * own; the strain's item applies its powers and maturity sets the amplifier.
 */
public class MorphlingStrainEffect extends MobEffect {
	private final String displayKey;

	public MorphlingStrainEffect(MobEffectCategory category, int color, String displayKey) {
		super(category, color);
		this.displayKey = displayKey;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(this.displayKey);
	}
}
