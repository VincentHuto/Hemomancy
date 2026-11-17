package com.vincenthuto.hemomancy.gametest.mixin;

import net.minecraft.gametest.framework.StructureGridSpawner;
import com.vincenthuto.hemomancy.gametest.GameTestIsolation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Test-only: widens the gap between test structures so no test can reach its neighbour's blocks or entities. */
@Mixin(value = StructureGridSpawner.class, remap = false)
public class GameTestGridSpacingMixin {
	@ModifyConstant(method = "spawnStructure", constant = {@Constant(intValue = 5), @Constant(intValue = 6)}, require = 2)
	private int hemomancy$isolatingGap(int vanillaGap) {
		return GameTestIsolation.GRID_GAP;
	}
}
