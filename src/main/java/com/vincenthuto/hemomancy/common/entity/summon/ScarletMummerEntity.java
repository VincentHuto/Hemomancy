package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.client.particle.data.SerpentParticleData;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ScarletMummerEntity extends GroundPuppetEntity implements BoundPuppeteerSummon {
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CROSSBAR_UUID =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> DATA_SUMMON_NAME =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_DISMISSAL_TICKS =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_TRIAL_SUMMON =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRIAL_CASTER_UUID =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Integer> DATA_PERFORMANCE_TICKS =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_EVADE_AVAILABLE =
			SynchedEntityData.defineId(ScarletMummerEntity.class, EntityDataSerializers.BOOLEAN);

	private final Set<UUID> redirectedTargets = new HashSet<>();

	public ScarletMummerEntity(EntityType<? extends Zombie> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Zombie.createAttributes()
				.add(Attributes.MAX_HEALTH, 24.0)
				.add(Attributes.ATTACK_DAMAGE, 3.0)
				.add(Attributes.MOVEMENT_SPEED, 0.30);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new HighStrungMeleeAttackGoal(this, 1.0, false));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_OWNER_UUID, Optional.empty());
		builder.define(DATA_CROSSBAR_UUID, Optional.empty());
		builder.define(DATA_SUMMON_NAME, "scarlet_mummer");
		builder.define(DATA_DISMISSAL_TICKS, 0);
		builder.define(DATA_TRIAL_SUMMON, false);
		builder.define(DATA_TRIAL_CASTER_UUID, Optional.empty());
		builder.define(DATA_PERFORMANCE_TICKS, 0);
		builder.define(DATA_EVADE_AVAILABLE, false);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		if (hemomancy$isTrialSummon()) {
			BoundSummonBehavior.trialServerTick(this, this);
			tickTrialPerformance();
			return;
		}
		if (!BoundSummonBehavior.commonServerTick(this, this)) return;
		Optional<Player> owner = BoundSummonBehavior.ownerFor(this, this);
		if (owner.orElse(null) instanceof ServerPlayer serverOwner) {
			if (getTarget() == null && BoundSummonBehavior.shouldFollowOwner(serverOwner, this)
					&& distanceToSqr(owner.get()) > 25.0D) {
				getNavigation().moveTo(owner.get(), 1.0D);
			}
			tickPerformance(serverOwner);
		}
	}

	private void tickPerformance(ServerPlayer owner) {
		if (isPerforming()) {
			entityData.set(DATA_PERFORMANCE_TICKS, entityData.get(DATA_PERFORMANCE_TICKS) - 1);
			if (!isPerforming()) releaseAttention();
			return;
		}
		if (tickCount % ScarletMummerRules.PERFORMANCE_INTERVAL_TICKS != 0) return;

		List<Mob> threats = level().getEntitiesOfClass(Mob.class,
				getBoundingBox().inflate(ScarletMummerRules.ATTENTION_RADIUS), mob -> mayRedirect(mob, owner))
				.stream()
				.sorted((left, right) -> Double.compare(distanceToSqr(left), distanceToSqr(right)))
				.limit(ScarletMummerRules.MAX_ATTENTION_TARGETS)
				.toList();
		if (threats.isEmpty()) return;
		startPerformance();
		for (Mob threat : threats) {
			redirectedTargets.add(threat.getUUID());
			threat.setTarget(this);
		}
	}

	private boolean mayRedirect(Mob mob, ServerPlayer owner) {
		if (distanceToSqr(mob) > ScarletMummerRules.ATTENTION_RADIUS * ScarletMummerRules.ATTENTION_RADIUS) return false;
		LivingEntity target = mob.getTarget();
		boolean alliedAttacker = mob instanceof BoundPuppeteerSummon ally
				&& owner.getUUID().equals(ally.hemomancy$getOwnerUUID());
		boolean attacksOwner = target != null && target.getUUID().equals(owner.getUUID());
		boolean attacksAlly = target instanceof BoundPuppeteerSummon ally
				&& owner.getUUID().equals(ally.hemomancy$getOwnerUUID());
		return ScarletMummerRules.mayRedirect(!alliedAttacker && (attacksOwner || attacksAlly),
				mob.getType().is(EntityInit.PUPPET_ATTENTION_IMMUNE), mob.canAttack(this));
	}

	private void tickTrialPerformance() {
		if (isPerforming()) {
			entityData.set(DATA_PERFORMANCE_TICKS, entityData.get(DATA_PERFORMANCE_TICKS) - 1);
			return;
		}
		if (tickCount % ScarletMummerRules.PERFORMANCE_INTERVAL_TICKS == 0 && getTarget() != null) {
			startPerformance();
		}
	}

	private void startPerformance() {
		entityData.set(DATA_PERFORMANCE_TICKS, ScarletMummerRules.PERFORMANCE_DURATION_TICKS);
		entityData.set(DATA_EVADE_AVAILABLE, true);
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(new SerpentParticleData(176, 8, 28), getX(), getY() + 1.4D, getZ(),
					18, 0.45D, 0.7D, 0.45D, 0.01D);
			serverLevel.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
					SoundSource.HOSTILE, 0.7F, 1.25F);
		}
	}

	private void releaseAttention() {
		if (level() instanceof ServerLevel serverLevel) {
			for (UUID targetId : redirectedTargets) {
				Entity entity = serverLevel.getEntity(targetId);
				if (entity instanceof Mob mob && mob.getTarget() == this) mob.setTarget(null);
			}
		}
		redirectedTargets.clear();
		entityData.set(DATA_EVADE_AVAILABLE, false);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		Entity attacker = source.getEntity();
		boolean alliedAttacker = attacker instanceof BoundPuppeteerSummon ally
				&& hemomancy$getOwnerUUID() != null
				&& hemomancy$getOwnerUUID().equals(ally.hemomancy$getOwnerUUID());
		boolean directMelee = attacker instanceof LivingEntity
				&& !(attacker instanceof Player)
				&& !alliedAttacker
				&& !attacker.getType().is(EntityInit.PUPPET_ATTENTION_IMMUNE)
				&& source.getDirectEntity() == attacker
				&& !source.is(DamageTypeTags.IS_PROJECTILE)
				&& !source.is(DamageTypeTags.IS_EXPLOSION)
				&& !source.is(DamageTypeTags.WITCH_RESISTANT_TO);
		Vec3 sidestep = directMelee ? findSafeSidestep(attacker.position()) : null;
		if (ScarletMummerRules.mayEvade(isPerforming(), entityData.get(DATA_EVADE_AVAILABLE),
				directMelee, sidestep != null)) {
			Vec3 old = position();
			setPos(sidestep);
			getNavigation().stop();
			entityData.set(DATA_EVADE_AVAILABLE, false);
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(new SerpentParticleData(242, 190, 20), old.x, old.y + 1.0D, old.z,
						10, 0.2D, 0.5D, 0.2D, 0.02D);
			}
			return false;
		}
		return super.hurt(source, amount);
	}

	private Vec3 findSafeSidestep(Vec3 attacker) {
		Vec3 direction = position().subtract(attacker).multiply(1.0D, 0.0D, 1.0D);
		if (direction.lengthSqr() < 0.001D) direction = new Vec3(1.0D, 0.0D, 0.0D);
		direction = direction.normalize();
		Vec3 lateral = new Vec3(-direction.z, 0.0D, direction.x)
				.scale(ScarletMummerRules.EVADE_DISTANCE);
		Vec3 first = position().add(lateral);
		if (isSafe(first)) return first;
		Vec3 second = position().subtract(lateral);
		return isSafe(second) ? second : null;
	}

	private boolean isSafe(Vec3 destination) {
		Vec3 movement = destination.subtract(position());
		BlockPos floor = BlockPos.containing(destination.x, destination.y - 1.0D, destination.z);
		return level().noCollision(this, getBoundingBox().move(movement))
				&& level().getBlockState(floor).isFaceSturdy(level(), floor, Direction.UP);
	}

	public boolean isPerforming() {
		return entityData.get(DATA_PERFORMANCE_TICKS) > 0;
	}

	@Override
	public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
		return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return PuppeteerSummonRules.shouldDespawnInPeaceful(hemomancy$isTrialSummon(), hemomancy$getOwnerUUID());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		BoundSummonBehavior.save(this, tag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		BoundSummonBehavior.load(this, tag);
	}

	@Override public UUID hemomancy$getOwnerUUID() { return entityData.get(DATA_OWNER_UUID).orElse(null); }
	@Override public void hemomancy$setOwnerUUID(UUID ownerUuid) { entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUuid)); }
	@Override public UUID hemomancy$getCrossbarUUID() { return entityData.get(DATA_CROSSBAR_UUID).orElse(null); }
	@Override public void hemomancy$setCrossbarUUID(UUID crossbarUuid) { entityData.set(DATA_CROSSBAR_UUID, Optional.ofNullable(crossbarUuid)); }
	@Override public String hemomancy$getSummonName() { return entityData.get(DATA_SUMMON_NAME); }
	@Override public void hemomancy$setSummonName(String summonName) { entityData.set(DATA_SUMMON_NAME, summonName == null ? "" : summonName); }
	@Override public int hemomancy$getDismissalTicks() { return entityData.get(DATA_DISMISSAL_TICKS); }
	@Override public void hemomancy$setDismissalTicks(int ticks) { entityData.set(DATA_DISMISSAL_TICKS, Math.max(0, ticks)); }
	@Override public boolean hemomancy$isTrialSummon() { return entityData.get(DATA_TRIAL_SUMMON); }
	@Override public void hemomancy$setTrialSummon(boolean trialSummon) { entityData.set(DATA_TRIAL_SUMMON, trialSummon); }
	@Override public UUID hemomancy$getTrialCasterUUID() { return entityData.get(DATA_TRIAL_CASTER_UUID).orElse(null); }
	@Override public void hemomancy$setTrialCasterUUID(UUID casterUuid) { entityData.set(DATA_TRIAL_CASTER_UUID, Optional.ofNullable(casterUuid)); }
}
