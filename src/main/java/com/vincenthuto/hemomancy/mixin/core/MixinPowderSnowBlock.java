package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public abstract class MixinPowderSnowBlock {
	@Inject(method = "canEntityWalkOnPowderSnow", at = @At("RETURN"), cancellable = true, remap = false)
	private static void hemomancy$allowWinterShroudTraversal(Entity entity,
			CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(MixinHooks.canWalkOnPowderSnow(entity, cir.getReturnValue()));
	}
}
