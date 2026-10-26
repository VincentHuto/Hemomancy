package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * How a Saint comes out of its sarcophagus: it starts sunk under the floor where the sarcophagus stood and rises over
 * {@link #SURFACED} ticks, grave-dust and a dark glow lifting round it, then stands until the fight begins at
 * {@link #TICKS}. Untouchable and still the whole time; the bar shows at the end. The renderers sink the body by
 * {@link #depth}.
 */
public final class SaintRise {
	public static final int TICKS = 60;
	public static final int SURFACED = 44;
	/** How far past its own height the Saint starts under the floor, in blocks. */
	public static final float START_MARGIN = 0.3F;

	private static final ParticleColor DUST = new ParticleColor(196, 180, 150);
	private static final ParticleColor SHADE = new ParticleColor(40, 22, 30);

	private SaintRise() {
	}

	/**
	 * Blocks below its feet the body is drawn at {@code t} ticks into the rise, for a Saint {@code height} blocks tall:
	 * the whole body starts under the floor; 0 once risen, or when not rising.
	 */
	public static float depth(float t, float height) {
		if (t < 0.0F) return 0.0F;
		return (height + START_MARGIN) * (1.0F - HemoEasing.smoothstep(HemoEasing.clamp01(t / SURFACED)));
	}

	/**
	 * Ticks into the rise from the synced tick, or -1 when not rising. The replay clock predicts forward from the last
	 * synced value, so a synced -1 must not be sampled: its prediction turns positive every few ticks.
	 */
	public static float time(BossReplayClock clock, int synced, int localTick, float partialTick) {
		return synced < 0 ? -1.0F : clock.sample(0, synced, localTick, partialTick, 1);
	}

	/** The dust lifting off the floor as it rises, and a dark burst as it surfaces. */
	public static void tickEffects(ServerLevel level, Entity saint, int t) {
		if (t < SURFACED && t % 3 == 0) {
			int points = 10;
			float radius = 1.1F + t * 0.012F;
			for (int i = 0; i < points; i++) {
				float angle = Mth.TWO_PI * i / points + t * 0.09F;
				level.sendParticles(HemoParticleData.glow(DUST), saint.getX() + Mth.cos(angle) * radius, saint.getY() + 0.1D,
						saint.getZ() + Mth.sin(angle) * radius, 1, 0.05D, 0.08D, 0.05D, 0.01D);
			}
			level.sendParticles(HemoParticleData.darkGlow(SHADE), saint.getX(), saint.getY() + 0.2D, saint.getZ(), 3,
					0.4D, 0.1D, 0.4D, 0.02D);
		}
		if (t == SURFACED) {
			level.sendParticles(HemoParticleData.darkGlow(SHADE), saint.getX(), saint.getY() + 1.0D, saint.getZ(), 26,
					0.7D, 1.0D, 0.7D, 0.05D);
			level.sendParticles(HemoParticleData.glow(DUST), saint.getX(), saint.getY() + 0.3D, saint.getZ(), 18,
					1.0D, 0.2D, 1.0D, 0.03D);
		}
	}
}
