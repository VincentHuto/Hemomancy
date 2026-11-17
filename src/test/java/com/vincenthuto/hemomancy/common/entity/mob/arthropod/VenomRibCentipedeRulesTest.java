package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VenomRibCentipedeRulesTest {
	@Test
	void keepsRareMiniBossScale() {
		assertEquals(26.0D, VenomRibCentipedeRules.MAX_HEALTH, "venom-rib centipedes have 26 health (CR-8)");
		assertTrue(VenomRibCentipedeRules.ATTACK_DAMAGE >= 5.0D,
				"venom-rib centipedes should hit harder than ordinary cave pests");
		JsonObject spawn = ResourceReader.data("neoforge/biome_modifier/add_venom_rib_centipede.json")
				.getAsJsonObject("spawners");
		assertTrue(spawn.get("weight").getAsInt() <= 4, "venom-rib centipedes should be rare natural spawns");
		assertEquals(1, VenomRibCentipedeRules.MIN_GROUP_SIZE, "minimum group size");
		assertEquals(1, VenomRibCentipedeRules.MAX_GROUP_SIZE, "maximum group size");
		assertEquals(1, spawn.get("minCount").getAsInt(), "the modifier spawns them alone");
		assertEquals(1, spawn.get("maxCount").getAsInt(), "the modifier spawns them alone");
	}

	@Test
	void warnsAndStrikesAtCloseRange() {
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
		assertEquals(160, VenomRibCentipedeRules.POISON_DURATION_TICKS, "venom duration");
	}

	@Test
	void segmentsAndLegsAnimateAsPredator() {
		assertTrue(VenomRibCentipedeSlitherRules.BODY_SEGMENT_COUNT >= 10,
				"centipede should read as long-bodied, not stout");
		assertTrue(VenomRibCentipedeSlitherRules.LEG_PAIRS_PER_SEGMENT >= 1,
				"each segment should carry visible legs");
		float first = VenomRibCentipedeSlitherRules.segmentLocalYaw(0, 1.2F, 0.9F);
		float later = VenomRibCentipedeSlitherRules.segmentLocalYaw(4, 1.2F, 0.9F);
		assertNotEquals(first, later, 0.0001F, "later segments should be phase-offset from the head");
		assertTrue(Math.abs(VenomRibCentipedeSlitherRules.segmentLocalYaw(2, 2.4F, 1.0F)) > 0.12F,
				"centipede body should have a stronger side-to-side squiggle");
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
		assertNotEquals(VenomRibCentipedeSlitherRules.antennaTwitch(12.0F, true),
				VenomRibCentipedeSlitherRules.antennaTwitch(12.0F, false), 0.0001F,
				"left and right antennae should twitch out of phase");
		assertTrue(VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_COUNT >= 5,
				"tail feelers should use enough small segments to curve smoothly");
		assertTrue(VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_LENGTH > 1.0F,
				"tail feeler segments should have visible length");
	}

	@Test
	void resourcesAreRegistered() {
		String entityInit = SourceAssertions.readMainJava("common/init/EntityInit.java");
		String itemInit = SourceAssertions.readMainJava("common/init/ItemInit.java");
		String lang = ResourceReader.text("assets/hemomancy/lang/en_us.json");

		SourceAssertions.assertContains("entity registry", entityInit, "venom_rib_centipede");
		SourceAssertions.assertContains("grounded centipede hitbox", entityInit, ".sized(1.85F, 0.55F)");
		SourceAssertions.assertContains("spawn egg registry", itemInit, "spawn_egg_venom_rib_centipede");
		SourceAssertions.assertContains("entity language", lang,
				"\"entity.hemomancy.venom_rib_centipede\": \"Venom-Rib Centipede\"");
		SourceAssertions.assertContains("spawn egg language", lang,
				"\"item.hemomancy.spawn_egg_venom_rib_centipede\": \"Venom-Rib Centipede Spawn Egg\"");
		SourceAssertions.assertContains("spawn egg model parent",
				ResourceReader.text("assets/hemomancy/models/item/spawn_egg_venom_rib_centipede.json"),
				"\"parent\": \"minecraft:item/template_spawn_egg\"");
		String model = SourceAssertions.readMainJava("client/model/entity/mob/arthropod/VenomRibCentipedeModel.java");
		SourceAssertions.assertContains("smaller centipede head width", model, "5.0F, 2.75F, 4.2F");
		SourceAssertions.assertContains("left antenna root part", model, "\"left_antenna_0\"");
		SourceAssertions.assertContains("right antenna root part", model, "\"right_antenna_0\"");
		SourceAssertions.assertContains("antenna segment count tuning", model,
				"VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_COUNT");
		SourceAssertions.assertContains("antenna segment length tuning", model,
				"VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_LENGTH");
		SourceAssertions.assertContains("tail model part", model, "\"tail\"");
		SourceAssertions.assertContains("left tail feeler root", model, "\"left_tail_feeler_0\"");
		SourceAssertions.assertContains("right tail feeler root", model, "\"right_tail_feeler_0\"");
		SourceAssertions.assertContains("tail feeler segment count tuning", model,
				"VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_COUNT");
		assertTrue(ResourceReader.exists("data/hemomancy/tags/worldgen/biome/venom_rib_centipede_spawnlist.json"),
				"venom-rib centipede spawnlist should exist");
	}
}
