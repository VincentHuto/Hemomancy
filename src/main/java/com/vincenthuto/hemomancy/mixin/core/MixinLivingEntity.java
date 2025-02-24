package com.vincenthuto.hemomancy.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.mixin.util.MixinHooks;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@SuppressWarnings("ConstantConditions")
	@ModifyArg(at = @At(value = "INVOKE", target = "net/minecraft/world/entity/LivingEntity.setSharedFlag(IZ)V"), method = "updateFallFlying")
	private boolean hemomancy$setFlag(boolean value) {
		return MixinHooks.canFly((LivingEntity) (Object) this, this.getSharedFlag(7), value);
	}
}