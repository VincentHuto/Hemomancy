package com.vincenthuto.hemomancy.entity.blood;

import javax.annotation.Nonnull;

import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.vincenthuto.hemomancy.init.EntityInit;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class EntityBloodOrbDirected extends ThrowableProjectile {
	public static EntityType<EntityBloodOrbDirected> TYPE = EntityInit.directed_blood_orb.get();

	public EntityBloodOrbDirected(EntityType<EntityBloodOrbDirected> type, Level world) {
		super(type, world);
	}

	public EntityBloodOrbDirected(Level world) {
		this(TYPE, world);
	}

	public EntityBloodOrbDirected(LivingEntity thrower, boolean evil) {
		super(TYPE, thrower, thrower.level);
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide) {
			level.addParticle(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
					getX() + ParticleUtils.inRange(-0.1, 0.1), getY() + ParticleUtils.inRange(-0.1, 0.1),
					getZ() + ParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);
			level.addParticle(ParticleTypes.ASH, this.getX() + (Math.random() - 0.5) * 0.4,
					this.getY() + (Math.random() - 0.5) * 0.4, this.getZ() + (Math.random() - 0.5) * 0.4, 0, 0, 0);
			level.addParticle(DustParticleOptions.REDSTONE, this.getX() + (Math.random() - 0.5) * 0.4,
					this.getY() + (Math.random() - 0.5) * 0.4, this.getZ() + (Math.random() - 0.5) * 0.4, 0, 0, 0);

		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
	}

	@Override
	protected void onHit(@Nonnull HitResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
			if (!level.isClientSide) {
				this.level.explode(this, this.getX(), this.getY() + this.getBbHeight() / 16.0F, this.getZ(), 3.0F,
						Explosion.BlockInteraction.DESTROY);
			}
			this.remove(RemovalReason.KILLED);
			break;
		}
		case ENTITY: {
			if (!level.isClientSide) {
				this.level.explode(this, this.getX(), this.getY() + this.getBbHeight() / 16.0F, this.getZ(), 3.0F,
						Explosion.BlockInteraction.NONE);
			}
			this.remove(RemovalReason.KILLED);
			break;
		}
		default: {
			this.remove(RemovalReason.KILLED);
			break;
		}
		}
	}

	/**
	 * Similar to setArrowHeading, it's point the throwable entity to a x, y, z
	 * direction.
	 */
	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		Vec3 vector3d = (new Vec3(x, y, z)).normalize().add(this.random.nextGaussian() * 0.0075F * inaccuracy,
				this.random.nextGaussian() * 0.0075F * inaccuracy, this.random.nextGaussian() * 0.0075F * inaccuracy)
				.scale(velocity);
		this.setDeltaMovement(vector3d);
		float f = Mth.sqrt(getHorizontalDistanceSqr(vector3d));
		this.yo = (float) (Mth.atan2(vector3d.x, vector3d.z) * (180F / (float) Math.PI));
		this.xo = (float) (Mth.atan2(vector3d.y, f) * (180F / (float) Math.PI));
		this.yRotO = (float) this.yo;
		this.xRotO = (float) this.xo;
	}

	private float getHorizontalDistanceSqr(Vec3 vec3) {
		return (float) (vec3.x * vec3.x + vec3.z * vec3.z);
	}

	@Override
	public void shootFromRotation(Entity p_234612_1_, float p_234612_2_, float p_234612_3_, float p_234612_4_,
			float p_234612_5_, float p_234612_6_) {
		float f = -Mth.sin(p_234612_3_ * ((float) Math.PI / 180F)) * Mth.cos(p_234612_2_ * ((float) Math.PI / 180F));
		float f1 = -Mth.sin((p_234612_2_ + p_234612_4_) * ((float) Math.PI / 180F));
		float f2 = Mth.cos(p_234612_3_ * ((float) Math.PI / 180F)) * Mth.cos(p_234612_2_ * ((float) Math.PI / 180F));
		this.shoot(f, f1, f2, p_234612_5_, p_234612_6_);
		Vec3 vector3d = p_234612_1_.getDeltaMovement();
		this.setDeltaMovement(
				this.getDeltaMovement().add(vector3d.x, p_234612_1_.isOnGround() ? 0.0D : vector3d.y, vector3d.z));
	}

	@Override
	protected void defineSynchedData() {
	}

}