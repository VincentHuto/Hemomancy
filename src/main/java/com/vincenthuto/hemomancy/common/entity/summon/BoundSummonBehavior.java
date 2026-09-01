package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphlingEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.ToggleablePlayerPowerRules;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillBendRules;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerCommandMode;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class BoundSummonBehavior {
	private static final String TAG_OWNER = "HemomancyOwner";
	private static final String TAG_CROSSBAR = "HemomancyCrossbar";
	private static final String TAG_SUMMON = "HemomancySummon";
	private static final String TAG_DISMISSAL_TICKS = "HemomancyDismissalTicks";
	private static final String TAG_TRIAL = "HemomancyTrial";
	private static final String TAG_TRIAL_CASTER = "HemomancyTrialCaster";
	private static final String TAG_NEXT_UPKEEP = "HemomancyNextUpkeepGameTime";
	private static final String TAG_OWNER_SESSION = "HemomancyOwnerSession";
	private static final String TAG_BOUND_AT = "HemomancyBoundAtGameTime";
	private static final String TAG_FOCUS_TARGET = "HemomancyFocusTarget";
	private static final String PLAYER_TAG_OWNER_SESSION = "HemomancyPuppeteerSession";
	private static final ResourceLocation HIGH_STRUNG_SPEED = Hemomancy.rloc("high_strung_speed");
	private static final ResourceLocation HIGH_STRUNG_FLIGHT = Hemomancy.rloc("high_strung_flight");

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
		if (summon instanceof Mob mob
				&& !PuppeteerSummonRules.shouldDespawnInPeaceful(
						summon.hemomancy$isTrialSummon(), summon.hemomancy$getOwnerUUID())) {
			mob.setPersistenceRequired();
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
		Optional<ServerPlayer> ownerOpt = ownerOnServer(mob, summon);
		if (ownerOpt.isEmpty()) {
			mob.discard();
			return false;
		}
		return commonServerTick(mob, summon, ownerOpt.get());
	}

	public static boolean commonServerTick(Mob mob, BoundPuppeteerSummon summon, ServerPlayer owner) {
		if (mob == null || summon == null || owner == null
				|| !owner.getUUID().equals(summon.hemomancy$getOwnerUUID())) {
			if (mob != null) {
				mob.discard();
			}
			return false;
		}
		if (PuppeteerSummonRules.shouldUnravelForDimension(owner.level() == mob.level())) {
			unravel(mob, owner, "hemomancy.summon.dimension.unravel");
			return false;
		}
		if (!ownerSessionMatches(mob, owner)) {
			unravel(mob, owner, "hemomancy.summon.session.unravel");
			return false;
		}
		if (mob.tickCount <= 1 && !reconcileLoadedActiveCap(mob, owner)) {
			return false;
		}
		UUID crossbarId = summon.hemomancy$getCrossbarUUID();
		if (crossbarId == null) {
			mob.discard();
			return false;
		}
		Optional<net.minecraft.world.item.ItemStack> crossbar =
				MarionetteCrossbarItem.findEquippedCrossbar(owner, crossbarId);
		if (crossbar.isEmpty()) {
			return tickMissingEquippedCrossbar(mob, summon, owner);
		}
		if (summon.hemomancy$getDismissalTicks() > 0) {
			summon.hemomancy$setDismissalTicks(0);
			mob.setNoAi(false);
		}
		long gameTime = mob.level().getGameTime();
		int highStrungLevel = SkillPointHelper.getHighStrungLevel(owner);
		updateHighStrungSpeed(mob, highStrungLevel);
		boolean morphlingInterference = hasEquippedMorphling(owner)
				&& qualifiesForMorphlingInterference(mob, summon, owner);
		CompoundTag persistentData = mob.getPersistentData();
		long nextUpkeep = persistentData.getLong(TAG_NEXT_UPKEEP);
		if (nextUpkeep <= 0L) {
			persistentData.putLong(TAG_NEXT_UPKEEP, PuppeteerSummonRules.nextUpkeepGameTime(gameTime));
		} else if (PuppeteerSummonRules.upkeepDue(gameTime, nextUpkeep)) {
			if (!payUpkeep(summon, owner, crossbar.get(), morphlingInterference, gameTime)) {
				unravel(mob, owner, "hemomancy.summon.upkeep.failed");
				return false;
			}
			persistentData.putLong(TAG_NEXT_UPKEEP, PuppeteerSummonRules.nextUpkeepGameTime(gameTime));
		}
		double range = PuppeteerSummonRules.effectiveCommandRange(SkillPointHelper.getFarTetherLevel(owner),
				SkillPointHelper.getBoundCommandLevel(owner), morphlingInterference);
		range = PuppeteerSummonRules.highStrungCommandRange(range, highStrungLevel);
		if (mob.distanceToSqr(owner) > range * range * 9.0) {
			mob.teleportTo(owner.getX(), owner.getY(), owner.getZ());
		}
		net.minecraft.world.item.ItemStack equippedCrossbar = crossbar.get();
		PuppeteerCommandMode mode = MarionetteCrossbarItem.getCommandMode(equippedCrossbar);
		if (mode == PuppeteerCommandMode.GUARD && !validGuardAnchor(owner, equippedCrossbar, range)) {
			MarionetteCrossbarItem.setCommandMode(equippedCrossbar, PuppeteerCommandMode.FOLLOW);
			MarionetteCrossbarItem.clearGuardAnchor(equippedCrossbar);
			mode = PuppeteerCommandMode.FOLLOW;
		}
		boolean focused = isFocusedTarget(mob, mob.getTarget());
		if (mob.getTarget() != null && ToggleablePlayerPowerRules.summonShouldSpare(
				SkillPointHelper.isTechniqueEnabled(owner, SkillPointInit.skill_merciful_command), focused,
				mob.getTarget().getHealth(), mob.getTarget().getMaxHealth())) {
			mob.setTarget(null);
			mob.getNavigation().stop();
		}
		if (mob.getTarget() == null || !mob.getTarget().isAlive() || !canAttack(mob, summon, mob.getTarget())
				|| !PuppeteerSummonRules.withinTetherRange(owner.distanceToSqr(mob.getTarget()), range)) {
			mob.setTarget(null);
			mob.getPersistentData().remove(TAG_FOCUS_TARGET);
			mob.getNavigation().stop();
			focused = false;
		}
		if (!focused && mob.getTarget() != null && (!mode.retainsAutomaticTarget()
				|| mode == PuppeteerCommandMode.GUARD
				&& !withinGuardAnchor(equippedCrossbar, mob.getTarget(), range))) {
			mob.setTarget(null);
			mob.getNavigation().stop();
		}
		if (mob.getTarget() == null) {
			Optional<LivingEntity> target = switch (mode) {
				case FOLLOW -> mode.automaticallyDefendsOwner()
						? findRetaliationTarget(mob, summon, owner, range) : Optional.empty();
				case GUARD -> findGuardTarget(mob, summon, owner, equippedCrossbar, range);
				case HUNT -> findTarget(mob, summon, owner, range);
				case PASSIVE -> Optional.empty();
			};
			target.ifPresent(mob::setTarget);
		}
		if (mode == PuppeteerCommandMode.GUARD && mob.getTarget() == null) {
			MarionetteCrossbarItem.getGuardPosition(equippedCrossbar).ifPresent(anchor ->
					mob.getNavigation().moveTo(anchor.getX() + 0.5, anchor.getY(), anchor.getZ() + 0.5, 1.0));
		}
		return true;
	}

	public static void setFocusedTarget(Mob mob, LivingEntity target) {
		if (mob != null && target != null) {
			mob.getPersistentData().putUUID(TAG_FOCUS_TARGET, target.getUUID());
			mob.setTarget(target);
		}
	}

	public static boolean shouldFollowOwner(ServerPlayer owner, BoundPuppeteerSummon summon) {
		if (owner == null || summon == null || summon.hemomancy$getCrossbarUUID() == null) return true;
		return MarionetteCrossbarItem.findEquippedCrossbar(owner, summon.hemomancy$getCrossbarUUID())
				.map(stack -> MarionetteCrossbarItem.getCommandMode(stack) != PuppeteerCommandMode.GUARD)
				.orElse(true);
	}

	private static boolean isFocusedTarget(Mob mob, LivingEntity target) {
		return target != null && mob.getPersistentData().hasUUID(TAG_FOCUS_TARGET)
				&& target.getUUID().equals(mob.getPersistentData().getUUID(TAG_FOCUS_TARGET));
	}

	private static boolean validGuardAnchor(ServerPlayer owner, net.minecraft.world.item.ItemStack crossbar,
			double range) {
		return MarionetteCrossbarItem.getGuardDimension(crossbar).filter(owner.level().dimension()::equals).isPresent()
				&& MarionetteCrossbarItem.getGuardPosition(crossbar)
						.filter(pos -> pos.distToCenterSqr(owner.position()) <= range * range).isPresent();
	}

	private static boolean withinGuardAnchor(net.minecraft.world.item.ItemStack crossbar,
			LivingEntity target, double range) {
		return MarionetteCrossbarItem.getGuardPosition(crossbar)
				.filter(pos -> pos.distToCenterSqr(target.position()) <= range * range).isPresent();
	}

	private static Optional<LivingEntity> findRetaliationTarget(Mob mob, BoundPuppeteerSummon summon,
			ServerPlayer owner, double range) {
		List<LivingEntity> candidates = new ArrayList<>();
		if (owner.getLastHurtByMob() != null) candidates.add(owner.getLastHurtByMob());
		if (SkillPointHelper.isTechniqueEnabled(owner, SkillPointInit.skill_autonomous_retaliation)) {
			for (Mob body : MarionetteCrossbarItem.activeSummonsForCrossbar(owner,
					summon.hemomancy$getCrossbarUUID(), null)) {
				if (body.getLastHurtByMob() != null) candidates.add(body.getLastHurtByMob());
			}
		}
		return candidates.stream().filter(target -> canAttack(mob, summon, target))
				.filter(target -> PuppeteerSummonRules.withinTetherRange(owner.distanceToSqr(target), range))
				.min(Comparator.comparingDouble(mob::distanceToSqr));
	}

	private static Optional<LivingEntity> findGuardTarget(Mob mob, BoundPuppeteerSummon summon,
			ServerPlayer owner, net.minecraft.world.item.ItemStack crossbar, double range) {
		return MarionetteCrossbarItem.getGuardPosition(crossbar).flatMap(anchor ->
				mob.level().getEntitiesOfClass(Mob.class, new AABB(anchor).inflate(range),
						target -> target instanceof Enemy && canAttack(mob, summon, target)
								&& PuppeteerSummonRules.withinTetherRange(owner.distanceToSqr(target), range))
						.stream().min(Comparator.comparingDouble(mob::distanceToSqr)).map(LivingEntity.class::cast));
	}

	public static void bindOwnerSession(Mob mob, ServerPlayer owner) {
		if (mob == null || owner == null) {
			return;
		}
		CompoundTag data = mob.getPersistentData();
		data.putUUID(TAG_OWNER_SESSION, currentOwnerSession(owner));
		if (!data.contains(TAG_BOUND_AT)) {
			data.putLong(TAG_BOUND_AT, Math.max(1L, owner.level().getGameTime()));
		}
	}

	public static void rotateOwnerSession(ServerPlayer owner) {
		if (owner != null) {
			owner.getPersistentData().putUUID(PLAYER_TAG_OWNER_SESSION, UUID.randomUUID());
		}
	}

	private static UUID currentOwnerSession(ServerPlayer owner) {
		CompoundTag data = owner.getPersistentData();
		if (!data.hasUUID(PLAYER_TAG_OWNER_SESSION)) {
			data.putUUID(PLAYER_TAG_OWNER_SESSION, UUID.randomUUID());
		}
		return data.getUUID(PLAYER_TAG_OWNER_SESSION);
	}

	private static boolean ownerSessionMatches(Mob mob, ServerPlayer owner) {
		CompoundTag data = mob.getPersistentData();
		UUID current = currentOwnerSession(owner);
		if (!data.hasUUID(TAG_OWNER_SESSION)) {
			// One-time migration for bodies saved before session-bound tethers existed.
			data.putUUID(TAG_OWNER_SESSION, current);
			if (!data.contains(TAG_BOUND_AT)) {
				data.putLong(TAG_BOUND_AT, Math.max(1L, mob.level().getGameTime()));
			}
			return true;
		}
		return current.equals(data.getUUID(TAG_OWNER_SESSION));
	}

	public static int claimedWillBonusCap(Player owner) {
		boolean silentArchon = FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE.equals(
				owner.getPersistentData().getString(FungalGardenTravelHelper.ARCHON_CHOICE_KEY));
		if (!silentArchon) {
			return 0;
		}
		int configuredBonus = HemoServerConfig.WILL_CLAIMED_BONUS_CAP_SILENT_ARCHON == null
				? WillBendRules.silentArchonBonusCap()
				: HemoServerConfig.WILL_CLAIMED_BONUS_CAP_SILENT_ARCHON.get();
		return Math.max(0, configuredBonus);
	}

	public static int totalActiveCap(ServerPlayer owner) {
		return PuppeteerSummonRules.activeSummonCap(SkillPointHelper.getPuppetSkeinLevel(owner))
				+ claimedWillBonusCap(owner);
	}

	public static boolean isClaimedWill(Mob mob) {
		return mob instanceof BoundPuppeteerSummon bound
				&& "claimed_will".equals(bound.hemomancy$getSummonName());
	}

	public static boolean hasEquippedMorphling(Player owner) {
		return owner != null && HemoCapabilityAccess.getEquippedMorphling(owner)
				.map(cap -> cap.hasMorphling()).orElse(false);
	}

	public static boolean hasActiveOwnedTether(ServerPlayer owner) {
		return owner != null && MarionetteCrossbarItem.activeSummonsForOwner(owner).stream()
				.anyMatch(mob -> mob instanceof BoundPuppeteerSummon summon
						&& qualifiesForMorphlingInterference(mob, summon, owner));
	}

	public static boolean qualifiesForMorphlingInterference(Mob mob, BoundPuppeteerSummon summon,
			ServerPlayer owner) {
		return mob != null && summon != null && owner != null
				&& PuppeteerSummonRules.qualifiesForMorphlingInterference(
						mob.isAlive(), !mob.isRemoved(),
						owner.getUUID().equals(summon.hemomancy$getOwnerUUID())
								&& summon.hemomancy$getCrossbarUUID() != null,
						summon.hemomancy$isTrialSummon(), owner.level() == mob.level(),
						ownerSessionMatches(mob, owner));
	}

	private static boolean reconcileLoadedActiveCap(Mob candidate, ServerPlayer owner) {
		int baseCap = PuppeteerSummonRules.activeSummonCap(SkillPointHelper.getPuppetSkeinLevel(owner));
		int claimedBonusCap = claimedWillBonusCap(owner);
		List<Mob> loaded = new ArrayList<>(MarionetteCrossbarItem.activeSummonsForOwner(owner));
		loaded.removeIf(mob -> mob.level() != owner.level() || !ownerSessionMatches(mob, owner));
		loaded.sort(Comparator
				.comparingLong(BoundSummonBehavior::boundAtGameTime)
				.thenComparing(Mob::getUUID));

		int keptTotal = 0;
		int keptShapedBodies = 0;
		boolean candidateKept = false;
		List<Mob> overflow = new ArrayList<>();
		for (Mob loadedBody : loaded) {
			boolean claimedWill = isClaimedWill(loadedBody);
			boolean keep = PuppeteerSummonRules.canRetainBody(claimedWill, keptTotal, keptShapedBodies,
					baseCap, claimedBonusCap);
			if (keep) {
				keptTotal++;
				if (!claimedWill) {
					keptShapedBodies++;
				}
				candidateKept |= loadedBody == candidate;
			} else {
				overflow.add(loadedBody);
			}
		}
		for (Mob overflowBody : overflow) {
			unravel(overflowBody, owner, "hemomancy.summon.cap.unravel");
		}
		if (!candidateKept && !candidate.isRemoved()) {
			unravel(candidate, owner, "hemomancy.summon.cap.unravel");
		}
		return candidateKept && !candidate.isRemoved();
	}

	private static long boundAtGameTime(Mob mob) {
		long boundAt = mob.getPersistentData().getLong(TAG_BOUND_AT);
		return boundAt <= 0L ? Long.MAX_VALUE : boundAt;
	}

	private static Optional<ServerPlayer> ownerOnServer(Mob mob, BoundPuppeteerSummon summon) {
		UUID ownerId = summon.hemomancy$getOwnerUUID();
		if (ownerId == null || mob.level().getServer() == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(mob.level().getServer().getPlayerList().getPlayer(ownerId));
	}

	private static boolean payUpkeep(BoundPuppeteerSummon summon, ServerPlayer owner,
			net.minecraft.world.item.ItemStack crossbar, boolean morphlingInterference, long gameTime) {
		int fallbackUpkeep = "claimed_will".equals(summon.hemomancy$getSummonName())
				? PuppeteerSummonRules.CLAIMED_WILL_UPKEEP_PER_MINUTE
				: 1;
		int baseUpkeep = PuppeteerSummonDefinitions.byName(summon.hemomancy$getSummonName())
				.map(PuppeteerSummonDefinition::threadUpkeepPerMinute)
				.orElse(fallbackUpkeep);
		int upkeep = PuppeteerSummonRules.adjustedThreadCost(baseUpkeep,
				SkillPointHelper.getThreadEconomyLevel(owner));
		upkeep = PuppeteerSummonRules.interferedThreadUpkeep(upkeep, morphlingInterference);
		int highStrungLevel = SkillPointHelper.getHighStrungLevel(owner);
		upkeep = PuppeteerSummonRules.highStrungThreadUpkeep(upkeep, highStrungLevel);
		double bloodUpkeep = PuppeteerSummonRules.highStrungBloodUpkeep(highStrungLevel);
		IBloodVolume volume = bloodUpkeep > 0.0D ? HemoCapabilityAccess.getBloodVolume(owner).orElse(null) : null;
		if (MarionetteCrossbarItem.getThread(crossbar) < upkeep
				|| bloodUpkeep > 0.0D && (volume == null || !volume.isActive()
				|| volume.getBloodVolume() < bloodUpkeep)) {
			return false;
		}
		if (!MarionetteCrossbarItem.consumeThread(crossbar, upkeep)) {
			return false;
		}
		if (bloodUpkeep > 0.0D) {
			volume.drain(bloodUpkeep);
			BloodVolumeEvents.syncVolume(owner, volume);
		}
		if (morphlingInterference) {
			HemoCapabilityAccess.getEquippedMorphling(owner).filter(cap -> cap.hasMorphling()).ifPresent(cap -> {
				MorphlingItem.markFedNow(cap.getEquippedMorphling(), gameTime);
				EquippedMorphlingEvents.syncToClient(owner);
			});
		}
		return true;
	}

	private static void updateHighStrungSpeed(Mob mob, int level) {
		double amount = PuppeteerSummonRules.highStrungSpeedMultiplier(level) - 1.0D;
		updateModifier(mob.getAttribute(Attributes.MOVEMENT_SPEED), HIGH_STRUNG_SPEED, amount);
		updateModifier(mob.getAttribute(Attributes.FLYING_SPEED), HIGH_STRUNG_FLIGHT, amount);
	}

	private static void updateModifier(AttributeInstance attribute, ResourceLocation id, double amount) {
		if (attribute == null) return;
		AttributeModifier current = attribute.getModifier(id);
		if (current != null && (amount == 0.0D || current.amount() != amount)) {
			attribute.removeModifier(id);
			current = null;
		}
		if (amount > 0.0D && current == null) {
			attribute.addTransientModifier(new AttributeModifier(id, amount,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}

	private static void unravel(Mob mob, ServerPlayer owner, String messageKey) {
		if (mob.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
					mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(),
					18, 0.25, 0.35, 0.25, 0.02);
		}
		mob.playSound(SoundEvents.CHAIN_BREAK, 0.5F, 0.8F);
		owner.displayClientMessage(net.minecraft.network.chat.Component.translatable(messageKey,
				mob.getDisplayName()).withStyle(net.minecraft.ChatFormatting.GRAY), true);
		mob.discard();
	}

	private static boolean tickMissingEquippedCrossbar(Mob mob, BoundPuppeteerSummon summon, Player owner) {
		int remaining = summon.hemomancy$getDismissalTicks();
		if (remaining <= 0) {
			remaining = PuppeteerSummonRules.dismissalGraceTicks(SkillPointHelper.getBoundCommandLevel(owner));
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
		if (target instanceof BoundPuppeteerSummon boundTarget
				&& !boundTarget.hemomancy$isTrialSummon()
				&& boundTarget.hemomancy$getOwnerUUID() != null) {
			return false;
		}
		return target instanceof Enemy;
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

	private static Optional<LivingEntity> findTarget(Mob mob, BoundPuppeteerSummon summon, Player owner,
			double range) {
		AABB search = mob.getBoundingBox().inflate(range);
		return mob.level().getEntitiesOfClass(Mob.class, search,
					target -> target instanceof Enemy
							&& canAttack(mob, summon, target)
							&& PuppeteerSummonRules.withinTetherRange(owner.distanceToSqr(target), range))
				.stream()
				.min(Comparator.comparingDouble(mob::distanceToSqr))
				.map(LivingEntity.class::cast);
	}
}
