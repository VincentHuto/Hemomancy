package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureFeedClientData;
import com.vincenthuto.hemomancy.client.particle.factory.AbocipherParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class BloodStructureFeedSpiralParticles {
	private static final int ABOCIPHER_INTERVAL_TICKS = 2;
	private static final int BLOOD_CELL_INTERVAL_TICKS = 3;
	private static final int SPIRAL_PERIOD_TICKS = 62;
	private static final double SPIRAL_TURNS = 1.85D;

	private BloodStructureFeedSpiralParticles() {
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || ActiveBloodStructureFeedClientData.getActiveFeeds().isEmpty()) {
			return;
		}

		long gameTime = level.getGameTime();
		for (ActiveBloodStructureFeedClientData.FeedEntry feed : ActiveBloodStructureFeedClientData.getActiveFeeds()) {
			if (feed.getPositions().isEmpty()) {
				continue;
			}
			spawnForFeed(level, feed, gameTime);
		}
	}

	private static void spawnForFeed(ClientLevel level, ActiveBloodStructureFeedClientData.FeedEntry feed,
			long gameTime) {
		BloodStructureFeedBounds bounds = BloodStructureFeedBounds.from(feed.getPositions());
		float progress = feed.getProgress();
		if (gameTime % ABOCIPHER_INTERVAL_TICKS == 0L) {
			spawnAbocipher(level, bounds, progress, gameTime);
		}
		if (gameTime % BLOOD_CELL_INTERVAL_TICKS == 0L) {
			spawnBloodCell(level, bounds, progress, gameTime);
		}
	}

	private static void spawnAbocipher(ClientLevel level, BloodStructureFeedBounds bounds, float progress,
			long gameTime) {
		SpiralPoint point = spiralPoint(bounds, progress, gameTime, 0.0D);
		level.addParticle(AbocipherParticleFactory.createFeedData(), point.x(), point.y(), point.z(),
				point.xSpeed(), AbocipherParticleFactory.FEED_SPIRAL_MARKER, point.zSpeed());
	}

	private static void spawnBloodCell(ClientLevel level, BloodStructureFeedBounds bounds, float progress,
			long gameTime) {
		SpiralPoint point = spiralPoint(bounds, progress, gameTime, Mth.PI * 0.72D);
		level.addParticle(BloodCellParticleFactory.createFeedData(ParticleColor.BLOOD), point.x(), point.y(),
				point.z(), point.xSpeed() * 0.55D, BloodCellParticleFactory.FEED_SPIRAL_MARKER,
				point.zSpeed() * 0.55D);
	}

	private static SpiralPoint spiralPoint(BloodStructureFeedBounds bounds, float progress, long gameTime,
			double offset) {
		double seedAngle = bounds.seed() * Mth.TWO_PI;
		double climb = Math.floorMod(gameTime + (long) (offset * 11.0D) + (long) (bounds.seed() * 31.0F),
				SPIRAL_PERIOD_TICKS) / (double) SPIRAL_PERIOD_TICKS;
		double spiralAngle = seedAngle + offset + climb * Mth.TWO_PI * SPIRAL_TURNS;
		double radius = bounds.radius() * (0.92D + progress * 0.08D);
		double centerX = bounds.worldCenterX();
		double centerZ = bounds.worldCenterZ();
		double height = Math.max(1.4D, bounds.height() + 0.55D);
		double x = centerX + Math.cos(spiralAngle) * radius;
		double y = bounds.bottomY() + 0.25D + climb * height;
		double z = centerZ + Math.sin(spiralAngle) * radius;

		double tangentSpeed = 0.012D + progress * 0.006D;
		double inwardSpeed = 0.004D + progress * 0.004D;
		double xSpeed = -Math.sin(spiralAngle) * tangentSpeed - Math.cos(spiralAngle) * inwardSpeed;
		double zSpeed = Math.cos(spiralAngle) * tangentSpeed - Math.sin(spiralAngle) * inwardSpeed;
		return new SpiralPoint(x, y, z, xSpeed, zSpeed);
	}

	private record SpiralPoint(double x, double y, double z, double xSpeed, double zSpeed) {
	}
}
