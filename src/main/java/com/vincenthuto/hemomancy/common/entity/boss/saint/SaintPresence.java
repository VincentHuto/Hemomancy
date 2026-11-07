package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.world.entity.player.Player;

/**
 * The proximity field a Saint carries: Hemorath drains, Seraphae over-heals, Putriciel colonises, Velorum chills. The
 * kit runs it over {@code arena.challengers()} only, through {@code canHarmFor}, and not during the rise, a phase card,
 * the ending, or while the Saint's {@code presencePaused()} says so. The radius and cadence are read live, so a phase
 * or a deed can change them.
 */
public interface SaintPresence {
	SaintPresence NONE = new SaintPresence() {
		@Override
		public double radius(SaintTrialEntity saint) {
			return 0.0D;
		}

		@Override
		public int cadence(SaintTrialEntity saint) {
			return 20;
		}

		@Override
		public void pulse(SaintTrialEntity saint, Player player, float strength) {
		}
	};

	/** Read every tick: the Last Thirst's 18. */
	double radius(SaintTrialEntity saint);

	default double verticalReach(SaintTrialEntity saint) {
		return 4.0D;
	}

	/** Read every pulse: the Repose deed's 14. */
	int cadence(SaintTrialEntity saint);

	default boolean needsSight(SaintTrialEntity saint) {
		return true;
	}

	/** Strength at a horizontal distance; 1 everywhere by default. */
	default float falloff(SaintTrialEntity saint, double distance) {
		return 1.0F;
	}

	/** A per-player factor on the strength (the Apotheos x1.25). */
	default float playerFactor(SaintTrialEntity saint, Player player) {
		return 1.0F;
	}

	/** Every tick for each player in the field (Velorum's multipliers). */
	default void everyTick(SaintTrialEntity saint, Player player, float strength) {
	}

	/** Every {@link #cadence} ticks for each player in the field. */
	void pulse(SaintTrialEntity saint, Player player, float strength);
}
