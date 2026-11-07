package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

/**
 * One damped spring that follows a driver value: the overlap and secondary motion on Hemorath's body (the head and
 * spine lagging the shoulders, the hanging chains, the vestments, the ribs and the face cords). A spring that lags its
 * driver and rings past it when the driver stops is what reads as weight. Pure maths, no Minecraft classes, so the JVM
 * tests drive it directly.
 *
 * <p>Time is in ticks. {@code frequency} is the undamped angular frequency in radians a tick and {@code damping} the
 * damping ratio (1 is critical: no overshoot; below 1 rings). It integrates semi-implicitly in sub-steps of at most
 * {@link #MAX_STEP} ticks, so a long frame or a stiff spring stays stable.
 */
public final class HemorathSpring {
	/** The largest integration sub-step, in ticks. */
	public static final float MAX_STEP = 0.2F;
	/** A frame longer than this (a hitch, an unloaded chunk) is treated as this long. */
	public static final float MAX_FRAME = 4.0F;

	private float value;
	private float velocity;
	private boolean primed;

	/** Moves the spring {@code dt} ticks toward {@code target} and returns its value. */
	public float step(float target, float dt, float frequency, float damping) {
		if (!primed) {
			value = target;
			velocity = 0.0F;
			primed = true;
			return value;
		}
		float remaining = Math.max(0.0F, Math.min(MAX_FRAME, dt));
		float stiffness = frequency * frequency;
		float friction = 2.0F * damping * frequency;
		while (remaining > 1.0E-5F) {
			float h = Math.min(MAX_STEP, remaining);
			velocity += (stiffness * (target - value) - friction * velocity) * h;
			value += velocity * h;
			remaining -= h;
		}
		return value;
	}

	/**
	 * Steps toward {@code driver} and returns how far the spring trails it ({@code value - driver}): negative while the
	 * driver runs ahead upward, positive as the spring overshoots after the driver stops.
	 */
	public float lag(float driver, float dt, float frequency, float damping) {
		return step(driver, dt, frequency, damping) - driver;
	}

	/** A sudden push (a hit, a landing): adds to the velocity, in units a tick. */
	public void kick(float impulse) {
		velocity += impulse;
	}

	/** Snaps to {@code value} at rest. */
	public void reset(float value) {
		this.value = value;
		this.velocity = 0.0F;
		this.primed = true;
	}

	public float value() {
		return value;
	}

	public float velocity() {
		return velocity;
	}
}
