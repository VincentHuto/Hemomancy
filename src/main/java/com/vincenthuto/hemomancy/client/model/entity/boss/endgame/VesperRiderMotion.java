package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperMountControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseOneAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWingedFlightRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** Which of Vesper's rider poses apply, and how strongly, for the Crowned Refusal's current state. */
public final class VesperRiderMotion {
	public enum Pose { SEATED, NECK_GRIP_TUCK, CROSSBAR_RAISE, WOUND_RECOIL, FLIGHT_LEAN, STAGGER }

	/** A snapshot of the entity state the rider reacts to. */
	public record State(VesperPhaseOneAttack attack, float attackTick, float staggerTicks, boolean wounded,
			VesperWingedFlightRules.FlightMode flight, boolean burrowing) {
	}

	private VesperRiderMotion() {
	}

	public static float weight(Pose pose, State state) {
		return switch (pose) {
			case SEATED -> 1.0F;
			case NECK_GRIP_TUCK -> state.burrowing() ? 1.0F : 0.0F;
			case CROSSBAR_RAISE -> crossbarRaise(state);
			case WOUND_RECOIL -> state.wounded() ? 1.0F : 0.0F;
			case FLIGHT_LEAN -> state.flight().airborne() ? 1.0F : 0.0F;
			case STAGGER -> Math.min(1.0F, state.staggerTicks() / VesperMountControllerRules.STAGGER_TICKS);
		};
	}

	private static float crossbarRaise(State state) {
		if (state.attack() != VesperPhaseOneAttack.PUPPET_MUSTER && state.attack() != VesperPhaseOneAttack.RECALL) {
			return 0.0F;
		}
		BossMoveTimeline timeline = VesperMountControllerRules.timeline(state.attack());
		float rise = HemoEasing.smoothstep(state.attackTick() / 10.0F);
		float fall = 1.0F - HemoEasing.smoothstep(
				(state.attackTick() - timeline.windup() - timeline.active()) / Math.max(1.0F, timeline.recovery()));
		return Math.min(rise, fall);
	}
}
