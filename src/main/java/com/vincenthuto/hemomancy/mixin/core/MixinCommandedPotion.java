package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownPotion.class)
public abstract class MixinCommandedPotion {
    @Redirect(method = "applySplash", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"), remap = false)
    private boolean hemomancy$safeSplash(LivingEntity target, MobEffectInstance effect, Entity source) {
        Entity potion = (Entity)(Object)this;
        return (effect.getEffect().value().isBeneficial() || HematicCommandManager.mayAffect(potion, target))
                && target.addEffect(effect, source);
    }

    @ModifyArg(method = "makeAreaOfEffectCloud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), index = 0, remap = false)
    private Entity hemomancy$cloudOwnership(Entity cloud) {
        HematicCommandManager.copyAttackOwner((Entity)(Object)this, cloud);
        return cloud;
    }
}
