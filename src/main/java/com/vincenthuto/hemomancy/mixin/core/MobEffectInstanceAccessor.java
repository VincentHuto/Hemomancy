package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MobEffectInstance.class)
public interface MobEffectInstanceAccessor {
	@Accessor("duration")
	void hemomancy$setDuration(int duration);
}
