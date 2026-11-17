package com.vincenthuto.hemomancy.common.resource;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PhaseThreeSerpentUtilityResourceTest {
	private static final Path ROOT = Path.of("src/main/resources");
	private static final Path DATA = ROOT.resolve("data/hemomancy");
	private static final Path ASSETS = ROOT.resolve("assets/hemomancy");
	private static final Path SRC = Path.of("src/main/java/com/vincenthuto/hemomancy");

	private PhaseThreeSerpentUtilityResourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		// The Constrictor Cord folded into the Blood Binding Sculpting (core-loop Track 2): its constriction is the tail.
		for (String id : new String[] { "scale_grip", "sinew_cord" }) {
			require(ASSETS.resolve("models/item/" + id + ".json"));
			require(ASSETS.resolve("textures/item/" + id + ".png"));
			require(DATA.resolve("recipe/" + id + ".json"));
		}
		require(ASSETS.resolve("textures/mob_effect/constricted.png"));
		assertTheBindingSculptingConstricts();
	}

	private static void assertTheBindingSculptingConstricts() throws IOException {
		String sculpting = Files.readString(SRC.resolve("common/manipulation/animus/BloodBindingSculpting.java"));
		assertContains(sculpting, "EffectInit.blood_binding");
		assertContains(sculpting, "EffectInit.constricted");
		if (Files.exists(SRC.resolve("common/entity/projectile/ConstrictorCordEntity.java"))) {
			throw new AssertionError("the Constrictor Cord projectile came back; it is the Blood Binding Sculpting's tail now");
		}
	}

	private static void require(Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
	}

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError("missing " + expected);
		}
	}
}
