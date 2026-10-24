package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Player-facing text must describe moved machines as Working crafts, not floorless Blood Structures. */
final class WorkingCraftPlayerCopyTest {
	@Test
	void materialAtlasDoesNotCallWorkingCraftStationsBloodStructures() throws IOException {
		String atlas = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/MaterialsData.java"));
		SourceAssertions.assertNotShowsEnglish(
				"The Scarlet Vanity and Fungal Implantation Pylon are Working crafts now", atlas, "Blood Structure station");
	}

	@Test
	void artificerTeachingPutsTheOfferingsInBraziers() throws IOException {
		JsonObject lang = JsonParser.parseString(Files.readString(
				Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
		String line = lang.get("hemomancy.succession.teaching.artificer").getAsString();
		assertTrue(line.contains("Armor Stand and Blood Stained Stone") && line.contains("brazier"),
				"The Vacant Effigy's offerings are lit in braziers: " + line);
	}
}
