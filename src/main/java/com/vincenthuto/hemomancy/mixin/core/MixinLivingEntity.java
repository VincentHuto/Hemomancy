package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.MixinHooks;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBaghnakhItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBladeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleMode;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSpearItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.SanguisLanceaItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AnnettasSanguisLanceaItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AbsolutionDaggerItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.SilthmereGlaiveItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.UnstainedWarhammerItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
		int ticks = hemomancy$weaponSwingTicks(entity.getItemInHand(hand));
		if (ticks != 6) {
			// Keep vanilla haste/fatigue scaling and the same motion duration on every observer.
			callback.setReturnValue(Math.max(1, Math.round(callback.getReturnValueI() * (ticks / 6.0F))));
		}
	}

	/**
	 * Swing lengths for custom attack strokes, timed to each weapon's cooldown so the visible contact lands
	 * early (damage is applied on the click) and the recovery finishes about when the next attack is ready.
	 */
	@Unique
	private static int hemomancy$weaponSwingTicks(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof UnstainedWarhammerItem) return 22;
		if (item instanceof LivingAxeItem) return 20;
		if (item instanceof LivingFlailItem) return 18;
		if (item instanceof SilthmereGlaiveItem) return 16;
		if (item instanceof LivingSickleItem) return LivingSickleItem.mode(stack) == LivingSickleMode.BLOOD_HOOK ? 14 : 9;
		if (item instanceof SanguisLanceaItem || item instanceof AnnettasSanguisLanceaItem) return 12;
		if (item instanceof LivingBladeItem || item instanceof LivingSpearItem || item instanceof LivingTorchItem) return 10;
		if (item instanceof AbsolutionDaggerItem) return 9;
		if (item instanceof LivingBaghnakhItem) return 7;
		return 6;
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
