package com.huto.hemomancy.entity.blood;

import java.util.List;

import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.hutoslib.math.Vector3;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntityBloodCloud extends EntityBloodConstruct {
	public float deathTicks = 1;

	public EntityBloodCloud(EntityType<? extends EntityBloodCloud> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityBloodCloud(EntityType<? extends EntityBloodCloud> type, Level worldIn, LivingEntity creator) {
		super(type, worldIn);
		this.creator = creator;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// Prevents it from falling
		Vec3 vector3d = this.getDeltaMovement();
		Vector3 pos = Vector3.fromEntityCenter(this);
		if (!this.onGround && vector3d.y < 0.0D) {
			this.setDeltaMovement(vector3d.multiply(1.0D, 0.0D, 1.0D));
		}
		if (level.isClientSide) {
			double radius = 0.2;
			level.addParticle(BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + ParticleUtils.inRange(-radius, radius), pos.y + ParticleUtils.inRange(-radius, radius),
					pos.z + ParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			level.addParticle(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + ParticleUtils.inRange(-radius, radius), pos.y + ParticleUtils.inRange(-radius, radius),
					pos.z + ParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			level.addParticle(BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + ParticleUtils.inRange(-radius, radius), pos.y + ParticleUtils.inRange(-radius, radius),
					pos.z + ParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			level.addParticle(ParticleTypes.FALLING_LAVA, pos.x + ParticleUtils.inRange(-radius, radius),
					pos.y + ParticleUtils.inRange(-radius, radius), pos.z + ParticleUtils.inRange(-radius, radius), 0,
					0.005, 0);

		}
		double x = this.getX();
		double y = this.getY();
		double offY = this.getY() - 10.0d;
		double z = this.getZ();
		AABB scanBelow = new AABB(x, y, z, x, offY, z).inflate(1.25, 1, 1.25);
		List<LivingEntity> entList = level.getEntitiesOfClass(LivingEntity.class, scanBelow);
		for (LivingEntity ent : entList) {
			if (ent != null) {
				if (ent != creator && ent != this) {
					if (!(ent instanceof EntityBloodConstruct)) {
						ent.hurt(ItemInit.bloodLoss, 2);
						ent.addEffect(new MobEffectInstance(PotionInit.blood_loss.get(), 20, 1));
					}
				}
			}
		}
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
			if (level.isClientSide) {
				level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.IRON_GOLEM_DAMAGE,
						SoundSource.HOSTILE, 3f, 0.2f, false);
				this.level.addParticle(ParticleTypes.SQUID_INK, this.getX() + g, this.getY() + 2.0D + g1,
						this.getZ() + g2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.deathTicks <= 0.1 && !this.level.isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);

	}
}
