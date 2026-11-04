package com.vincenthuto.hemomancy.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.vincenthuto.hemomancy.support.RepoPaths;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Ids and code names renamed to what players see stay gone: the mod is not live, so none keeps an alias. */
class RetiredNamesGuardrailTest {
	private static final Pattern RETIRED = Pattern.compile("spawn_egg_eruptor|puppeteering_thread|iron_wall|covenant_waybill"
			+ "|calcified_blood_spine|spawn_mark|warp_chair|fungal_elytra|humanity_sprite|frayed_thread|circus_carousel"
			+ "|hollow_vessel|legacy_rite_completion|MuscleMemory|muscleMemory|musclememory|muscle_memory|MUSCLE_MEMORY|vermillion_bloodwood|voracious_bloodwood|VermillionCore|VermillionTree");

	private static void scan(Path root, List<String> found) throws IOException {
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				String name = RepoPaths.ROOT.relativize(file).toString().replace('\\', '/');
				if (RETIRED.matcher(file.getFileName().toString()).find()) found.add("file name: " + name);
				if (!name.endsWith(".java") && !name.endsWith(".json")) continue;
				if (RETIRED.matcher(new String(Files.readAllBytes(file), StandardCharsets.UTF_8)).find()) found.add(name);
			}
		}
	}

	@Test
	void noRetiredNameRemainsInCodeOrResources() throws IOException {
		List<String> found = new ArrayList<>();
		scan(RepoPaths.MAIN_JAVA, found);
		scan(RepoPaths.ASSETS, found);
		scan(RepoPaths.DATA, found);
		assertEquals(List.of(), found);
	}
}
