package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cast alignment and kill alignment have separate switches. Source-level because toggling live server config
 * inside a GameTest run disturbs unrelated concurrently running fixtures.
 */
class TemperShiftConfigSourceTest {
	private static final Path EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/temperament/BloodTemperamentEvents.java");
	private static final Path CONFIG = Path.of("src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java");

	@Test
	void castShiftsUseTheirOwnSwitch() throws IOException {
		String events = Files.readString(EVENTS);
		int kill = events.indexOf("public static void onEntityKilled(");
		int cast = events.indexOf("@Nullable EnumBloodTemper secondaryTemper, float scale) {");
		assertTrue(kill >= 0 && cast > kill, "kill handler and scaled cast shift must exist");
		String killBody = events.substring(kill, events.indexOf("determineTemperFromKill(victim", kill));
		String castBody = events.substring(cast, events.indexOf("syncTemperament(player, cap);", cast));
		assertTrue(killBody.contains("TEMPER_SHIFT_ON_KILL_ENABLED.get()"), "kills gate on the kill switch");
		assertTrue(castBody.contains("TEMPER_SHIFT_ON_CAST_ENABLED.get()"), "casts gate on the cast switch");
		assertTrue(!castBody.contains("TEMPER_SHIFT_ON_KILL_ENABLED"), "casts must not read the kill switch");
	}

	@Test
	void castSwitchDefaultsOn() throws IOException {
		String config = Files.readString(CONFIG);
		String define = ".define(\"temperShiftOnCastEnabled\", true)";
		int first = config.indexOf(define);
		assertTrue(first >= 0 && config.indexOf(define, first + 1) < 0,
				"temperShiftOnCastEnabled must be defined once, default true");
	}
}
