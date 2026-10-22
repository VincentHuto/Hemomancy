package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Shapes and timing for a world rite's strike. A ground slam tears jagged cracks
 * out from the impact in a snap; they blaze red, cool, and leave black char
 * that fades last. A sky strike is a sharp red flash with a shock ring spreading
 * across the sky. Pure maths so it can be tested without a client.
 */
public final class WorldRiteFinaleRules {
	/** How long the cracks take to tear out to their full reach. */
	public static final int TEAR_TICKS = 5;
	/** The cracks blaze at full heat until here, then cool. */
	public static final int BLAZE_TICKS = 24;
	/** The last of the red is gone; only black char remains. */
	public static final int COOL_END_TICKS = 70;
	/** The char has faded and the strike is over. */
	public static final int LIFETIME_TICKS = 140;
	/** How long the sky flash and its ring last. */
	public static final int FLASH_TICKS = 24;

	private WorldRiteFinaleRules() {
	}

	/**
	 * The cracks of one slam, flat on the ground ({@code y = 0}) around the impact: a starburst of jagged
	 * main cracks, some forking, none reaching past {@code reach}. The same seed always cracks the same way.
	 */
	public static List<List<Vec3>> cracks(int seed, double reach) {
		Random random = new Random(seed);
		int count = 9 + random.nextInt(4);
		List<List<Vec3>> cracks = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			double angle = i * Math.PI * 2.0D / count + (random.nextDouble() - 0.5D) * 0.5D;
			double length = reach * (0.65D + 0.35D * random.nextDouble());
			int steps = 6;
			List<Vec3> main = new ArrayList<>(steps + 1);
			for (int k = 0; k <= steps; k++) {
				double distance = 0.35D + (length - 0.35D) * k / steps;
				angle += random.nextGaussian() * 0.16D;
				main.add(within(new Vec3(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance), reach));
			}
			cracks.add(main);
			if (random.nextFloat() < 0.6F) {
				Vec3 fork = main.get(3);
				double forkAngle = Math.atan2(fork.z, fork.x) + (random.nextBoolean() ? 0.6D : -0.6D);
				double step = length * 0.15D;
				List<Vec3> branch = new ArrayList<>(4);
				branch.add(fork);
				for (int k = 1; k <= 3; k++) {
					forkAngle += random.nextGaussian() * 0.1D;
					Vec3 previous = branch.get(k - 1);
					branch.add(within(previous.add(Math.cos(forkAngle) * step, 0.0D, Math.sin(forkAngle) * step), reach));
				}
				cracks.add(branch);
			}
		}
		return cracks;
	}

	private static Vec3 within(Vec3 point, double reach) {
		double distance = Math.hypot(point.x, point.z);
		return distance <= reach ? point : new Vec3(point.x * reach / distance * 0.999D, 0.0D, point.z * reach / distance * 0.999D);
	}

	/** How far along their length the cracks have torn, {@code age} ticks after the slam. */
	public static float crackReach(float age) {
		float t = HemoEasing.clamp01(age / TEAR_TICKS);
		return 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
	}

	/** The red heat in the cracks. */
	public static float blaze(float age) {
		if (age <= BLAZE_TICKS) return 1.0F;
		return 1.0F - HemoEasing.smoothstep((age - BLAZE_TICKS) / (COOL_END_TICKS - BLAZE_TICKS));
	}

	/** The black char along the cracks, outlasting the heat. */
	public static float charred(float age) {
		if (age <= COOL_END_TICKS) return 1.0F;
		return 1.0F - HemoEasing.smoothstep((age - COOL_END_TICKS) / (LIFETIME_TICKS - COOL_END_TICKS));
	}

	/** The sky flash: full at the strike, gone almost at once. */
	public static float skyFlash(float age) {
		float t = HemoEasing.clamp01(age / FLASH_TICKS);
		return (1.0F - t) * (1.0F - t) * (1.0F - t);
	}

	/** The shock ring spreading across the sky from the strike. */
	public static double ringRadius(float age) {
		float t = HemoEasing.clamp01(age / FLASH_TICKS);
		return 4.0D + 46.0D * (1.0D - (1.0D - t) * (1.0D - t));
	}
}
