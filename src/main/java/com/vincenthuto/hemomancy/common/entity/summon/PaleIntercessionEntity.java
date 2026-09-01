package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedAcolyteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedGuardianEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedScoutEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedZealotEntity;
import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.data.HitColorParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class PaleIntercessionEntity extends PathfinderMob implements OwnableEntity {
	public static final String ACTIVE_MARKER = "HemomancyPaleIntercession";
	private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(
			PaleIntercessionEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Integer> REMAINING = SynchedEntityData.defineId(
			PaleIntercessionEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> PRESENTATION = SynchedEntityData.defineId(
			PaleIntercessionEntity.class, EntityDataSerializers.INT);

	public enum Presentation { MANIFEST, STILL, GLIDE, INTERPOSE, STRIKE, DISTORT, DISSOLVE }

	private int presentationTicks;
	private int dissolveTicks;
	private int failedPathTicks;
	public final AnimationState presentationAnimationState = new AnimationState();
	private Presentation clientPresentation;
	@Nullable private ServerPlayer cachedOwner;

	public PaleIntercessionEntity(EntityType<? extends PaleIntercessionEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 1.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.34D)
				.add(Attributes.ATTACK_DAMAGE, PaleIntercessionRules.STRIKE_DAMAGE)
				.add(Attributes.FOLLOW_RANGE, PaleIntercessionRules.THREAT_RANGE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(OWNER, Optional.empty());
		builder.define(REMAINING, PaleIntercessionRules.DURATION_TICKS);
		builder.define(PRESENTATION, Presentation.MANIFEST.ordinal());
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
	}

	public void bindTo(ServerPlayer owner) {
		cachedOwner = owner;
		entityData.set(OWNER, Optional.of(owner.getUUID()));
		refreshDuration();
		setPresentation(Presentation.MANIFEST, 20);
	}

	public void recallTo(Vec3 position) {
		moveTo(position.x, position.y, position.z, getYRot(), getXRot());
		getNavigation().stop();
		setTarget(null);
		refreshDuration();
		setPresentation(Presentation.DISSOLVE, 20);
	}

	public void refreshDuration() {
		entityData.set(REMAINING, PaleIntercessionRules.DURATION_TICKS);
		dissolveTicks = 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (presentationTicks > 0 && --presentationTicks == 0 && dissolveTicks == 0) {
			setPresentation(Presentation.STILL, 0);
		}
		if (level().isClientSide) {
			Presentation current = getPresentation();
			if (current != clientPresentation) {
				presentationAnimationState.stop();
				presentationAnimationState.start(tickCount);
				clientPresentation = current;
			}
			if (random.nextInt(4) == 0) {
				level().addParticle(ParticleInit.lethean_drip.get(), getRandomX(0.5), getRandomY(), getRandomZ(0.5), 0, -0.01, 0);
			}
			return;
		}
		if (dissolveTicks > 0) {
			getNavigation().stop();
			setTarget(null);
			if (--dissolveTicks == 0) discard();
			return;
		}
		ServerPlayer owner = owner();
		if (!ownerIsValid(owner)) {
			beginDissolution();
			return;
		}
		entityData.set(REMAINING, Math.max(0, entityData.get(REMAINING) - 1));
		if (getRemainingTicks() == 0) {
			beginDissolution();
			return;
		}
		LivingEntity threat = chooseThreat(owner);
		setTarget(threat);
		if (threat != null) interpose(owner, threat); else followOwner(owner);
	}

	private boolean ownerIsValid(@Nullable ServerPlayer owner) {
		if (owner == null || !owner.isAlive() || owner.level() != level()) return false;
		if (owner.connection != null && owner.server.getPlayerList().getPlayer(owner.getUUID()) != owner) return false;
		if (!HemoCapabilityAccess.getUnstainedProgress(owner)
				.map(progress -> progress.hasClarityUnlocked() && progress.getClarity() >= 75.0f).orElse(false)) return false;
		if (!HemoCapabilityAccess.getKnownStillArts(owner)
				.map(known -> known.isKnown(com.vincenthuto.hemomancy.common.init.StillArtInit.pale_intercession.get()))
				.orElse(false)) return false;
		CompoundTag data = owner.getPersistentData();
		return data.hasUUID(ACTIVE_MARKER) && getUUID().equals(data.getUUID(ACTIVE_MARKER));
	}

	@Nullable
	private LivingEntity chooseThreat(ServerPlayer owner) {
		LivingEntity attacker = owner.getLastHurtByMob();
		if (attacker != null && owner.tickCount - owner.getLastHurtByMobTimestamp() <= PaleIntercessionRules.THREAT_MEMORY_TICKS
				&& canDefendAgainst(attacker)) return attacker;
		Monster targetingOwner = level().getEntitiesOfClass(Monster.class,
				owner.getBoundingBox().inflate(PaleIntercessionRules.THREAT_RANGE),
				monster -> monster.getTarget() == owner && canDefendAgainst(monster)).stream()
				.min(Comparator.comparingDouble(owner::distanceToSqr)).orElse(null);
		if (targetingOwner != null) return targetingOwner;
		LivingEntity ownerTarget = owner.getLastHurtMob();
		if (ownerTarget instanceof Monster && owner.tickCount - owner.getLastHurtMobTimestamp() <= PaleIntercessionRules.THREAT_MEMORY_TICKS
				&& canDefendAgainst(ownerTarget)) return ownerTarget;
		return null;
	}

	public boolean canDefendAgainst(LivingEntity target) {
		if (!(target instanceof Monster) || target instanceof PaleIntercessionEntity || target instanceof Player) return false;
		if (target instanceof UnstainedAcolyteEntity || target instanceof UnstainedGuardianEntity
				|| target instanceof UnstainedScoutEntity || target instanceof UnstainedZealotEntity) return false;
		ServerPlayer owner = owner();
		if (owner == null || target.isAlliedTo(owner) || owner.isAlliedTo(target)) return false;
		if (target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID())) return false;
		if (target instanceof BoundPuppeteerSummon bound && owner.getUUID().equals(bound.hemomancy$getOwnerUUID())) return false;
		return target.isAlive();
	}

	private void interpose(ServerPlayer owner, LivingEntity threat) {
		Vec3 line = threat.position().subtract(owner.position());
		Vec3 destination = line.lengthSqr() < 0.01 ? owner.position() : owner.position().add(line.normalize().scale(2.2));
		getNavigation().moveTo(destination.x, destination.y, destination.z, 1.2D);
		setPresentation(Presentation.INTERPOSE, 10);
		if (threat instanceof Monster monster && monster.getTarget() == owner) monster.setTarget(this);
		trackPathOrTeleport(owner);
	}

	private void followOwner(ServerPlayer owner) {
		double distance = distanceTo(owner);
		if (distance > PaleIntercessionRules.FOLLOW_MAX_DISTANCE) {
			getNavigation().moveTo(owner, 1.05D);
			setPresentation(Presentation.GLIDE, 0);
		} else if (distance < PaleIntercessionRules.FOLLOW_MIN_DISTANCE) {
			getNavigation().stop();
		}
		trackPathOrTeleport(owner);
	}

	private void trackPathOrTeleport(ServerPlayer owner) {
		if (distanceTo(owner) > PaleIntercessionRules.TELEPORT_DISTANCE) failedPathTicks = 20;
		else if (getNavigation().isDone() && distanceTo(owner) > PaleIntercessionRules.FOLLOW_MAX_DISTANCE) failedPathTicks++;
		else failedPathTicks = 0;
		if (failedPathTicks >= 20) {
			PaleIntercessionSummonService.safePosition(owner, this).ifPresent(this::recallTo);
			failedPathTicks = 0;
		}
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		if (!(target instanceof LivingEntity living) || !canDefendAgainst(living)) return false;
		ServerPlayer owner = owner();
		if (owner == null) return false;
		return applyStrike(living, HemoDamageTypes.paleIntercession(level(), this, owner));
	}

	public boolean applyStrike(LivingEntity living, DamageSource source) {
		boolean hurt = living.hurt(source, PaleIntercessionRules.STRIKE_DAMAGE);
		if (hurt) {
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, PaleIntercessionRules.SLOWNESS_TICKS, 0));
			setPresentation(Presentation.STRIKE, 12);
			level().playSound(null, blockPosition(), SoundInit.ENTITY_PALE_INTERCESSION_STRIKE.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
			paleEffects(12);
		}
		return hurt;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide) return amount > 0;
		if (dissolveTicks > 0 || amount <= 0 || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL)
				|| source.is(DamageTypes.DROWN)) return false;
		Entity attacker = source.getEntity();
		ServerPlayer owner = owner();
		if (attacker == null || attacker == owner || attacker == this
				|| (owner != null && (attacker.isAlliedTo(owner) || owner.isAlliedTo(attacker)))) return false;
		if (owner != null && attacker instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID())) return false;
		if (owner != null && attacker instanceof BoundPuppeteerSummon bound
				&& owner.getUUID().equals(bound.hemomancy$getOwnerUUID())) return false;
		entityData.set(REMAINING, PaleIntercessionRules.remainingAfterDamage(getRemainingTicks(), amount));
		setHealth(getMaxHealth());
		setPresentation(Presentation.DISTORT, PaleIntercessionRules.DISTORTION_TICKS);
		if (getRemainingTicks() == 0) beginDissolution();
		return true;
	}

	public void beginDissolution() {
		if (dissolveTicks > 0) return;
		dissolveTicks = PaleIntercessionRules.DISSOLUTION_TICKS;
		setInvulnerable(true);
		setPresentation(Presentation.DISSOLVE, dissolveTicks);
		level().playSound(null, blockPosition(), SoundInit.ENTITY_PALE_INTERCESSION_DISSOLVE.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
		paleEffects(24);
	}

	public void paleEffects(int count) {
		if (!(level() instanceof ServerLevel server)) return;
		server.sendParticles(ParticleInit.lethean_drip.get(), getX(), getY() + 1, getZ(), count,
				0.35, 0.7, 0.35, 0.01);
		server.sendParticles(new HitColorParticleData(210, 235, 232),
				getX(), getY() + 1, getZ(), Math.max(4, count / 2), 0.35, 0.55, 0.35, 0.02);
	}

	private void setPresentation(Presentation state, int ticks) {
		entityData.set(PRESENTATION, state.ordinal());
		presentationTicks = ticks;
	}

	public Presentation getPresentation() {
		return Presentation.values()[Math.min(Presentation.values().length - 1, entityData.get(PRESENTATION))];
	}

	public int getRemainingTicks() { return entityData.get(REMAINING); }

	@Override public boolean isPushable() { return false; }
	@Override protected void doPush(Entity entity) { }
	@Override public boolean removeWhenFarAway(double distance) { return false; }
	@Override public boolean isAffectedByPotions() { return false; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		getOwnerUUIDOptional().ifPresent(uuid -> tag.putUUID("Owner", uuid));
		tag.putInt("RemainingTicks", getRemainingTicks());
		tag.putInt("Presentation", entityData.get(PRESENTATION));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("Owner")) entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
		if (tag.contains("RemainingTicks")) entityData.set(REMAINING, Math.max(0, tag.getInt("RemainingTicks")));
		if (tag.contains("Presentation")) entityData.set(PRESENTATION, tag.getInt("Presentation"));
	}

	private Optional<UUID> getOwnerUUIDOptional() { return entityData.get(OWNER); }
	@Override @Nullable public UUID getOwnerUUID() { return getOwnerUUIDOptional().orElse(null); }
	@Nullable public ServerPlayer owner() {
		UUID uuid = getOwnerUUID();
		if (uuid == null) return null;
		if (cachedOwner != null && cachedOwner.isAlive() && uuid.equals(cachedOwner.getUUID())) return cachedOwner;
		if (level() instanceof ServerLevel server) cachedOwner = server.getServer().getPlayerList().getPlayer(uuid);
		return cachedOwner;
	}
}
