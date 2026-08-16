package com.vincenthuto.hemomancy.common.tile.crafting;

public final class PallidRetortOutputRules {
	public enum Destination { RECIPE_RESULT, FLASK_OUTPUT }
	private PallidRetortOutputRules() {}
	public static Destination recipeDestination(boolean flaskPresent) { return Destination.RECIPE_RESULT; }
	public static Destination bottlingDestination() { return Destination.FLASK_OUTPUT; }
}
