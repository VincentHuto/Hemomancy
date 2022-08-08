package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.tile.BlockEntityDendriticDistributor;
import com.vincenthuto.hemomancy.tile.BlockEntityEarthenVein;
import com.vincenthuto.hemomancy.tile.BlockEntityHumaneIdol;
import com.vincenthuto.hemomancy.tile.BlockEntityIronBrazier;
import com.vincenthuto.hemomancy.tile.BlockEntityJuicinator;
import com.vincenthuto.hemomancy.tile.BlockEntityMorphlingIncubator;
import com.vincenthuto.hemomancy.tile.BlockEntityMortalDisplay;
import com.vincenthuto.hemomancy.tile.BlockEntityScryingPodium;
import com.vincenthuto.hemomancy.tile.BlockEntitySemiSentientConstruct;
import com.vincenthuto.hemomancy.tile.BlockEntitySerpentineIdol;
import com.vincenthuto.hemomancy.tile.BlockEntityUnstainedPodium;
import com.vincenthuto.hemomancy.tile.BlockEntityVisceralRecaller;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityInit {
	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITIES, Hemomancy.MOD_ID);

	public static final RegistryObject<BlockEntityType<BlockEntitySemiSentientConstruct>> semi_sentient_construct = TILES
			.register("semi_sentient_construct", () -> BlockEntityType.Builder
					.of(BlockEntitySemiSentientConstruct::new, BlockInit.semi_sentient_construct.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityMorphlingIncubator>> morphling_incubator = TILES
			.register("morphling_incubator", () -> BlockEntityType.Builder
					.of(BlockEntityMorphlingIncubator::new, BlockInit.morphling_incubator.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityUnstainedPodium>> unstained_podium = TILES
			.register("unstained_podium", () -> BlockEntityType.Builder
					.of(BlockEntityUnstainedPodium::new, BlockInit.unstained_podium.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityScryingPodium>> scrying_podium = TILES
			.register("scrying_podium", () -> BlockEntityType.Builder
					.of(BlockEntityScryingPodium::new, BlockInit.scrying_podium.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityDendriticDistributor>> dendritic_distributor = TILES
			.register("dendritic_distributor", () -> BlockEntityType.Builder
					.of(BlockEntityDendriticDistributor::new, BlockInit.dendritic_distributor.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntitySerpentineIdol>> serpentine_idol = TILES
			.register("serpentine_idol", () -> BlockEntityType.Builder
					.of(BlockEntitySerpentineIdol::new, BlockInit.serpentine_idol.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityHumaneIdol>> humane_idol = TILES.register(
			"humane_idol",
			() -> BlockEntityType.Builder.of(BlockEntityHumaneIdol::new, BlockInit.humane_idol.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityEarthenVein>> earthen_vein = TILES.register(
			"earthen_vein",
			() -> BlockEntityType.Builder.of(BlockEntityEarthenVein::new, BlockInit.earthen_vein.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityIronBrazier>> iron_brazier = TILES.register(
			"iron_brazier",
			() -> BlockEntityType.Builder.of(BlockEntityIronBrazier::new, BlockInit.iron_brazier.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityMortalDisplay>> mortal_display = TILES
			.register("mortal_display", () -> BlockEntityType.Builder
					.of(BlockEntityMortalDisplay::new, BlockInit.mortal_display.get()).build(null));
	public static final RegistryObject<BlockEntityType<BlockEntityVisceralRecaller>> visceral_artificial_recaller = TILES
			.register("visceral_artificial_recaller", () -> BlockEntityType.Builder
					.of(BlockEntityVisceralRecaller::new, BlockInit.visceral_artificial_recaller.get()).build(null));

	public static final RegistryObject<BlockEntityType<BlockEntityJuicinator>> juiceinator = TILES.register(
			"juiceinator",
			() -> BlockEntityType.Builder.of(BlockEntityJuicinator::new, BlockInit.juiceinator.get()).build(null));

}
