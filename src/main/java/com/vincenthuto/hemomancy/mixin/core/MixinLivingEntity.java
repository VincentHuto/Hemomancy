package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AbsolutionDaggerItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.SilthmereGlaiveItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.UnstainedWarhammerItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
	@Unique
	private boolean hemomancy$oldFallFlyingFlag;

	public MixinLivingEntity(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true, remap = false)
	private void hemomancy$weaponSwingDuration(CallbackInfoReturnable<Integer> callback) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (!(entity instanceof Player)) return;
		var hand = entity.swingingArm == null ? InteractionHand.MAIN_HAND : entity.swingingArm;
		var item = entity.getItemInHand(hand).getItem();
		int ticks = item instanceof LivingAxeItem || item instanceof UnstainedWarhammerItem ? 20
				: item instanceof SilthmereGlaiveItem ? 16 : item instanceof AbsolutionDaggerItem ? 8 : 6;
		if (ticks != 6) {
			// Keep vanilla haste/fatigue scaling and the same motion duration on every observer.
			callback.setReturnValue(Math.max(1, Math.round(callback.getReturnValueI() * (ticks / 6.0F))));
		}
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
