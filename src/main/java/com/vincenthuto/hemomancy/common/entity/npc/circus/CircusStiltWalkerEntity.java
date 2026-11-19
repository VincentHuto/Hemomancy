package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public final class CircusStiltWalkerEntity extends CircusPerformerEntity {
	private static final EntityDataAccessor<Integer> SPIN_TICKS = SynchedEntityData.defineId(
			CircusStiltWalkerEntity.class, EntityDataSerializers.INT);
	private static final double SLAM_RADIUS = 3.5D;
	private int attackCooldown;
	/**
	 * The true self's ordeal gestures ({@link CircusPerformerEntity#gesture}, 2026-11-17): a stilt planted like a spear,
	 * a stride across the ring, the spin, and the long shadow falling. Timed by the true self's model from each move's
	 * first wind-up tick ({@code StiltWalkerMoves}).
	 */
	public static final int GESTURE_PLANT = 0, GESTURE_STRIDE = 1, GESTURE_SPIN = 2, GESTURE_FALL = 3;
	/** The true self's walk, counted in strides ({@code OrdealStiltWalk}), so the model swings its stilts in step. */
	private static final EntityDataAccessor<Float> STRIDE = SynchedEntityData.defineId(
			CircusStiltWalkerEntity.class, EntityDataSerializers.FLOAT);
	/** A gesture's second point: the stride's chord, the spin's start, the shadow's fall (with {@link #gestureAt}). */
	private static final EntityDataAccessor<org.joml.Vector3f> GESTURE_AIM = SynchedEntityData.defineId(
			CircusStiltWalkerEntity.class, EntityDataSerializers.VECTOR3);
	private float strideO, strideNow;
	/** Server: the walk stands still through this tick while a move is made. */
	private long strideHeldUntil = Long.MIN_VALUE;

	public CircusStiltWalkerEntity(EntityType<? extends CircusStiltWalkerEntity> type, Level level) {
		super(type, level);
	}

	/** Starts gesture {@code kind} at {@code at} with its second point {@code aim}, on every watching client. */
	public void gesture(int kind, net.minecraft.world.phys.Vec3 at, net.minecraft.world.phys.Vec3 aim) {
		entityData.set(GESTURE_AIM, aim.toVector3f());
		gesture(kind, at);
	}

	public net.minecraft.world.phys.Vec3 gestureAim() {
		return new net.minecraft.world.phys.Vec3(entityData.get(GESTURE_AIM));
	}

	/** Walks on one tick's worth of stride, unless a move holds the walk still; returns the strides walked. */
	public float walk(long now) {
		float stride = entityData.get(STRIDE);
		if (now > strideHeldUntil) {
			stride += 1.0F / com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealStiltWalk.STRIDE_TICKS;
			entityData.set(STRIDE, stride);
		}
		return stride;
	}

	/** Holds the walk still through {@code until}: the stilts stay planted while a move is made. */
	public void holdWalk(long until) {
		strideHeldUntil = Math.max(strideHeldUntil, until);
	}

	public boolean walkHeld(long now) {
		return now <= strideHeldUntil;
	}

	/** The strides walked, between ticks. */
	public float stride(float partialTick) {
		return strideO + (strideNow - strideO) * partialTick;
	}

	/** The true self stands about twelve blocks tall over a man-sized body: cull it by what is drawn. */
	@Override
	public net.minecraft.world.phys.AABB getBoundingBoxForCulling() {
		if (!isTrueSelf()) return super.getBoundingBoxForCulling();
		return getBoundingBox().inflate(7.0D, 0.0D, 7.0D).expandTowards(0.0D, 13.0D, 0.0D);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.ARMOR, 4.0D)
				.add(Attributes.STEP_HEIGHT, 1.5D).add(Attributes.ATTACK_DAMAGE, 3.0D).add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(SPIN_TICKS, 0);
		builder.define(STRIDE, 0.0F);
		builder.define(GESTURE_AIM, new org.joml.Vector3f());
	}

	@Override
	public void tick() {
		super.tick();
		strideO = strideNow;
		strideNow = entityData.get(STRIDE);
		if (Math.abs(strideNow - strideO) > 1.0F) strideO = strideNow;
		if (!level().isClientSide && getActState() != ActState.PERFORM && entityData.get(SPIN_TICKS) != 0) {
			entityData.set(SPIN_TICKS, 0);
		}
	}

	@Override protected String roleId() { return "circus_stilt_walker"; }
	@Override protected String texturePath() { return "textures/entity/npc/harbinger/circus/stilt_walker_0.png"; }
	@Override protected int performanceDuration() { return 160; }

	@Override
	protected void tickPerformance(int actTick) {
		int spinTicks = entityData.get(SPIN_TICKS);
		if (actTick == 60) {
			getNavigation().stop();
			entityData.set(SPIN_TICKS, 1);
		} else if (spinTicks > 0) {
			entityData.set(SPIN_TICKS, spinTicks >= 40 ? 0 : spinTicks + 1);
		}
		if (spinTicks > 0) return;
		if (actTick % 40 == 1) {
			double angle = actTick / 160.0D * Math.PI * 2.0D;
			getNavigation().moveTo(getHome().getX() + 0.5D + Math.cos(angle) * 4.0D, getHome().getY(),
					getHome().getZ() + 0.5D + Math.sin(angle) * 4.0D, 0.65D);
		}
	}

	public boolean isSpinning() {
		return entityData.get(SPIN_TICKS) > 0;
	}

	public float getSpinProgress(float partialTick) {
		int ticks = entityData.get(SPIN_TICKS);
		return ticks == 0 ? 0.0F : Math.min(1.0F, (ticks + partialTick) / 40.0F);
	}

	/** The slam is an area move: 20 ticks with both arms raised while its 3.5-block ring is drawn round the stilts. */
	@Override
	public BossMoveTimeline defenseMove() {
		return CircusPerformerRules.AREA_MOVE;
	}

	@Override
	protected void tickDefense(LivingEntity target) {
		if (distanceToSqr(target) > 16.0D) getNavigation().moveTo(target, 0.85D);
		if (attackCooldown-- > 0 || distanceToSqr(target) > 16.0D) return;
		attackCooldown = 70 - defenseMove().total();
		getNavigation().stop();
		beginDefenseMove(target);
	}

	@Override
	protected void telegraphDefense(ServerLevel server, LivingEntity target, int tick, BossMoveTimeline move) {
		float charge = tick / (float) move.windup();
		CircusEffects.gather(server, position().add(0.0D, getBbHeight() * 0.8D, 0.0D), CircusEffects.BONE, charge);
		CircusEffects.ringFootprint(server, position(), SLAM_RADIUS, charge);
	}

	@Override
	protected void strikeDefense(LivingEntity target) {
		level().playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 0.9F, 0.65F);
		if (level() instanceof ServerLevel server) {
			server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,
					level().getBlockState(getBlockPosBelowThatAffectsMyMovement())),
					getX(), getY() + 0.1D, getZ(), 24, 1.4D, 0.05D, 1.4D, 0.0D);
			BossTelegraphs.ring(server, position(), SLAM_RADIUS, CircusEffects.CRIMSON, 40);
			BossTelegraphs.ring(server, position(), SLAM_RADIUS * 0.55D, CircusEffects.CLOT, 24);
			BossTelegraphs.bloodCells(server, position().add(0.0D, 0.3D, 0.0D), CircusEffects.CRIMSON, 28,
					SLAM_RADIUS * 0.4D, 0.14D);
		}
		for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(SLAM_RADIUS))) {
			if (!canStrike(victim)) continue;
			double dx = victim.getX() - getX();
			double dz = victim.getZ() - getZ();
			double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
			if (victim.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE)))
				victim.push(dx / length * 1.1D, 0.35D, dz / length * 1.1D);
		}
	}
}
