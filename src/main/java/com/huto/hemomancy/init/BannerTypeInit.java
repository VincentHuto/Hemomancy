package com.huto.hemomancy.init;

import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerTypeInit {
	public static final BannerPattern heart = BannerPattern.create("hemomancy_heart".toUpperCase(), "hemomancy_heart",
			"hrt", true);
	
	public static final BannerPattern veins = BannerPattern.create("hemomancy_veins".toUpperCase(), "hemomancy_veins",
			"ven", true);
}
