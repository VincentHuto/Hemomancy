package com.vincenthuto.hemomancy.common.entity.boss.seraphae;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A radiant fragment shed by Seraphae during FRACTURING / DISPERSED states.
 * <p>
 * Fragments are not killed — they are "contained" when their health reaches 0,
 * which signals the parent {@link SeraphaeEntity} to award containment integrity.
 * While alive they apply passive pressure (minor damage, movement disruption)
 * to nearby players.
 */
public class SeraphaeFragmentEntity extends Monster {

	/** Small visual pulsation. */
	public final AnimationState idleAnimationState = new AnimationState();

	/** UUID of the parent Seraphae that owns this fragment. */
	@Nullable
	private UUID parentId;

	/** Whether this fragment has been contained (health ≤ 0 processed). */
	private boolean contained;

	/** Set to true by ContainmentAnchorEntity before killing so die() skips its own parent notification. */
	private boolean anchorCaptured;

	public SeraphaeFragmentEntity(EntityType<? extends SeraphaeFragmentEntity> type, Level level) {
		super(type, level);
		this.xpReward = 0; // no XP — not a real kill
	}

	/* ---- attributes ---- */

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 12.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.24D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.0D);
	}

	/* ---- goals ---- */

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.9D));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 12.0F));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	/* ---- parent tracking ---- */

	public void setParentId(@Nullable UUID id) {
		this.parentId = id;
	}

	@Nullable
	public UUID getParentId() {
		return parentId;
	}

	public boolean isContained() {
		return contained;
	}

	public void setAnchorCaptured(boolean value) {
		this.anchorCaptured = value;
	}

	/* ---- tick logic ---- */

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide) {
			this.idleAnimationState.startIfStopped(this.tickCount);
			return;
		}

		// Passive pressure: slow nearby players every 20 ticks
		if (this.tickCount % 20 == 0 && this.level() instanceof ServerLevel server) {
			for (Player player : server.getEntitiesOfClass(Player.class,
					this.getBoundingBox().inflate(4.0))) {
				// light knockback / disruption
				Vec3 push = player.position().subtract(this.position()).normalize().scale(0.15);
				player.push(push.x, 0.05, push.z);
				player.hurtMarked = true;
			}
			// Ambient particles
			server.sendParticles(ParticleTypes.END_ROD,
					this.getX(), this.getY() + 0.5, this.getZ(),
					4, 0.3, 0.4, 0.3, 0.02);
		}
	}

	/**
	 * When the fragment "dies" it is considered contained rather than killed.
	 * Notifies the parent Seraphae to award containment integrity, unless the
	 * fragment was captured by an anchor (which handles notification itself).
	 */
	@Override
	public void die(DamageSource source) {
		if (!this.contained) {
			this.contained = true;
			// Notify parent Seraphae — direct kill (not anchor-assisted)
			// Anchor-captured fragments skip this because the anchor notifies with bonus gain.
			if (!anchorCaptured && parentId != null && this.level() instanceof ServerLevel server) {
				for (SeraphaeEntity seraphae : server.getEntitiesOfClass(
						SeraphaeEntity.class,
						this.getBoundingBox().inflate(64.0))) {
					if (seraphae.getUUID().equals(parentId) && seraphae.isAlive()) {
						seraphae.onFragmentContained(false);
						break;
					}
				}
			}
		}
		if (this.level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.FLASH,
					this.getX(), this.getY() + 0.5, this.getZ(),
					1, 0.0, 0.0, 0.0, 0.0);
			server.sendParticles(ParticleTypes.END_ROD,
					this.getX(), this.getY() + 0.5, this.getZ(),
					20, 0.5, 0.5, 0.5, 0.08);
		}
		super.die(source);
	}

	/* ---- persistence / serialisation ---- */

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (parentId != null) {
			tag.putUUID("ParentSeraphae", parentId);
		}
		tag.putBoolean("Contained", contained);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("ParentSeraphae")) {
			parentId = tag.getUUID("ParentSeraphae");
		}
		contained = tag.getBoolean("Contained");
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	/* ---- sounds ---- */

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.AMETHYST_BLOCK_CHIME;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.AMETHYST_BLOCK_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.AMETHYST_BLOCK_BREAK;
	}
}
