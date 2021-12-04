package com.vincenthuto.hemomancy.entity.blood;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

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
	@Override
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
	Vec3[] fibboSphere = HLParticleUtils.fibboSphere(globalPartCount, -level.getGameTime() * 0.01, 0.15);
	Vec3[] corona = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.15);
	Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -level.getGameTime() * 0.016, 0.15, false);

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

	@Override
	protected boolean canHitEntity(Entity p_230298_1_) {
		return super.canHitEntity(p_230298_1_) && !p_230298_1_.noPhysics;
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	/**
	 * Return the motion factor for this projectile. The factor is multiplied by the
	 * original motion.
	 */
	@Override
	protected float getInertia() {
		return 1F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.put("power", this.newDoubleList(new double[] { this.xPower, this.yPower, this.zPower }));
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	@Override
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
	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public float getPickRadius() {
		return 1.0F;
	}

	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			this.markHurt();
			Entity entity = source.getEntity();
			if (entity != null) {
				Vec3 Vec3 = entity.getLookAngle();
				this.setDeltaMovement(Vec3);
				this.xPower = Vec3.x * 0.1D;
				this.yPower = Vec3.y * 0.1D;
				this.zPower = Vec3.z * 0.1D;
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
	protected void onHit(@Nonnull HitResult pos) {
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

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		Vec3 Vec3 = (new Vec3(x, y, z)).normalize().scale(velocity);
		this.setDeltaMovement(Vec3);

		float f = Mth.sqrt(getHorizontalDistanceSqr(Vec3));
		this.yo = (float) (Mth.atan2(Vec3.x, Vec3.z) * (180F / (float) Math.PI));
		this.xo = (float) (Mth.atan2(Vec3.y, f) * (180F / (float) Math.PI));
		this.yRotO = (float) this.yo;
		this.xRotO = (float) this.xo;
	}

	private float getHorizontalDistanceSqr(Vec3 vec3) {
		return (float) (vec3.x * vec3.x + vec3.z * vec3.z);
	}

	public void setDirectionMotion(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
		float f = -Mth.sin(y * ((float) Math.PI / 180F)) * Mth.cos(x * ((float) Math.PI / 180F));
		float f1 = -Mth.sin((x + z) * ((float) Math.PI / 180F));
		float f2 = Mth.cos(y * ((float) Math.PI / 180F)) * Mth.cos(x * ((float) Math.PI / 180F));
		this.shoot(f, f1, f2, velocity, inaccuracy);
		Vec3 Vec3 = shooter.getDeltaMovement();
		this.setDeltaMovement(this.getDeltaMovement().add(Vec3.x, Vec3.y, Vec3.z));
	}

	/**
	 * Gets how bright this entity is.
	 */
	@Override
	public float getBrightness() {
		return 1.0F;
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);

	}
}
