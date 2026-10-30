package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.entity.mob.facsimile.FacsimileBlazeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** A facsimile blaze smoulders crimson: its body's smoke becomes the hive's embers. */
@Mixin(Blaze.class)
public abstract class MixinFacsimileBlazeEmbers {
	@ModifyArg(method = "aiStep",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"),
			index = 0, remap = false)
	private ParticleOptions hemomancy$crimsonEmbers(ParticleOptions vanilla) {
		return (Object) this instanceof FacsimileBlazeEntity blaze ? blaze.smoulder() : vanilla;
	}
}
