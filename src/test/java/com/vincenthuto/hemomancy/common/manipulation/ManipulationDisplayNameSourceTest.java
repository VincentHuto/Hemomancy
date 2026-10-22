package com.vincenthuto.hemomancy.common.manipulation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Player-facing manipulation names come from {@code hemomancy.manipulation.<id>} when the lang has one (Osseous
 * Bloom displays as Crystalline Bloom) and fall back to the ID-derived name otherwise. Source-level because the
 * display sites are client screens.
 */
class ManipulationDisplayNameSourceTest {
	private static final Path MAIN = Path.of("src/main/java/com/vincenthuto/hemomancy");

	private static String read(String relative) throws IOException {
		return Files.readString(MAIN.resolve(relative), StandardCharsets.UTF_8);
	}

	@Test
	void displayNameUsesTheLangKeyWithTheIdFallback() throws IOException {
		String manip = read("common/manipulation/BloodManipulation.java");
		assertTrue(manip.contains("\"hemomancy.manipulation.\" + manipulationName"), "display key is hemomancy.manipulation.<id>");
		assertTrue(manip.contains("Component.translatableWithFallback(displayNameKey(manipulationName),"),
				"display name must fall back when the lang has no entry");
		assertTrue(manip.contains("HLTextUtils.convertInitToLang(manipulationName)"), "fallback stays the ID-derived name");
	}

	@Test
	void playerFacingSitesUseTheDisplayName() throws IOException {
		for (String site : List.of(
				"client/screen/manips/RadialChooseManipScreen.java",
				"client/screen/skilltree/harbinger/ManipulationsTabController.java",
				"client/screen/tile/functional/MnemonicReliquaryScreen.java",
				"client/screen/item/ScryingDiagnosticsScreen.java",
				"client/screen/item/VascularViewScreen.java",
				"common/item/harbinger/memories/BloodMemoryItem.java",
				"common/item/harbinger/memories/CrudeMemoryShardItem.java",
				"common/entity/npc/DrudgeEntity.java",
				"common/network/capa/harbinger/manips/ChangeSelectedManipPacket.java",
				"common/network/capa/harbinger/manips/UpdateCurrentManipPacket.java",
				"common/capability/player/harbinger/manip/KnownManipulationEvents.java")) {
			String source = read(site);
			assertFalse(source.contains(".getProperName()"), site + " still shows the ID-derived name");
		}
		String tab = read("client/screen/skilltree/harbinger/ManipulationsTabController.java");
		assertFalse(tab.contains("toProperCase(manip.getName()"), "tab name must come from the display helper");
	}

	@Test
	void crystallineBloomRenameIsLangOnly() throws IOException {
		JsonObject lang = JsonParser.parseString(Files.readString(
				Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"), StandardCharsets.UTF_8)).getAsJsonObject();
		assertEquals("Crystalline Bloom", lang.get("hemomancy.manipulation.osseous_bloom").getAsString());
		assertTrue(read("common/init/ManipulationInit.java").contains("MANIPS.register(\"osseous_bloom\""),
				"the registry ID stays osseous_bloom");
	}
}
