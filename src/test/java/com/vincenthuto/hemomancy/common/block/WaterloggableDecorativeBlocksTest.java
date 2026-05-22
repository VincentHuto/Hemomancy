package com.vincenthuto.hemomancy.common.block;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WaterloggableDecorativeBlocksTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
	private static final String HELPER_PATH =
			"com/vincenthuto/hemomancy/common/block/shared/WaterloggedBlockSupport.java";

	private static final TargetBlock[] TARGETS = {
			new TargetBlock("suspended_blood_crystal",
					"com/vincenthuto/hemomancy/common/block/harbinger/SuspendedBloodCrystalBlock.java"),
			new TargetBlock("suspended_cleansed_blood_crystal",
					"com/vincenthuto/hemomancy/common/block/unstained/SuspendedCleansedBloodCrystalBlock.java"),
			new TargetBlock("suspended_vivianite",
					"com/vincenthuto/hemomancy/common/block/harbinger/SuspendedVivianiteBlock.java"),
			new TargetBlock("blood_echo_inscription",
					"com/vincenthuto/hemomancy/common/block/inscription/DiscoveryInscriptionBlock.java"),
			new TargetBlock("rite_fragment_inscription",
					"com/vincenthuto/hemomancy/common/block/inscription/DiscoveryInscriptionBlock.java"),
			new TargetBlock("specimen_jar",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/SpecimenJarBlock.java"),
			new TargetBlock("blood_crystal",
					"com/vincenthuto/hemomancy/common/block/harbinger/BloodCrystalBlock.java"),
			new TargetBlock("pale_silver_bells",
					"com/vincenthuto/hemomancy/common/block/unstained/functional/PaleSilverBellsBlock.java"),
			new TargetBlock("iron_brazier",
					"com/vincenthuto/hemomancy/common/block/harbinger/BrazierBlock.java"),
			new TargetBlock("sanguine_conduit",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineConduitBlock.java"),
			new TargetBlock("mycelial_lantern",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/MycelialLanternBlock.java"),
			new TargetBlock("morphling_cradle",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/MorphlingCradleBlock.java"),
			new TargetBlock("mortal_display",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/MortalDisplayBlock.java"),
			new TargetBlock("dictation_table",
					"com/vincenthuto/hemomancy/common/block/inscription/DictationTableBlock.java"),
			new TargetBlock("mnemonic_reliquary",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/MnemonicReliquaryBlock.java"),
			new TargetBlock("visceral_mirror",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/VisceralMirrorBlock.java"),
			new TargetBlock("ghastly_alembic",
					"com/vincenthuto/hemomancy/common/block/harbinger/crafting/GhastlyAlembicBlock.java"),
			new TargetBlock("pallid_retort",
					"com/vincenthuto/hemomancy/common/block/unstained/crafting/PallidRetortBlock.java"),
			new TargetBlock("gourd",
					"com/vincenthuto/hemomancy/common/block/harbinger/plant/GourdBlock.java"),
			new TargetBlock("humane_idol",
					"com/vincenthuto/hemomancy/common/block/harbinger/idol/BlockHumaneIdol.java"),
			new TargetBlock("serpentine_idol",
					"com/vincenthuto/hemomancy/common/block/harbinger/idol/BlockSerpentineIdol.java"),
			new TargetBlock("scar_station",
					"com/vincenthuto/hemomancy/common/block/harbinger/crafting/ScarStationBlock.java"),
			new TargetBlock("morphling_incubator",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/MorphlingIncubatorBlock.java"),
			new TargetBlock("mycelial_crucible",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/MycelialCrucibleBlock.java"),
			new TargetBlock("fungal_implantation_pylon",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/FungalImplantationPylonBlock.java"),
			new TargetBlock("vial_centrifuge",
					"com/vincenthuto/hemomancy/common/block/harbinger/crafting/VialCentrifugeBlock.java"),
			new TargetBlock("scrying_podium",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/ScryingPodiumBlock.java"),
			new TargetBlock("unstained_podium",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/UnstainedPodiumBlock.java"),
			new TargetBlock("semi_sentient_construct",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/SemiSentientConstructBlock.java"),
			new TargetBlock("altar_of_cleansing",
					"com/vincenthuto/hemomancy/common/block/unstained/functional/AltarOfCleansingBlock.java"),
			new TargetBlock("sanguine_monolith",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/SanguineMonolithBlock.java"),
			new TargetBlock("filler_block",
					"com/vincenthuto/hemomancy/common/block/shared/FillerBlock.java"),
			new TargetBlock("somatic_loom",
					"com/vincenthuto/hemomancy/common/block/harbinger/crafting/SomaticLoomBlock.java"),
			new TargetBlock("puppeteers_spindle",
					"com/vincenthuto/hemomancy/common/block/harbinger/crafting/PuppeteersSpindleBlock.java"),
			new TargetBlock("fungal_podium",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/FungalPodiumBlock.java"),
			new TargetBlock("covenant_throne",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/CovenantThroneBlock.java"),
			new TargetBlock("saint_sarcophagus",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/SaintSarcophagusBlock.java"),
			new TargetBlock("dendritic_distributor",
					"com/vincenthuto/hemomancy/common/block/harbinger/functional/DendriticDistributorBlock.java")
	};

	private WaterloggableDecorativeBlocksTest() {
	}

	public static void main(String[] args) throws IOException {
		String helper = readSource(HELPER_PATH);
		assertContains("shared waterlogging helper should expose placement helper", helper, "waterloggedForPlacement");
		assertContains("shared waterlogging helper should expose fluid-state helper", helper, "fluidState");
		assertContains("shared waterlogging helper should expose water tick helper", helper, "scheduleWaterTick");
		assertContains("shared waterlogging helper should preserve water on support loss", helper, "survivorOrWater");

		for (TargetBlock target : TARGETS) {
			String source = readSource(target.sourcePath());
			assertContains(target.id() + " should declare WATERLOGGED", source, "WATERLOGGED");
			assertContains(target.id() + " should be a SimpleWaterloggedBlock", source, "SimpleWaterloggedBlock");
			assertContains(target.id() + " should register WATERLOGGED in its state definition", source,
					".add(");
			assertContains(target.id() + " should expose water as its fluid state", source, "getFluidState");
			assertContains(target.id() + " should schedule water ticks", source, "scheduleWaterTick");

			String blockstate = readResource("assets/hemomancy/blockstates/" + target.id() + ".json");
			assertContains(target.id() + " blockstate should ignore waterlogged variants", blockstate, "\"multipart\"");
			assertDoesNotContain(target.id() + " blockstate should not enumerate waterlogged variants",
					blockstate, "waterlogged=");
		}
	}

	private static String readSource(String path) throws IOException {
		return read(SOURCE_ROOT.resolve(path));
	}

	private static String readResource(String path) throws IOException {
		return read(RESOURCE_ROOT.resolve(path));
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}

	private record TargetBlock(String id, String sourcePath) {
	}
}
