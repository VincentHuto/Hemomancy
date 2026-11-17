package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StemBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The fruit a stem grows, which vanilla keeps private: the cloche harvests a stem's fruit (core-loop Track 7). */
@Mixin(StemBlock.class)
public interface StemBlockAccessor {
	@Accessor(value = "fruit", remap = false)
	ResourceKey<Block> hemomancy$fruit();
}
