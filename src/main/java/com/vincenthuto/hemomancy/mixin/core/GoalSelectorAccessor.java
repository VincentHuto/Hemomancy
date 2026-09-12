package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.EnumSet;

@Mixin(GoalSelector.class)
public interface GoalSelectorAccessor {
    @Accessor(value = "disabledFlags", remap = false)
    EnumSet<Goal.Flag> hemomancy$disabledFlags();
}
