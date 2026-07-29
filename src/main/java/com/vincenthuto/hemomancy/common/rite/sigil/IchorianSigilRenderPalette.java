package com.vincenthuto.hemomancy.common.rite.sigil;

public final class IchorianSigilRenderPalette {
	private static final Color CORE_BLOOD = new Color(0.58F, 0.015F, 0.02F);
	private static final Color GLOW_BLOOD = new Color(0.82F, 0.035F, 0.05F);
	private static final Color CORE_TISSUE = new Color(0.16F, 0.008F, 0.012F);
	private static final Color GLOW_TISSUE = new Color(0.31F, 0.012F, 0.018F);
	private static final Color PALE_MEMBRANE = new Color(0.70F, 0.58F, 0.61F);

	private IchorianSigilRenderPalette() {
	}

	public static Color vessel(boolean glow) {
		return glow ? GLOW_BLOOD : CORE_BLOOD;
	}

	public static Color tissue(boolean glow) {
		return glow ? GLOW_TISSUE : CORE_TISSUE;
	}

	public static Color membrane() {
		return PALE_MEMBRANE;
	}

	public static Color authoredIchor(int color, boolean glow) {
		float red = ((color >> 16) & 255) / 255.0F;
		float green = ((color >> 8) & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		if (glow) {
			red = Math.min(1.0F, red * 1.18F + 0.08F);
			green = Math.min(1.0F, green * 1.18F + 0.02F);
			blue = Math.min(1.0F, blue * 1.18F + 0.02F);
		}
		return new Color(red, green, blue);
	}

	public record Color(float red, float green, float blue) {
	}
}
