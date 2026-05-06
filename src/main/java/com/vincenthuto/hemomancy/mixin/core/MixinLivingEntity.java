package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
	@Unique
	private boolean hemomancy$oldFallFlyingFlag;

	public MixinLivingEntity(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "updateFallFlying", at = @At("HEAD"), require = 0, remap = false)
	private void hemomancy$captureOldFallFlying(CallbackInfo ci) {
		this.hemomancy$oldFallFlyingFlag = this.getSharedFlag(7);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "updateFallFlying", at = @At("TAIL"), require = 0, remap = false)
	private void hemomancy$setFlag(CallbackInfo ci) {
		boolean oldFlag = this.hemomancy$oldFallFlyingFlag;
		boolean newFlag = this.getSharedFlag(7);
		boolean adjusted = MixinHooks.canFly((LivingEntity) (Object) this, oldFlag, newFlag);
		if (adjusted != newFlag) {
			this.setSharedFlag(7, adjusted);
		}
	}
}