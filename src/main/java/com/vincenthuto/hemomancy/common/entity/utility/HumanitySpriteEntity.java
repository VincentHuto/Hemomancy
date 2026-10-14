package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteDaemonEmergence;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteHumanityGeometry;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * A reusable daemon manifestation. The body is a smoke volume drawn by {@code HumanitySpriteRenderer}; the server
 * only places, scales and turns it. The client glides it between the server's updates, and poses and streams its
 * smoke from how it actually moves ({@link DaemonFlight}).
 * <p>
 * Scale {@code 1.0} is the canonical four-block form. The synchronized scale
 * may be changed at runtime, while an optional rite owner lets Cardinal rites
 * reclaim their own manifestation without affecting independently spawned
 * daemons.
 */
public final class HumanitySpriteEntity extends Entity {
	public static final float MIN_SCALE = 0.1F;
	public static final float MAX_SCALE = 8.0F;
	private static final int SHED_INTERVAL_TICKS = 7;
	private static final double RENDER_RANGE = 96.0D;
	/** Share of the remaining size change the drawn daemon makes each tick, so a swell or shrink never jumps. */
	private static final float SCALE_RATE = 0.3F;
	private static final ParticleColor SHED_CELL = new ParticleColor(150, 8, 20);

	private static final EntityDataAccessor<Float> SPRITE_SCALE =
			SynchedEntityData.defineId(HumanitySpriteEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Optional<UUID>> RITE_OWNER =
			SynchedEntityData.defineId(HumanitySpriteEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	/** Set by the server once the daemon has finished forming out of the staff. */
	private static final EntityDataAccessor<Boolean> EMERGED =
			SynchedEntityData.defineId(HumanitySpriteEntity.class, EntityDataSerializers.BOOLEAN);
	/** Client only: the daemon had already emerged when this client first saw it, so it never emerges here. */
	private boolean seenMature;
	// Client only: the server's latest place and turn, which the daemon glides toward instead of snapping to.
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private float lerpYRot;
	private float lerpXRot;
	private int lerpSteps;
	private final DaemonFlight flight = new DaemonFlight();
	private float shownScale = -1.0F;
	private float shownScaleO;

	public HumanitySpriteEntity(EntityType<? extends HumanitySpriteEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	public static HumanitySpriteEntity spawn(ServerLevel level, Vec3 position, float scale) {
		HumanitySpriteEntity entity = EntityInit.humanity_sprite.get().create(level);
		if (entity == null) return null;
		entity.initialize(position, scale);
		level.addFreshEntity(entity);
		return entity;
	}

	public static HumanitySpriteEntity findBoundToRite(
			ServerLevel level, UUID owner, BlockPos center) {
		if (level == null || owner == null || center == null) return null;
		return level.getEntitiesOfClass(HumanitySpriteEntity.class,
						new AABB(center).inflate(128.0D),
						daemon -> daemon.isBoundToRite(owner))
				.stream()
				.findFirst()
				.orElse(null);
	}

	public void initialize(Vec3 position, float scale) {
		setPos(position.x(), position.y(), position.z());
		setSpriteScale(scale);
	}

	public float getSpriteScale() {
		return entityData.get(SPRITE_SCALE);
	}

	public void setSpriteScale(float scale) {
		float sanitized = Float.isFinite(scale) ? scale : 1.0F;
		entityData.set(SPRITE_SCALE, Mth.clamp(sanitized, MIN_SCALE, MAX_SCALE));
		refreshDimensions();
	}

	public void bindToRite(UUID owner) {
		entityData.set(RITE_OWNER, Optional.ofNullable(owner));
	}

	public boolean isBoundToRite(UUID owner) {
		return owner != null && entityData.get(RITE_OWNER).filter(owner::equals).isPresent();
	}

	// Client-side turning: the head eases toward the facing quickly and the body slowly behind it.
	private float headTurn;
	private float headTurnO;
	private float bodyTurn;
	private float bodyTurnO;
	private boolean turnSeeded;

	public void faceDirection(double x, double z) {
		if (x * x + z * z < 0.000001D) return;
		setYRot((float) Math.toDegrees(Math.atan2(-x, z)));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SPRITE_SCALE, 1.0F);
		builder.define(RITE_OWNER, Optional.empty());
		builder.define(EMERGED, false);
	}

	@Override
	public void tick() {
		super.tick();
		noPhysics = true;
		setNoGravity(true);
		setDeltaMovement(Vec3.ZERO);
		if (level().isClientSide) {
			if (lerpSteps > 0) {
				lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
				lerpSteps--;
			}
			turnTowardFacing();
			float scale = getSpriteScale();
			flight.tick(getX() - xo, getY() - yo, getZ() - zo, Math.toRadians(getYRot()),
					CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * scale);
			shownScaleO = shownScale < 0.0F ? scale : shownScale;
			shownScale = shownScaleO + (scale - shownScaleO) * SCALE_RATE;
			emitClientAccents();
			return;
		}
		if (!entityData.get(EMERGED) && tickCount >= CardinalRiteDaemonEmergence.EMERGENCE_TICKS) {
			entityData.set(EMERGED, true);
		}

		if (tickCount % 20 == 0 && hasExpiredRiteBinding((ServerLevel) level())) {
			discard();
		}
	}

	// A plain entity snaps to each of the server's updates, so a daemon the server moves every tick would step. It
	// glides toward them instead, the way living mobs do.
	@Override
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
		lerpX = x;
		lerpY = y;
		lerpZ = z;
		lerpYRot = yRot;
		lerpXRot = xRot;
		lerpSteps = steps;
	}

	@Override
	public double lerpTargetX() {
		return lerpSteps > 0 ? lerpX : getX();
	}

	@Override
	public double lerpTargetY() {
		return lerpSteps > 0 ? lerpY : getY();
	}

	@Override
	public double lerpTargetZ() {
		return lerpSteps > 0 ? lerpZ : getZ();
	}

	@Override
	public float lerpTargetXRot() {
		return lerpSteps > 0 ? lerpXRot : getXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return lerpSteps > 0 ? lerpYRot : getYRot();
	}

	private boolean hasExpiredRiteBinding(ServerLevel level) {
		UUID owner = entityData.get(RITE_OWNER).orElse(null);
		if (owner == null) return false;
		ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(owner);
		return rite == null || rite.isTerminal();
	}

	/**
	 * Occasional blood cells shed from the body, spawned locally. The body, envelope and glow are all geometry drawn
	 * by the renderer; billboard haze particles read as blocky squares against the sky, so none are spawned.
	 */
	private void emitClientAccents() {
		if (tickCount <= 1 && entityData.get(EMERGED)) seenMature = true;
		if (tickCount % SHED_INTERVAL_TICKS != 0) return;
		float scale = getSpriteScale();
		double height = CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * scale;
		// Body axis: upright while it hovers, along its path as it flies.
		double[] crown = flight.crown(1.0F);
		double axisX = crown[0];
		double axisY = crown[1];
		double axisZ = crown[2];
		double along = (random.nextDouble() - 0.6D) * height * 0.6D;
		level().addParticle(HemoParticleData.bloodCell(SHED_CELL),
				getX() + axisX * along + (random.nextDouble() - 0.5D) * 0.3D * scale,
				getY() + axisY * along,
				getZ() + axisZ * along + (random.nextDouble() - 0.5D) * 0.3D * scale,
				0.0D, -0.01D, 0.0D);
	}

	private void turnTowardFacing() {
		if (!turnSeeded) {
			headTurn = getYRot();
			bodyTurn = getYRot();
			turnSeeded = true;
		}
		headTurnO = headTurn;
		bodyTurnO = bodyTurn;
		headTurn = DaemonTurn.toward(headTurn, getYRot(), DaemonTurn.HEAD_RATE);
		bodyTurn = DaemonTurn.toward(bodyTurn, getYRot(), DaemonTurn.BODY_RATE);
	}

	/** The head's eased facing (degrees), leading the body. */
	public float renderHeadYaw(float partialTick) {
		return turnSeeded ? Mth.rotLerp(partialTick, headTurnO, headTurn) : getYRot();
	}

	/** The body's eased facing (degrees), following the head. */
	public float renderBodyYaw(float partialTick) {
		return turnSeeded ? Mth.rotLerp(partialTick, bodyTurnO, bodyTurn) : getYRot();
	}

	/** The drawn size: the synchronized scale, eased so a swell or shrink never jumps. */
	public float renderScale(float partialTick) {
		if (shownScale < 0.0F) return getSpriteScale();
		return Mth.lerp(partialTick, shownScaleO, shownScale);
	}

	/** How its smoke carries itself as it moves: pose, streaming, flow and smoothed velocity. */
	public DaemonFlight flight() {
		return flight;
	}

	/** Client-side progress of the daemon spinning out of the staff, from 0 to 1. */
	public float emergenceProgress(float partialTick) {
		if (seenMature) return 1.0F;
		return Mth.clamp((tickCount + partialTick) / CardinalRiteDaemonEmergence.EMERGENCE_TICKS, 0.0F, 1.0F);
	}

	/** The rite this manifestation belongs to, or {@code null} for an independent daemon. */
	public UUID getRiteOwner() {
		return entityData.get(RITE_OWNER).orElse(null);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		// Tiny while emerging or merging, but its tendrils and tethers still need to read across the rite.
		double range = RENDER_RANGE;
		return distance < range * range;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return super.getDimensions(pose).scale(getSpriteScale());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (SPRITE_SCALE.equals(key)) refreshDimensions();
		super.onSyncedDataUpdated(key);
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setSpriteScale(tag.contains("SpriteScale") ? tag.getFloat("SpriteScale") : 1.0F);
		entityData.set(EMERGED, tag.getBoolean("Emerged"));
		if (tag.hasUUID("RiteOwner")) {
			entityData.set(RITE_OWNER, Optional.of(tag.getUUID("RiteOwner")));
		} else {
			entityData.set(RITE_OWNER, Optional.empty());
		}
		noPhysics = true;
		setNoGravity(true);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putFloat("SpriteScale", getSpriteScale());
		tag.putBoolean("Emerged", entityData.get(EMERGED));
		entityData.get(RITE_OWNER).ifPresent(owner -> tag.putUUID("RiteOwner", owner));
	}
}
