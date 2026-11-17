package com.vincenthuto.hemomancy.gametest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import com.vincenthuto.hemomancy.gametest.GameTestIsolation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Test-only: a test starts only once its margin chunks (see {@link GameTestChunkMarginMixin}) are entity-ticking too. */
@Mixin(value = GameTestInfo.class, remap = false)
public class GameTestChunkWaitMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/gametest/framework/StructureUtils;getStructureBoundingBox(Lnet/minecraft/world/level/block/entity/StructureBlockEntity;)Lnet/minecraft/world/level/levelgen/structure/BoundingBox;"))
	private BoundingBox hemomancy$waitForMargin(BoundingBox structure) {
		return GameTestIsolation.withMargin(structure);
	}
}
