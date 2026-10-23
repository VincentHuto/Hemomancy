package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 11 (roadmap Phase 10, L-9): a station's operations live in its block entity, and its menu only offers and
 * dispatches them. Since the Enzymatic Scriptorium's inscribing and denaturing moved out of its menu, no menu spends
 * blood, records station practice or enchants an item itself.
 */
class StationMenuGuardrailTest {
	private static final List<String> STATION_WORK = List.of(".drain(", "drainBlood(", "recordUse(", ".enchant(", "updateEnchantments(", "setEnchantments(",
			"onEnchantmentPerformed(");

	@Test
	void menusDispatchStationWorkToTheirBlockEntities() throws IOException {
		Map<String, String> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.mainJava("common/menu"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String source = SourceAssertions.read(file);
				for (String call : STATION_WORK) {
					if (source.contains(call)) found.merge(RepoPaths.display(file), call, (a, b) -> a + " " + b);
				}
			}
		}
		assertEquals(Map.of(), found, "move the operation into the station's block entity and call it from the menu");
	}

	@Test
	void theScriptoriumMenuCallsItsStation() {
		String menu = SourceAssertions.readMainJava("common/menu/tile/crafting/EnzymaticScriptoriumMenu.java");
		String handler = SourceAssertions.methodBody(menu, "@Override public boolean clickMenuButton(");
		SourceAssertions.assertContains("inscribing is the station's", handler, "station.inscribe(");
		SourceAssertions.assertContains("denaturing is the station's", handler, "station.denature(");
	}
}
