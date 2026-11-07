package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

/**
 * The Saint move picker, on the Mycophant's model: each move answers bands and sectors with a weight, recent picks are
 * damped, and strings come from follow-ups, which skip the picker. Pure, so it is JVM-tested.
 */
public final class SaintPickRules {
	/** Follow-ups, so a string is at most three moves. */
	public static final int MAX_CHAIN = 2;
	private static final float[] REPEAT_FACTORS = {0.0F, 0.4F, 0.6F, 0.8F};

	private SaintPickRules() {
	}

	/** A table cell: the full weight where the move belongs, half where it half belongs, else 0. */
	public static int cell(int weight, boolean full, boolean half) {
		if (full) return weight;
		return half ? weight / 2 : 0;
	}

	/**
	 * Weighted pick among the moves that are picked, in the phase, off cooldown, eligible and weighted here, each damped
	 * by {@link History#repeatFactor}. Null when nothing qualifies. The caller records the pick.
	 */
	@Nullable
	public static SaintMove choose(List<? extends SaintMove> moves, SaintPickContext ctx,
			ToIntFunction<SaintMove> cooldownLeft, History history, IntUnaryOperator roll) {
		int[] weights = new int[moves.size()];
		int sum = 0;
		for (int i = 0; i < moves.size(); i++) {
			SaintMove move = moves.get(i);
			if (!move.picked() || !move.inPhase(ctx.phase()) || cooldownLeft.applyAsInt(move) > 0 || !move.eligible(ctx))
				continue;
			int base = Math.max(0, move.weight(ctx));
			int damped = Math.round(base * history.repeatFactor(move));
			weights[i] = damped;
			sum += damped;
		}
		if (sum <= 0) return null;
		int pick = Math.floorMod(roll.applyAsInt(sum), sum);
		for (int i = 0; i < moves.size(); i++) {
			pick -= weights[i];
			if (pick < 0) return moves.get(i);
		}
		return null;
	}

	/** The follow-up that fires after {@code done}, or null; see {@link #followUpLink}. */
	@Nullable
	public static SaintMove followUp(SaintMove done, SaintFollowContext ctx, int depth, boolean interruptPending,
			DoubleSupplier roll) {
		SaintFollowUp link = followUpLink(done, ctx, depth, interruptPending, roll);
		return link == null ? null : link.next();
	}

	/**
	 * The link that fires after {@code done}: the first listed follow-up in the phase whose condition holds and whose
	 * chance roll succeeds (one roll per candidate). {@code depth} is how many follow-ups already ran in this chain; a
	 * pending interrupt (a stagger, a phase change, a forced move) cancels the chain. Follow-ups ignore the anti-repeat
	 * damping and the cooldowns.
	 */
	@Nullable
	public static SaintFollowUp followUpLink(SaintMove done, SaintFollowContext ctx, int depth, boolean interruptPending,
			DoubleSupplier roll) {
		if (depth >= MAX_CHAIN || interruptPending) return null;
		for (SaintFollowUp link : done.followUps(ctx.phase())) {
			if (!link.next().inPhase(ctx.phase()) || !link.when().test(ctx)) continue;
			if (roll.getAsDouble() < link.chance()) return link;
		}
		return null;
	}

	/** The last four picks, most recent first, for the no-repeat rule and the decay. */
	public static final class History {
		private final SaintMove[] recent = new SaintMove[REPEAT_FACTORS.length];

		public void record(SaintMove move) {
			System.arraycopy(recent, 0, recent, 1, recent.length - 1);
			recent[0] = move;
		}

		@Nullable
		public SaintMove last() {
			return recent[0];
		}

		/** 0 for the last pick, then 0.4, 0.6 and 0.8 down the history, 1 for anything older or unused. */
		public float repeatFactor(SaintMove move) {
			for (int i = 0; i < recent.length; i++) if (recent[i] == move) return REPEAT_FACTORS[i];
			return 1.0F;
		}

		public void clear() {
			Arrays.fill(recent, null);
		}
	}

	/**
	 * The quarry over the last 200 ticks, one sample per tick: how long they kited (MID or FAR), hugged (HUG) and stood
	 * out of the Saint's sight.
	 */
	public static final class Pressure {
		public static final int WINDOW = 200;
		private static final byte EMPTY = -1;
		private final byte[] bands = new byte[WINDOW];
		private final boolean[] unseen = new boolean[WINDOW];
		private int next;
		private int kite;
		private int hug;
		private int hidden;

		public Pressure() {
			Arrays.fill(bands, EMPTY);
		}

		public void sample(BossBands.Band band, boolean seen) {
			forget(next);
			bands[next] = (byte) band.ordinal();
			unseen[next] = !seen;
			if (isKite(bands[next])) kite++;
			else if (bands[next] == BossBands.Band.HUG.ordinal()) hug++;
			if (!seen) hidden++;
			next = (next + 1) % WINDOW;
		}

		/** Ticks in MID or FAR within the window. */
		public int kiteTicks() {
			return kite;
		}

		/** Ticks in HUG within the window. */
		public int hugTicks() {
			return hug;
		}

		/** Ticks out of sight within the window. */
		public int unseenTicks() {
			return hidden;
		}

		/** A move that answers kiting started: the kiting that earned it no longer counts. */
		public void resetKite() {
			for (int i = 0; i < WINDOW; i++) if (isKite(bands[i])) bands[i] = EMPTY;
			kite = 0;
		}

		public void clear() {
			Arrays.fill(bands, EMPTY);
			Arrays.fill(unseen, false);
			kite = 0;
			hug = 0;
			hidden = 0;
		}

		private void forget(int index) {
			byte sample = bands[index];
			if (isKite(sample)) kite--;
			else if (sample == BossBands.Band.HUG.ordinal()) hug--;
			if (unseen[index]) hidden--;
			unseen[index] = false;
		}

		private static boolean isKite(byte sample) {
			return sample == BossBands.Band.MID.ordinal() || sample == BossBands.Band.FAR.ordinal();
		}
	}
}
