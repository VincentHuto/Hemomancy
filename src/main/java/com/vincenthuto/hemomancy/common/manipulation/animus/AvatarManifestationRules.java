package com.vincenthuto.hemomancy.common.manipulation.animus;

import java.util.Optional;

public final class AvatarManifestationRules {
	private AvatarManifestationRules() {
	}

	public static boolean isAvatarForm(String manipulationId) {
		return stats(manipulationId).isPresent();
	}

	public static Optional<Stats> stats(String manipulationId) {
		return Optional.ofNullable(switch (manipulationId == null ? "" : manipulationId) {
			case "summon_avatar" -> new Stats(0, .25D, 0, 0, 0, 0, 0, 0, 0, 0);
			case "summon_avatar_arms" -> new Stats(1, .25D, 4, .5D, 0, 0, 0, 1.5D, 1, 0);
			case "summon_avatar_armor" -> new Stats(2, .45D, 4, .5D, 0, 0, 0, 1.5D, 1, 0);
			case "summon_avatar_legs" -> new Stats(3, .45D, 4, .5D, .20D, .5D, .20D, 1.5D, 1, 0);
			case "summon_avatar_complete" -> new Stats(4, .65D, 8, 1, .35D, .75D, .35D, 4.5D, 3, 1);
			default -> null;
		});
	}

	public record Stats(int stage, double damageReduction, double attackDamage, double attackKnockback,
			double movementSpeed, double stepHeight, double jumpStrength, double blockReach,
			double entityReach, double sizeBonus) {
		public float playerVisualScale(float entityScale) {
			return entityScale / avatarVisualScale();
		}

		public float avatarVisualScale() {
			return (float) (1.0D + sizeBonus);
		}

		public float playerChestLift() {
			return (float) (0.9D * sizeBonus);
		}
	}
}
