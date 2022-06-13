package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hutoslib.HutosLib;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class BannerTypeInit {

	public static final DeferredRegister<BannerPattern> BANNERPATTERNS = DeferredRegister
			.create(Registry.BANNER_PATTERN_REGISTRY, HutosLib.MOD_ID);

	public static final RegistryObject<BannerPattern> heart = BANNERPATTERNS.register("hemomancy_heart",
			() -> new BannerPattern("hemomancy_heart"));

	public static final RegistryObject<BannerPattern> veins = BANNERPATTERNS.register("hemomancy_veins",
			() -> new BannerPattern("hemomancy_veins"));

}
