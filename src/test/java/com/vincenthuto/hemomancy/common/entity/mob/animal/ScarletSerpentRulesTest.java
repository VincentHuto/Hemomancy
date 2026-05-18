package com.vincenthuto.hemomancy.common.entity.mob.animal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class ScarletSerpentRulesTest {
	public static void main(String[] args) throws IOException {
		flaresBeforeItStrikes();
		ignoresCreativeAndSpectatorPlayers();
		exposesMidTierStrikeDurations();
		propagatesSlitherDownBody();
		lootTableUsesCompleteNumberProviders();
	}

	private static void flaresBeforeItStrikes() {
		assertTrue(ScarletSerpentRules.shouldFlareHood(true, false, false,
				ScarletSerpentRules.HOOD_FLARE_RANGE_BLOCKS - 0.25D),
				"scarlet serpents should flare their hood near vulnerable players");
		assertFalse(ScarletSerpentRules.shouldFlareHood(true, false, false,
				ScarletSerpentRules.HOOD_FLARE_RANGE_BLOCKS + 0.25D),
				"scarlet serpents should fold their hood outside warning range");
		assertTrue(ScarletSerpentRules.shouldStrike(true, false, false,
				ScarletSerpentRules.STRIKE_RANGE_BLOCKS - 0.25D),
				"scarlet serpents should strike only once players remain close");
		assertFalse(ScarletSerpentRules.shouldStrike(true, false, false,
				ScarletSerpentRules.STRIKE_RANGE_BLOCKS + 0.25D),
				"scarlet serpents should warn before they commit to melee");
	}

	private static void ignoresCreativeAndSpectatorPlayers() {
		assertFalse(ScarletSerpentRules.shouldFlareHood(true, true, false, 1.0D),
				"creative players should not provoke hood flares");
		assertFalse(ScarletSerpentRules.shouldFlareHood(true, false, true, 1.0D),
				"spectator players should not provoke hood flares");
		assertFalse(ScarletSerpentRules.shouldStrike(false, false, false, 1.0D),
				"dead players should not be strike targets");
	}

	private static void exposesMidTierStrikeDurations() {
		assertEquals("poison duration", 100, ScarletSerpentRules.POISON_DURATION_TICKS);
		assertEquals("blood binding duration", 60, ScarletSerpentRules.BLOOD_BINDING_DURATION_TICKS);
		assertEquals("strike animation duration", 10, ScarletSerpentRules.STRIKE_ANIMATION_TICKS);
	}

	private static void propagatesSlitherDownBody() {
		float waveTime = 1.25F;
		float movement = 0.8F;
		int segment = 4;

		float localYaw = ScarletSerpentSlitherRules.segmentLocalYaw(segment, waveTime, movement);
		float propagatedYaw = ScarletSerpentSlitherRules.segmentPropagatedYaw(segment, waveTime, movement);

		assertClose("propagated yaw includes upstream segments",
				localYaw + ScarletSerpentSlitherRules.segmentPropagatedYaw(segment - 1, waveTime, movement),
				propagatedYaw);
		assertNotClose("middle segments should not move independently", localYaw, propagatedYaw);
		assertEquals("body segment count", 9, ScarletSerpentSlitherRules.BODY_SEGMENT_COUNT);
	}

	private static void lootTableUsesCompleteNumberProviders() throws IOException {
		String lootTable = Files.readString(Path.of(
				"src/main/resources/data/hemomancy/loot_table/entities/scarlet_serpent.json"))
				.replace("\r\n", "\n");

		assertContains("serpent scale drop", lootTable, "\"name\": \"hemomancy:serpent_scale\"");
		assertContains("looting count provider", lootTable,
				"""
				            {
				              "function": "minecraft:enchanted_count_increase",
				              "enchantment": "minecraft:looting",
				              "count": {
				                "type": "minecraft:uniform",
				                "min": 0.0,
				                "max": 1.0
				              }
				            }
				""".replace("\r\n", "\n").trim());
		assertContains("visible base drop", lootTable, "\"min\": 1.0");
		assertFalse(lootTable.contains("minecraft:looting_enchant"),
				"scarlet serpent loot table should not use the removed 1.20 looting function id");
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertClose(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertNotClose(String label, float unexpected, float actual) {
		if (Math.abs(unexpected - actual) <= 0.0001F) {
			throw new AssertionError(label + ": both values were " + actual);
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

	private static void assertContains(String label, String content, String expected) {
		if (!content.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
