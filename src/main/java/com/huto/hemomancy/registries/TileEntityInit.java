package com.huto.hemomancy.registries;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.tile.TileEntityChiselStation;
import com.huto.hemomancy.tile.TileEntityRuneModStation;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityInit {
	public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister
			.create(ForgeRegistries.TILE_ENTITIES, Hemomancy.MOD_ID);
	public static final RegistryObject<TileEntityType<TileEntityChiselStation>> runic_chisel_station = TILES
			.register("runic_chisel_station", () -> TileEntityType.Builder
					.create(TileEntityChiselStation::new, BlockInit.runic_chisel_station.get()).build(null));
	public static final RegistryObject<TileEntityType<TileEntityRuneModStation>> rune_mod_station = TILES
			.register("rune_mod_station", () -> TileEntityType.Builder
					.create(TileEntityRuneModStation::new, BlockInit.rune_mod_station.get()).build(null));
}
