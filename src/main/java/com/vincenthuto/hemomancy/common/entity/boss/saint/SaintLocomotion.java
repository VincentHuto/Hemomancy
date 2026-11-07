package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;

/**
 * How a Saint moves between and during its moves: a stalk, a chained glide on a tether, a shamble, near-stillness. The
 * controller owns when it moves; the locomotion owns how, through the {@link SaintMotor}, which clamps every motion to
 * the arena's leash.
 */
public interface SaintLocomotion {
	/** The distance bands its picker reads; every Saint uses {@link BossBands#STANDARD} (resolution B-1). */
	default BossBands bands() {
		return BossBands.STANDARD;
	}

	/** Between moves: approach, hold or circle. */
	void roam(SaintTrialEntity saint, SaintMotor motor, @Nullable LivingEntity target);

	/** During a move, after the controller has faced the aim. */
	default void inMove(SaintTrialEntity saint, SaintMotor motor, SaintMoveContext ctx) {
	}

	/** False while the Saint may not pick (Seraphae reeled in, Velorum's settle). */
	default boolean mayPick(SaintTrialEntity saint) {
		return true;
	}

	/** Default: the challenger who hurt it in the last 100 ticks, else the nearest one in sight, else the nearest. */
	default @Nullable LivingEntity chooseTarget(SaintTrialEntity saint, List<Player> challengers,
			@Nullable LivingEntity current) {
		return SaintCombatController.defaultTarget(saint, challengers);
	}

	/** Degrees per tick it may turn; {@code trackingWindup} while a wind-up still follows its target. */
	float turnRate(SaintTrialEntity saint, boolean trackingWindup);

	/** Ticks between a move's end and the next pick when no follow-up fires. */
	int decisionDelay(SaintTrialEntity saint);

	/** Standing still, turning slowly: what an unconfigured kit Saint does. */
	SaintLocomotion STILL = new SaintLocomotion() {
		@Override
		public void roam(SaintTrialEntity saint, SaintMotor motor, @Nullable LivingEntity target) {
			motor.stop();
		}

		@Override
		public float turnRate(SaintTrialEntity saint, boolean trackingWindup) {
			return 6.0F;
		}

		@Override
		public int decisionDelay(SaintTrialEntity saint) {
			return 20;
		}
	};
}
