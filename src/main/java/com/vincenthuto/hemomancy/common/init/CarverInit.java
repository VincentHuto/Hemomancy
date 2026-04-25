package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.carver.FungalCaveWorldCarver;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CarverInit {
	public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(Registries.CARVER,
			Hemomancy.MOD_ID);

	public static final WorldCarver<CaveCarverConfiguration> DRY_FUNGAL_CAVE = register("dry_fungal_cave",
			new FungalCaveWorldCarver(CaveCarverConfiguration.CODEC));

	private static <C extends net.minecraft.world.level.levelgen.carver.CarverConfiguration, F extends WorldCarver<C>> F register(
			String key, F value) {
		CARVERS.register(key, () -> value);
		return value;
	}
}
