package com.vincenthuto.hemomancy.common.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.DendriticDistributorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.EarthenVeinBlockEntity;
import com.vincenthuto.hemomancy.common.tile.HumaneIdolBlockEntity;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.tile.JuicinatorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.MortalDisplayBlockEntity;
import com.vincenthuto.hemomancy.common.tile.ScryingPodiumBlockEntity;
import com.vincenthuto.hemomancy.common.tile.SemiSentientConstructBlockEntity;
import com.vincenthuto.hemomancy.common.tile.SerpentineIdolBlockEntity;
import com.vincenthuto.hemomancy.common.tile.UnstainedPodiumBlockEntity;
import com.vincenthuto.hemomancy.common.tile.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.VisceralRecallerBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityInit {
	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Hemomancy.MOD_ID);

	public static final RegistryObject<BlockEntityType<SemiSentientConstructBlockEntity>> semi_sentient_construct = TILES
			.register("semi_sentient_construct", () -> BlockEntityType.Builder
					.of(SemiSentientConstructBlockEntity::new, BlockInit.semi_sentient_construct.get()).build(null));
	public static final RegistryObject<BlockEntityType<MorphlingIncubatorBlockEntity>> morphling_incubator = TILES
			.register("morphling_incubator", () -> BlockEntityType.Builder
					.of(MorphlingIncubatorBlockEntity::new, BlockInit.morphling_incubator.get()).build(null));

	public static final RegistryObject<BlockEntityType<UnstainedPodiumBlockEntity>> unstained_podium = TILES
			.register("unstained_podium", () -> BlockEntityType.Builder
					.of(UnstainedPodiumBlockEntity::new, BlockInit.unstained_podium.get()).build(null));

	public static final RegistryObject<BlockEntityType<ScryingPodiumBlockEntity>> scrying_podium = TILES
			.register("scrying_podium", () -> BlockEntityType.Builder
					.of(ScryingPodiumBlockEntity::new, BlockInit.scrying_podium.get()).build(null));

	public static final RegistryObject<BlockEntityType<DendriticDistributorBlockEntity>> dendritic_distributor = TILES
			.register("dendritic_distributor", () -> BlockEntityType.Builder
					.of(DendriticDistributorBlockEntity::new, BlockInit.dendritic_distributor.get()).build(null));

	public static final RegistryObject<BlockEntityType<SerpentineIdolBlockEntity>> serpentine_idol = TILES
			.register("serpentine_idol", () -> BlockEntityType.Builder
					.of(SerpentineIdolBlockEntity::new, BlockInit.serpentine_idol.get()).build(null));

	public static final RegistryObject<BlockEntityType<HumaneIdolBlockEntity>> humane_idol = TILES.register(
			"humane_idol",
			() -> BlockEntityType.Builder.of(HumaneIdolBlockEntity::new, BlockInit.humane_idol.get()).build(null));

	public static final RegistryObject<BlockEntityType<EarthenVeinBlockEntity>> earthen_vein = TILES.register(
			"earthen_vein",
			() -> BlockEntityType.Builder.of(EarthenVeinBlockEntity::new, BlockInit.earthen_vein.get()).build(null));
	public static final RegistryObject<BlockEntityType<IronBrazierBlockEntity>> iron_brazier = TILES.register(
			"iron_brazier",
			() -> BlockEntityType.Builder.of(IronBrazierBlockEntity::new, BlockInit.iron_brazier.get()).build(null));

	public static final RegistryObject<BlockEntityType<MortalDisplayBlockEntity>> mortal_display = TILES
			.register("mortal_display", () -> BlockEntityType.Builder
					.of(MortalDisplayBlockEntity::new, BlockInit.mortal_display.get()).build(null));

	public static final RegistryObject<BlockEntityType<VisceralRecallerBlockEntity>> visceral_artificial_recaller = TILES
			.register("visceral_artificial_recaller", () -> BlockEntityType.Builder
					.of(VisceralRecallerBlockEntity::new, BlockInit.visceral_artificial_recaller.get()).build(null));

	public static final RegistryObject<BlockEntityType<JuicinatorBlockEntity>> juiceinator = TILES.register(
			"juiceinator",
			() -> BlockEntityType.Builder.of(JuicinatorBlockEntity::new, BlockInit.juiceinator.get()).build(null));

	public static final RegistryObject<BlockEntityType<VialCentrifugeBlockEntity>> vial_centrifuge = TILES
			.register("vial_centrifuge", () -> BlockEntityType.Builder
					.of(VialCentrifugeBlockEntity::new, BlockInit.vial_centrifuge.get()).build(null));
}
