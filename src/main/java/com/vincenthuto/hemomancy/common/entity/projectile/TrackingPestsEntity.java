package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/** A homing swarm of pests. */
public class TrackingPestsEntity extends AbstractTrackingOrbEntity {
	public static EntityType<TrackingPestsEntity> TYPE = EntityInit.tracking_pests.get();

	public TrackingPestsEntity(EntityType<TrackingPestsEntity> type, Level world) {
		super(type, world);
	}

	public TrackingPestsEntity(Level world) {
		this(TYPE, world);
	}

	public TrackingPestsEntity(LivingEntity thrower, boolean evil) {
		super(TYPE, thrower, evil);
	}

	@Override
	protected void spawnTrail() {
		level().addParticle(HemoParticleData.glow(new ParticleColor(0, 150, 0)),
				getX() + HLParticleUtils.inRange(-0.25, 0.25), getY() + HLParticleUtils.inRange(-0.25, 0.25),
				getZ() + HLParticleUtils.inRange(-0.25, 0.25), 0, 0.000, 0);

		level().addParticle(HemoParticleData.glow(new ParticleColor(0, 250, 0)),
				getX() + HLParticleUtils.inRange(-0.25, 0.25), getY() + HLParticleUtils.inRange(-0.1, 0.1),
				getZ() + HLParticleUtils.inRange(-0.25, 0.25), 0, 0.000, 0);
	}
}
