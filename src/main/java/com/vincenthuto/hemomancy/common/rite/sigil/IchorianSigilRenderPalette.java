package com.vincenthuto.hemomancy.common.rite.sigil;

public final class IchorianSigilRenderPalette {
	private static final Color CORE_BLOOD = new Color(0.58F, 0.015F, 0.02F);
	private static final Color GLOW_BLOOD = new Color(0.82F, 0.035F, 0.05F);

	private IchorianSigilRenderPalette() {
	}

	public static Color vessel(boolean glow) {
		return glow ? GLOW_BLOOD : CORE_BLOOD;
	}

	public static Color node(int color, boolean glow) {
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
