package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Optional;

@Mixin(value = Brain.class, remap = false)
public abstract class MixinCommandedBrain {
    @Unique private Mob hemomancy$brainHolder;

    @Inject(method = "tick", at = @At("HEAD"))
    private void hemomancy$followOrder(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        hemomancy$brainHolder = entity instanceof Mob mob ? mob : null;
        if (hemomancy$brainHolder != null) HematicCommandManager.maintainBrainOrder(hemomancy$brainHolder);
    }

    @Inject(method = "setMemoryInternal", at = @At("HEAD"), cancellable = true)
    private void hemomancy$reserveOrder(MemoryModuleType<?> type,
            Optional<? extends ExpirableValue<?>> memory, CallbackInfo ci) {
        if (hemomancy$brainHolder != null && !HematicCommandManager.mayChangeBrainMemory(
                hemomancy$brainHolder, type, memory.map(ExpirableValue::getValue).orElse(null))) ci.cancel();
    }
}
