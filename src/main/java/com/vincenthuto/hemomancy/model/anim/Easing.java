package com.vincenthuto.hemomancy.model.anim;

public enum Easing {
	LINEAR(x -> x), EASE_IN_CUBIC(Easing::cube), EASE_OUT_CUBIC(x -> 1 - Easing.cube(1 - x)),
	EASE_IN_OUT_CUBIC(x -> x < 0.5f ? 4 * x * x * x : 1 - Easing.cube(-2 * x + 2) / 2);

	private final ToFloatFunc<Float> function;

	Easing(ToFloatFunc<Float> function) {
		this.function = function;
	}

	private static float cube(float x) {
		return x * x * x;
	}

	/**
	 * Expects a value [0, 1]
	 */
	public float apply(float x) {
		return function.apply(x);
	}
}