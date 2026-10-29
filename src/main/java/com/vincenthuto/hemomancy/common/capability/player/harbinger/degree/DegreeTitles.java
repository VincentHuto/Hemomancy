package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import net.minecraft.network.chat.Component;

/** A player's own degree title; the eighth degree is named by the path that reached it. */
public final class DegreeTitles {
	private DegreeTitles() {}

	public static String titleKey(int degree, EnumArchonPath path) {
		int clamped = Math.max(0, Math.min(8, degree));
		if (clamped == 8 && path == EnumArchonPath.APOTHEOS) return "hemomancy.degree.8.apotheos";
		if (clamped == 8 && path == EnumArchonPath.SILENTIUM) return "hemomancy.degree.8.silentium";
		return "hemomancy.degree." + clamped;
	}

	public static Component title(int degree, EnumArchonPath path) {
		return Component.translatable(titleKey(degree, path));
	}
}
