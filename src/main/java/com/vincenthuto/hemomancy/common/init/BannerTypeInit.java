package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BannerTypeInit {

	public static final DeferredRegister<BannerPattern> BANNERPATTERNS = DeferredRegister
			.create(Registries.BANNER_PATTERN, Hemomancy.MOD_ID);

	public static final RegistryObject<BannerPattern> heart = BANNERPATTERNS.register("heart",
			() -> new BannerPattern("heart"));

	public static final RegistryObject<BannerPattern> veins = BANNERPATTERNS.register("veins",
			() -> new BannerPattern("veins"));

}