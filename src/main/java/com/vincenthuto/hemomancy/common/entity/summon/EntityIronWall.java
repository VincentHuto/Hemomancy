package com.vincenthuto.hemomancy.common.entity.summon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityIronWall extends FerricConstructEntity {
	public float deathTicks = 1;

	public EntityIronWall(EntityType<? extends EntityIronWall> type, Level worldIn) {
		super(type, worldIn);

	}

	public EntityIronWall(EntityType<? extends EntityIronWall> type, Level worldIn, LivingEntity creator) {
		super(type, worldIn);
		this.creator = creator;
	}

	@Override
	protected void doPush(Entity entityIn) {
		if (isPlayerConstruct()) return;
		if (!(entityIn instanceof EntityIronWall)) {
			if (getCreator() != null) {
				if (entityIn != creator) {
					Vec3 p = new Vec3(getX() + 0.5, getY(), getZ() + 0.5);
					Vec3 t = new Vec3(entityIn.getX(), entityIn.getY(), entityIn.getZ());
					double distance = p.distanceTo(t) + 0.1D;
					Vec3 r = new Vec3(t.x - p.x, t.y - p.y, t.z - p.z);
					entityIn.setDeltaMovement(r.x * 2 / distance, r.y * 2 / distance, r.z * 2 / distance);
				}
			} else {
				Vec3 p = new Vec3(getX() + 0.5, getY(), getZ() + 0.5);
				Vec3 t = new Vec3(entityIn.getX(), entityIn.getY(), entityIn.getZ());
				double distance = p.distanceTo(t) + 0.1D;
				Vec3 r = new Vec3(t.x - p.x, t.y - p.y, t.z - p.z);
				entityIn.setDeltaMovement(r.x * 2 / distance, r.y * 2 / distance, r.z * 2 / distance);
			}

		}

	}

	@Override
	public void tick() {
		super.tick();
		if (isPlayerConstruct()) return;
		this.setYBodyRot(0);
		// Particle MobEffects
		float f = (this.random.nextFloat() - 0.5F) * 2.0F;
		float f1 = -1;
		float f2 = (this.random.nextFloat() - 0.5F) * 2.0F;
		float f3 = (this.random.nextFloat() - 0.5F) * 1.5F;
		if (this.tickCount < 2) {
			FerricConstructEffects.erupt(level(), random, this.getX() + f, this.getY() + 2.0D + f1, this.getZ() + f2);
		}
		if (this.tickCount > 2 && this.tickCount < 120) {
			for (int i = 0; i < 2; i++) {
				FerricConstructEffects.seep(level(), this.getX() + f * 0.5, this.getY(), this.getZ() + f2 * 0.5);
				FerricConstructEffects.flake(level(), this.getX() + f, this.getY() + i + f3, this.getZ() + f2);
			}
		}
		if (this.tickCount == 120) {
			FerricConstructEffects.expire(level(), random, this.getX() + f, this.getY() + 2.0D + f1, this.getZ() + f2);
			this.setHealth(0);

			playConstructExpirationSound();

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
				FerricConstructEffects.dissolve(level(), random, this.getX() + g, this.getY() + 2.0D + g1, this.getZ() + g2);
			}
		}

		if (this.deathTicks <= 0.1 && !this.level().isClientSide) {
			this.remove(RemovalReason.KILLED);
		}

	}
}
