package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public final class BoundSummonBehavior {
	private static final String TAG_OWNER = "HemomancyOwner";
	private static final String TAG_CROSSBAR = "HemomancyCrossbar";
	private static final String TAG_SUMMON = "HemomancySummon";
	private static final String TAG_DISMISSAL_TICKS = "HemomancyDismissalTicks";
	private static final String TAG_TRIAL = "HemomancyTrial";
	private static final String TAG_TRIAL_CASTER = "HemomancyTrialCaster";

	private BoundSummonBehavior() {
	}

	public static void save(BoundPuppeteerSummon summon, CompoundTag tag) {
		UUID owner = summon.hemomancy$getOwnerUUID();
		UUID crossbar = summon.hemomancy$getCrossbarUUID();
		if (owner != null) {
			tag.putUUID(TAG_OWNER, owner);
		}
		if (crossbar != null) {
			tag.putUUID(TAG_CROSSBAR, crossbar);
		}
		String summonName = summon.hemomancy$getSummonName();
		if (summonName != null && !summonName.isBlank()) {
			tag.putString(TAG_SUMMON, summonName);
		}
		if (summon.hemomancy$getDismissalTicks() > 0) {
			tag.putInt(TAG_DISMISSAL_TICKS, summon.hemomancy$getDismissalTicks());
		}
		if (summon.hemomancy$isTrialSummon()) {
			tag.putBoolean(TAG_TRIAL, true);
		}
		UUID trialCaster = summon.hemomancy$getTrialCasterUUID();
		if (trialCaster != null) {
			tag.putUUID(TAG_TRIAL_CASTER, trialCaster);
		}
	}

	public static void load(BoundPuppeteerSummon summon, CompoundTag tag) {
		if (tag.hasUUID(TAG_OWNER)) {
			summon.hemomancy$setOwnerUUID(tag.getUUID(TAG_OWNER));
		}
		if (tag.hasUUID(TAG_CROSSBAR)) {
			summon.hemomancy$setCrossbarUUID(tag.getUUID(TAG_CROSSBAR));
		}
		if (tag.contains(TAG_SUMMON)) {
			summon.hemomancy$setSummonName(tag.getString(TAG_SUMMON));
		}
		if (tag.contains(TAG_DISMISSAL_TICKS)) {
			summon.hemomancy$setDismissalTicks(tag.getInt(TAG_DISMISSAL_TICKS));
		}
		summon.hemomancy$setTrialSummon(tag.getBoolean(TAG_TRIAL));
		if (tag.hasUUID(TAG_TRIAL_CASTER)) {
			summon.hemomancy$setTrialCasterUUID(tag.getUUID(TAG_TRIAL_CASTER));
		}
	}

	public static Optional<Player> ownerFor(Mob mob, BoundPuppeteerSummon summon) {
		UUID ownerId = summon.hemomancy$getOwnerUUID();
		if (ownerId == null || !(mob.level() instanceof ServerLevel serverLevel)) {
			return Optional.empty();
		}
		return Optional.ofNullable(serverLevel.getPlayerByUUID(ownerId));
	}

	public static void applyStats(Mob mob, PuppeteerSummonDefinition definition, int livingSinewLevel) {
		if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
			mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(
					definition.baseHealth() * PuppeteerSummonRules.healthMultiplier(livingSinewLevel));
			mob.setHealth(mob.getMaxHealth());
		}
		if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
			mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
					definition.baseDamage() * PuppeteerSummonRules.damageMultiplier(livingSinewLevel));
		}
		if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
			mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(definition.movementSpeed());
		}
		if (mob.getAttribute(Attributes.FLYING_SPEED) != null) {
			mob.getAttribute(Attributes.FLYING_SPEED).setBaseValue(definition.movementSpeed() * 1.2);
		}
	}

	public static void applyTrialStats(Mob mob, PuppeteerSummonDefinition definition) {
		if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
			mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(definition.baseHealth() * 1.5);
			mob.setHealth(mob.getMaxHealth());
		}
		if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
			mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(definition.baseDamage() * 1.25);
		}
		if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
			mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(definition.movementSpeed());
		}
		if (mob.getAttribute(Attributes.FLYING_SPEED) != null) {
			mob.getAttribute(Attributes.FLYING_SPEED).setBaseValue(definition.movementSpeed() * 1.2);
		}
	}

	public static void trialServerTick(Mob mob, BoundPuppeteerSummon summon) {
		if (mob.getTarget() != null && mob.getTarget().isAlive() && canAttack(mob, summon, mob.getTarget())) {
			return;
		}
		mob.level().getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(24.0),
						player -> player.isAlive() && !player.isSpectator() && !player.isCreative())
				.stream()
				.min(Comparator.comparingDouble(mob::distanceToSqr))
				.ifPresent(mob::setTarget);
	}

	public static boolean commonServerTick(Mob mob, BoundPuppeteerSummon summon) {
		Optional<Player> ownerOpt = ownerFor(mob, summon);
		if (ownerOpt.isEmpty()) {
			mob.discard();
			return false;
		}
		Player owner = ownerOpt.get();
		UUID crossbarId = summon.hemomancy$getCrossbarUUID();
		if (crossbarId == null) {
			mob.discard();
			return false;
		}
		if (MarionetteCrossbarItem.findEquippedCrossbar(owner, crossbarId).isEmpty()) {
			return tickMissingEquippedCrossbar(mob, summon);
		}
		if (summon.hemomancy$getDismissalTicks() > 0) {
			summon.hemomancy$setDismissalTicks(0);
			mob.setNoAi(false);
		}
		double range = PuppeteerSummonRules.commandRange(SkillPointHelper.getFarTetherLevel(owner));
		if (mob.distanceToSqr(owner) > range * range * 9.0) {
			mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
		}
		if (mob.getTarget() == null || !mob.getTarget().isAlive() || !canAttack(mob, summon, mob.getTarget())) {
			findTarget(mob, summon, range).ifPresent(mob::setTarget);
		}
		return true;
	}

	private static boolean tickMissingEquippedCrossbar(Mob mob, BoundPuppeteerSummon summon) {
		int remaining = summon.hemomancy$getDismissalTicks();
		if (remaining <= 0) {
			remaining = PuppeteerSummonRules.CROSSBAR_DISMISSAL_TICKS;
		}
		if (remaining <= 1) {
			mob.discard();
			return false;
		}
		summon.hemomancy$setDismissalTicks(remaining - 1);
		mob.setTarget(null);
		mob.getNavigation().stop();
		mob.setNoAi(true);
		return false;
	}

	public static boolean canAttack(Mob mob, BoundPuppeteerSummon summon, LivingEntity target) {
		if (target == null || !target.isAlive() || target == mob) {
			return false;
		}
		if (summon.hemomancy$isTrialSummon()) {
			return target instanceof Player player && !player.isSpectator() && !player.isCreative();
		}
		UUID ownerId = summon.hemomancy$getOwnerUUID();
		if (ownerId != null && ownerId.equals(target.getUUID())) {
			return false;
		}
		return !(target instanceof BoundPuppeteerSummon);
	}

	public static void followFlyingOwner(Mob mob, Player owner, double speed, double teleportDistance) {
		Vec3 anchor = owner.position().add(0.0, owner.getBbHeight() + 0.6, 0.0);
		Vec3 delta = anchor.subtract(mob.position());
		if (delta.lengthSqr() > 0.01) {
			mob.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, speed);
			mob.setDeltaMovement(mob.getDeltaMovement().scale(0.86).add(delta.normalize().scale(0.055)));
		}
		if (mob.distanceToSqr(owner) > teleportDistance * teleportDistance && mob.tickCount % 20 == 0) {
			mob.teleportTo(owner.getX(), owner.getY() + 1.2, owner.getZ());
			mob.setDeltaMovement(Vec3.ZERO);
		}
	}

	private static Optional<LivingEntity> findTarget(Mob mob, BoundPuppeteerSummon summon, double range) {
		AABB search = mob.getBoundingBox().inflate(range);
		return mob.level().getEntitiesOfClass(Monster.class, search,
						target -> canAttack(mob, summon, target))
				.stream()
				.min(Comparator.comparingDouble(mob::distanceToSqr))
				.map(LivingEntity.class::cast);
	}
}
