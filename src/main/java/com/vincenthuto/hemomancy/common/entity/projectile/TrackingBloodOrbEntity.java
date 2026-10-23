package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** A homing blood orb. */
public class TrackingBloodOrbEntity extends AbstractTrackingOrbEntity {
	public static EntityType<TrackingBloodOrbEntity> TYPE = EntityInit.tracking_blood_orb.get();

	public TrackingBloodOrbEntity(EntityType<TrackingBloodOrbEntity> type, Level world) {
		super(type, world);
	}

	public TrackingBloodOrbEntity(Level world) {
		this(TYPE, world);
	}

	public TrackingBloodOrbEntity(LivingEntity thrower, boolean evil) {
		super(TYPE, thrower, evil);
	}

	@Override
	protected void spawnTrail() {
		level().addParticle(HemoParticleData.glow(new ParticleColor(200, 0, 0)),
				getX() + HLParticleUtils.inRange(-0.1, 0.1), getY() + HLParticleUtils.inRange(-0.1, 0.1),
				getZ() + HLParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);
		level().addParticle(ParticleTypes.ASH, this.getX() + (Math.random() - 0.5) * 0.4,
				this.getY() + (Math.random() - 0.5) * 0.4, this.getZ() + (Math.random() - 0.5) * 0.4, 0, 0, 0);
		level().addParticle(DustParticleOptions.REDSTONE, this.getX() + (Math.random() - 0.5) * 0.4,
				this.getY() + (Math.random() - 0.5) * 0.4, this.getZ() + (Math.random() - 0.5) * 0.4, 0, 0, 0);
	}
}
