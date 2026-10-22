package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The double helix that rises from the Focus as a rite progresses, spawned on
 * the client from each synced rite rather than sent as particle packets by the
 * server. Harbinger rites climb in blood cells; Unstained rites in pale
 * silver-teal motes. The helix thins away as culmination takes over.
 */
public final class CardinalRiteHelixEffects {
	private static final int SPAWN_INTERVAL_TICKS = 2;
	private static final int POINTS_PER_STRAND = 8;
	private static final double STRAND_RADIUS = 0.5D;
	private static final double CULL_DISTANCE = 48.0D;
	private static final float CULMINATION_FADE_TICKS = 20.0F;
	private static final ParticleOptions HARBINGER_CELL = HemoParticleData.bloodCell(new ParticleColor(200, 0, 0));
	private static final ParticleOptions HARBINGER_CLOT = HemoParticleData.bloodCell(new ParticleColor(122, 16, 32));
	private static final ParticleOptions HARBINGER_TIP = HemoParticleData.glow(new ParticleColor(170, 12, 24));
	private static final ParticleOptions UNSTAINED_MOTE = HemoParticleData.glow(new ParticleColor(184, 218, 214));
	private static final ParticleOptions UNSTAINED_TIP = HemoParticleData.glow(new ParticleColor(222, 236, 238));
	private static final RandomSource RANDOM = RandomSource.create();
	private static long lastSpawnTick = Long.MIN_VALUE;

	private CardinalRiteHelixEffects() {
	}

	public static void tick(Minecraft mc, List<ActiveRiteData.RiteEntry> rites, float time, Vec3 camera) {
		if (mc.level == null || rites.isEmpty() || mc.isPaused()) return;
		long gameTick = (long) Math.floor(time);
		if (lastSpawnTick != Long.MIN_VALUE && gameTick >= lastSpawnTick
				&& gameTick - lastSpawnTick < SPAWN_INTERVAL_TICKS) {
			return;
		}
		lastSpawnTick = gameTick;
		ParticleStatus status = mc.options.particles().get();
		if (status == ParticleStatus.MINIMAL) return;
		boolean decreased = status == ParticleStatus.DECREASED;
		for (ActiveRiteData.RiteEntry rite : rites) {
			spawnHelix(mc, rite, gameTick, camera, decreased);
		}
	}

	private static void spawnHelix(Minecraft mc, ActiveRiteData.RiteEntry rite, long gameTick,
			Vec3 camera, boolean decreased) {
		BlockPos center = rite.getCenter();
		double cx = center.getX() + 0.5D;
		double cy = center.getY() + 1.0D;
		double cz = center.getZ() + 0.5D;
		if (camera.distanceToSqr(cx, cy, cz) > CULL_DISTANCE * CULL_DISTANCE) return;
		float progress = (float) HemoEasing.clamp01(rite.getProgress());
		double height = (3.0D + rite.getRiteSize() * 0.5D) * progress;
		if (height < 0.05D) return;
		float density = rite.isPhase(CardinalRitePhase.CULMINATION)
				? 1.0F - HemoEasing.smoothstep(rite.getPhaseTicks() / CULMINATION_FADE_TICKS)
				: 1.0F;
		if (density <= 0.05F) return;
		boolean unstained = rite.isUnstained();
		double spin = gameTick * 0.15D;
		for (int point = 0; point < POINTS_PER_STRAND; point++) {
			if (decreased && (point & 1) == 1) continue;
			double h = height * point / POINTS_PER_STRAND;
			for (int strand = 0; strand < 2; strand++) {
				if (density < 1.0F && RANDOM.nextFloat() > density) continue;
				double angle = spin + h * 2.0D + strand * Math.PI;
				double x = cx + Math.cos(angle) * STRAND_RADIUS + RANDOM.nextGaussian() * 0.02D;
				double y = cy + h + RANDOM.nextGaussian() * 0.02D;
				double z = cz + Math.sin(angle) * STRAND_RADIUS + RANDOM.nextGaussian() * 0.02D;
				ParticleOptions options = unstained ? UNSTAINED_MOTE
						: (point + strand) % 4 == 0 ? HARBINGER_CLOT : HARBINGER_CELL;
				mc.level.addParticle(options, x, y, z, 0.0D, 0.0D, 0.0D);
			}
		}
		if (!decreased && height > 0.5D) {
			double tipAngle = spin + height * 2.0D;
			mc.level.addParticle(unstained ? UNSTAINED_TIP : HARBINGER_TIP,
					cx + Math.cos(tipAngle) * STRAND_RADIUS * 0.5D, cy + height,
					cz + Math.sin(tipAngle) * STRAND_RADIUS * 0.5D, 0.0D, 0.01D, 0.0D);
		}
	}
}
