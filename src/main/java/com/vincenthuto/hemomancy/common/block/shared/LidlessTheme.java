package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import net.minecraft.world.entity.player.Player;

/**
 * The colours a Lidless portal and its crossing turn toward: the Qliphoth Chamber's, or at Degree 8 the Chamber the
 * owner's path has made of theirs. The shaders read {@link #ordinal()}, so the order is part of their contract.
 */
public enum LidlessTheme {
	QLIPHOTH, SILENTIUM, APOTHEOS;

	public static LidlessTheme of(Player player) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
		if (HarbingerPathPermissions.isApotheos(degree.getDegreeNumber(), degree.getArchonPath())) return APOTHEOS;
		if (HarbingerPathPermissions.isSilentium(degree.getDegreeNumber(), degree.getArchonPath())) return SILENTIUM;
		return QLIPHOTH;
	}

	public static LidlessTheme byOrdinal(int ordinal) {
		LidlessTheme[] themes = values();
		return themes[Math.floorMod(ordinal, themes.length)];
	}
}
