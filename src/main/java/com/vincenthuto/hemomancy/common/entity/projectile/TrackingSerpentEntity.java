package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** A slow, weaving serpent that binds the blood of what it strikes. */
public class TrackingSerpentEntity extends AbstractTrackingOrbEntity {
	public static EntityType<TrackingSerpentEntity> TYPE = EntityInit.tracking_snake.get();

	public TrackingSerpentEntity(EntityType<TrackingSerpentEntity> type, Level world) {
		super(type, world);
	}

	public TrackingSerpentEntity(Level world) {
		this(TYPE, world);
	}

	public TrackingSerpentEntity(LivingEntity thrower, boolean evil) {
		super(TYPE, thrower, evil);
	}

	@Override
	protected void spawnTrail() {
		level().addParticle(HemoParticleData.serpent(new ParticleColor(100, 100, 0)),
				this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
				this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
		level().addParticle(HemoParticleData.serpent(new ParticleColor(100, 100, 0)),
				this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
				this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
		for (int i = 0; i < 10; i++) {
			level().addParticle(HemoParticleData.serpent(new ParticleColor(255, 50, 50)),
					this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
					this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			level().addParticle(HemoParticleData.serpent(new ParticleColor(255, 50, 50)),
					this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
					this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			level().addParticle(HemoParticleData.serpent(new ParticleColor(255, 50, 50)),
					this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
					this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
		}
	}

	@Override
	protected double speed(boolean evil) {
		// This changes how angular the beam gets
		return evil ? 0.15 : 0.6;
	}

	@Override
	protected void liftEarly() {
		setDeltaMovement(getDeltaMovement().x() + HLParticleUtils.inRange(-0.15, 0.15),
				Math.abs(getDeltaMovement().y()), getDeltaMovement().z() + HLParticleUtils.inRange(-0.15, 0.15));
	}

	@Override
	protected float damage(boolean evil) {
		return 2;
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		if (hit.getEntity() == getTargetEntity())
			((LivingEntity) hit.getEntity()).addEffect(new MobEffectInstance(EffectInit.blood_binding, 300));
		this.remove(RemovalReason.KILLED);
	}
}
