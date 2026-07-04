package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public record ManipulationCastContext(
		LivingEntity caster,
		Level level,
		ItemStack heldItem,
		BlockPos origin,
		@Nullable LivingEntity target,
		Vec3 aim,
		boolean playerCast,
		boolean sendCasterMessages,
		double rangeScale,
		double damageScale) {
	public static ManipulationCastContext forPlayer(Player player, Level level, ItemStack heldItem, BlockPos origin) {
		return new ManipulationCastContext(player, level, safeStack(heldItem), origin, null,
				player.getViewVector(1.0F).normalize(), true, true, 1.0D, 1.0D);
	}

	public static ManipulationCastContext forWill(LivingEntity caster, @Nullable LivingEntity target, ItemStack heldItem) {
		return new ManipulationCastContext(caster, caster.level(), safeStack(heldItem), caster.blockPosition(), target,
				aimAt(caster, target), false, false, 1.0D, 1.0D);
	}

	public boolean hasTarget() {
		return target != null && target.isAlive();
	}

	public float scaleDamage(float damage) {
		return (float) (damage * damageScale);
	}

	public double scaleRange(double range) {
		return range * rangeScale;
	}

	@Nullable
	public Player casterAsPlayer() {
		return caster instanceof Player player ? player : null;
	}

	private static ItemStack safeStack(@Nullable ItemStack stack) {
		return stack == null ? ItemStack.EMPTY : stack;
	}

	private static Vec3 aimAt(LivingEntity caster, @Nullable LivingEntity target) {
		if (target != null && target.isAlive()) {
			Vec3 direction = target.getEyePosition().subtract(caster.getEyePosition());
			if (direction.lengthSqr() > 1.0E-5D) {
				return direction.normalize();
			}
		}
		Vec3 look = caster.getLookAngle();
		return look.lengthSqr() > 1.0E-5D ? look.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
	}
}
