package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.particle.AbsorbedBloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffMorphSequence;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public final class LivingStaffMorphEffects {
	private static final Map<Key, Long> LAST_EMISSION = new HashMap<>();
	private static ClientLevel activeLevel;

	private LivingStaffMorphEffects() {
	}

	public static void emit(LivingEntity holder, HumanoidArm arm, Vec3 origin,
			LivingStaffMorphSequence.Phase phase) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!(holder.level() instanceof ClientLevel level) || minecraft.isPaused()) return;
		if (activeLevel != level) {
			LAST_EMISSION.clear();
			activeLevel = level;
		}
		long gameTime = level.getGameTime();
		Key key = new Key(holder.getId(), arm);
		Long previousEmission = LAST_EMISSION.put(key, gameTime);
		if (previousEmission != null && previousEmission == gameTime) return;

		if (phase == LivingStaffMorphSequence.Phase.DISSOLVE) {
			Particle particle = minecraft.particleEngine.createParticle(
					AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
					origin.x, origin.y, origin.z,
					(level.random.nextDouble() - 0.5D) * 0.34D,
					(level.random.nextDouble() - 0.35D) * 0.28D,
					(level.random.nextDouble() - 0.5D) * 0.34D);
			if (particle instanceof AbsorbedBloodCellParticle absorbed) absorbed.setTargetYOffset(0.0D);
		} else if (phase == LivingStaffMorphSequence.Phase.FORM) {
			level.addParticle(BloodCellParticleFactory.createData(ParticleColor.BLOOD),
					origin.x, origin.y, origin.z,
					(level.random.nextDouble() - 0.5D) * 0.045D,
					0.01D + level.random.nextDouble() * 0.025D,
					(level.random.nextDouble() - 0.5D) * 0.045D);
		}
	}

	private record Key(int entityId, HumanoidArm arm) {
	}
}
