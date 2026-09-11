package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class ManipulationCombatHelper {
	private ManipulationCombatHelper() {
	}

	public static boolean allied(Player player, LivingEntity target) {
		if (target == player || player.isAlliedTo(target) || target.isAlliedTo(player)) return true;
		java.util.UUID owner = target instanceof OwnableEntity ownable ? ownable.getOwnerUUID()
				: target instanceof BoundPuppeteerSummon bound ? bound.hemomancy$getOwnerUUID() : null;
		if (player.getUUID().equals(owner)) return true;
		Player ownerPlayer = owner == null ? null : player.level().getPlayerByUUID(owner);
		return ownerPlayer != null && (player.isAlliedTo(ownerPlayer) || ownerPlayer.isAlliedTo(player))
				|| player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
				&& HematicCommandManager.isImpressed(target, serverPlayer);
	}

	public static boolean canHarm(Player player, LivingEntity target) {
		if (!target.isAlive() || target.isSpectator() || allied(player, target)) return false;
		if (target instanceof Player other) {
			return !other.isCreative() && player.canHarmPlayer(other)
					&& (player.getServer() == null || player.getServer().isPvpAllowed());
		}
		return true;
	}

	public static Vec3 clipToGeometry(Entity caster, Vec3 end) {
		return caster.level().clip(new ClipContext(caster.getEyePosition(), end,
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
	}

	public static boolean safeLanding(Player player, net.minecraft.core.BlockPos pos) {
		Level world = player.level();
		if (!world.hasChunkAt(pos) || world.isOutsideBuildHeight(pos) || world.isOutsideBuildHeight(pos.above())
				|| !world.getWorldBorder().isWithinBounds(pos)) return false;
		Vec3 destination = Vec3.atBottomCenterOf(pos);
		var box = player.getBoundingBox().move(destination.subtract(player.position()));
		var floor = world.getBlockState(pos.below());
		return world.noCollision(player, box) && !world.containsAnyLiquid(box)
				&& floor.isFaceSturdy(world, pos.below(), net.minecraft.core.Direction.UP)
				&& !floor.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
				&& !floor.is(net.minecraft.world.level.block.Blocks.CAMPFIRE)
				&& !floor.is(net.minecraft.world.level.block.Blocks.SOUL_CAMPFIRE)
				&& !world.getBlockState(pos).is(net.minecraft.tags.BlockTags.FIRE);
	}

	public static boolean visible(Player player, LivingEntity target) {
		return player.level().clip(new ClipContext(player.getEyePosition(), target.getEyePosition(),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
	}

	public static List<LivingEntity> hostileTargets(Player player, Level world, double range) {
		return world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
				target -> canHarm(player, target));
	}

	public static LivingEntity aimedTarget(Player player, Level world, double range, double dot) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		return hostileTargets(player, world, range).stream().filter(target -> {
			Vec3 delta = target.getEyePosition().subtract(eye);
			return delta.lengthSqr() > 0.001D && look.dot(delta.normalize()) >= dot && visible(player, target);
		}).min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
	}

	public static boolean hostileProjectile(Player player, net.minecraft.world.entity.projectile.Projectile projectile) {
		Entity owner = projectile.getOwner();
		return owner != player && (!(owner instanceof LivingEntity living) || canHarm(player, living));
	}

	public static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
		Vec3 line = end.subtract(start);
		double t = Math.max(0, Math.min(1, point.subtract(start).dot(line) / Math.max(1.0E-12D, line.lengthSqr())));
		return point.distanceTo(start.add(line.scale(t)));
	}

	public static boolean hurt(BloodManipulation manipulation, Player player, LivingEntity target,
			ServerLevel level, float amount) {
		if (!canHarm(player, target)) return false;
		float adjusted = TendencyAffinityRules.adjustManipulationDamage(player, target, manipulation, amount);
		return ManipulationParticles.hurt(manipulation, target, level.damageSources().magic(), adjusted);
	}
}
