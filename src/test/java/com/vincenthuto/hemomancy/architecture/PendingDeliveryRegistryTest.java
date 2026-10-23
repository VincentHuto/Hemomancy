package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * S-5: earned items are delivered by {@code PendingDelivery} alone. Each claim is listed in its driver, no other class
 * counts insertions by hand, and the claims keep the NBT keys their saves already hold.
 */
class PendingDeliveryRegistryTest {
	private static final String HELPER = "common/capability/PendingDelivery.java";
	private static final Pattern COUNTED_INSERT = Pattern.compile("before\\s*=\\s*[^;]*countItem\\(");

	/** Each claim, the class that owns it, and a key its saved data already uses. */
	private static final Map<String, String[]> CLAIMS = Map.of(
			"FirstDrawsAssignment.VIALS",
			new String[] {"common/mission/alchemist/FirstDrawsAssignment.java", "\"VialsPending\""},
			"FirstSeparationAssignment.BRIEFING_VIALS",
			new String[] {"common/mission/alchemist/FirstSeparationAssignment.java", "\"hemomancy:first_separation_pending_vials\""},
			"FirstSeparationAssignment.TOOLS",
			new String[] {"common/mission/alchemist/FirstSeparationAssignment.java", "\"hemomancy:first_separation_pending_rewards\""},
			"MycophantEncounterManager.TENDRIL",
			new String[] {"common/encounter/MycophantEncounterManager.java", "\":mycophant_tendril_pending\""},
			"VesperOrdealManager.MEMORY",
			new String[] {"common/encounter/VesperOrdealManager.java", "\":vesper_memory_pending\""},
			"QliphothBloomEvents.FUNGAL_SPINE",
			new String[] {"common/rite/harbinger/QliphothBloomEvents.java", "setFungalSpineGranted(true)"});

	@Test
	void onlyPendingDeliveryCountsInsertions() throws Exception {
		List<String> offending = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
				if (!shown.equals(HELPER) && COUNTED_INSERT.matcher(SourceAssertions.read(file)).find()) offending.add(shown);
			}
		}
		assertEquals(List.of(), offending, "Deliver earned items through PendingDelivery");
	}

	@Test
	void everyClaimIsDrivenAndKeepsItsSavedKey() {
		String helper = SourceAssertions.readMainJava(HELPER);
		for (var claim : CLAIMS.entrySet()) {
			SourceAssertions.assertContains("driver", helper, claim.getKey());
			String owner = SourceAssertions.readMainJava(claim.getValue()[0]);
			String field = claim.getKey().substring(claim.getKey().indexOf('.') + 1);
			SourceAssertions.assertContains(claim.getValue()[0], owner, "PendingDelivery.Claim " + field + " = ");
			SourceAssertions.assertContains(claim.getValue()[0], owner, claim.getValue()[1]);
		}
	}
}
