package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.*;
import com.vincenthuto.hemomancy.common.tile.crafting.*;
import com.vincenthuto.hemomancy.common.tile.functional.*;
import com.vincenthuto.hemomancy.common.tile.puzzle.BloodBasinBlockEntity;
import com.vincenthuto.hemomancy.common.tile.puzzle.BloodPylonBlockEntity;
import com.vincenthuto.hemomancy.common.tile.puzzle.BloodTrialAltarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityInit {
	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister
			.create(Registries.BLOCK_ENTITY_TYPE, Hemomancy.MOD_ID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScarStationBlockEntity>> scar_station = TILES
			.register("scar_station", () -> BlockEntityType.Builder
					.of(ScarStationBlockEntity::new, BlockInit.scar_station.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SemiSentientConstructBlockEntity>> semi_sentient_construct = TILES
			.register("semi_sentient_construct", () -> BlockEntityType.Builder
					.of(SemiSentientConstructBlockEntity::new, BlockInit.semi_sentient_construct.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MorphlingIncubatorBlockEntity>> morphling_incubator = TILES
			.register("morphling_incubator", () -> BlockEntityType.Builder
					.of(MorphlingIncubatorBlockEntity::new, BlockInit.morphling_incubator.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MorphlingCradleBlockEntity>> morphling_cradle = TILES
			.register("morphling_cradle", () -> BlockEntityType.Builder
					.of(MorphlingCradleBlockEntity::new, BlockInit.morphling_cradle.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpecimenJarBlockEntity>> specimen_jar = TILES
			.register("specimen_jar", () -> BlockEntityType.Builder
					.of(SpecimenJarBlockEntity::new, BlockInit.specimen_jar.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UnstainedPodiumBlockEntity>> unstained_podium = TILES
			.register("unstained_podium", () -> BlockEntityType.Builder
					.of(UnstainedPodiumBlockEntity::new, BlockInit.unstained_podium.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarOfCleansingBlockEntity>> altar_of_cleansing = TILES
			.register("altar_of_cleansing", () -> BlockEntityType.Builder
					.of(AltarOfCleansingBlockEntity::new, BlockInit.altar_of_cleansing.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScryingPodiumBlockEntity>> scrying_podium = TILES
			.register("scrying_podium", () -> BlockEntityType.Builder
					.of(ScryingPodiumBlockEntity::new, BlockInit.scrying_podium.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScarletVanityBlockEntity>> scarlet_vanity = TILES
			.register("scarlet_vanity", () -> BlockEntityType.Builder
					.of(ScarletVanityBlockEntity::new, BlockInit.scarlet_vanity.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FungalPodiumBlockEntity>> fungal_podium = TILES.register(
			"fungal_podium",
			() -> BlockEntityType.Builder.of(FungalPodiumBlockEntity::new, BlockInit.fungal_podium.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FungalImplantationPylonBlockEntity>> fungal_implantation_pylon = TILES
			.register("fungal_implantation_pylon", () -> BlockEntityType.Builder
					.of(FungalImplantationPylonBlockEntity::new, BlockInit.fungal_implantation_pylon.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DendriticDistributorBlockEntity>> dendritic_distributor = TILES
			.register("dendritic_distributor", () -> BlockEntityType.Builder
					.of(DendriticDistributorBlockEntity::new, BlockInit.dendritic_distributor.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.vincenthuto.hemomancy.common.tile.functional.ConsecratedBloodwellBlockEntity>> consecrated_bloodwell = TILES
			.register("consecrated_bloodwell", () -> BlockEntityType.Builder
					.of(com.vincenthuto.hemomancy.common.tile.functional.ConsecratedBloodwellBlockEntity::new,
							BlockInit.consecrated_bloodwell.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.vincenthuto.hemomancy.common.tile.functional.CovenantThroneBlockEntity>> covenant_throne = TILES
			.register("covenant_throne", () -> BlockEntityType.Builder
					.of(com.vincenthuto.hemomancy.common.tile.functional.CovenantThroneBlockEntity::new,
							BlockInit.covenant_throne.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HematicArmatureBlockEntity>> hematic_armature = TILES
			.register("hematic_armature", () -> BlockEntityType.Builder
					.of(HematicArmatureBlockEntity::new, BlockInit.hematic_armature.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.vincenthuto.hemomancy.common.tile.functional.SanguineVigilBlockEntity>> sanguine_vigil = TILES
			.register("sanguine_vigil", () -> BlockEntityType.Builder
					.of(com.vincenthuto.hemomancy.common.tile.functional.SanguineVigilBlockEntity::new,
							BlockInit.sanguine_vigil.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SerpentineIdolBlockEntity>> serpentine_idol = TILES
			.register("serpentine_idol", () -> BlockEntityType.Builder
					.of(SerpentineIdolBlockEntity::new, BlockInit.serpentine_idol.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HumaneIdolBlockEntity>> humane_idol = TILES.register(
			"humane_idol",
			() -> BlockEntityType.Builder.of(HumaneIdolBlockEntity::new, BlockInit.humane_idol.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EarthenVeinBlockEntity>> earthen_vein = TILES.register(
			"earthen_vein",
			() -> BlockEntityType.Builder.of(EarthenVeinBlockEntity::new, BlockInit.earthen_vein.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronBrazierBlockEntity>> iron_brazier = TILES.register(
			"iron_brazier",
			() -> BlockEntityType.Builder.of(IronBrazierBlockEntity::new, BlockInit.iron_brazier.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MortalDisplayBlockEntity>> mortal_display = TILES
			.register("mortal_display", () -> BlockEntityType.Builder
					.of(MortalDisplayBlockEntity::new, BlockInit.mortal_display.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SomaticLoomBlockEntity>> somatic_loom = TILES
			.register("somatic_loom", () -> BlockEntityType.Builder
					.of(SomaticLoomBlockEntity::new, BlockInit.somatic_loom.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GhastlyAlembicBlockEntity>> ghastly_alembic = TILES.register(
			"ghastly_alembic",
			() -> BlockEntityType.Builder.of(GhastlyAlembicBlockEntity::new, BlockInit.ghastly_alembic.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PallidRetortBlockEntity>> pallid_retort = TILES.register(
			"pallid_retort",
			() -> BlockEntityType.Builder.of(PallidRetortBlockEntity::new, BlockInit.pallid_retort.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VialCentrifugeBlockEntity>> vial_centrifuge = TILES
			.register("vial_centrifuge", () -> BlockEntityType.Builder
					.of(VialCentrifugeBlockEntity::new, BlockInit.vial_centrifuge.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PuppeteersSpindleBlockEntity>> puppeteers_spindle = TILES
			.register("puppeteers_spindle", () -> BlockEntityType.Builder
					.of(PuppeteersSpindleBlockEntity::new, BlockInit.puppeteers_spindle.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SuspendedVivianiteBlockEntity>> suspended_vivianite = TILES
			.register("suspended_vivianite", () -> BlockEntityType.Builder
					.of(SuspendedVivianiteBlockEntity::new, BlockInit.suspended_vivianite.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SuspendedBloodCrystalBlockEntity>> suspended_blood_crystal = TILES
			.register("suspended_blood_crystal", () -> BlockEntityType.Builder
					.of(SuspendedBloodCrystalBlockEntity::new, BlockInit.suspended_blood_crystal.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SuspendedCleansedBloodCrystalBlockEntity>> suspended_cleansed_blood_crystal = TILES
			.register("suspended_cleansed_blood_crystal", () -> BlockEntityType.Builder
					.of(SuspendedCleansedBloodCrystalBlockEntity::new, BlockInit.suspended_cleansed_blood_crystal.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FillerBlockEntity>> filler_block = TILES
			.register("filler_block", () -> BlockEntityType.Builder
					.of(FillerBlockEntity::new, BlockInit.filler_block.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbocipherEmitterBlockEntity>> abocipher_emitter = TILES
			.register("abocipher_emitter", () -> BlockEntityType.Builder
					.of(AbocipherEmitterBlockEntity::new, BlockInit.abocipher_emitter.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QliphothBloomBlockEntity>> qliphoth_bloom = TILES
			.register("qliphoth_bloom", () -> BlockEntityType.Builder
					.of(QliphothBloomBlockEntity::new, BlockInit.qliphoth_bloom.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisceralMirrorBlockEntity>> visceral_mirror = TILES
			.register("visceral_mirror", () -> BlockEntityType.Builder
					.of(VisceralMirrorBlockEntity::new, BlockInit.visceral_mirror.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MnemonicReliquaryBlockEntity>> mnemonic_reliquary = TILES
			.register("mnemonic_reliquary", () -> BlockEntityType.Builder
					.of(MnemonicReliquaryBlockEntity::new, BlockInit.mnemonic_reliquary.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DictationTableBlockEntity>> dictation_table = TILES
			.register("dictation_table", () -> BlockEntityType.Builder
					.of(DictationTableBlockEntity::new, BlockInit.dictation_table.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaintSarcophagusBlockEntity>> saint_sarcophagus = TILES
			.register("saint_sarcophagus", () -> BlockEntityType.Builder
					.of(SaintSarcophagusBlockEntity::new, BlockInit.saint_sarcophagus.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SanguineMonolithBlockEntity>> sanguine_monolith = TILES
			.register("sanguine_monolith", () -> BlockEntityType.Builder
					.of(SanguineMonolithBlockEntity::new, BlockInit.sanguine_monolith.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SanguineConduitBlockEntity>> sanguine_conduit = TILES
			.register("sanguine_conduit", () -> BlockEntityType.Builder
					.of(SanguineConduitBlockEntity::new, BlockInit.sanguine_conduit.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HematicSutureNodeBlockEntity>> hematic_suture_node = TILES
			.register("hematic_suture_node", () -> BlockEntityType.Builder
					.of(HematicSutureNodeBlockEntity::new, BlockInit.hematic_suture_node.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DiscoveryInscriptionBlockEntity>> discovery_inscription = TILES
			.register("discovery_inscription", () -> BlockEntityType.Builder
					.of(DiscoveryInscriptionBlockEntity::new, BlockInit.blood_echo_inscription.get(),
							BlockInit.rite_fragment_inscription.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MycelialCrucibleBlockEntity>> mycelial_crucible = TILES
			.register("mycelial_crucible", () -> BlockEntityType.Builder
					.of(MycelialCrucibleBlockEntity::new, BlockInit.mycelial_crucible.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MycelialLanternBlockEntity>> mycelial_lantern = TILES
			.register("mycelial_lantern", () -> BlockEntityType.Builder
					.of(MycelialLanternBlockEntity::new, BlockInit.mycelial_lantern.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodBasinBlockEntity>> blood_basin = TILES
			.register("blood_basin", () -> BlockEntityType.Builder
					.of(BloodBasinBlockEntity::new, BlockInit.blood_basin.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodPylonBlockEntity>> blood_pylon = TILES
			.register("blood_pylon", () -> BlockEntityType.Builder
					.of(BloodPylonBlockEntity::new, BlockInit.blood_pylon.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloodTrialAltarBlockEntity>> blood_trial_altar = TILES
			.register("blood_trial_altar", () -> BlockEntityType.Builder
					.of(BloodTrialAltarBlockEntity::new, BlockInit.blood_trial_altar.get()).build(null));
}
