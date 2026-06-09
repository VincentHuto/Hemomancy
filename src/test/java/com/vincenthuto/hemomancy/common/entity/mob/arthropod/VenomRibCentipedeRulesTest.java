package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class VenomRibCentipedeRulesTest {
	public static void main(String[] args) throws IOException {
		keepsRareMiniBossScale();
		warnsAndStrikesAtCloseRange();
		segmentsAndLegsAnimateAsPredator();
		resourcesAreRegistered();
	}

	private static void keepsRareMiniBossScale() {
		assertTrue(VenomRibCentipedeRules.MAX_HEALTH >= 28.0D,
				"venom-rib centipedes should have rare predator durability");
		assertTrue(VenomRibCentipedeRules.ATTACK_DAMAGE >= 5.0D,
				"venom-rib centipedes should hit harder than ordinary cave pests");
		assertTrue(VenomRibCentipedeRules.NATURAL_SPAWN_WEIGHT <= 4,
				"venom-rib centipedes should be rare natural spawns");
		assertEquals("minimum group size", 1, VenomRibCentipedeRules.MIN_GROUP_SIZE);
		assertEquals("maximum group size", 1, VenomRibCentipedeRules.MAX_GROUP_SIZE);
	}

	private static void warnsAndStrikesAtCloseRange() {
		assertTrue(VenomRibCentipedeRules.shouldWarn(true, false, false,
				VenomRibCentipedeRules.WARNING_RANGE_BLOCKS - 0.25D),
				"survival players entering the warning radius should alert centipedes");
		assertTrue(VenomRibCentipedeRules.shouldStrike(true, false, false,
				VenomRibCentipedeRules.STRIKE_RANGE_BLOCKS - 0.25D),
				"centipedes should strike only at close range");
		assertFalse(VenomRibCentipedeRules.shouldStrike(true, true, false, 1.0D),
				"creative players should not be hunted");
		assertFalse(VenomRibCentipedeRules.shouldStrike(true, false, true, 1.0D),
				"spectators should not be hunted");
		assertEquals("venom duration", 160, VenomRibCentipedeRules.POISON_DURATION_TICKS);
	}

	private static void segmentsAndLegsAnimateAsPredator() {
		assertTrue(VenomRibCentipedeSlitherRules.BODY_SEGMENT_COUNT >= 10,
				"centipede should read as long-bodied, not stout");
		assertTrue(VenomRibCentipedeSlitherRules.LEG_PAIRS_PER_SEGMENT >= 1,
				"each segment should carry visible legs");
		float first = VenomRibCentipedeSlitherRules.segmentLocalYaw(0, 1.2F, 0.9F);
		float later = VenomRibCentipedeSlitherRules.segmentLocalYaw(4, 1.2F, 0.9F);
		assertNotClose("later segments should be phase-offset from the head", first, later);
		assertTrue(Math.abs(VenomRibCentipedeSlitherRules.segmentLocalYaw(2, 2.4F, 1.0F)) > 0.12F,
				"centipede body should have a stronger side-to-side squiggle");
		assertTrue(Math.abs(VenomRibCentipedeSlitherRules.legPitch(3, true, 2.0F, 1.0F)) > 0.05F,
				"legs should visibly skitter while moving");
		assertTrue(Math.abs(VenomRibCentipedeSlitherRules.legPitch(3, true, 2.0F, 1.0F)) > 0.35F,
				"moving legs should have a strong skittering swing");
		assertTrue(Math.abs(VenomRibCentipedeSlitherRules.legLift(3, true, 2.0F, 1.0F)) > 0.08F,
				"moving legs should also lift/drop instead of only rotating");
		assertTrue(VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_COUNT >= 5,
				"antennae should be segmented enough to curl like real centipede feelers");
		assertTrue(VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_LENGTH > 1.0F,
				"individual antenna segments should have visible length");
		assertTrue(VenomRibCentipedeSlitherRules.ANTENNA_BACK_SWEEP > 0.1F,
				"antennae should sweep back when the centipede moves");
		assertNotClose("left and right antennae should twitch out of phase",
				VenomRibCentipedeSlitherRules.antennaTwitch(12.0F, true),
				VenomRibCentipedeSlitherRules.antennaTwitch(12.0F, false));
		assertTrue(VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_COUNT >= 5,
				"tail feelers should use enough small segments to curve smoothly");
		assertTrue(VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_LENGTH > 1.0F,
				"tail feeler segments should have visible length");
	}

	private static void resourcesAreRegistered() throws IOException {
		String entityInit = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java"));
		String itemInit = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String lang = Files.readString(Path.of("src/main/resources/assets/hemomancy/lang/en_us.json"));

		assertContains("entity registry", entityInit, "venom_rib_centipede");
		assertContains("grounded centipede hitbox", entityInit, ".sized(1.85F, 0.55F)");
		assertContains("spawn egg registry", itemInit, "spawn_egg_venom_rib_centipede");
		assertContains("entity language", lang,
				"\"entity.hemomancy.venom_rib_centipede\": \"Venom-Rib Centipede\"");
		assertContains("spawn egg language", lang,
				"\"item.hemomancy.spawn_egg_venom_rib_centipede\": \"Venom-Rib Centipede Spawn Egg\"");
		assertSpawnEggModel("spawn_egg_venom_rib_centipede");
		String model = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/arthropod/VenomRibCentipedeModel.java"));
		assertContains("smaller centipede head width", model, "5.0F, 2.75F, 4.2F");
		assertContains("left antenna root part", model, "\"left_antenna_0\"");
		assertContains("right antenna root part", model, "\"right_antenna_0\"");
		assertContains("antenna segment count tuning", model, "VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_COUNT");
		assertContains("antenna segment length tuning", model, "VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_LENGTH");
		assertContains("tail model part", model, "\"tail\"");
		assertContains("left tail feeler root", model, "\"left_tail_feeler_0\"");
		assertContains("right tail feeler root", model, "\"right_tail_feeler_0\"");
		assertContains("tail feeler segment count tuning", model,
				"VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_COUNT");
		assertTrue(Files.exists(Path.of(
				"src/main/resources/data/hemomancy/neoforge/biome_modifier/add_venom_rib_centipede.json")),
				"venom-rib centipede biome modifier should exist");
		assertTrue(Files.exists(Path.of(
				"src/main/resources/data/hemomancy/tags/worldgen/biome/venom_rib_centipede_spawnlist.json")),
				"venom-rib centipede spawnlist should exist");
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}

	private static void assertNotClose(String label, float unexpected, float actual) {
		if (Math.abs(unexpected - actual) <= 0.0001F) {
			throw new AssertionError(label + ": both values were " + actual);
		}
	}

	private static void assertContains(String label, String content, String expected) {
		if (!content.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertSpawnEggModel(String itemId) throws IOException {
		String model = Files.readString(Path.of(
				"src/main/resources/assets/hemomancy/models/item/" + itemId + ".json"));
		assertContains(itemId + " model parent", model, "\"parent\": \"minecraft:item/template_spawn_egg\"");
	}
}
