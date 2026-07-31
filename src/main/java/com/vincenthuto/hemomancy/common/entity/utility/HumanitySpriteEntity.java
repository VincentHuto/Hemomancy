package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteHumanityGeometry;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteFinaleTiming;
import com.vincenthuto.hemomancy.client.particle.factory.DaemonDiffuseGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.data.EmberParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

import java.util.Optional;
import java.util.UUID;

/**
 * A reusable, particle-built daemon manifestation.
 * <p>
 * Scale {@code 1.0} is the canonical four-block form. The synchronized scale
 * may be changed at runtime, while an optional rite owner lets Cardinal rites
 * reclaim their own manifestation without affecting independently spawned
 * daemons.
 */
public final class HumanitySpriteEntity extends Entity {
	public static final float MIN_SCALE = 0.1F;
	public static final float MAX_SCALE = 8.0F;
	private static final float PRONE_PITCH_DEGREES = 90.0F;
	private static final float BODY_ROTATION_DEGREES_PER_TICK = 18.0F;

	private static final EntityDataAccessor<Float> SPRITE_SCALE =
			SynchedEntityData.defineId(HumanitySpriteEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Optional<UUID>> RITE_OWNER =
			SynchedEntityData.defineId(HumanitySpriteEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private float targetBodyPitch;

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

	public void faceDirection(double x, double z) {
		if (x * x + z * z < 0.000001D) return;
		setYRot((float) Math.toDegrees(Math.atan2(-x, z)));
	}

	public void setFlying(boolean flying) {
		targetBodyPitch = flying ? PRONE_PITCH_DEGREES : 0.0F;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SPRITE_SCALE, 1.0F);
		builder.define(RITE_OWNER, Optional.empty());
	}

	@Override
	public void tick() {
		super.tick();
		noPhysics = true;
		setNoGravity(true);
		setDeltaMovement(Vec3.ZERO);
		setXRot(Mth.approach(getXRot(), targetBodyPitch, BODY_ROTATION_DEGREES_PER_TICK));
		if (level().isClientSide) return;

		if (tickCount % 20 == 0 && hasExpiredRiteBinding((ServerLevel) level())) {
			discard();
			return;
		}
		emitParticles((ServerLevel) level());
	}

	private boolean hasExpiredRiteBinding(ServerLevel level) {
		UUID owner = entityData.get(RITE_OWNER).orElse(null);
		if (owner == null) return false;
		ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(owner);
		return rite == null || rite.isTerminal();
	}

	private void emitParticles(ServerLevel level) {
		float scale = getSpriteScale();
		double yaw = Math.toRadians(getYRot());
		double forwardX = -Math.sin(yaw);
		double forwardZ = Math.cos(yaw);
		double centerOffset = CardinalRiteHumanityGeometry.DEFAULT_ENTITY_HEIGHT * scale * 0.5D;

		var cloud = CardinalRiteHumanityGeometry.scaledCloud(
				scale, tickCount * 0.15D, forwardX, forwardZ);
		for (int pointIndex = 0; pointIndex < cloud.size(); pointIndex++) {
			if (!HumanitySpriteEmissionSchedule.isDue(
					pointIndex, tickCount, CardinalRiteHumanityGeometry.EMISSION_INTERVAL_TICKS)) {
				continue;
			}
			CardinalRiteHumanityGeometry.Point sourcePoint = cloud.get(pointIndex);
			CardinalRiteHumanityGeometry.Point point = CardinalRiteHumanityGeometry.orientPoint(
					new CardinalRiteHumanityGeometry.Point(sourcePoint.layer(), sourcePoint.x(),
							sourcePoint.y() - centerOffset, sourcePoint.z(), sourcePoint.red(),
							sourcePoint.green(), sourcePoint.blue()),
					forwardX, forwardZ, getXRot());
			ParticleColor color = new ParticleColor(point.red(), point.green(), point.blue());
			float alpha = switch (point.layer()) {
				case VOID_CORE -> 0.92F;
				case PALE_AURA -> 0.66F;
				case EYE -> 1.0F;
				case BLOOD_WISP -> 0.82F;
			};
			ParticleOptions particle = switch (
					CardinalRiteHumanityGeometry.particleStyle(point.layer())) {
				case DIFFUSE_GLOW -> DaemonDiffuseGlowParticleFactory.createData(
						CardinalRiteHumanityGeometry.particleScale(point.layer(), scale));
				case GLOW -> new EmberParticleData(color, alpha,
						CardinalRiteHumanityGeometry.particleScale(point.layer(), scale),
						CardinalRiteHumanityGeometry.particleLifetime(point.layer()));
			};
			double spread = switch (point.layer()) {
				case VOID_CORE -> 0.025D;
				case PALE_AURA -> 0.008D;
				case EYE -> 0.004D;
				case BLOOD_WISP -> 0.025D;
			};
			level.sendParticles(particle,
					getX() + point.x(),
					getY() + point.y(),
					getZ() + point.z(),
					1, spread, spread, spread, 0.0D);
		}
		emitAbsorbedOfferingParticles(level, scale);
	}

	private void emitAbsorbedOfferingParticles(ServerLevel level, float scale) {
		UUID owner = entityData.get(RITE_OWNER).orElse(null);
		if (owner == null) return;
		ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(owner);
		if (rite == null || rite.getAbsorbedOfferings().isEmpty()) return;
		double strength = switch (rite.getPhase()) {
			case OFFERING_PROCESSION -> 1.0D;
			case CULMINATION -> CardinalRiteFinaleTiming.offeringParticleStrength(rite.getPhaseTicks());
			default -> 0.0D;
		};
		if (strength <= 0.0D) return;
		int period = strength > 0.66D ? 2 : strength > 0.33D ? 4 : 8;
		if (tickCount % period != 0) return;
		double horizontalSpread = Math.max(0.08D, 0.24D * scale);
		double verticalSpread = Math.max(0.12D, 0.55D * scale);
		for (ActiveCardinalRite.RiteOffering offering : rite.getAbsorbedOfferings()) {
			if (offering.stack() == null || offering.stack().isEmpty()) continue;
			level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, offering.stack()),
					getX(), getY(), getZ(),
					1, horizontalSpread, verticalSpread, horizontalSpread, 0.015D);
		}
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
		entityData.get(RITE_OWNER).ifPresent(owner -> tag.putUUID("RiteOwner", owner));
	}
}
