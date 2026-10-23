package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import com.vincenthuto.hemomancy.common.entity.summon.BloodConstructEntity;
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
		return canHarm(player, target, Reach.STRICT);
	}

	/**
	 * How far an effect reaches past {@link #canHarm(Player, LivingEntity)}. {@link #STRICT} is canHarm itself. The
	 * wider presets reproduce sites written before step 9 flag for flag. Moving a site onto STRICT is a pending
	 * decision in the consolidation ledger: G-2b for the caster's own puppets in Monster filters, F-16 for the area
	 * damage from armour, Still Arts, Sheolic and Saprovitta. Other wider sites (the morphling look target, the
	 * Verdigris mark) would need their own decision.
	 *
	 * @param allies     also reach the caster's allies: itself excepted, its pets, teammates, its own and allied
	 *                   puppets, and mobs it has impressed
	 * @param anyPlayer  reach other players whatever PvP and team friendly fire say
	 * @param creative   reach players in creative mode
	 * @param spectators reach spectators
	 * @param dying      reach bodies already at zero health
	 */
	public record Reach(boolean allies, boolean anyPlayer, boolean creative, boolean spectators, boolean dying) {
		public static final Reach STRICT = new Reach(false, false, false, false, false);
		/** A {@code Monster.class} gather filtered on {@code isAlive}: every monster, the caster's own puppets too. */
		public static final Reach MONSTER_FILTER = new Reach(true, true, true, true, false);
		/** A {@code Monster.class} gather with no predicate, which also returns dying bodies. */
		public static final Reach MONSTER_FILTER_ANY_STATE = new Reach(true, true, true, true, true);
		/** Every living body but the caster: pets, allies, puppets, and players whatever PvP says. */
		public static final Reach UNGATED = new Reach(true, true, true, true, false);
		/** As {@link #UNGATED}, but creative and spectating players are spared. */
		public static final Reach UNGATED_SURVIVAL = new Reach(true, true, false, false, false);

		boolean relational() {
			return !allies || !anyPlayer;
		}
	}

	/**
	 * Whether {@code caster}'s effect may reach {@code target}. Only a player caster can be held to allies or PvP; a
	 * Drudge or a scar's non-player wearer uses a preset that checks neither.
	 */
	public static boolean canHarm(LivingEntity caster, LivingEntity target, Reach reach) {
		if (target == caster || !reach.dying() && !target.isAlive() || !reach.spectators() && target.isSpectator()) return false;
		if (target instanceof Player other && !reach.creative() && other.isCreative()) return false;
		if (!reach.relational()) return true;
		if (!(caster instanceof Player player)) {
			throw new IllegalArgumentException("only a player caster can be held to allies or PvP: " + reach);
		}
		if (target instanceof Player other && !reach.anyPlayer() && !(player.canHarmPlayer(other)
				&& (player.getServer() == null || player.getServer().isPvpAllowed()))) return false;
		return reach.allies() || !allied(player, target);
	}

	/**
	 * Whether something acting for {@code source} (a construct, summon, projectile owner or effect holder) may harm
	 * {@code target}. When a player controls the source, that player's {@link #canHarm(Player, LivingEntity)} decides
	 * and Blood Constructs are spared. Otherwise the source's own team and, for a mob, its target or vanilla
	 * {@code canAttack} decide.
	 */
	public static boolean canHarmFor(LivingEntity source, LivingEntity target) {
		if (target instanceof BloodConstructEntity || target == source || !target.isAlive() || target.isSpectator()) return false;
		LivingEntity controller = controller(source);
		if (controller instanceof Player player) return canHarm(player, target);
		if (target instanceof Player player && player.isCreative()) return false;
		if (source.isAlliedTo(target) || target.isAlliedTo(source)) return false;
		return !(source instanceof net.minecraft.world.entity.Mob mob) || mob.getTarget() == target || mob.canAttack(target);
	}

	private static LivingEntity controller(LivingEntity entity) {
		if (entity instanceof BloodConstructEntity construct && construct.getCreator() != null) return construct.getCreator();
		java.util.UUID id = entity instanceof OwnableEntity ownable ? ownable.getOwnerUUID()
				: entity instanceof BoundPuppeteerSummon bound ? bound.hemomancy$getOwnerUUID() : null;
		if (id != null && entity.level() instanceof ServerLevel level && level.getEntity(id) instanceof Player player) return player;
		return entity;
	}

	/**
	 * Auto-targeting only picks fights: monsters, mobs already hunting the caster, and players the caster may harm.
	 * Passive animals, pets and villagers stay out of a halo's or aneurysm's sights.
	 */
	public static boolean isHostileTarget(Player caster, LivingEntity e) {
		return canHarm(caster, e) && (e instanceof net.minecraft.world.entity.monster.Enemy
				|| e instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == caster
				|| e instanceof Player);
	}

	/** Whether a manipulation's caster may place or remove a block here, as with their own hands. */
	public static boolean mayAlter(Player p, net.minecraft.core.BlockPos pos) {
		return p.mayBuild() && p.level().mayInteract(p, pos);
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
        return hurt(manipulation, player, target, level, amount, 0, 1);
    }

    public static boolean hurt(BloodManipulation manipulation, Player player, LivingEntity target,
            ServerLevel level, float amount, int duration, int levels) {
        if (!canHarm(player, target)) return false;
		return ManipulationParticles.hurt(manipulation, target,
                com.vincenthuto.hemomancy.common.damage.SchoolDamage.attributed(level.damageSources().magic(),
                        com.vincenthuto.hemomancy.common.damage.SchoolDamage.context(manipulation, player).withApplication(duration, levels), player), amount);
	}
}
