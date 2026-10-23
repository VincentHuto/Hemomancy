package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 9 (roadmap Phase 6): heals are modified in one stage, the {@code HealModifier} table (G-4), and a player's
 * flight abilities are written only by {@code FlightArbiter} (G-7).
 */
class HealAndFlightGuardrailTest {
	private static final String COMMON = "src/main/java/com/vincenthuto/hemomancy/common/";
	private static final Pattern FLIGHT_WRITE = Pattern.compile("\\.(?:mayfly|flying)\\s*=(?!=)");
	private static final Pattern HEAL_LISTENER = Pattern.compile("\\(\\s*LivingHealEvent\\s+\\w+\\s*\\)");

	@Test
	void onlyTheFlightArbiterWritesFlight() throws IOException {
		assertEquals(Map.of(COMMON + "util/FlightArbiter.java", 5), found(FLIGHT_WRITE),
				"grant, revoke or ground flight through FlightArbiter with the Source that grants it");
	}

	@Test
	void healsAreModifiedInOneStage() throws IOException {
		assertEquals(Map.of(COMMON + "manipulation/ManipulationStatusEvents.java", 1,
						COMMON + "capability/player/unstained/PurityGainEvents.java", 1), found(HEAL_LISTENER),
				"add a HealModifier entry instead of another LivingHealEvent listener");
		String stage = SourceAssertions.methodBody(SourceAssertions.readMainJava(
				"common/manipulation/ManipulationStatusEvents.java"), "public static void onLivingHeal(");
		SourceAssertions.assertContains("the stage runs the table", stage, "HealModifier.values()");
		String purity = SourceAssertions.readMainJava("common/capability/player/unstained/PurityGainEvents.java");
		SourceAssertions.assertContains("restraint purity reads the requested heal before the table", purity,
				"@SubscribeEvent(priority = EventPriority.HIGH) // the requested heal, before the HealModifier stage\n"
						+ "    public static void onLivingHeal(LivingHealEvent event)");
		SourceAssertions.assertNotContains("the reader never changes the heal",
				SourceAssertions.methodBody(purity, "public static void onLivingHeal("), "setAmount(");
	}

	private static Map<String, Integer> found(Pattern pattern) throws IOException {
		Map<String, Integer> found = new TreeMap<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (shown.contains("/compat/mna/") || shown.contains("/compat/curios/")) continue;
				Matcher matcher = pattern.matcher(SourceAssertions.read(file));
				int count = 0;
				while (matcher.find()) count++;
				if (count > 0) found.put(shown, count);
			}
		}
		return found;
	}
}
