package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MorphlingStage4To8SourceTest {
	private static final Path SRC = Path.of("src/main/java");
	private static final Path RES = Path.of("src/main/resources");

	private MorphlingStage4To8SourceTest() {
	}

	public static void main(String[] args) throws Exception {
		hungerConfigAndStackStateAreActiveByDefault();
		wildCradleStaffAndClientPipelinesAreRetargeted();
		puppetInterferenceUsesExistingUpkeepAndHudPipelines();
	}

	private static void hungerConfigAndStackStateAreActiveByDefault() throws IOException {
		String config = read("com/vincenthuto/hemomancy/config/HemoServerConfig.java");
		String morphling = read("com/vincenthuto/hemomancy/common/item/harbinger/morphlings/MorphlingItem.java");
		assertContains("hunger config field", config, "MORPHLING_HUNGER_ENABLED");
		assertContains("hunger default true", config, "define(\"hungerEnabled\", true)");
		assertContains("husbandry quota config field", config, "MORPHLING_HUSBANDRY_STAGE_QUOTA");
		assertContains("husbandry default zero", config, "defineInRange(\"husbandryStageQuota\", 0");
		assertContains("last fed stack data", morphling, "LAST_FED_GAME_TIME_KEY");
		assertContains("experience stack data", morphling, "EXPERIENCE_PROGRESS_KEY");
		assertContains("hunger rule helper", morphling, "MorphlingHungerRules");
	}

	private static void wildCradleStaffAndClientPipelinesAreRetargeted() throws IOException {
		String cradle = read("com/vincenthuto/hemomancy/common/tile/harbinger/functional/MorphlingCradleBlockEntity.java");
		String staffModel = Files.readString(RES.resolve("assets/hemomancy/models/item/living_staff.json"));
		assertContains("cradle primal filter exists", cradle, "isCradleSupportedPrimalStrain");
		for (int i = 1; i <= 8; i++) {
			assertContains("staff predicate " + i, staffModel, "\"hemomancy:staff_visual\": " + i);
		}
	}

	private static void puppetInterferenceUsesExistingUpkeepAndHudPipelines() throws IOException {
		String events = read("com/vincenthuto/hemomancy/common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java");
		String overlay = read("com/vincenthuto/hemomancy/client/screen/overlay/EquippedMorphlingOverlay.java");
		assertContains("active tether detection", events, "BoundSummonBehavior.hasActiveOwnedTether");
		assertContains("final blood surcharge", events, "MorphlingUpkeepRules.withPuppetInterference");
		assertContains("surcharge does not add bonding", events, "MorphlingUpkeepRules.bondingCredit");
		assertContains("distracted HUD status", overlay, "morphling.interference.distracted");
	}

	private static String read(String relative) throws IOException {
		return Files.readString(SRC.resolve(relative));
	}

	private static void assertContains(String label, String haystack, String needle) {
		if (!haystack.contains(needle)) {
			throw new AssertionError(label + ": missing " + needle);
		}
	}

	private static void assertDoesNotContain(String label, String haystack, String needle) {
		if (haystack.contains(needle)) {
			throw new AssertionError(label + ": still contains " + needle);
		}
	}
}
