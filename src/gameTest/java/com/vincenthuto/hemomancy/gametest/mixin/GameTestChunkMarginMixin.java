package com.vincenthuto.hemomancy.gametest.mixin;

import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import com.vincenthuto.hemomancy.gametest.GameTestIsolation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Test-only: forces the chunks around each test structure as well as those under it. A forced chunk is
 * entity-ticking, but its neighbours only block-tick, so a mob a test spawns a few blocks past its template's edge
 * would otherwise stand frozen whenever that edge fell near a chunk border.
 */
@Mixin(value = StructureUtils.class, remap = false)
public class GameTestChunkMarginMixin {
	@ModifyVariable(method = "forceLoadChunks", at = @At("HEAD"), argsOnly = true)
	private static BoundingBox hemomancy$forceMargin(BoundingBox structure) {
		return GameTestIsolation.withMargin(structure);
	}
}
