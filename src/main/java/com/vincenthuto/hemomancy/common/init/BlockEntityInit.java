package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.*;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityInit {
	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Hemomancy.MOD_ID);

	public static final RegistryObject<BlockEntityType<ChiselStationBlockEntity>> runic_chisel_station = TILES
			.register("runic_chisel_station", () -> BlockEntityType.Builder
					.of(ChiselStationBlockEntity::new, BlockInit.runic_chisel_station.get()).build(null));

	public static final RegistryObject<BlockEntityType<SemiSentientConstructBlockEntity>> semi_sentient_construct = TILES
			.register("semi_sentient_construct", () -> BlockEntityType.Builder
					.of(SemiSentientConstructBlockEntity::new, BlockInit.semi_sentient_construct.get()).build(null));
	public static final RegistryObject<BlockEntityType<MorphlingIncubatorBlockEntity>> morphling_incubator = TILES
			.register("morphling_incubator", () -> BlockEntityType.Builder
					.of(MorphlingIncubatorBlockEntity::new, BlockInit.morphling_incubator.get()).build(null));

	public static final RegistryObject<BlockEntityType<UnstainedPodiumBlockEntity>> unstained_podium = TILES
			.register("unstained_podium", () -> BlockEntityType.Builder
					.of(UnstainedPodiumBlockEntity::new, BlockInit.unstained_podium.get()).build(null));

	public static final RegistryObject<BlockEntityType<AltarOfCleansingBlockEntity>> altar_of_cleansing = TILES
			.register("altar_of_cleansing", () -> BlockEntityType.Builder
					.of(AltarOfCleansingBlockEntity::new, BlockInit.altar_of_cleansing.get()).build(null));

	public static final RegistryObject<BlockEntityType<ScryingPodiumBlockEntity>> scrying_podium = TILES
			.register("scrying_podium", () -> BlockEntityType.Builder
					.of(ScryingPodiumBlockEntity::new, BlockInit.scrying_podium.get()).build(null));

	public static final RegistryObject<BlockEntityType<FungalPodiumBlockEntity>> fungal_podium = TILES.register(
			"fungal_podium",
			() -> BlockEntityType.Builder.of(FungalPodiumBlockEntity::new, BlockInit.fungal_podium.get()).build(null));

	public static final RegistryObject<BlockEntityType<FungalImplantationPylonBlockEntity>> fungal_implantation_pylon = TILES
			.register("fungal_implantation_pylon", () -> BlockEntityType.Builder
					.of(FungalImplantationPylonBlockEntity::new, BlockInit.fungal_implantation_pylon.get()).build(null));

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

	public static final RegistryObject<BlockEntityType<SomaticLoomBlockEntity>> somatic_loom = TILES
			.register("somatic_loom", () -> BlockEntityType.Builder
					.of(SomaticLoomBlockEntity::new, BlockInit.somatic_loom.get()).build(null));

	public static final RegistryObject<BlockEntityType<JuicinatorBlockEntity>> juiceinator = TILES.register(
			"juiceinator",
			() -> BlockEntityType.Builder.of(JuicinatorBlockEntity::new, BlockInit.juiceinator.get()).build(null));

	public static final RegistryObject<BlockEntityType<VialCentrifugeBlockEntity>> vial_centrifuge = TILES
			.register("vial_centrifuge", () -> BlockEntityType.Builder
					.of(VialCentrifugeBlockEntity::new, BlockInit.vial_centrifuge.get()).build(null));

	public static final RegistryObject<BlockEntityType<SuspendedVivianiteBlockEntity>> suspended_vivianite = TILES
			.register("suspended_vivianite", () -> BlockEntityType.Builder
					.of(SuspendedVivianiteBlockEntity::new, BlockInit.suspended_vivianite.get()).build(null));

	public static final RegistryObject<BlockEntityType<SuspendedBloodCrystalBlockEntity>> suspended_blood_crystal = TILES
			.register("suspended_blood_crystal", () -> BlockEntityType.Builder
					.of(SuspendedBloodCrystalBlockEntity::new, BlockInit.suspended_blood_crystal.get()).build(null));

	public static final RegistryObject<BlockEntityType<FillerBlockEntity>> filler_block = TILES
			.register("filler_block", () -> BlockEntityType.Builder
					.of(FillerBlockEntity::new, BlockInit.filler_block.get()).build(null));

	public static final RegistryObject<BlockEntityType<VisceralMirrorBlockEntity>> visceral_mirror = TILES
			.register("visceral_mirror", () -> BlockEntityType.Builder
					.of(VisceralMirrorBlockEntity::new, BlockInit.visceral_mirror.get()).build(null));

	public static final RegistryObject<BlockEntityType<MnemonicReliquaryBlockEntity>> mnemonic_reliquary = TILES
			.register("mnemonic_reliquary", () -> BlockEntityType.Builder
					.of(MnemonicReliquaryBlockEntity::new, BlockInit.mnemonic_reliquary.get()).build(null));
}
