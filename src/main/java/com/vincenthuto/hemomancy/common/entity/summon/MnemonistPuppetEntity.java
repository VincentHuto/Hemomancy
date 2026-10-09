package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public class MnemonistPuppetEntity extends GroundPuppetEntity implements SyncedBoundSummon {
	private static final BoundSummonSync SYNC = BoundSummonSync.define(MnemonistPuppetEntity.class);
	private static final ParticleOptions MEMORY_GLOW = HemoParticleData.glow(new ParticleColor(158, 10, 18));
	private static final ParticleOptions PALE_GLOW = HemoParticleData.glow(new ParticleColor(184, 173, 178));
	private static final int REPLAY_VISUAL_TICKS = 20;
	// Server-side puppets in loaded levels, so the damage listener never has to search the world for them.
	private static final Set<MnemonistPuppetEntity> LOADED = Collections.newSetFromMap(new WeakHashMap<>());

	private final List<MnemonistPuppetRules.AttackMemory> memories = new ArrayList<>();

	public MnemonistPuppetEntity(EntityType<? extends Zombie> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return BoundSummonBehavior.definitionAttributes(Zombie.createAttributes(),
				PuppeteerSummonDefinitions.MNEMONIST_PUPPET);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new HighStrungMeleeAttackGoal(this, 1.0, false));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		SYNC.defineDefaults(builder, PuppeteerSummonDefinitions.MNEMONIST_PUPPET);
	}

	static List<MnemonistPuppetEntity> loaded() {
		return List.copyOf(LOADED);
	}

	@Override
	public void onAddedToLevel() {
		super.onAddedToLevel();
		if (!level().isClientSide) LOADED.add(this);
	}

	@Override
	public void onRemovedFromLevel() {
		super.onRemovedFromLevel();
		LOADED.remove(this);
	}

	/** Only the Mnemonist shape replays; older saves stored the Ringmaster Pattern on this entity type. */
	public boolean replaysMemories() {
		return !PuppeteerSummonDefinitions.RINGMASTER_PATTERN.equals(hemomancy$getSummonName());
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			return;
		}
		if (!replaysMemories()) {
			convertLegacyRingmaster((ServerLevel) level());
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

	/** Moves a Ringmaster Pattern saved before it had its own entity type onto that type, keeping its binding. */
	private void convertLegacyRingmaster(ServerLevel level) {
		if (!isAlive()) {
			return;
		}
		RingmasterPatternEntity pattern = EntityInit.ringmaster_pattern.get().create(level);
		if (pattern == null) {
			return;
		}
		CompoundTag tag = saveWithoutId(new CompoundTag());
		tag.remove("UUID");
		pattern.load(tag);
		discard();
		level.addFreshEntity(pattern);
	}

	public void rememberDamage(LivingEntity damaged, float damage) {
		rememberDamage(damaged, damage, null);
	}

	public void rememberDamage(LivingEntity damaged, float damage, net.minecraft.world.entity.Entity attacker) {
		if (damaged == null || !replaysMemories() || !MnemonistPuppetRules.canRecord(targetId(), damaged.getUUID(), damage,
				MnemonistPuppetEvents.isReplayingDamage())) {
			return;
		}
		MnemonistPuppetRules.record(memories, new MnemonistPuppetRules.AttackMemory(damaged.getUUID(), damage,
				level().getGameTime(), attacker == null ? null : attacker.getUUID()));
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(MEMORY_GLOW, damaged.getX(), damaged.getY() + damaged.getBbHeight() * 0.65D,
					damaged.getZ(), 4, 0.22D, 0.24D, 0.22D, 0.0D);
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
		MnemonistPuppetEvents.withReplayDamage(() -> {
            if (target.hurt(level().damageSources().magic(), damage))
                com.vincenthuto.hemomancy.common.circus.CircusPracticalController.replay(this, target, memory.attackerId());
        });
		playReplayEffects(serverLevel, target);
	}

	private void playReplayEffects(ServerLevel serverLevel, LivingEntity target) {
		double y = target.getY() + target.getBbHeight() * 0.55D;
		// The threads reach from the spool on its back to the remembered wound.
		Vec3 spool = position().add(0.0D, getBbHeight() * 0.62D, 0.0D);
		ManipulationVisuals.burst(serverLevel, ManipulationVisuals.Form.MNEMONIC_REPLAY, spool,
				new Vec3(target.getX(), y, target.getZ()), 1.0D, REPLAY_VISUAL_TICKS);
		serverLevel.sendParticles(MEMORY_GLOW, target.getX(), y, target.getZ(), 6, 0.3D, 0.28D, 0.3D, 0.0D);
		serverLevel.sendParticles(PALE_GLOW, target.getX(), y + 0.15D, target.getZ(), 4, 0.24D, 0.22D, 0.24D, 0.0D);
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

	@Override public BoundSummonSync hemomancy$sync() { return SYNC; }
}
