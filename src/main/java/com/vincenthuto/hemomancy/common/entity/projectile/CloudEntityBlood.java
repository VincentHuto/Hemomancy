package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.summon.BloodConstructEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CloudEntityBlood extends BloodConstructEntity {
	private static final EntityDataAccessor<Float> EFFECT_RADIUS = SynchedEntityData.defineId(
			CloudEntityBlood.class, EntityDataSerializers.FLOAT);
	public float deathTicks = 1;
	@Nullable
	private UUID creatorId;
	@Nullable
	private EnumBloodTendency damageTendency;
	@Nullable
	private EnumBloodTendency secondaryDamageTendency;
	private int durationTicks = 100;
	private Mode mode = Mode.STATIC;

	public CloudEntityBlood(EntityType<? extends CloudEntityBlood> type, Level worldIn) {
		super(type, worldIn);

	}

	public CloudEntityBlood(EntityType<? extends CloudEntityBlood> type, Level worldIn, LivingEntity creator) {
		super(type, worldIn);
		this.creator = creator;
		this.creatorId = creator != null ? creator.getUUID() : null;
	}

	public void setDamageTendencies(@Nullable EnumBloodTendency damageTendency,
			@Nullable EnumBloodTendency secondaryDamageTendency) {
		this.damageTendency = damageTendency;
		this.secondaryDamageTendency = secondaryDamageTendency;
	}

	public void configure(double radius, int durationTicks, Mode mode) {
		this.entityData.set(EFFECT_RADIUS, (float) Math.max(0.5D, radius));
		this.durationTicks = Math.max(1, durationTicks);
		this.mode = mode != null ? mode : Mode.STATIC;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(EFFECT_RADIUS, 1.25F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (creatorId != null) tag.putUUID("Creator", creatorId);
		if (damageTendency != null) tag.putString("DamageTendency", damageTendency.name());
		if (secondaryDamageTendency != null) {
			tag.putString("SecondaryDamageTendency", secondaryDamageTendency.name());
		}
		tag.putDouble("EffectRadius", effectRadius());
		tag.putInt("DurationTicks", durationTicks);
		tag.putString("CloudMode", mode.name());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		creatorId = tag.hasUUID("Creator") ? tag.getUUID("Creator") : null;
		damageTendency = readTendency(tag, "DamageTendency");
		secondaryDamageTendency = readTendency(tag, "SecondaryDamageTendency");
		if (tag.contains("EffectRadius")) entityData.set(EFFECT_RADIUS,
				(float) Math.max(0.5D, tag.getDouble("EffectRadius")));
		if (tag.contains("DurationTicks")) durationTicks = tag.getInt("DurationTicks");
		if (tag.contains("CloudMode")) {
			try {
				mode = Mode.valueOf(tag.getString("CloudMode"));
			} catch (IllegalArgumentException ignored) {
				mode = Mode.STATIC;
			}
		}
	}

    @Override protected boolean usesLegacyConstructParticles() { return false; }

	@Override
	public void aiStep() {
		super.aiStep();
		resolveCreator();
		// Prevents it from falling
		Vec3 vector3d = this.getDeltaMovement();
		if (!this.onGround() && vector3d.y < 0.0D) {
			this.setDeltaMovement(vector3d.multiply(1.0D, 0.0D, 1.0D));
		}

		double x = this.getX();
		double y = this.getY();
		double offY = this.getY() - 10.0d;
		double z = this.getZ();
		AABB scanBelow = new AABB(x, y, z, x, offY, z).inflate(effectRadius(), 1, effectRadius());
		List<LivingEntity> entList = level().getEntitiesOfClass(LivingEntity.class, scanBelow);
		for (LivingEntity ent : entList) {
			if (ent != null) {
				if (ent != creator && ent != this && (creator == null || !creator.isAlliedTo(ent))) {
					if (!(ent instanceof BloodConstructEntity)) {
						float damage = creator instanceof Player player && damageTendency != null
								? 2.0F * TendencyAffinityRules.damageMultiplier(player, ent,
										damageTendency, secondaryDamageTendency)
								: 2.0F;
						ent.hurt(ent.damageSources().generic(), damage);
						ent.addEffect(new MobEffectInstance(EffectInit.blood_loss, 20, 1));
					}
				}
			}
		}
		if (!level().isClientSide && mode == Mode.PURSUING) pursueTarget();
		if (!level().isClientSide && mode == Mode.TEMPEST && tickCount % 20 == 0) strikeTarget();
	}

	private void resolveCreator() {
		if (creator != null || creatorId == null || !(level() instanceof ServerLevel server)) return;
		if (server.getEntity(creatorId) instanceof LivingEntity living) creator = living;
	}

	@Nullable
	private static EnumBloodTendency readTendency(CompoundTag tag, String key) {
		try {
			return tag.contains(key) ? EnumBloodTendency.valueOf(tag.getString(key)) : null;
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	@Override
	public void tick() {
		super.tick();
        if (!level().isClientSide && tickCount % 20 == 1) {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.attached(this,
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.CLOUD,
                    effectRadius(), Math.min(25, durationTicks-tickCount), mode.ordinal()+1);
        }
		if (!level().isClientSide && tickCount > durationTicks) {
			this.remove(RemovalReason.KILLED);
		}
	}

	private void pursueTarget() {
		LivingEntity target = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12),
				candidate -> candidate != this && candidate != creator && candidate.isAlive()
						&& (creator == null || !creator.isAlliedTo(candidate))).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
		if (target == null) {
			setDeltaMovement(Vec3.ZERO);
			return;
		}
		Vec3 direction = new Vec3(target.getX() - getX(), 0, target.getZ() - getZ());
		setDeltaMovement(direction.lengthSqr() > 0.001D ? direction.normalize().scale(0.08D) : Vec3.ZERO);
	}

	private void strikeTarget() {
		if (!(level() instanceof ServerLevel server)) return;
		LivingEntity target = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(effectRadius()),
				candidate -> candidate != this && candidate != creator && candidate.isAlive()
						&& (creator == null || !creator.isAlliedTo(candidate))).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
		if (target == null) return;
        com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner.spawn(server, position().add(0,.4,0), target.getEyePosition(),
                new com.vincenthuto.hutoslib.common.lightning.LightningTestConfig(
                        com.vincenthuto.hutoslib.common.lightning.LightningTestConfig.Backend.BOLT,
                        0xBB800B24,0xBB800B24,0xFFF3ACAF,16F,0F,0F,0F,.25F,1F,10,3,.12F,.035F,
                        true,getUUID().getLeastSignificantBits() ^ tickCount,false,20));
        server.playSound(null,target.blockPosition(),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,
                net.minecraft.sounds.SoundSource.WEATHER,10000F,.8F+random.nextFloat()*.2F);
        server.playSound(null,target.blockPosition(),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,
                net.minecraft.sounds.SoundSource.WEATHER,2F,.5F+random.nextFloat()*.2F);

		float damage = creator instanceof Player player && damageTendency != null
				? 4.0F * TendencyAffinityRules.damageMultiplier(player, target, damageTendency, secondaryDamageTendency)
				: 4.0F;
		target.hurt(server.damageSources().magic(), damage);
	}

	private double effectRadius() {
		return entityData.get(EFFECT_RADIUS);
	}

	public enum Mode {
		STATIC,
		PURSUING,
		TEMPEST
	}

	@Override
	protected void tickDeath() {
		deathTicks -= 0.05;
		if (this.deathTicks <= 0.1) {
			if (level().isClientSide) {
				playConstructDissolutionSound();

			}
		}
		if (this.deathTicks <= 0.1 && !this.level().isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}
}
