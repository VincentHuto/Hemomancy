package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Putriciel's two ways of walking as pure numbers (design section 8), both keyed to the distance walked, not to time,
 * so a planted foot never skates.
 * <ul>
 *   <li><b>The shamble</b> (the rot): one lurch per {@link #SHAMBLE_BLOCKS} (the locomotion's 0.9-block lunge). The
 *   same foot always leads, a limp: the lead (left) foot swings out ahead in the first {@link #LEAD_END} of the cycle,
 *   the weight is thrown over it, then the trailing (right) foot drags up from {@link #DRAG_START} to meet it, low and
 *   scraping. Both feet stand together under the hips at the cycle's ends, so a lunge that stops leaves him square.</li>
 *   <li><b>The stride</b> (the fire): an even walk of {@link #STRIDE_BLOCKS} a cycle (a 14-tick step cycle at 0.11
 *   blocks a tick), the left foot swinging in the first half and the right in the second.</li>
 * </ul>
 * Feet are placed along the facing in model units (negative ahead of the hip); a leg reaches a foot by its thigh angle
 * (positive: foot behind). Also the legs' contact with the floor: the soles, and the knees when kneeling, decide how
 * far the hips sink. Model units throughout (render scale 2: eight to a block). JVM-tested.
 */
public final class PutricielGait {
	/** Model units in a block at render scale 2. */
	public static final float BLOCK = 8.0F;
	/** The thigh, and the shin to the ankle, in model units (putriciel.py legs after the 0.7 rescale). */
	public static final float THIGH = 3.25F;
	public static final float SHIN = 2.75F;
	/** The sole below the ankle of a foot held flat. */
	public static final float SOLE = 0.25F;
	/** How far below the knee joint the kneecap meets the floor. */
	public static final float KNEE_PAD = 0.75F;
	/** The shamble's lunge (PutricielLocomotion.STEP) and the stride's two steps (14 ticks at 0.11). */
	public static final float SHAMBLE_BLOCKS = 0.9F;
	public static final float STRIDE_BLOCKS = 1.54F;
	/** The shamble's lead swing ends here; the trailing drag starts here. */
	public static final float LEAD_END = 0.45F;
	public static final float DRAG_START = 0.6F;
	/** The dragged foot barely leaves the floor. */
	public static final float DRAG_LIFT = 0.25F;
	/** The stride's swing windows: left, then right. */
	public static final float LEFT = 0.0F, RIGHT = 0.5F;

	private PutricielGait() {
	}

	/**
	 * The cycle's phase in [0, 1) from the walk animation's position, which grows by four a block while the body moves
	 * under a quarter of a block a tick.
	 */
	public static float phase(float limbSwing, float cycleBlocks) {
		return wrap(limbSwing / (4.0F * cycleBlocks));
	}

	/** The lead foot through the shamble, model units along the facing (negative ahead of the hip). */
	public static float shambleLead(float p) {
		float d = SHAMBLE_BLOCKS * BLOCK;
		float ahead = -(1.0F - LEAD_END) * d;
		if (p < LEAD_END) return HemoEasing.lerp(0.0F, ahead, HemoEasing.smoothstep(p / LEAD_END));
		return HemoEasing.lerp(ahead, 0.0F, (p - LEAD_END) / (1.0F - LEAD_END));
	}

	/** The trailing foot through the shamble: planted and sweeping back, then dragged up to the lead. */
	public static float shambleTrail(float p) {
		float d = SHAMBLE_BLOCKS * BLOCK;
		float behind = DRAG_START * d;
		if (p < DRAG_START) return HemoEasing.lerp(0.0F, behind, p / DRAG_START);
		return HemoEasing.lerp(behind, 0.0F, HemoEasing.smoothstep((p - DRAG_START) / (1.0F - DRAG_START)));
	}

	/** How high the lead foot lifts (0 to 1), and the dragged foot (at most {@link #DRAG_LIFT}). */
	public static float shambleLift(float p, boolean lead) {
		if (lead) return p < LEAD_END ? (float) Math.sin(Math.PI * p / LEAD_END) : 0.0F;
		return p >= DRAG_START ? DRAG_LIFT * (float) Math.sin(Math.PI * (p - DRAG_START) / (1.0F - DRAG_START)) : 0.0F;
	}

	/** The weight thrown over the lead foot: 0 to 1 to 0, peaking just after it lands. */
	public static float lurch(float p) {
		return HemoEasing.window(p, LEAD_END * 0.55F, LEAD_END + 0.05F, LEAD_END + 0.12F, DRAG_START + 0.25F);
	}

	/** A stride foot whose swing window starts at {@code start}: half a cycle swinging, half planted. */
	public static float strideFoot(float p, float start) {
		float half = STRIDE_BLOCKS * BLOCK * 0.25F;
		float into = wrap(p - start);
		if (into < 0.5F) return HemoEasing.lerp(half, -half, HemoEasing.smoothstep(into / 0.5F));
		return HemoEasing.lerp(-half, half, (into - 0.5F) / 0.5F);
	}

	/** 0 to 1 to 0 across a stride foot's swing window, else 0. */
	public static float strideLift(float p, float start) {
		float into = wrap(p - start);
		return into < 0.5F ? (float) Math.sin(Math.PI * into / 0.5F) : 0.0F;
	}

	/** The thigh angle (positive: foot behind) that reaches an ankle {@code along} units along the facing. */
	public static float thigh(float along) {
		float sine = Math.max(-0.9F, Math.min(0.9F, along / (THIGH + SHIN)));
		return (float) Math.asin(sine);
	}

	/** A short jolt after a foot lands at phase {@code land}: the body taking the weight. */
	public static float landing(float p, float land) {
		return HemoEasing.pulse(wrap(p - land) * 3.0F, 0.12F, 0.9F);
	}

	/** How far below the hip joint a leg's flat sole hangs, from the thigh's absolute pitch and the knee's bend. */
	public static float reach(float thigh, float knee) {
		return THIGH * (float) Math.cos(thigh) + SHIN * (float) Math.cos(thigh + knee) + SOLE;
	}

	/** How far below the hip joint the leg touches the floor first: its sole, or its kneecap when it kneels. */
	public static float contact(float thigh, float knee) {
		return Math.max(reach(thigh, knee), THIGH * (float) Math.cos(thigh) + KNEE_PAD);
	}

	/** The knees' fold for a crouch, radians: the thigh comes forward by it and the knee bends twice as far. */
	public static float fold(float crouch) {
		return Math.max(0.0F, Math.min(1.3F, crouch * 0.42F));
	}

	static float wrap(float value) {
		float f = value - (float) Math.floor(value);
		return f >= 1.0F ? 0.0F : f;
	}
}
