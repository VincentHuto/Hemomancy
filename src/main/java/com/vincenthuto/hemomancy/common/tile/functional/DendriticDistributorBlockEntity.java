package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.client.particle.data.BoltParticleData;
import com.vincenthuto.hutoslib.client.particle.data.BoltParticleData.FadeFunction;
import com.vincenthuto.hutoslib.client.particle.data.BoltParticleData.SpawnFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DendriticDistributorBlockEntity extends BlockEntity {
	private static final int BOLT_CADENCE_TICKS = 4;
	private static final int BOLTS_PER_PULSE = 7;
	private static final double EFFECT_VIEW_DISTANCE = 48.0D;
	private static final double ANTENNA_HEIGHT = 1.12D;
	private static final double MIN_RADIUS = 0.15D;
	private static final double MAX_RADIUS = 0.85D;
	private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
	private static final int OUTER_YELLOW = 0xE8FFE84A;
	private static final int INNER_WHITE = 0xFFFFFFFF;

	public DendriticDistributorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.dendritic_distributor.get(), pos, state);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, DendriticDistributorBlockEntity te) {
		if (level.getGameTime() % BOLT_CADENCE_TICKS != 0) {
			return;
		}
		Vec3 origin = Vec3.atLowerCornerOf(pos).add(0.5D, ANTENNA_HEIGHT, 0.5D);
		if (level.getNearestPlayer(origin.x, origin.y, origin.z, EFFECT_VIEW_DISTANCE, false) == null) {
			return;
		}

		double pulseProgress = (level.getGameTime() % 24L) / 24.0D;
		double shellRadius = MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * pulseProgress;
		double rotation = level.getGameTime() * 0.23D + pos.asLong() * 0.00001D;
		for (int i = 0; i < BOLTS_PER_PULSE; i++) {
			Vec3 direction = sphericalDirection(i, BOLTS_PER_PULSE, rotation);
			double jitter = 0.85D + level.random.nextDouble() * 0.3D;
			Vec3 end = origin.add(direction.scale(shellRadius * jitter));
			long seed = level.random.nextLong() ^ pos.asLong() ^ (level.getGameTime() << 16) ^ i;
			spawnBolt(origin, end, seed);
		}
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			DendriticDistributorBlockEntity te) {

	}

	private static Vec3 sphericalDirection(int index, int count, double rotation) {
		double y = 1.0D - (index + 0.5D) * 2.0D / count;
		double horizontalRadius = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
		double theta = index * GOLDEN_ANGLE + rotation;
		return new Vec3(Math.cos(theta) * horizontalRadius, y, Math.sin(theta) * horizontalRadius);
	}

	private static void spawnBolt(Vec3 origin, Vec3 end, long seed) {
		BoltRenderer.INSTANCE.add(createBolt(origin, end, seed, OUTER_YELLOW, 0.055F), 0.0F);
		BoltRenderer.INSTANCE.add(createBolt(origin, end, seed, INNER_WHITE, 0.024F), 0.0F);
	}

	private static BoltParticleData createBolt(Vec3 origin, Vec3 end, long seed, int color, float size) {
		BoltParticleData base = new BoltParticleData(origin, end, seed, color);
		BoltParticleData.BoltRenderInfo info = base.getRenderInfo()
				.noise(0.2F, 0.18F)
				.branching(0.12F, 0.45F)
				.spreader(BoltParticleData.SegmentSpreader.memory(0.35F));
		return new BoltParticleData(info, origin, end, 8, 1, size, 10, SpawnFunction.NO_DELAY,
				FadeFunction.fade(0.45F), seed);
	}
}
