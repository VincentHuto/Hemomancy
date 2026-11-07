package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;

/**
 * A saved ending window. {@code SaintTrialEntity.beginEnding} cancels the move, releases the hold, expires hazards,
 * stops the presence and makes the Saint untouchable; {@code endTrial} runs once at {@code endTrialTick} (or earlier
 * when {@code finishEarly} and the Saint's condition holds); the arena restores after it in the spec's order; the Saint
 * is discarded at {@code length}. With {@code resumeOnLoad} a reload carries on; without it the ending finishes on the
 * first tick after the load.
 */
public final class SaintEnding {
	public record Spec(int endTrialTick, int length, boolean finishEarly, boolean resumeOnLoad,
			SaintArena.RestoreOrder restore, int restoreTicks) {
		/** An ending that settles at once: endTrial on its first tick, discarded on the next. */
		public static final Spec IMMEDIATE = new Spec(0, 1, false, true, SaintArena.RestoreOrder.IMMEDIATE, 0);

		public CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			tag.putInt("end_trial", endTrialTick);
			tag.putInt("length", length);
			tag.putBoolean("finish_early", finishEarly);
			tag.putBoolean("resume", resumeOnLoad);
			tag.putString("restore", restore.name());
			tag.putInt("restore_ticks", restoreTicks);
			return tag;
		}

		public static Spec load(CompoundTag tag) {
			SaintArena.RestoreOrder order;
			try {
				order = SaintArena.RestoreOrder.valueOf(tag.getString("restore"));
			} catch (IllegalArgumentException unknown) {
				order = SaintArena.RestoreOrder.IMMEDIATE;
			}
			return new Spec(tag.getInt("end_trial"), tag.getInt("length"), tag.getBoolean("finish_early"),
					tag.getBoolean("resume"), order, tag.getInt("restore_ticks"));
		}
	}

	/** What one tick of the ending asks for. */
	public enum Step { NONE, END_TRIAL, DISCARD, END_TRIAL_AND_DISCARD }

	/** The ending's pure clock: its tick, whether endTrial ran, and how a load resumes. JVM-tested. */
	public static final class Clock {
		private final Spec spec;
		private int tick = -1;
		private boolean trialEnded;
		private boolean finishOnNextTick;

		public Clock(Spec spec) {
			this.spec = spec;
		}

		public Spec spec() {
			return spec;
		}

		/** -1 before the first tick. */
		public int tick() {
			return tick;
		}

		public boolean trialEnded() {
			return trialEnded;
		}

		/** One tick: advances and says whether endTrial runs now, the Saint is discarded now, or both. */
		public Step advance(boolean done) {
			if (finishOnNextTick) {
				finishOnNextTick = false;
				tick = Math.max(tick, spec.length());
				boolean end = !trialEnded;
				trialEnded = true;
				return end ? Step.END_TRIAL_AND_DISCARD : Step.DISCARD;
			}
			tick++;
			boolean end = !trialEnded && (tick >= spec.endTrialTick() || spec.finishEarly() && done);
			if (end) trialEnded = true;
			boolean discard = tick >= spec.length() || spec.finishEarly() && done && trialEnded;
			if (end && discard) return Step.END_TRIAL_AND_DISCARD;
			if (end) return Step.END_TRIAL;
			return discard ? Step.DISCARD : Step.NONE;
		}

		public CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			tag.put("spec", spec.save());
			tag.putInt("tick", tick);
			tag.putBoolean("trial_ended", trialEnded);
			return tag;
		}

		public static Clock load(CompoundTag tag) {
			Clock clock = new Clock(Spec.load(tag.getCompound("spec")));
			clock.tick = tag.getInt("tick");
			clock.trialEnded = tag.getBoolean("trial_ended");
			clock.finishOnNextTick = !clock.spec.resumeOnLoad();
			return clock;
		}
	}

	private SaintEnding() {
	}
}
