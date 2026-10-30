package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.render.world.HeartNodeSpreadRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tells the heart-node spread a block changed where the client can see it, so the faces of that section are found
 * again. NeoForge fires no client-side event for a block change the server sends.
 */
@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {
	@Inject(method = "sendBlockUpdated", at = @At("HEAD"), remap = false)
	private void hemomancy$heartNodeSpreadBlockChange(BlockPos pos, BlockState oldState, BlockState newState, int flags,
			CallbackInfo ci) {
		if (oldState != newState) HeartNodeSpreadRenderer.invalidate(pos);
	}
}
