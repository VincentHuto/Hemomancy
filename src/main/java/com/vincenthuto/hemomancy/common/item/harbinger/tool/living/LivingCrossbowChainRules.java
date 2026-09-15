package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

public final class LivingCrossbowChainRules {
	public static final int MAX_HOPS = 3;
	public static final double HOP_RANGE = 5.0D;
	private static final float[] DAMAGE_MULTIPLIERS = {0.75F, 0.50F, 0.25F};

	private LivingCrossbowChainRules() { }

	public record Candidate(UUID id, double x, double y, double z, boolean hostile, boolean visible) {
		private double distanceSquared(double fromX, double fromY, double fromZ) {
			double dx = x - fromX;
			double dy = y - fromY;
			double dz = z - fromZ;
			return dx * dx + dy * dy + dz * dz;
		}
	}

	public static float damageForHop(float initialDamage, int hop) {
		return hop >= 0 && hop < DAMAGE_MULTIPLIERS.length
				? Math.max(0.0F, initialDamage) * DAMAGE_MULTIPLIERS[hop]
				: 0.0F;
	}

	@Nullable
	public static Candidate nextHop(Collection<Candidate> candidates, double x, double y, double z,
			Set<UUID> visited) {
		double rangeSquared = HOP_RANGE * HOP_RANGE;
		return candidates.stream()
				.filter(candidate -> candidate.hostile && candidate.visible && !visited.contains(candidate.id))
				.filter(candidate -> candidate.distanceSquared(x, y, z) <= rangeSquared)
				.min(Comparator.comparingDouble((Candidate candidate) -> candidate.distanceSquared(x, y, z))
						.thenComparing(Candidate::id))
				.orElse(null);
	}
}
