package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Blaze.class)
public interface BlazeChargeInvoker {
    @Invoker(value = "setCharged", remap = false)
    void hemomancy$setCharged(boolean charged);
}
