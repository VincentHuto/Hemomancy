package com.vincenthuto.hemomancy.client.render.world;

/**
 * One Saint's telegraph painter ({@code SaintClientStyles} maps each Saint to one). The pass calls it twice a frame:
 * once in the solid pass ({@link TelegraphCanvas#solids()}, where only {@link TelegraphCanvas#iron()} may be drawn) and
 * once in the translucent pass. Returning false leaves the move or hazard to the default painter
 * ({@link SaintTelegraphPainter}), which keeps the subtlety budget: fills at alpha 0.22 or less, edges 0.012 wide or
 * less, no strands.
 */
public interface SaintTelegraphStyle {
	/** True when the style drew this move; false falls back to the default painter. */
	boolean paint(TelegraphDraw draw, TelegraphCanvas canvas);

	/** True when the style drew this hazard; false paints its shape's default fill. */
	default boolean paintHazard(HazardDraw draw, TelegraphCanvas canvas) {
		return false;
	}
}
