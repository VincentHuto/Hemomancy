package com.vincenthuto.hemomancy.common.resource;

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

	public static void main(String[] args) throws IOException {
		for (String id : new String[] { "constrictor_cord", "scale_grip" }) {
			require(ASSETS.resolve("models/item/" + id + ".json"));
			require(ASSETS.resolve("textures/item/" + id + ".png"));
			require(DATA.resolve("recipe/" + id + ".json"));
		}
		require(ASSETS.resolve("textures/mob_effect/constricted.png"));
		assertConstrictorCordAppliesBloodBinding();
	}

	private static void assertConstrictorCordAppliesBloodBinding() throws IOException {
		String projectile = Files.readString(SRC.resolve("common/entity/projectile/ConstrictorCordEntity.java"));
		assertContains(projectile, "EffectInit.blood_binding");
		assertContains(projectile, "ConstrictorCordRules.BLOOD_BINDING_AMPLIFIER");
		assertContains(projectile, "super.onHit(result)");
		assertContains(projectile, "setDeltaMovement(0.0D, 0.0D, 0.0D)");
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
