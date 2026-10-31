package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A relay post at a depth-layer boundary (Track 8), where the Court's messengers hand off between depths. Its red
 * current threads drift in the water round it: a few slow glow motes peel off the post and wander downstream. Set
 * dressing only (decision L4): nothing is carried or signalled. Model from tools/model_export/relay_marker.py.
 */
public class RelayMarkerBlock extends CourtFixtureBlock {
	public static final double[][] OUTLINE = {{3.5, 0, 3.5, 12.5, 3, 12.5}, {6, 3, 6, 10, 16, 10}};
	private static final ParticleColor THREAD = new ParticleColor(196, 30, 44);
	private static final ParticleColor THREAD_HOT = new ParticleColor(236, 70, 70);

	public RelayMarkerBlock(Properties properties) {
		super(OUTLINE, properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextFloat() > 0.35f) return;
		// The current runs one way per post, so its threads trail together.
		float drift = (float) (Mth.getSeed(pos) & 7) / 8f * Mth.TWO_PI;
		double height = 0.4 + random.nextDouble() * 1.0;
		double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3;
		double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3;
		level.addParticle(HemoParticleData.glow(random.nextFloat() < 0.2f ? THREAD_HOT : THREAD), x, pos.getY() + height,
				z, Mth.cos(drift) * 0.012, 0.002 + random.nextDouble() * 0.004, Mth.sin(drift) * 0.012);
	}
}
