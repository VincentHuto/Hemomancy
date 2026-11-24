package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilMotion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

/**
 * A completed ichorian inscription that peels upright and hovers over its own pattern, turning slowly in place.
 */
public final class AwakenedIchorianSigilEntity extends Entity {
	private static final EntityDataAccessor<String> SIGIL_ID =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Optional<UUID>> RITE_CASTER =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> FULLY_PEELED =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Vector3f> HOVER =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Float> TURN_PHASE =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.FLOAT);

	private double originX;
	private double originY;
	private double originZ;
	/** Kept in doubles on the server; the synced {@link #HOVER} is a float copy for clients. */
	private double hoverX;
	private double hoverY;
	private double hoverZ;
	private int motionAge;
	/** Saved before the form hovered: it has no point to hold, so it leaves on its first server tick. */
	private boolean discardOnTick;
	private double clientLerpX;
	private double clientLerpY;
	private double clientLerpZ;
	private int clientLerpSteps;

	public AwakenedIchorianSigilEntity(EntityType<? extends AwakenedIchorianSigilEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	public void initialize(UUID caster, ResourceLocation sigilId,
			double originX, double originY, double originZ,
			AwakenedIchorianSigilMotion.Position hover, float turnPhase) {
		entityData.set(RITE_CASTER, Optional.of(caster));
		entityData.set(SIGIL_ID, sigilId.toString());
		entityData.set(TURN_PHASE, turnPhase);
		this.originX = originX;
		this.originY = originY;
		this.originZ = originZ;
		setHover(hover.x(), hover.y(), hover.z());
		setPos(originX, originY, originZ);
	}

	/** False for a tag saved before the form hovered (it has only the old orbit keys). */
	public static boolean hasHover(CompoundTag tag) {
		return AwakenedIchorianSigilMotion.hasSavedHover(tag);
	}

	public ResourceLocation getSigilId() {
		return ResourceLocation.tryParse(entityData.get(SIGIL_ID));
	}

	public Vector3f getHover() {
		return new Vector3f(entityData.get(HOVER));
	}

	public float getPeelProgress(float partialTick) {
		if (entityData.get(FULLY_PEELED)) return 1.0F;
		return AwakenedIchorianSigilMotion.peelProgress(Math.round(tickCount + partialTick));
	}

	/** Minecraft yaw of the slow turn in place. */
	public float getRenderFacingYaw(float partialTick) {
		return AwakenedIchorianSigilMotion.turnYaw(getSigilId(), entityData.get(TURN_PHASE),
				tickCount + partialTick);
	}

	public AABB getAnatomyRenderBounds() {
		return getBoundingBox().inflate(1.5D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SIGIL_ID, "hemomancy:reservoir");
		builder.define(RITE_CASTER, Optional.empty());
		builder.define(FULLY_PEELED, false);
		builder.define(HOVER, new Vector3f());
		builder.define(TURN_PHASE, 0.0F);
	}

	@Override
	public void tick() {
		super.tick();
		noPhysics = true;
		setNoGravity(true);
		if (level().isClientSide) {
			tickClientInterpolation();
			return;
		}
		if (discardOnTick) {
			discard();
			return;
		}
		motionAge++;
		if (motionAge >= AwakenedIchorianSigilMotion.PEEL_TICKS) {
			entityData.set(FULLY_PEELED, true);
		}

		UUID caster = entityData.get(RITE_CASTER).orElse(null);
		if (caster == null) {
			discard();
			return;
		}
		ServerLevel server = (ServerLevel) level();
		ActiveCardinalRite rite = CardinalRiteSavedData.get(server).getRite(caster);
		if (rite == null || rite.isTerminal()) {
			discard();
			return;
		}

		AwakenedIchorianSigilMotion.Position target = AwakenedIchorianSigilMotion.position(
				originX, originY, originZ, hoverX, hoverY, hoverZ, motionAge);
		setPos(target.x(), target.y(), target.z());
		setDeltaMovement(0.0D, 0.0D, 0.0D);
	}

	@Override
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
		if (!level().isClientSide) {
			super.lerpTo(x, y, z, yRot, xRot, steps);
			return;
		}
		clientLerpX = x;
		clientLerpY = y;
		clientLerpZ = z;
		clientLerpSteps = Math.max(1, steps);
	}

	private void tickClientInterpolation() {
		if (clientLerpSteps <= 0) return;
		AwakenedIchorianSigilMotion.Position next = AwakenedIchorianSigilMotion.smoothStep(
				new AwakenedIchorianSigilMotion.Position(getX(), getY(), getZ()),
				new AwakenedIchorianSigilMotion.Position(clientLerpX, clientLerpY, clientLerpZ),
				clientLerpSteps);
		setPos(next.x(), next.y(), next.z());
		clientLerpSteps--;
	}

	private void setHover(double x, double y, double z) {
		hoverX = x;
		hoverY = y;
		hoverZ = z;
		entityData.set(HOVER, new Vector3f((float) x, (float) y, (float) z));
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		entityData.set(SIGIL_ID, tag.getString("SigilId"));
		if (tag.hasUUID("RiteCaster")) entityData.set(RITE_CASTER, Optional.of(tag.getUUID("RiteCaster")));
		originX = tag.getDouble("OriginX");
		originY = tag.getDouble("OriginY");
		originZ = tag.getDouble("OriginZ");
		discardOnTick = !hasHover(tag);
		setHover(tag.getDouble("HoverX"), tag.getDouble("HoverY"), tag.getDouble("HoverZ"));
		entityData.set(TURN_PHASE, tag.getFloat("TurnPhase"));
		motionAge = Math.max(0, tag.getInt("MotionAge"));
		entityData.set(FULLY_PEELED, motionAge >= AwakenedIchorianSigilMotion.PEEL_TICKS);
		noPhysics = true;
		setNoGravity(true);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putString("SigilId", entityData.get(SIGIL_ID));
		entityData.get(RITE_CASTER).ifPresent(caster -> tag.putUUID("RiteCaster", caster));
		tag.putDouble("OriginX", originX);
		tag.putDouble("OriginY", originY);
		tag.putDouble("OriginZ", originZ);
		tag.putDouble("HoverX", hoverX);
		tag.putDouble("HoverY", hoverY);
		tag.putDouble("HoverZ", hoverZ);
		tag.putFloat("TurnPhase", entityData.get(TURN_PHASE));
		tag.putInt("MotionAge", motionAge);
	}
}
