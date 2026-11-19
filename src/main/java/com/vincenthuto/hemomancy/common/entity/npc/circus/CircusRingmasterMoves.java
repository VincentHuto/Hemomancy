package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;

import javax.annotation.Nullable;

/**
 * The Ringmaster's gestures (2026-11-18): the poses his models play for a move, each on a {@link BossMoveTimeline} whose
 * commit tick is the blow. His finale's moves ({@code RingmasterFinaleMoves}) play these on his copy; the old in-world
 * finale that chose among them is gone (its moves are kept in removed_assets/2026-11-18-ringmaster-finale-v1).
 */
public final class CircusRingmasterMoves {
	private CircusRingmasterMoves() {
	}

	public enum Move {
		NONE(null),
		/** A knife drawn back over the shoulder and thrown. */
		KNIFE_THROW(new BossMoveTimeline(12, 1, 5)),
		/** The staff hauled back and brought down in front of him. */
		STAFF_STRIKE(new BossMoveTimeline(12, 1, 6)),
		/** One arm raised to a light overhead. */
		SPOTLIGHT(new BossMoveTimeline(20, 40, 8)),
		/** The staff raised to call an act. */
		CUE(new BossMoveTimeline(20, 1, 8)),
		/** Both fists hauling back on a thread. */
		STRINGS_CUT(new BossMoveTimeline(20, 1, 10));

		@Nullable
		private final BossMoveTimeline timeline;

		Move(@Nullable BossMoveTimeline timeline) {
			this.timeline = timeline;
		}

		@Nullable
		public BossMoveTimeline timeline() {
			return timeline;
		}

		public static Move byId(int id) {
			Move[] values = values();
			return id >= 0 && id < values.length ? values[id] : NONE;
		}
	}
}
