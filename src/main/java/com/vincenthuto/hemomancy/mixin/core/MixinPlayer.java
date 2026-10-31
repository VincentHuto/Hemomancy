package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
	@ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true, remap = false)
	private float hemomancy$scaleHungerExhaustion(float exhaustion) {
		return MixinHooks.scaleHungerExhaustion((Player) (Object) this, exhaustion);
	}

	@Inject(method = "isPushedByFluid", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$clingAgainstCurrents(CallbackInfoReturnable<Boolean> cir) {
		if (MixinHooks.resistsCurrents((Player) (Object) this)) cir.setReturnValue(false);
	}
}
