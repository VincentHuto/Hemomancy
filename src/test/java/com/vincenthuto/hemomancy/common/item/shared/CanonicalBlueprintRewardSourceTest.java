package com.vincenthuto.hemomancy.common.item.shared;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CanonicalBlueprintRewardSourceTest {
	@Test
	void legacyBlueprintItemsRemainFullyRetired() throws Exception {
		String items = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		assertFalse(items.contains("BASEITEMS.register(\"rite_hint\""));
		assertFalse(items.contains("BASEITEMS.register(\"blood_structure_hint\""));
		assertFalse(Files.exists(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/shared/RiteHintItem.java")));
		assertFalse(Files.exists(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/BloodStructureHintItem.java")));
		String target = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/shared/MnemonicBlueprintTarget.java"));
		assertFalse(target.contains("fromLegacyTag"));
	}

	@Test
	void dialogueRewardsConstructTheCanonicalMnemonicBlueprint() throws Exception {
		String dialogue = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		assertTrue(dialogue.contains("MnemonicBlueprintItem.create(ItemInit.mnemonic_blueprint.get()"));
		assertFalse(dialogue.contains("RiteHintItem.createForRite"));
		assertFalse(dialogue.contains("BloodStructureHintItem.createForStructure"));
	}

	@Test
	void noGameplayDataIssuesLegacyBlueprintRegistryIds() throws Exception {
		Path dataRoot = Path.of("src/main/resources/data");
		List<Path> offenders;
		try (var files = Files.walk(dataRoot)) {
			offenders = files.filter(Files::isRegularFile)
					.filter(path -> path.toString().endsWith(".json") || path.toString().endsWith(".mcfunction"))
					.filter(path -> {
						try {
							String text = Files.readString(path);
							return text.contains("hemomancy:rite_hint")
									|| text.contains("hemomancy:blood_structure_hint");
						} catch (Exception exception) {
							throw new RuntimeException(exception);
						}
					}).toList();
		}
		assertTrue(offenders.isEmpty(), "Legacy blueprint rewards found in: " + offenders);
	}
}
