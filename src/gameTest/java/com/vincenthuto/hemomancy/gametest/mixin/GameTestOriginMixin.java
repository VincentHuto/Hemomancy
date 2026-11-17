package com.vincenthuto.hemomancy.gametest.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestServer;
import com.vincenthuto.hemomancy.gametest.GameTestIsolation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Test-only: lays the run's grid out from a chunk-aligned corner (see {@link GameTestIsolation#origin}). */
@Mixin(value = GameTestServer.class, remap = false)
public class GameTestOriginMixin {
	@ModifyVariable(method = "startTests", at = @At("STORE"), ordinal = 0)
	private BlockPos hemomancy$alignedOrigin(BlockPos random) {
		return GameTestIsolation.origin(random);
	}
}
