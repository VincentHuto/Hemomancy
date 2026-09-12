package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Creeper.class)
public abstract class MixinCommandedCreeper implements HematicCommandManager.CommandedCreeper {
    @org.spongepowered.asm.mixin.Shadow(remap = false) private int swell;
    @org.spongepowered.asm.mixin.Shadow(remap = false) private int oldSwell;

    @Override
    public void hemomancy$cancelCommandedFuse() {
        swell = 0;
        oldSwell = 0;
        ((Creeper)(Object)this).setSwellDir(-1);
    }

    @ModifyArg(method = "spawnLingeringCloud", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), index = 0, remap = false)
    private Entity hemomancy$cloudOwnership(Entity cloud) {
        HematicCommandManager.copyAttackOwner((Entity)(Object)this, cloud);
        return cloud;
    }
}
