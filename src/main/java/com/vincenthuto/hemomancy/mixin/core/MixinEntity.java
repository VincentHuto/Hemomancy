package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.world.fold.FoldedHallwayCollision;
import com.vincenthuto.hemomancy.common.world.fold.FoldedHallwayManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public class MixinEntity {
	@Inject(method = "move", at = @At("HEAD"), remap = false)
	private void hemomancy$applySilentSlippingNoClip(MoverType type, Vec3 movement, CallbackInfo ci) {
		if ((Object) this instanceof Player player) {
			SilentArchonArmorAbilityHandler.applySilentSlippingNoClip(player);
		}
	}

	@ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
	private Vec3 hemomancy$compressFoldedHallwayMovement(Vec3 movement) {
		if ((Object) this instanceof Player player) {
			return FoldedHallwayManager.compressMovement(player, movement);
		}
		return movement;
	}

	@Inject(method = "move", at = @At("TAIL"), remap = false)
	private void hemomancy$finishFoldedHallwayMovement(MoverType type, Vec3 movement, CallbackInfo ci) {
		if ((Object) this instanceof Player player) {
			FoldedHallwayManager.finishMove(player);
		}
	}

	@Redirect(
			method = "collide",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntityCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"),
			remap = false)
	private List<VoxelShape> hemomancy$suppressFoldedHallwayEntityCollisions(Level level, Entity entity,
			AABB collisionBox) {
		if (FoldedHallwayCollision.overridesCollisions(entity)) {
			return List.of();
		}
		return level.getEntityCollisions(entity, collisionBox);
	}

	@Redirect(
			method = "collectColliders",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/lang/Iterable;"),
			remap = false)
	private static Iterable<VoxelShape> hemomancy$replaceFoldedHallwayBlockCollisions(Level level, Entity entity,
			AABB boundingBox) {
		if (entity instanceof Player player && FoldedHallwayCollision.overridesCollisions(player)) {
			return FoldedHallwayCollision.blockCollisions(player, boundingBox);
		}
		return level.getBlockCollisions(entity, boundingBox);
	}
}
