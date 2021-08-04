package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.tile.BlockEntityChiselStation;
import com.huto.hemomancy.tile.BlockEntityDendriticDistributor;
import com.huto.hemomancy.tile.BlockEntityHumaneIdol;
import com.huto.hemomancy.tile.BlockEntityMorphlingIncubator;
import com.huto.hemomancy.tile.BlockEntityMortalDisplay;
import com.huto.hemomancy.tile.BlockEntityRuneModStation;
import com.huto.hemomancy.tile.BlockEntitySemiSentientConstruct;
import com.huto.hemomancy.tile.BlockEntitySerpentineIdol;
import com.huto.hemomancy.tile.BlockEntityUnstainedPodium;
import com.huto.hemomancy.tile.BlockEntityVisceralRecaller;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockEntityInit {
	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITIES, Hemomancy.MOD_ID);

	public static final RegistryObject<BlockEntityType<BlockEntityChiselStation>> runic_chisel_station = TILES
			.register("runic_chisel_station", () -> BlockEntityType.Builder
					.of(BlockEntityChiselStation::new, BlockInit.runic_chisel_station.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityRuneModStation>> rune_mod_station = TILES
			.register("rune_mod_station", () -> BlockEntityType.Builder
					.of(BlockEntityRuneModStation::new, BlockInit.rune_mod_station.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntitySemiSentientConstruct>> semi_sentient_construct = TILES
			.register("semi_sentient_construct", () -> BlockEntityType.Builder
					.of(BlockEntitySemiSentientConstruct::new, BlockInit.semi_sentient_construct.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityMorphlingIncubator>> morphling_incubator = TILES
			.register("morphling_incubator", () -> BlockEntityType.Builder
					.of(BlockEntityMorphlingIncubator::new, BlockInit.morphling_incubator.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityUnstainedPodium>> unstained_podium = TILES
			.register("unstained_podium", () -> BlockEntityType.Builder
					.of(BlockEntityUnstainedPodium::new, BlockInit.unstained_podium.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityDendriticDistributor>> dendritic_distributor = TILES
			.register("dendritic_distributor", () -> BlockEntityType.Builder
					.of(BlockEntityDendriticDistributor::new, BlockInit.dendritic_distributor.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntitySerpentineIdol>> serpentine_idol = TILES
			.register("serpentine_idol", () -> BlockEntityType.Builder
					.of(BlockEntitySerpentineIdol::new, BlockInit.serpentine_idol.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityHumaneIdol>> humane_idol = TILES
			.register("humane_idol",	() -> BlockEntityType.Builder
					.of(BlockEntityHumaneIdol::new, BlockInit.humane_idol.get()).build(null));
	
	
	
	
	public static final RegistryObject<BlockEntityType<BlockEntityMortalDisplay>> mortal_display = TILES
			.register("mortal_display", () -> BlockEntityType.Builder
					.of(BlockEntityMortalDisplay::new, BlockInit.mortal_display.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityVisceralRecaller>> visceral_artificial_recaller = TILES
			.register("visceral_artificial_recaller", () -> BlockEntityType.Builder
					.of(BlockEntityVisceralRecaller::new, BlockInit.visceral_artificial_recaller.get()).build(null));
}
