package com.huto.hemomancy.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.entity.blood.EntityBloodCloud;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.math.Vector3;
import com.mojang.math.Vector3d;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityBloodCloudCarrier extends AbstractHurtingProjectile {

	public EntityBloodCloudCarrier(EntityType<? extends EntityBloodCloudCarrier> ent, Level world) {
		super(ent, world);
	}

	LivingEntity shooter;

	public EntityBloodCloudCarrier(Level worldIn, LivingEntity shooter) {
		super(EntityInit.blood_cloud_carrier.get(), worldIn);
		this.shooter = shooter;
	}

	public EntityBloodCloudCarrier(Level worldIn, LivingEntity shooter, double aX, double aY, double aZ) {
		super(EntityInit.blood_cloud_carrier.get(), shooter, aZ, aZ, aZ, worldIn);
		this.shooter = shooter;
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderAtSqrDistance(double distance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
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
	Vec3[] fibboSphere = ParticleUtils.fibboSphere(globalPartCount, -level.getGameTime() * 0.01, 0.15);
	Vector3d[] corona = ParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.15);
	Vector3d[] inversedSphere = ParticleUtils.inversedSphere(globalPartCount, -level.getGameTime() * 0.016, 0.15,
			false);

	@Override
	public void tick() {
		super.tick();
		Vector3 pos = Vector3.fromEntityCenter(this);
		if (!level.isClientSide) {
			ServerLevel sLevel = (ServerLevel) level;
			for (int i = 0; i < globalPartCount; i++) {
				sLevel.sendParticles(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						pos.x + inversedSphere[i].x, pos.y + inversedSphere[i].y, pos.z + inversedSphere[i].z, 3, 0.0,
						0.1, 0.00, 0.002d);
			}
		}

		if (tickCount > 50) {

			if (!level.isClientSide) {
				EntityBloodCloud cloud = new EntityBloodCloud(EntityInit.blood_cloud.get(), level, shooter);
				cloud.setPos(getX(), getY(), getZ());
				level.addFreshEntity(cloud);
							this.remove(RemovalReason.KILLED);

			}
		}

	}

	protected boolean canHitEntity(Entity p_230298_1_) {
		return super.canHitEntity(p_230298_1_) && !p_230298_1_.noPhysics;
	}

	protected boolean shouldBurn() {
		return false;
	}

	/**
	 * Return the motion factor for this projectile. The factor is multiplied by the
	 * original motion.
	 */
	protected float getInertia() {
		return 1F;
	}

	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.put("power", this.newDoubleList(new double[] { this.xPower, this.yPower, this.zPower }));
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("power", 9)) {
			ListTag listnbt = compound.getList("power", 6);
			if (listnbt.size() == 3) {
				this.xPower = listnbt.getDouble(0);
				this.yPower = listnbt.getDouble(1);
				this.zPower = listnbt.getDouble(2);
			}
		}

	}

	/**
	 * Returns true if other Entities should be prevented from moving through this
	 * Entity.
	 */
	public boolean isPickable() {
		return true;
	}

	public float getPickRadius() {
		return 1.0F;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean hurt(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			this.markHurt();
			Entity entity = source.getEntity();
			if (entity != null) {
				Vector3d vector3d = entity.getLookAngle();
				this.setDeltaMovement(vector3d);
				this.xPower = vector3d.x * 0.1D;
				this.yPower = vector3d.y * 0.1D;
				this.zPower = vector3d.z * 0.1D;
				this.setOwner(entity);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	protected ParticleOptions getTrailParticle() {
		return BloodCellParticleFactory.createData(ParticleColor.RED);
	}

	@Override
	protected void onHit(@Nonnull RayTraceResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
			if (!level.isClientSide) {
				EntityBloodCloud cloud = new EntityBloodCloud(EntityInit.blood_cloud.get(), level, shooter);
				cloud.setPos(getX(), getY(), getZ());
				level.addFreshEntity(cloud);
							this.remove(RemovalReason.KILLED);

			}
			break;
		}
		case ENTITY: {
			if (!level.isClientSide) {
				EntityBloodCloud cloud = new EntityBloodCloud(EntityInit.blood_cloud.get(), level, shooter);
				cloud.setPos(getX(), getY() + 3, getZ());
				level.addFreshEntity(cloud);
							this.remove(RemovalReason.KILLED);

			}
			break;
		}
		default: {
						this.remove(RemovalReason.KILLED);
			break;
		}
		}
	}

	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		Vector3d vector3d = (new Vector3d(x, y, z)).normalize().scale((double) velocity);
		this.setDeltaMovement(vector3d);
		float f = Mth.sqrt(getHorizontalDistanceSqr(vector3d));
		this.yRot = (float) (Mth.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
		this.xRot = (float) (Mth.atan2(vector3d.y, (double) f) * (double) (180F / (float) Math.PI));
		this.yRotO = this.yRot;
		this.xRotO = this.xRot;
	}

	public void setDirectionMotion(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
		float f = -Mth.sin(y * ((float) Math.PI / 180F)) * Mth.cos(x * ((float) Math.PI / 180F));
		float f1 = -Mth.sin((x + z) * ((float) Math.PI / 180F));
		float f2 = Mth.cos(y * ((float) Math.PI / 180F)) * Mth.cos(x * ((float) Math.PI / 180F));
		this.shoot((double) f, (double) f1, (double) f2, velocity, inaccuracy);
		Vector3d vector3d = shooter.getDeltaMovement();
		this.setDeltaMovement(this.getDeltaMovement().add(vector3d.x, vector3d.y, vector3d.z));
	}

	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness() {
		return 1.0F;
	}

	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);

	}
}
