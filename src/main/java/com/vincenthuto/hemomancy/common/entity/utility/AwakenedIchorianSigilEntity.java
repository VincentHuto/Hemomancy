package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteFootprintRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilMotion;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

/**
 * A completed ichorian inscription that peels upright and circles its rite.
 */
public final class AwakenedIchorianSigilEntity extends Entity {
	private static final EntityDataAccessor<String> SIGIL_ID =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<BlockPos> RITE_CENTER =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Float> ORBIT_RADIUS =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> STARTING_ANGLE =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Optional<UUID>> RITE_CASTER =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> FULLY_PEELED =
			SynchedEntityData.defineId(AwakenedIchorianSigilEntity.class, EntityDataSerializers.BOOLEAN);

	private double originX;
	private double originY;
	private double originZ;
	private int motionAge;
	private double clientLerpX;
	private double clientLerpY;
	private double clientLerpZ;
	private int clientLerpSteps;

	public AwakenedIchorianSigilEntity(EntityType<? extends AwakenedIchorianSigilEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	public void initialize(UUID caster, ResourceLocation sigilId, BlockPos riteCenter,
			double originX, double originY, double originZ, float orbitRadius, float startingAngle) {
		entityData.set(RITE_CASTER, Optional.of(caster));
		entityData.set(SIGIL_ID, sigilId.toString());
		entityData.set(RITE_CENTER, riteCenter.immutable());
		entityData.set(ORBIT_RADIUS, orbitRadius);
		entityData.set(STARTING_ANGLE, startingAngle);
		this.originX = originX;
		this.originY = originY;
		this.originZ = originZ;
		setPos(originX, originY, originZ);
	}

	public ResourceLocation getSigilId() {
		return ResourceLocation.tryParse(entityData.get(SIGIL_ID));
	}

	public float getPeelProgress(float partialTick) {
		if (entityData.get(FULLY_PEELED)) return 1.0F;
		return AwakenedIchorianSigilMotion.peelProgress(Math.round(tickCount + partialTick));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SIGIL_ID, "hemomancy:reservoir");
		builder.define(RITE_CENTER, BlockPos.ZERO);
		builder.define(ORBIT_RADIUS, 2.0F);
		builder.define(STARTING_ANGLE, 0.0F);
		builder.define(RITE_CASTER, Optional.empty());
		builder.define(FULLY_PEELED, false);
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
		if (tickCount == 1 || tickCount % 20 == 0) {
			CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(server, rite.getRecipeId());
			float footprintRadius = HarbingerCardinalRiteEvents.ritualFootprintRadius(rite, recipe);
			entityData.set(ORBIT_RADIUS,
					CardinalRiteFootprintRules.awakenedSigilOrbitRadius(footprintRadius));
		}

		BlockPos center = entityData.get(RITE_CENTER);
		AwakenedIchorianSigilMotion.Position target = AwakenedIchorianSigilMotion.position(
				getSigilId(),
				originX, originY, originZ,
				center.getX() + 0.5D, center.getY() + 2.5D, center.getZ() + 0.5D,
				entityData.get(ORBIT_RADIUS), entityData.get(STARTING_ANGLE), motionAge);
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

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		entityData.set(SIGIL_ID, tag.getString("SigilId"));
		entityData.set(RITE_CENTER, BlockPos.of(tag.getLong("RiteCenter")));
		entityData.set(ORBIT_RADIUS, tag.getFloat("OrbitRadius"));
		entityData.set(STARTING_ANGLE, tag.getFloat("StartingAngle"));
		if (tag.hasUUID("RiteCaster")) entityData.set(RITE_CASTER, Optional.of(tag.getUUID("RiteCaster")));
		originX = tag.getDouble("OriginX");
		originY = tag.getDouble("OriginY");
		originZ = tag.getDouble("OriginZ");
		motionAge = Math.max(0, tag.getInt("MotionAge"));
		entityData.set(FULLY_PEELED, motionAge >= AwakenedIchorianSigilMotion.PEEL_TICKS);
		noPhysics = true;
		setNoGravity(true);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putString("SigilId", entityData.get(SIGIL_ID));
		tag.putLong("RiteCenter", entityData.get(RITE_CENTER).asLong());
		tag.putFloat("OrbitRadius", entityData.get(ORBIT_RADIUS));
		tag.putFloat("StartingAngle", entityData.get(STARTING_ANGLE));
		entityData.get(RITE_CASTER).ifPresent(caster -> tag.putUUID("RiteCaster", caster));
		tag.putDouble("OriginX", originX);
		tag.putDouble("OriginY", originY);
		tag.putDouble("OriginZ", originZ);
		tag.putInt("MotionAge", motionAge);
	}
}
