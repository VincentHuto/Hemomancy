package com.vincenthuto.hemomancy.compat.mna;

import com.mna.api.capabilities.resource.SimpleCastingResource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class HarbingersMana extends SimpleCastingResource {
	public HarbingersMana() {
		// The number should be defined by the config
		super(2400);
	}

	public int getRegenerationRate(LivingEntity caster) {
		// The number should be defined by the config
		return (int) ((float) 2400 * this.getRegenerationModifier(caster));
	}

	public ResourceLocation getRegistryName() {
		return MnAPlugin.HARBINGERS_MANA;
	}

	public void setMaxAmountByLevel(int level) {
		this.setMaxAmount((float) (100 + 20 * level));
	}
}
