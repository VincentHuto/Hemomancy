package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerAssignmentLedgerPortraitSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HarbingerAssignmentLedgerPortraitSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		ledgerDefinesAssignerPortraitResources();
		ledgerRendersPortraitsAtCardHeight();
		ledgerWiresPortraitsToAssignmentCards();
	}

	private static void ledgerDefinesAssignerPortraitResources() throws IOException {
		String ledger = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));

		for (String constant : new String[] {
				"VICAR_PORTRAIT",
				"ALCHEMIST_PORTRAIT",
				"MNEMONIST_PORTRAIT",
				"VEIN_MASON_PORTRAIT",
				"ARTIFICER_PORTRAIT"
		}) {
			assertContains("ledger defines assigner portrait " + constant, ledger, constant);
		}

		for (String texture : new String[] {
				"assets/hemomancy/textures/entity/harbinger_vicar/harbinger_vicar_portrait.png",
				"assets/hemomancy/textures/entity/harbinger_alchemist/harbinger_alchemist_portrait.png",
				"assets/hemomancy/textures/entity/harbinger_mnemonist/harbinger_mnemonist_portrait.png",
				"assets/hemomancy/textures/entity/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite_portrait.png",
				"assets/hemomancy/textures/entity/harbinger_artificer/harbinger_artificer_portrait.png"
		}) {
			assertFileExists("assigner portrait texture exists: " + texture, RESOURCE_ROOT.resolve(texture));
		}
	}

	private static void ledgerRendersPortraitsAtCardHeight() throws IOException {
		String ledger = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));

		assertContains("ledger has shared assigner portrait renderer", ledger, "renderAssignerPortrait");
		assertContains("assignment rows accept an assigner portrait", ledger,
				"ResourceLocation assignerPortrait");
		assertContains("portrait square uses task card height", ledger,
				"int portraitSize = h;");
		assertContains("task card body is offset after portrait", ledger,
				"x + portraitSize + PORTRAIT_GAP");
		assertContains("compact assignment body is offset after portrait", ledger,
				"x + h + PORTRAIT_GAP");
	}

	private static void ledgerWiresPortraitsToAssignmentCards() throws IOException {
		String ledger = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java"));

		assertMethodContains("First Bloodcraft uses Vicar portrait", ledger, "renderFirstBloodcraft", "VICAR_PORTRAIT");
		assertMethodContains("Hermit Road uses Vicar portrait", ledger, "renderHermitRoad", "VICAR_PORTRAIT");
		assertMethodContains("First Separation uses Alchemist portrait", ledger, "renderFirstSeparation", "ALCHEMIST_PORTRAIT");
		assertMethodContains("Red Taxonomy uses Alchemist portrait", ledger, "renderRedTaxonomy", "ALCHEMIST_PORTRAIT");
		assertMethodContains("Living Bestiary uses Alchemist portrait", ledger, "renderLivingBestiary", "ALCHEMIST_PORTRAIT");
		assertMethodContains("Enzyme Mastery uses Alchemist portrait", ledger, "renderEnzymeMastery", "ALCHEMIST_PORTRAIT");
		assertMethodContains("Woven Vessel uses Mnemonist portrait", ledger, "renderWovenVessel", "MNEMONIST_PORTRAIT");
		assertMethodContains("Vein-Mason uses Vein-Mason portrait", ledger, "renderVeinMason", "VEIN_MASON_PORTRAIT");
		assertMethodContains("Worn Vow uses Artificer portrait", ledger, "renderTheWornVow", "ARTIFICER_PORTRAIT");
		assertMethodContains("Three Answers uses Artificer portrait", ledger, "renderTheThreeAnswers", "ARTIFICER_PORTRAIT");
		assertMethodContains("Crimson Vestment uses Artificer portrait", ledger, "renderCrimsonVestment", "ARTIFICER_PORTRAIT");
		assertMethodContains("Weight of the Frame uses Artificer portrait", ledger, "renderWeightOfTheFrame", "ARTIFICER_PORTRAIT");
		assertMethodContains("Assumed Limb uses Artificer portrait", ledger, "renderTheAssumedLimb", "ARTIFICER_PORTRAIT");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertFileExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + " missing: " + path);
		}
	}

	private static void assertMethodContains(String label, String text, String methodName, String expected) {
		int start = text.indexOf("private void " + methodName + "(");
		if (start < 0) {
			throw new AssertionError(label + " missing method: " + methodName);
		}
		int nextMethod = text.indexOf("\n\tprivate ", start + 1);
		String method = nextMethod < 0 ? text.substring(start) : text.substring(start, nextMethod);
		assertContains(label, method, expected);
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " missing: " + expected);
		}
	}
}
