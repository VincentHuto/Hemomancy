package com.vincenthuto.hemomancy.client.player;

/**
 * How much of the mouse the view takes while a lash is out. The whip takes the whole turn; the view takes at most a
 * capped rate of it, and what the cap cuts is dropped, never paid back later. A turn under the cap passes whole, so
 * slow aiming is ordinary aiming. After a flick the cap cut short, the view does not stop dead: once the hand has been
 * still for a moment it coasts on from the capped rate, losing a share of it each tick, as if it had weight. Pure
 * arithmetic on degrees and ticks, integrated over time, so a frame's length never changes the answer.
 */
public final class LashCameraRules {
	/** Below this many degrees a tick a coasting view is at rest. */
	public static final double REST_RATE = 0.05D;
	/**
	 * Ticks the mouse must be silent before the view coasts. A moving mouse reports far more often than this, so frames
	 * that happen to fall between its reports are not taken for a hand that has stopped.
	 */
	public static final double COAST_GAP = 0.5D;

	private LashCameraRules() {
	}

	/** The rate a capped flick left the view turning at, in degrees a tick, and how long the mouse has been silent. */
	public static final class State {
		double yawRate, pitchRate, idle;

		public void reset() {
			yawRate = 0.0D;
			pitchRate = 0.0D;
			idle = 0.0D;
		}
	}

	/** How much of the lead's limit the whip may sit above or below the view: less than to either side. */
	public static final double PITCH_SHARE = 0.7D;
	/** Below this many degrees the whip is on the crosshair. */
	public static final double LEAD_REST = 0.05D;

	/** How far the whip aim sits from the view, in degrees. */
	public static final class Lead {
		double yaw, pitch;

		public double yaw() {
			return yaw;
		}

		public double pitch() {
			return pitch;
		}

		public void set(double yaw, double pitch) {
			this.yaw = yaw;
			this.pitch = pitch;
		}
	}

	/**
	 * Moves the whip's lead over the view through one frame. What the view's cap cut from the mouse ({@code cutYaw},
	 * {@code cutPitch}) carries the whip further out; the lead is held inside {@code most} degrees to either side and
	 * {@link #PITCH_SHARE} of that above and below, so no swing puts the whip behind the caster; and it keeps
	 * {@code keep} of itself each tick, so with the hand at rest the whip comes back to the crosshair.
	 */
	public static void lead(Lead lead, double cutYaw, double cutPitch, double dtTicks, double most, double keep) {
		double fade = Math.pow(Math.max(0.0D, Math.min(0.999D, keep)), Math.max(0.0D, dtTicks));
		lead.yaw = lead.yaw * fade + cutYaw;
		lead.pitch = lead.pitch * fade + cutPitch;
		// An ellipse: wider than it is tall.
		double reach = Math.sqrt(lead.yaw * lead.yaw + (lead.pitch / PITCH_SHARE) * (lead.pitch / PITCH_SHARE));
		if (reach > most && reach > 0.0D) {
			lead.yaw *= most / reach;
			lead.pitch *= most / reach;
		} else if (reach < LEAD_REST && cutYaw == 0.0D && cutPitch == 0.0D) {
			lead.yaw = 0.0D;
			lead.pitch = 0.0D;
		}
	}

	/**
	 * Holds the whip aim's move this frame, the view's turn ({@code viewYaw}, {@code viewPitch}) plus the lead's change
	 * from ({@code beforeYaw}, {@code beforePitch}), to {@code most} degrees, as the server holds the aim it is sent. The
	 * direction is kept; only the lead is shortened.
	 */
	public static void limitStep(Lead lead, double beforeYaw, double beforePitch, double viewYaw, double viewPitch,
			double most) {
		double moveYaw = viewYaw + lead.yaw - beforeYaw, movePitch = viewPitch + lead.pitch - beforePitch;
		double size = Math.sqrt(moveYaw * moveYaw + movePitch * movePitch);
		if (!(size > most) || !(most >= 0.0D)) return;
		double keep = most / size;
		lead.yaw = beforeYaw + moveYaw * keep - viewYaw;
		lead.pitch = beforePitch + movePitch * keep - viewPitch;
	}

	/**
	 * The degrees (yaw, pitch) the view turns this frame for a raw mouse turn of {@code rawYaw}, {@code rawPitch}
	 * degrees over {@code dtTicks} ticks. {@code cap} is the most it may turn in a tick, {@code drag} the share of its
	 * rate it keeps each tick once the hand stops, and {@code strength} blends the whole effect in: 0 is the raw turn,
	 * untouched; 1 is the cap in full.
	 */
	public static double[] view(State state, double rawYaw, double rawPitch, double dtTicks, double cap, double drag,
			double strength) {
		if (!(strength > 0.0D) || !(dtTicks > 0.0D)) {
			state.reset();
			return new double[] {rawYaw, rawPitch};
		}
		double kept = Math.max(1.0E-6D, Math.min(0.999D, drag));
		double size = Math.sqrt(rawYaw * rawYaw + rawPitch * rawPitch);
		double yaw, pitch;
		if (size > 1.0E-9D) {
			state.idle = 0.0D;
			double budget = cap * dtTicks;
			if (size > budget) {
				// A flick: the view takes the cap, and remembers it was turning that fast.
				yaw = rawYaw * budget / size;
				pitch = rawPitch * budget / size;
				state.yawRate = yaw / dtTicks;
				state.pitchRate = pitch / dtTicks;
			} else {
				// Ordinary aiming passes whole; what a flick left behind fades under it.
				yaw = rawYaw;
				pitch = rawPitch;
				double fade = Math.pow(kept, dtTicks);
				state.yawRate *= fade;
				state.pitchRate *= fade;
				if (Math.sqrt(state.yawRate * state.yawRate + state.pitchRate * state.pitchRate) < REST_RATE) {
					state.yawRate = 0.0D;
					state.pitchRate = 0.0D;
				}
			}
		} else {
			double before = state.idle;
			state.idle += dtTicks;
			double coast = Math.max(0.0D, state.idle - COAST_GAP) - Math.max(0.0D, before - COAST_GAP);
			// The turn a rate decaying by `kept` a tick covers in `coast` ticks, and what is left of the rate after it.
			double fade = Math.pow(kept, coast);
			double travelled = (1.0D - fade) / -Math.log(kept);
			yaw = state.yawRate * travelled;
			pitch = state.pitchRate * travelled;
			state.yawRate *= fade;
			state.pitchRate *= fade;
			if (Math.sqrt(state.yawRate * state.yawRate + state.pitchRate * state.pitchRate) < REST_RATE) {
				state.yawRate = 0.0D;
				state.pitchRate = 0.0D;
			}
		}
		double blend = Math.min(1.0D, strength);
		return new double[] {rawYaw + (yaw - rawYaw) * blend, rawPitch + (pitch - rawPitch) * blend};
	}
}
