package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneClientEvents;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Places the camera for a boss scene's cinematic shot. The shot's angles already came through
 * {@code ViewportEvent.ComputeCameraAngles}; only the position, which NeoForge has no event for, is set here.
 */
@Mixin(Camera.class)
public abstract class MixinCamera {
	@Shadow(remap = false)
	protected abstract void setPosition(Vec3 pos);

	@Inject(method = "setup", at = @At("TAIL"), remap = false)
	private void hemomancy$bossSceneShot(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse,
			float partialTick, CallbackInfo ci) {
		Vec3 shot = BossSceneClientEvents.shotPosition(partialTick);
		if (shot != null) setPosition(shot);
	}
}
