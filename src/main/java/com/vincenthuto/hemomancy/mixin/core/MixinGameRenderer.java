package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.world.fold.FoldedHallwayManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
	@Inject(method = "pick", at = @At("TAIL"), remap = false)
	private void hemomancy$suppressFoldedHallwayPicking(float partialTicks, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !FoldedHallwayManager.isActive(minecraft.player)) {
			return;
		}

		Vec3 eye = minecraft.player.getEyePosition(partialTicks);
		Vec3 look = minecraft.player.getViewVector(partialTicks);
		Vec3 missLocation = eye.add(look.scale(minecraft.player.blockInteractionRange()));
		Direction missDirection = Direction.getNearest(look.x, look.y, look.z);
		minecraft.hitResult = BlockHitResult.miss(missLocation, missDirection, BlockPos.containing(missLocation));
		minecraft.crosshairPickEntity = null;
	}
}
