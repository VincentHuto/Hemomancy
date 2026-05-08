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

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public final class BoundSummonBehavior {
	private static final String TAG_OWNER = "HemomancyOwner";
	private static final String TAG_CROSSBAR = "HemomancyCrossbar";
	private static final String TAG_SUMMON = "HemomancySummon";
	private static final String TAG_DISMISSAL_TICKS = "HemomancyDismissalTicks";

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
		double range = PuppeteerSummonRules.commandRange(SkillPointHelper.getFarTetherLevel());
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
		UUID ownerId = summon.hemomancy$getOwnerUUID();
		if (ownerId != null && ownerId.equals(target.getUUID())) {
			return false;
		}
		return !(target instanceof BoundPuppeteerSummon);
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
