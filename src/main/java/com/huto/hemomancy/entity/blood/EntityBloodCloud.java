package com.huto.hemomancy.entity.blood;

import java.util.List;

import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.hutoslib.client.particle.ParticleColor;
import com.hutoslib.client.particle.ParticleUtils;
import com.hutoslib.client.particles.factory.GlowParticleFactory;
import com.hutoslib.math.Vector3;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class EntityBloodCloud extends EntityBloodConstruct {
	public float deathTicks = 1;

	public EntityBloodCloud(EntityType<? extends EntityBloodCloud> type, World worldIn) {
		super(type, worldIn);

	}

	public EntityBloodCloud(EntityType<? extends EntityBloodCloud> type, World worldIn, LivingEntity creator) {
		super(type, worldIn);
		this.creator = creator;
	}

	@Override
	public void livingTick() {
		super.livingTick();
		// Prevents it from falling
		Vector3d vector3d = this.getMotion();
		Vector3 pos = Vector3.fromEntityCenter(this);
		if (!this.onGround && vector3d.y < 0.0D) {
			this.setMotion(vector3d.mul(1.0D, 0.0D, 1.0D));
		}
		if (world.isRemote) {
			double radius = 0.2;
			world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + ParticleUtils.inRange(-radius, radius), pos.y + ParticleUtils.inRange(-radius, radius),
					pos.z + ParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			world.addParticle(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + ParticleUtils.inRange(-radius, radius), pos.y + ParticleUtils.inRange(-radius, radius),
					pos.z + ParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(200, 0, 0)),
					pos.x + ParticleUtils.inRange(-radius, radius), pos.y + ParticleUtils.inRange(-radius, radius),
					pos.z + ParticleUtils.inRange(-radius, radius), 0, 0.005, 0);
			world.addParticle(ParticleTypes.FALLING_LAVA, pos.x + ParticleUtils.inRange(-radius, radius),
					pos.y + ParticleUtils.inRange(-radius, radius), pos.z + ParticleUtils.inRange(-radius, radius), 0,
					0.005, 0);

		}
		double x = (double) this.getPosX();
		double y = (double) this.getPosY();
		double offY = (double) this.getPosY() - 10.0d;
		double z = (double) this.getPosZ();
		AxisAlignedBB scanBelow = new AxisAlignedBB(x, y, z, x, offY, z).grow(1.25, 1, 1.25);
		List<LivingEntity> entList = world.getEntitiesWithinAABB(LivingEntity.class, scanBelow);
		for (LivingEntity ent : entList) {
			if (ent != null) {
				if (ent != creator && ent != this) {
					if (!(ent instanceof EntityBloodConstruct)) {
						ent.attackEntityFrom(ItemInit.bloodLoss, 2);
						ent.addPotionEffect(new EffectInstance(PotionInit.blood_loss.get(), 20, 1));
					}
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (ticksExisted > 100) {
			remove();
		}
	}

	@Override
	protected void onDeathUpdate() {
		// Particle Effects
		float g = (this.rand.nextFloat() - 0.5F) * 2.0F;
		float g1 = -1;
		float g2 = (this.rand.nextFloat() - 0.5F) * 2.0F;
		deathTicks -= 0.05;
		if (this.deathTicks <= 0.1) {
			if (world.isRemote) {
				world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_IRON_GOLEM_DAMAGE,
						SoundCategory.HOSTILE, 3f, 0.2f, false);
				this.world.addParticle(ParticleTypes.SQUID_INK, this.getPosX() + (double) g,
						this.getPosY() + 2.0D + (double) g1, this.getPosZ() + (double) g2, 0.0D, 0.0D, 0.0D);
			}
		}
		if (this.deathTicks <= 0.1 && !this.world.isRemote) {
			this.remove();
		}

	}

	@Override
	protected void collideWithEntity(Entity entityIn) {
		super.collideWithEntity(entityIn);

	}
}
