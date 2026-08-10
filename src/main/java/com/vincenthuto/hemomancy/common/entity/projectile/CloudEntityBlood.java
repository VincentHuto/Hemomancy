package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.entity.summon.BloodConstructEntity;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class CloudEntityBlood extends BloodConstructEntity {
	public float deathTicks = 1;
	@Nullable
	private UUID creatorId;
	@Nullable
	private EnumBloodTendency damageTendency;
	@Nullable
	private EnumBloodTendency secondaryDamageTendency;

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

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (creatorId != null) tag.putUUID("Creator", creatorId);
		if (damageTendency != null) tag.putString("DamageTendency", damageTendency.name());
		if (secondaryDamageTendency != null) {
			tag.putString("SecondaryDamageTendency", secondaryDamageTendency.name());
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		creatorId = tag.hasUUID("Creator") ? tag.getUUID("Creator") : null;
		damageTendency = readTendency(tag, "DamageTendency");
		secondaryDamageTendency = readTendency(tag, "SecondaryDamageTendency");
	}

	@Override
	public void aiStep() {
		super.aiStep();
		resolveCreator();
		// Prevents it from falling
		Vec3 vector3d = this.getDeltaMovement();
		Vector3 pos = Vector3.fromEntityCenter(this);
		if (!this.onGround() && vector3d.y < 0.0D) {
			this.setDeltaMovement(vector3d.multiply(1.0D, 0.0D, 1.0D));
		}
		if (level().isClientSide) {
			double radius = 0.2;
			level().addParticle(BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + HLParticleUtils.inRange(-radius, radius), pos.y + HLParticleUtils.inRange(-radius, radius),
					pos.z + HLParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			level().addParticle(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + HLParticleUtils.inRange(-radius, radius), pos.y + HLParticleUtils.inRange(-radius, radius),
					pos.z + HLParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			level().addParticle(BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + HLParticleUtils.inRange(-radius, radius), pos.y + HLParticleUtils.inRange(-radius, radius),
					pos.z + HLParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			level().addParticle(ParticleTypes.FALLING_LAVA, pos.x + HLParticleUtils.inRange(-radius, radius),
					pos.y + HLParticleUtils.inRange(-radius, radius), pos.z + HLParticleUtils.inRange(-radius, radius),
					0, 0.005, 0);

		}
		double x = this.getX();
		double y = this.getY();
		double offY = this.getY() - 10.0d;
		double z = this.getZ();
		AABB scanBelow = new AABB(x, y, z, x, offY, z).inflate(1.25, 1, 1.25);
		List<LivingEntity> entList = level().getEntitiesOfClass(LivingEntity.class, scanBelow);
		for (LivingEntity ent : entList) {
			if (ent != null) {
				if (ent != creator && ent != this) {
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
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);

	}

	@Override
	public void tick() {
		super.tick();
		if (tickCount > 100) {
			this.remove(RemovalReason.KILLED);
		}
	}

	@Override
	protected void tickDeath() {
		// Particle MobEffects
		float g = (this.random.nextFloat() - 0.5F) * 2.0F;
		float g1 = -1;
		float g2 = (this.random.nextFloat() - 0.5F) * 2.0F;
		deathTicks -= 0.05;
		if (this.deathTicks <= 0.1) {
			if (level().isClientSide) {
				playConstructDissolutionSound();
				this.level().addParticle(ParticleTypes.SQUID_INK, this.getX() + g, this.getY() + 2.0D + g1,
						this.getZ() + g2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.deathTicks <= 0.1 && !this.level().isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}
}
