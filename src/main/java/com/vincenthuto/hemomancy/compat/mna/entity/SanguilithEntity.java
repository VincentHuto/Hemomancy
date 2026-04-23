package com.vincenthuto.hemomancy.compat.mna.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.mna.api.affinity.Affinity;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX.Spell.Impact.Single;
import com.mna.tools.SummonUtils;
import com.mna.tools.math.MathUtils;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SanguilithEntity extends Monster implements OwnableEntity {
	public float deathTicks = 1;

	private static final EntityDataAccessor<Direction> SWING_DIRECTION;
	private int swing_count;
	private int nonRotatedDeathTime;
	private int duration;
	private boolean dealingDamage;
	private LivingEntity summoner;
	private List<LivingEntity> knownTargets;

	public SanguilithEntity(Level pLevel) {
		this( MnAPluginEntityInit.sanguilith.get(), pLevel);
	}

	public SanguilithEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		this.swing_count = 0;
		this.nonRotatedDeathTime = 0;
		this.duration = 0;
		this.dealingDamage = false;
		this.knownTargets = new ArrayList();
	}

	public SanguilithEntity setSummoner(LivingEntity summoner, int duration) {
		this.summoner = summoner;
		this.duration = duration;
		return this;
	}
	

	public void tick() {
		this.lookAt(Anchor.FEET, this.position().add(new Vec3(1.0, 0.0, 0.0)));
		super.tick();
		if (!this.level().isClientSide()) {
			if (this.summoner == null) {
				this.discard();
				return;
			}

			if (this.tickCount > this.duration) {
				if (this.isAlive() && !this.level().isClientSide()) {
					this.hurt(this.damageSources().fellOutOfWorld(), 99999.0F);
				}

				return;
			}

			if (this.swing_count > 0) {
				--this.swing_count;
				if (this.swing_count == 6) {
					this.tryHurtTarget();
				}

				if (this.swing_count == 0) {
					this.entityData.set(SWING_DIRECTION, Direction.UP);
					this.setTarget((LivingEntity) null);
				}
			} else {
				if (this.getTarget() != null && (this.getTarget().isDeadOrDying() || this.getTarget().isRemoved())) {
					this.setTarget((LivingEntity) null);
				}

				if (this.tickCount % 20 == 0) {
					this.reassessTargets();
				}

				this.pickTarget();
			}
		} else if (!this.isDeadOrDying()) {
			
			Vec3 fwd = new Vec3(0.25, 0.0, 0.0);
			Vec3 velRange = Vec3.ZERO;
			boolean isSwinging = true;
			Direction swingDir = (Direction) this.entityData.get(SWING_DIRECTION);
			switch (swingDir) {
			case EAST:
				velRange = new Vec3(0.1, 0.0, 0.25);
				break;
			case NORTH:
				fwd = new Vec3(0.0, 0.0, -0.25);
				velRange = new Vec3(0.25, 0.0, 0.1);
				break;
			case SOUTH:
				fwd = new Vec3(0.0, 0.0, 0.25);
				velRange = new Vec3(0.25, 0.0, 0.1);
				break;
			case WEST:
				fwd = new Vec3(-0.25, 0.0, 0.0);
				velRange = new Vec3(0.1, 0.0, 0.25);
				break;
			default:
				isSwinging = false;
			}

			Vec3 side = fwd.normalize().cross(new Vec3(0.0, 1.0, 0.0)).normalize().scale(0.25);
			int particleCount = isSwinging ? 5 : 1;

			for (int i = 0; i < particleCount; ++i) {
				Vec3 particleVelocity = isSwinging ? fwd.add(MathUtils.RandomBetween(-velRange.x, velRange.x), 0.25,
						MathUtils.RandomBetween(-velRange.z, velRange.z)) : Vec3.ZERO;
				Vec3 particlePos = this.position().add(
						(MathUtils.RandomBetween(-fwd.x, fwd.x) + MathUtils.RandomBetween(-side.x, side.x))
								* this.getBoundingBox().getXsize(),
						MathUtils.RandomBetween(0.25, isSwinging ? 0.75 : 0.5) * this.getBoundingBox().getYsize(),
						(MathUtils.RandomBetween(-fwd.z, fwd.z) + MathUtils.RandomBetween(-side.z, side.z))
								* this.getBoundingBox().getZsize());
				this.level().addParticle(
						(new MAParticleType((ParticleType) ParticleInit.DROPLET.get())).setColor(Affinity.BLOOD)
								.setGravity(isSwinging ? 0.05F : 0.01F).setPhysics(true),
						particlePos.x, particlePos.y, particlePos.z, particleVelocity.x, particleVelocity.y,
						particleVelocity.z);
			}
		}

	}

	protected void tickDeath() {
		++this.nonRotatedDeathTime;
		if (this.nonRotatedDeathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
			this.level().broadcastEntityEvent(this, (byte) 60);
			this.remove(RemovalReason.KILLED);
		}
		// Particle MobEffects
		float g = (this.random.nextFloat() - 0.5F) * 2.0F;
		float g1 = -1;
		float g2 = (this.random.nextFloat() - 0.5F) * 2.0F;
		deathTicks -= 0.05;
		if (this.deathTicks <= 0.1) {
			if (level().isClientSide) {
				level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_DAMAGE,
						SoundSource.HOSTILE, 3f, 0.2f, false);
				this.level().addParticle(ParticleTypes.SQUID_INK, this.getX() + g,
						this.getY() + 2.0D + g1, this.getZ() + g2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.deathTicks <= 0.1 && !this.level().isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}

	private void tryHurtTarget() {
		if (this.getTarget() != null) {
			double targetDist = 2.0 * this.getBoundingBox().getYsize();
			if (!(this.getTarget().distanceToSqr(this) > targetDist)) {
				this.dealingDamage = true;
				this.doHurtTarget(this.getTarget());
				this.dealingDamage = false;
			}
		}
	}

	public float getYRot() {
		if (this.dealingDamage) {
			Direction dir = (Direction) this.entityData.get(SWING_DIRECTION);
			switch (dir) {
			case EAST:
				return 270.0F;
			case NORTH:
				return 180.0F;
			case SOUTH:
				return 0.0F;
			case WEST:
				return 90.0F;
			}
		}

		return super.getYRot();
	}

	private void reassessTargets() {
		this.knownTargets.clear();
		this.knownTargets.addAll(this.level().getEntities(this, this.getBoundingBox().inflate(32.0), (e) -> {
			return e != this && e != this.summoner && e instanceof LivingEntity
					&& !SummonUtils.isTargetFriendly(e, this.summoner);
		}).stream().map((e) -> {
			return (LivingEntity) e;
		}).toList());
	}

	private void pickTarget() {
		this.knownTargets.removeIf((e) -> {
			return e == null || e.isDeadOrDying() || !e.isAlive();
		});
		if (this.knownTargets.size() != 0) {
			LivingEntity closest = null;
			double closestDist = Double.MAX_VALUE;
			Iterator var4 = this.knownTargets.iterator();

			while (var4.hasNext()) {
				LivingEntity target = (LivingEntity) var4.next();
				double dist = target.distanceToSqr(this);
				if (dist < closestDist) {
					closestDist = dist;
					closest = target;
				}
			}

			double targetDist = 2.0 * this.getBoundingBox().getYsize();
			if (closestDist < targetDist) {
				this.setTarget(closest);
				this.swingAtTarget();
			}

		}
	}

	private void swingAtTarget() {
		if (this.getTarget() != null) {
			Vec3 delta = this.position().subtract(this.getTarget().position());
			if (Math.abs(delta.x) > Math.abs(delta.z)) {
				if (delta.x < 0.0) {
					this.entityData.set(SWING_DIRECTION, Direction.EAST);
				} else {
					this.entityData.set(SWING_DIRECTION, Direction.WEST);
				}
			} else if (delta.z < 0.0) {
				this.entityData.set(SWING_DIRECTION, Direction.SOUTH);
			} else {
				this.entityData.set(SWING_DIRECTION, Direction.NORTH);
			}

		}
	}

	protected SoundEvent getAmbientSound() {
		return com.mna.api.sound.SFX.Entity.BloodTendril.IDLE;
	}

	protected SoundEvent getDeathSound() {
		return com.mna.api.sound.SFX.Entity.BloodTendril.DEATH;
	}

	protected SoundEvent getHurtSound(DamageSource pDamageSource) {
		return Single.BLOOD;
	}

	protected void registerGoals() {
	}

	public void travel(Vec3 pTravelVector) {
	}

	public void setDeltaMovement(double pX, double pY, double pZ) {
	}

	public void setDeltaMovement(Vec3 pDeltaMovement) {
	}

	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SWING_DIRECTION, Direction.UP);
	}

	public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
		super.onSyncedDataUpdated(pKey);
		if (pKey == SWING_DIRECTION) {
			this.swing_count = 10;
		}

	}

	public void onAddedToWorld() {
		super.onAddedToWorld();
		if (!this.level().isClientSide()) {
			List<SanguilithEntity> existing = this.level().getEntitiesOfClass(SanguilithEntity.class,
					this.getBoundingBox().inflate(6.0));
			existing.sort((a, b) -> {
				return b.tickCount - a.tickCount;
			});
			int removeCount = existing.size() - 4;

			for (int i = 0; i < removeCount; ++i) {
				((SanguilithEntity) existing.get(i)).discard();
			}
		}

	}

	public UUID getOwnerUUID() {
		return null;
	}

	public LivingEntity getOwner() {
		return this.summoner;
	}

	public static AttributeSupplier.Builder getGlobalAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, 0.0)
				.add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.ATTACK_SPEED, 40.0)
				.add(Attributes.ATTACK_KNOCKBACK, 1.0).add(Attributes.FOLLOW_RANGE, 32.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	static {
		SWING_DIRECTION = SynchedEntityData.defineId(SanguilithEntity.class, EntityDataSerializers.DIRECTION);
	}
}