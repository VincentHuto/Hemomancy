package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;

import java.util.List;

/** A small move table for the kit's JVM tests: a close sweep, a far lance, a string, and a move only the Saint starts. */
enum TestMoves implements SaintMove {
	SWEEP(1, Kind.MELEE, 20, 10) {
		@Override
		public int weight(SaintPickContext ctx) {
			return SaintPickRules.cell(weightValue, ctx.band() == BossBands.Band.HUG || ctx.band() == BossBands.Band.NEAR,
					ctx.band() == BossBands.Band.MID);
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			return List.of(new SaintFollowUp(RETURN, 1.0F, 4, FollowCondition.onHit()));
		}
	},
	RETURN(2, Kind.MELEE, 0, 0) {
		@Override
		public List<SaintFollowUp> followUps(int phase) {
			return List.of(new SaintFollowUp(LANCE, 0.5F, 6, FollowCondition.ALWAYS));
		}
	},
	LANCE(3, Kind.AREA, 30, 40) {
		@Override
		public int weight(SaintPickContext ctx) {
			return SaintPickRules.cell(weightValue, ctx.band() == BossBands.Band.FAR, ctx.band() == BossBands.Band.MID);
		}

		@Override
		public List<SaintFollowUp> followUps(int phase) {
			return List.of(new SaintFollowUp(SWEEP, 1.0F, 4, FollowCondition.ALWAYS));
		}
	},
	SECOND_PHASE(4, Kind.AREA, 10, 0) {
		@Override
		public boolean inPhase(int phase) {
			return phase >= 2;
		}
	},
	FORCED(5, Kind.AREA, 50, 0) {
		@Override
		public boolean picked() {
			return false;
		}
	};

	private final int id;
	private final Kind kind;
	final int weightValue;
	private final int cooldown;

	TestMoves(int id, Kind kind, int weight, int cooldown) {
		this.id = id;
		this.kind = kind;
		this.weightValue = weight;
		this.cooldown = cooldown;
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public String key() {
		return name().toLowerCase(java.util.Locale.ROOT);
	}

	@Override
	public Kind kind() {
		return kind;
	}

	@Override
	public Aim aim() {
		return Aim.TRACK;
	}

	@Override
	public boolean inPhase(int phase) {
		return true;
	}

	@Override
	public BossMoveTimeline timeline(SaintMoveInputs in) {
		return new BossMoveTimeline(8, 6, 10);
	}

	@Override
	public BossFootprint footprint(SaintMoveInputs in) {
		return BossFootprint.None.INSTANCE;
	}

	@Override
	public int weight(SaintPickContext ctx) {
		return weightValue;
	}

	@Override
	public int cooldown(SaintPickContext ctx) {
		return cooldown;
	}
}
