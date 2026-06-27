package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.resources.ResourceLocation;

public final class ChamberOfWillClientData {
	private static ResourceLocation skyTheme = ChamberOfWillManager.THEME_WILL_DEFAULT;
	private static int tier = 0;
	private static int radius = ChamberOfWillManager.BASE_ROOM_RADIUS;
	private static int qliphothPomesConsumed = 0;

	private ChamberOfWillClientData() {
	}

	public static void set(ResourceLocation theme, int chamberTier, int chamberRadius, int pomeCount) {
		skyTheme = theme != null ? theme : ChamberOfWillManager.THEME_WILL_DEFAULT;
		tier = Math.max(0, chamberTier);
		radius = Math.max(ChamberOfWillManager.BASE_ROOM_RADIUS, chamberRadius);
		qliphothPomesConsumed = Math.max(0, Math.min(9, pomeCount));
	}

	public static ResourceLocation skyTheme() {
		return skyTheme;
	}

	public static int tier() {
		return tier;
	}

	public static int radius() {
		return radius;
	}

	public static int qliphothPomesConsumed() {
		return qliphothPomesConsumed;
	}

	public static void clear() {
		skyTheme = ChamberOfWillManager.THEME_WILL_DEFAULT;
		tier = 0;
		radius = ChamberOfWillManager.BASE_ROOM_RADIUS;
		qliphothPomesConsumed = 0;
	}
}
