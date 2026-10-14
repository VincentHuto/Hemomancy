package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Late in a rite, blood rains inside its boundary from under the sky wound: blood cells falling through the air,
 * with splashes bursting on the ground beneath. Splashes are spawned on the ground directly rather than tracking each
 * drop down to it.
 */
public final class RiteBloodRain {
	private static final double SPAWN_HEIGHT = 12.0D;
	private static final double SPAWN_SPREAD = 4.0D;
	/** The blood-cell particle doubles its speed and fades over its life; this falls about fourteen blocks. */
	private static final double FALL_SPEED = -0.4D;
	private static final double CULL_DISTANCE = 64.0D;
	private static final RandomSource RANDOM = RandomSource.create();

	private RiteBloodRain() {
	}

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.isPaused()) return;
		float particleScale = switch (mc.options.particles().get()) {
			case ALL -> 1.0F;
			case DECREASED -> 0.5F;
			case MINIMAL -> 0.0F;
		};
		if (particleScale <= 0.0F) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		for (RiteWorldState.World world : RiteWorldState.worlds()) {
			float rain = RiteWorldMath.rainLevel(world.level(1.0F), world.total())
					* RiteWorldMath.rainEnding(world.ending(), world.endingAge());
			int count = RiteWorldMath.rainPerTick(rain, world.footprint(), particleScale, world.strain());
			if (count <= 0) continue;
			BlockPos center = world.center();
			double cx = center.getX() + 0.5D;
			double cz = center.getZ() + 0.5D;
			double reach = CULL_DISTANCE + world.footprint();
			if ((cx - camera.x) * (cx - camera.x) + (cz - camera.z) * (cz - camera.z) > reach * reach) continue;
			ParticleOptions drop = HemoParticleData.bloodCell(colour(world.floor().palette().vein()));
			for (int i = 0; i < count; i++) {
				double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
				double radius = Math.sqrt(RANDOM.nextDouble()) * world.footprint();
				mc.level.addParticle(drop, cx + Math.cos(angle) * radius,
						center.getY() + 1.0D + SPAWN_HEIGHT + RANDOM.nextDouble() * SPAWN_SPREAD,
						cz + Math.sin(angle) * radius, 0.0D, FALL_SPEED, 0.0D);
			}
			ParticleOptions splash = HemoParticleData.bloodCell(colour(world.floor().palette().glow()));
			for (int i = 0; i < Math.max(1, count / 3); i++) {
				double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
				double radius = Math.sqrt(RANDOM.nextDouble()) * world.footprint();
				int x = Mth.floor(cx + Math.cos(angle) * radius);
				int z = Mth.floor(cz + Math.sin(angle) * radius);
				int top = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
				if (Math.abs(top - (center.getY() + 1)) > 6) continue;
				for (int k = 0; k < 3; k++) {
					mc.level.addParticle(splash, x + RANDOM.nextDouble(), top + 0.05D, z + RANDOM.nextDouble(),
							(RANDOM.nextDouble() - 0.5D) * 0.06D, 0.04D + RANDOM.nextDouble() * 0.03D,
							(RANDOM.nextDouble() - 0.5D) * 0.06D);
				}
			}
		}
	}

	private static ParticleColor colour(int argb) {
		return new ParticleColor((argb >> 16) & 255, (argb >> 8) & 255, argb & 255);
	}
}
