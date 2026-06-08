package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
public final class ManipulationLoadoutTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private ManipulationLoadoutTest() {
	}

	public static void main(String[] args) throws IOException {
		nBTContractPersistsNameSelectionAndOrder();
		blankNamesDefaultToSlotIndex();
		mechanicalManipulationsAreNotSavedAsLoadoutEntries();
		applyingLoadoutsUsesMutableEquippedNameCopies();
	}

	private static void nBTContractPersistsNameSelectionAndOrder() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationLoadout.java");
		String knownSource = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulations.java");

		assertContains("loadout writes display name", source, "tag.putString(\"name\", name)");
		assertContains("loadout writes selected manipulation", source, "tag.putString(\"selectedManip\", selectedManipName)");
		assertContains("loadout writes ordered manipulation list", source, "tag.put(\"manips\", manipTag)");
		assertContains("loadout reads ordered manipulation list", source, "names.add(manipTag.getString(i))");
		assertContains("known manipulations saves loadouts", knownSource, "loadoutsEntry.put(\"synapticLoadouts\", loadoutsTag)");
		assertContains("known manipulations loads loadouts", knownSource, "setLoadouts(parsedLoadouts)");
	}

	private static void blankNamesDefaultToSlotIndex() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationLoadout.java");

		assertContains("blank names default to slot index", source, "return \"Loadout \" + (slotIndex + 1)");
	}

	private static void mechanicalManipulationsAreNotSavedAsLoadoutEntries() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationLoadout.java");

		assertContains("selected mechanical manipulation falls back to a saved normal manipulation", source,
				"ManipulationEquipHelper.isFixedMechanicalManip(selectedManipName)");
		assertContains("fixed mechanical manipulations are not persisted in loadouts", source,
				"!ManipulationEquipHelper.isFixedMechanicalManip(cleaned)");
	}

	private static void applyingLoadoutsUsesMutableEquippedNameCopies() throws IOException {
		String knownSource = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/KnownManipulations.java");

		assertContains("known manipulations defensively copies equipped names before normalization", knownSource,
				"this.equippedManipNames = names != null ? new ArrayList<>(names) : new ArrayList<>()");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
