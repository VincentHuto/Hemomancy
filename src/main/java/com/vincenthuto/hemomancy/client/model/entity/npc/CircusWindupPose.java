package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Reads a performer's defense move for its model. The server starts the wind-up event on the move's first tick; the
 * model coils through the wind-up ({@link #charge}) and snaps through the strike and follow-through ({@link #strike}),
 * both timed by the same {@link BossMoveTimeline} the server lands the blow on.
 */
final class CircusWindupPose {
	private float charge;
	private float strike;

	/** Samples the move once per frame; call before posing, then read {@link #charge()} and {@link #strike()}. */
	void sample(CircusPerformerEntity entity, float ageInTicks) {
		charge = 0.0F;
		strike = 0.0F;
		entity.windupState.updateTime(ageInTicks, 1.0F);
		if (!entity.windupState.isStarted()) return;
		BossMoveTimeline move = entity.defenseMove();
		float ticks = entity.windupState.getAccumulatedTime() / 50.0F;
		if (ticks > move.total()) {
			entity.windupState.stop();
			return;
		}
		float commit = move.commitTick();
		charge = HemoEasing.smoothstep(0.0F, commit, ticks) * (1.0F - HemoEasing.smoothstep(commit - 0.5F, commit + 1.0F, ticks));
		strike = HemoEasing.smoothstep(commit - 0.5F, commit + 1.0F, ticks)
				* (1.0F - HemoEasing.smoothstep(commit + 2.0F, move.total(), ticks));
	}

	/** 0 at rest, 1 fully coiled just before the strike. */
	float charge() {
		return charge;
	}

	/** 0 at rest, 1 at the strike's extension, easing back through the follow-through. */
	float strike() {
		return strike;
	}
}
