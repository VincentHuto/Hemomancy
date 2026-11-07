package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielPhase;

/**
 * Which of Putriciel's tracks plays and where it stands on its authored clock, from the replayed clocks alone (the
 * kit's move, opening and ending, each read through a {@code BossReplayClock} in {@code SaintMoveView}, the rise, and
 * the last accepted absolution). Moves are authored on their base timeline (Rot I or the first interval, no deed):
 * a Rot III wind-up cut by three ticks plays the same keys a little faster, and a Repose recovery cut by a quarter
 * plays the authored settle in less time, so a strike always lands on its commit. Pure, JVM-tested.
 */
public final class PutricielTracks {
	public static final int IDLE = 0;
	public static final int MOVE = 100;
	public static final int OPENING = 200;
	public static final int ENDING = 300;
	public static final int RECOIL = 301;
	public static final int RISE = 302;
	/** The recoil beneath an accepted Offering plays this long (the 14-tick stagger and its settle). */
	public static final float RECOIL_TICKS = PutricielAbsolutionRules.RECOIL_TICKS + 4.0F;

	private PutricielTracks() {
	}

	/** A track: its id, its time on the authored clock, its authored length, and a count the track reads. */
	public record Track(int id, float time, float length, int count) {
		public boolean move() {
			return id > MOVE && id < OPENING;
		}

		public boolean opening() {
			return id > OPENING && id < ENDING;
		}
	}

	/** The timeline a move is authored on: Rot I (or the interval for fire), no deed, the given variant and group. */
	public static BossMoveTimeline authoredTimeline(PutricielMove move, SaintMoveInputs in) {
		int phase = move.fire() ? PutricielPhase.INTERVAL.number() : PutricielPhase.ROT_I.number();
		return move.timeline(new SaintMoveInputs(phase, in.variant(), 0, in.challengers()));
	}

	/**
	 * Maps {@code t} ticks into a move running on {@code actual} to its authored clock: the wind-up stretched onto the
	 * authored wind-up, the active span tick for tick, the recovery stretched onto the authored recovery.
	 */
	public static float authored(float t, BossMoveTimeline actual, BossMoveTimeline authored) {
		if (t < 0.0F) return t;
		int w = actual.windup(), a = actual.active(), r = actual.recovery();
		int aw = authored.windup(), ar = authored.recovery();
		if (t < w) return w <= 0 ? aw : t * aw / (float) w;
		if (t < w + a) return aw + (t - w);
		float into = t - w - a;
		return aw + a + (r <= 0 ? into : into * ar / (float) r);
	}

	/**
	 * The track for one frame. {@code moveTime}, {@code openingTime} and {@code endingTime} are -1 when nothing runs;
	 * {@code riseTime} is negative once risen; {@code sinceAccepted} is ticks since the last absolution was accepted.
	 * The kneeling beats (the Offering, the Embering) and the Communion keep their own track through their opening,
	 * which their recovery is; every other move hands its recovery to the opening's track when one opens.
	 */
	public static Track select(int moveId, float moveTime, int openingId, float openingTime, int openingLength,
			float endingTime, float riseTime, float sinceAccepted, SaintMoveInputs in, float age) {
		if (endingTime >= 0.0F) return new Track(ENDING, endingTime, 101.0F, 0);
		if (riseTime >= 0.0F) return new Track(RISE, riseTime, 60.0F, 0);
		if (sinceAccepted >= 0.0F && sinceAccepted < RECOIL_TICKS) return new Track(RECOIL, sinceAccepted, RECOIL_TICKS, 0);
		PutricielMove move = PutricielMove.byId(moveId);
		if (move != null && moveTime >= 0.0F) {
			BossMoveTimeline actual = move.timeline(in);
			BossMoveTimeline authored = authoredTimeline(move, in);
			boolean kneels = move == PutricielMove.OFFERING || move == PutricielMove.EMBERING;
			if (openingId != 0 && !kneels && moveTime >= actual.windup() + actual.active())
				return new Track(OPENING + openingId, Math.max(0.0F, openingTime), Math.max(1, openingLength), 0);
			int count = move == PutricielMove.SHELF_VOLLEY ? PutricielMove.volleyDiscs(in) : 0;
			if (kneels) {
				float length = authored.windup() + authored.active() + Math.max(actual.recovery(), openingLength);
				return new Track(MOVE + move.id(), moveTime, length, count);
			}
			return new Track(MOVE + move.id(), authored(moveTime, actual, authored), authored.total(), count);
		}
		if (openingId != 0) return new Track(OPENING + openingId, Math.max(0.0F, openingTime), Math.max(1, openingLength), 0);
		return new Track(IDLE, age, 0.0F, 0);
	}
}
