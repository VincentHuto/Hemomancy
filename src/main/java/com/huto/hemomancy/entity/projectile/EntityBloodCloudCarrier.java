package com.huto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.entity.blood.EntityBloodCloud;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.hutoslib.math.Vector3;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityBloodCloudCarrier extends DamagingProjectileEntity {

	public EntityBloodCloudCarrier(EntityType<? extends EntityBloodCloudCarrier> ent, World world) {
		super(ent, world);
	}

	LivingEntity shooter;

	public EntityBloodCloudCarrier(World worldIn, LivingEntity shooter) {
		super(EntityInit.blood_cloud_carrier.get(), worldIn);
		this.shooter = shooter;
	}

	public EntityBloodCloudCarrier(World worldIn, LivingEntity shooter, double aX, double aY, double aZ) {
		super(EntityInit.blood_cloud_carrier.get(), shooter, aZ, aZ, aZ, worldIn);
		this.shooter = shooter;
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRenderDist(double distance) {
		double d0 = this.getBoundingBox().getAverageEdgeLength() * 4.0D;
		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}
		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	int globalPartCount = 20;
	Vector3d[] fibboSphere = ParticleUtils.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
	Vector3d[] corona = ParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
	Vector3d[] inversedSphere = ParticleUtils.inversedSphere(globalPartCount, -world.getGameTime() * 0.016, 0.15, false);

	@Override
	public void tick() {
		super.tick();
		Vector3 pos = Vector3.fromEntityCenter(this);
		if (!world.isRemote) {
			ServerWorld sWorld = (ServerWorld) world;
			for (int i = 0; i < globalPartCount; i++) {
				sWorld.spawnParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						pos.x + inversedSphere[i].x, pos.y + inversedSphere[i].y, pos.z + inversedSphere[i].z, 3, 0.0,
						0.1, 0.00, 0.002d);
			}
		}

		if (ticksExisted > 50) {

			if (!world.isRemote) {
				EntityBloodCloud cloud = new EntityBloodCloud(EntityInit.blood_cloud.get(), world, shooter);
				cloud.setPosition(getPosX(), getPosY(), getPosZ());
				world.addEntity(cloud);
				remove();

			}
		}

	}

	protected boolean func_230298_a_(Entity p_230298_1_) {
		return super.func_230298_a_(p_230298_1_) && !p_230298_1_.noClip;
	}

	protected boolean isFireballFiery() {
		return false;
	}

	/**
	 * Return the motion factor for this projectile. The factor is multiplied by the
	 * original motion.
	 */
	protected float getMotionFactor() {
		return 1F;
	}

	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("power",
				this.newDoubleNBTList(new double[] { this.accelerationX, this.accelerationY, this.accelerationZ }));
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if (compound.contains("power", 9)) {
			ListNBT listnbt = compound.getList("power", 6);
			if (listnbt.size() == 3) {
				this.accelerationX = listnbt.getDouble(0);
				this.accelerationY = listnbt.getDouble(1);
				this.accelerationZ = listnbt.getDouble(2);
			}
		}

	}

	/**
	 * Returns true if other Entities should be prevented from moving through this
	 * Entity.
	 */
	public boolean canBeCollidedWith() {
		return true;
	}

	public float getCollisionBorderSize() {
		return 1.0F;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			this.markVelocityChanged();
			Entity entity = source.getTrueSource();
			if (entity != null) {
				Vector3d vector3d = entity.getLookVec();
				this.setMotion(vector3d);
				this.accelerationX = vector3d.x * 0.1D;
				this.accelerationY = vector3d.y * 0.1D;
				this.accelerationZ = vector3d.z * 0.1D;
				this.setShooter(entity);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	protected IParticleData getParticle() {
		return BloodCellParticleFactory.createData(ParticleColor.RED);
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
			if (!world.isRemote) {
				EntityBloodCloud cloud = new EntityBloodCloud(EntityInit.blood_cloud.get(), world, shooter);
				cloud.setPosition(getPosX(), getPosY(), getPosZ());
				world.addEntity(cloud);
				remove();

			}
			break;
		}
		case ENTITY: {
			if (!world.isRemote) {
				EntityBloodCloud cloud = new EntityBloodCloud(EntityInit.blood_cloud.get(), world, shooter);
				cloud.setPosition(getPosX(), getPosY() + 3, getPosZ());
				world.addEntity(cloud);
				remove();

			}
			break;
		}
		default: {
			remove();
			break;
		}
		}
	}

	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		Vector3d vector3d = (new Vector3d(x, y, z)).normalize().scale((double) velocity);
		this.setMotion(vector3d);
		float f = MathHelper.sqrt(horizontalMag(vector3d));
		this.rotationYaw = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
		this.rotationPitch = (float) (MathHelper.atan2(vector3d.y, (double) f) * (double) (180F / (float) Math.PI));
		this.prevRotationYaw = this.rotationYaw;
		this.prevRotationPitch = this.rotationPitch;
	}

	public void setDirectionMotion(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
		float f = -MathHelper.sin(y * ((float) Math.PI / 180F)) * MathHelper.cos(x * ((float) Math.PI / 180F));
		float f1 = -MathHelper.sin((x + z) * ((float) Math.PI / 180F));
		float f2 = MathHelper.cos(y * ((float) Math.PI / 180F)) * MathHelper.cos(x * ((float) Math.PI / 180F));
		this.shoot((double) f, (double) f1, (double) f2, velocity, inaccuracy);
		Vector3d vector3d = shooter.getMotion();
		this.setMotion(this.getMotion().add(vector3d.x, vector3d.y, vector3d.z));
	}

	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness() {
		return 1.0F;
	}

	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);

	}
}
