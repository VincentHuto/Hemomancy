package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AlchemistDialogueScopeTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void alchemistDoesNotTeachArtificerOrVicarSubjects() throws IOException {
		String alchemist = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java");
		String artificer = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerArtificerDialogueTrees.java");
		String vicar = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

		assertFalse(alchemist.contains("tell_me_about_armature"));
		assertFalse(alchemist.contains("blood_structure_intro"));
		assertFalse(alchemist.contains("blood_crafting_lore"));
		assertFalse(lang.contains("hemomancy.alchemist.votary.armature_lore"));
		assertFalse(lang.contains("hemomancy.alchemist.votary.blood_structure_intro"));
		assertFalse(lang.contains("hemomancy.alchemist.illuminatus.blood_crafting_lore"));
		assertFalse(lang.contains("hemomancy.alchemist.item_inquiry.enzyme_ferric.line2\": \"Ferric practitioners make better use of blood-structure crafting than most."));
		assertFalse(lang.contains("hemomancy.alchemist.item_inquiry.foul_paste.line1\": \"Foul Paste. A dense organically derived compound used as the binding substrate in blood-structure crafting."));
		assertTrue(artificer.contains("teach_armature"));
		assertTrue(vicar.contains("ask_about_blood_crafting"));
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}
}
