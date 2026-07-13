package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MnemonistPuppetEntity extends GroundPuppetEntity implements BoundPuppeteerSummon {
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(MnemonistPuppetEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CROSSBAR_UUID =
			SynchedEntityData.defineId(MnemonistPuppetEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> DATA_SUMMON_NAME =
			SynchedEntityData.defineId(MnemonistPuppetEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_DISMISSAL_TICKS =
			SynchedEntityData.defineId(MnemonistPuppetEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_TRIAL_SUMMON =
			SynchedEntityData.defineId(MnemonistPuppetEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRIAL_CASTER_UUID =
			SynchedEntityData.defineId(MnemonistPuppetEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final DustParticleOptions MEMORY_DUST =
			new DustParticleOptions(new Vector3f(0.62F, 0.04F, 0.07F), 0.9F);
	private static final DustParticleOptions PALE_DUST =
			new DustParticleOptions(new Vector3f(0.72F, 0.68F, 0.70F), 0.65F);

	private final List<MnemonistPuppetRules.AttackMemory> memories = new ArrayList<>();

	public MnemonistPuppetEntity(EntityType<? extends Zombie> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Zombie.createAttributes()
				.add(Attributes.MAX_HEALTH, 26.0)
				.add(Attributes.ATTACK_DAMAGE, 3.0)
				.add(Attributes.MOVEMENT_SPEED, 0.26);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_OWNER_UUID, Optional.empty());
		builder.define(DATA_CROSSBAR_UUID, Optional.empty());
		builder.define(DATA_SUMMON_NAME, "mnemonist_puppet");
		builder.define(DATA_DISMISSAL_TICKS, 0);
		builder.define(DATA_TRIAL_SUMMON, false);
		builder.define(DATA_TRIAL_CASTER_UUID, Optional.empty());
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			return;
		}
		if (hemomancy$isTrialSummon()) {
			BoundSummonBehavior.trialServerTick(this, this);
			tickMemoryReplay();
			return;
		}
		if (BoundSummonBehavior.commonServerTick(this, this)) {
			Optional<Player> owner = BoundSummonBehavior.ownerFor(this, this);
			if (getTarget() == null && owner.isPresent()
					&& BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner.get(), this)
					&& distanceToSqr(owner.get()) > 25.0) {
				getNavigation().moveTo(owner.get(), 1.0);
			}
			tickMemoryReplay();
		}
	}

	public void rememberDamage(LivingEntity damaged, float damage) {
		if (damaged == null || !MnemonistPuppetRules.canRecord(targetId(), damaged.getUUID(), damage,
				MnemonistPuppetEvents.isReplayingDamage())) {
			return;
		}
		MnemonistPuppetRules.record(memories, new MnemonistPuppetRules.AttackMemory(damaged.getUUID(), damage,
				level().getGameTime()));
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(MEMORY_DUST, damaged.getX(), damaged.getY() + damaged.getBbHeight() * 0.65D,
					damaged.getZ(), 5, 0.22D, 0.24D, 0.22D, 0.0D);
		}
	}

	private void tickMemoryReplay() {
		MnemonistPuppetRules.pruneExpired(memories, level().getGameTime());
		if (tickCount % MnemonistPuppetRules.REPLAY_INTERVAL_TICKS != 0 || !(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		LivingEntity target = getTarget();
		if (target == null || !target.isAlive() || distanceToSqr(target) > 18.0D * 18.0D) {
			return;
		}
		MnemonistPuppetRules.AttackMemory memory =
				MnemonistPuppetRules.pollReplayMemory(memories, target.getUUID(), level().getGameTime());
		if (memory == null) {
			return;
		}
		float damage = MnemonistPuppetRules.replayDamage(memory.damage());
		if (damage <= 0.0F) {
			return;
		}
		MnemonistPuppetEvents.withReplayDamage(() -> target.hurt(level().damageSources().magic(), damage));
		playReplayEffects(serverLevel, target);
	}

	private void playReplayEffects(ServerLevel serverLevel, LivingEntity target) {
		double y = target.getY() + target.getBbHeight() * 0.55D;
		serverLevel.sendParticles(MEMORY_DUST, target.getX(), y, target.getZ(), 10, 0.35D, 0.32D, 0.35D, 0.0D);
		serverLevel.sendParticles(PALE_DUST, target.getX(), y + 0.15D, target.getZ(), 8, 0.28D, 0.26D, 0.28D, 0.0D);
		serverLevel.playSound(null, target.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE,
				0.35F, 1.65F);
	}

	private UUID targetId() {
		return getTarget() == null ? null : getTarget().getUUID();
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return PuppeteerSummonRules.shouldDespawnInPeaceful(
				hemomancy$isTrialSummon(), hemomancy$getOwnerUUID());
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
