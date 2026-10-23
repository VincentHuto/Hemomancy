package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * F-23 / D-4: Minecraft 1.21 reads singular data folders ({@code tags/item}, {@code recipe}, {@code loot_table}). A file
 * in a pre-1.21 plural folder is silently ignored, so none may be added. The listed files stay where they are on
 * purpose: they name items that are not registered, so moving them would make their tags fail to load.
 */
class DataFolderNamingGuardrailTest {
	private static final Set<String> PLURAL_FOLDERS = Set.of("items", "blocks", "fluids", "entity_types", "game_events",
			"recipes", "loot_tables", "advancements", "structures", "functions", "predicates", "item_modifiers");
	private static final Map<String, String> IGNORED_ON_PURPOSE = Map.of(
			"src/main/resources/data/curios/tags/items/belt.json", "names the unregistered harbinger_grimore (Curios compat is off)",
			"src/main/resources/data/curios/tags/items/spell.json", "names the unregistered harbinger_grimore (Curios compat is off)",
			"src/main/resources/data/forge/tags/fluids/test_fluid.json", "names the unregistered test fluids, in the retired forge namespace",
			"src/main/resources/data/mna/tags/items/dusts/purified_vinteum.json", "names the unregistered befouled_vinteum_dust (MnA compat is off)");

	@Test
	void noDataFileSitsInAPluralFolder() throws Exception {
		Set<String> found = new TreeSet<>();
		for (String root : List.of("src/main/resources/data", "src/generated/resources/data")) {
			Path base = RepoPaths.resolve(root);
			if (!Files.isDirectory(base)) continue;
			try (Stream<Path> files = Files.walk(base)) {
				files.filter(Files::isRegularFile).forEach(file -> {
					Path relative = base.relativize(file);
					// namespace / folder [/ folder] ...: data folders sit at depth 1, tag registries under tags/
					for (int i = 1; i < relative.getNameCount() - 1 && i <= 2; i++) {
						String folder = relative.getName(i).toString();
						boolean dataFolder = i == 1 || relative.getName(1).toString().equals("tags");
						if (dataFolder && PLURAL_FOLDERS.contains(folder)) {
							found.add(RepoPaths.display(file));
							break;
						}
					}
				});
			}
		}
		assertEquals(new TreeSet<>(IGNORED_ON_PURPOSE.keySet()), found,
				"Use the singular 1.21 folder (tags/item, recipe, loot_table, ...); list a deliberately ignored file with its reason");
	}
}
