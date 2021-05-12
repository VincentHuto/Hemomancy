package com.huto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.init.EntityInit;
import com.hutoslib.client.particle.ParticleColor;
import com.hutoslib.client.particle.ParticleUtils;
import com.hutoslib.client.particles.factory.GlowParticleFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityBloodOrbDirected extends ThrowableEntity {
	public static EntityType<EntityBloodOrbDirected> TYPE = EntityInit.directed_blood_orb.get();

	public EntityBloodOrbDirected(EntityType<EntityBloodOrbDirected> type, World world) {
		super(type, world);
	}

	public EntityBloodOrbDirected(World world) {
		this(TYPE, world);
	}

	public EntityBloodOrbDirected(LivingEntity thrower, boolean evil) {
		super(TYPE, thrower, thrower.world);
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isRemote) {
			world.addParticle(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
					getPosX() + ParticleUtils.inRange(-0.1, 0.1), getPosY() + ParticleUtils.inRange(-0.1, 0.1),
					getPosZ() + ParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);
			world.addParticle(ParticleTypes.ASH, this.getPosX() + (Math.random() - 0.5) * 0.4,
					this.getPosY() + (Math.random() - 0.5) * 0.4, this.getPosZ() + (Math.random() - 0.5) * 0.4, 0, 0,
					0);
			world.addParticle(RedstoneParticleData.REDSTONE_DUST, this.getPosX() + (Math.random() - 0.5) * 0.4,
					this.getPosY() + (Math.random() - 0.5) * 0.4, this.getPosZ() + (Math.random() - 0.5) * 0.4, 0, 0,
					0);

		}
	}

	@Override
	public void writeAdditional(CompoundNBT cmp) {
		super.writeAdditional(cmp);
	}

	@Override
	public void readAdditional(CompoundNBT cmp) {
		super.readAdditional(cmp);
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
			if (!world.isRemote) {
				this.world.createExplosion(this, this.getPosX(), this.getPosY() + (double) (this.getHeight() / 16.0F),
						this.getPosZ(), 3.0F, Explosion.Mode.DESTROY);
			}
			remove();
			break;
		}
		case ENTITY: {
			if (!world.isRemote) {
				this.world.createExplosion(this, this.getPosX(), this.getPosY() + (double) (this.getHeight() / 16.0F),
						this.getPosZ(), 3.0F, Explosion.Mode.NONE);
			}
			remove();
			break;
		}
		default: {
			remove();
			break;
		}
		}
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z
	 * direction.
	 */
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		Vector3d vector3d = (new Vector3d(x, y, z)).normalize()
				.add(this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy,
						this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy,
						this.rand.nextGaussian() * (double) 0.0075F * (double) inaccuracy)
				.scale((double) velocity);
		this.setMotion(vector3d);
		float f = MathHelper.sqrt(horizontalMag(vector3d));
		this.rotationYaw = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
		this.rotationPitch = (float) (MathHelper.atan2(vector3d.y, (double) f) * (double) (180F / (float) Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
	}

	public void setDirectionAndMovement(Entity p_234612_1_, float p_234612_2_, float p_234612_3_, float p_234612_4_,
			float p_234612_5_, float p_234612_6_) {
		float f = -MathHelper.sin(p_234612_3_ * ((float) Math.PI / 180F))
				* MathHelper.cos(p_234612_2_ * ((float) Math.PI / 180F));
		float f1 = -MathHelper.sin((p_234612_2_ + p_234612_4_) * ((float) Math.PI / 180F));
		float f2 = MathHelper.cos(p_234612_3_ * ((float) Math.PI / 180F))
				* MathHelper.cos(p_234612_2_ * ((float) Math.PI / 180F));
		this.shoot((double) f, (double) f1, (double) f2, p_234612_5_, p_234612_6_);
		Vector3d vector3d = p_234612_1_.getMotion();
		this.setMotion(this.getMotion().add(vector3d.x, p_234612_1_.isOnGround() ? 0.0D : vector3d.y, vector3d.z));
	}

	@Override
	protected void registerData() {
	}

}