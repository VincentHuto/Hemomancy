package com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cast alignment and kill alignment have separate switches. Source-level because toggling live server config
 * inside a GameTest run disturbs unrelated concurrently running fixtures.
 */
class TendencyShiftConfigSourceTest {
	private static final Path EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/tendency/BloodTendencyEvents.java");
	private static final Path CONFIG = Path.of("src/main/java/com/vincenthuto/hemomancy/config/HemoServerConfig.java");

	@Test
	void castShiftsUseTheirOwnSwitch() throws IOException {
		String events = Files.readString(EVENTS);
		int kill = events.indexOf("public static void onEntityKilled(");
		int cast = events.indexOf("@Nullable EnumBloodTendency secondaryTendency, float scale) {");
		assertTrue(kill >= 0 && cast > kill, "kill handler and scaled cast shift must exist");
		String killBody = events.substring(kill, events.indexOf("determineTendencyFromKill(victim", kill));
		String castBody = events.substring(cast, events.indexOf("syncTendency(player, cap);", cast));
		assertTrue(killBody.contains("TENDENCY_SHIFT_ON_KILL_ENABLED.get()"), "kills gate on the kill switch");
		assertTrue(castBody.contains("TENDENCY_SHIFT_ON_CAST_ENABLED.get()"), "casts gate on the cast switch");
		assertTrue(!castBody.contains("TENDENCY_SHIFT_ON_KILL_ENABLED"), "casts must not read the kill switch");
	}

	@Test
	void castSwitchDefaultsOn() throws IOException {
		String config = Files.readString(CONFIG);
		String define = ".define(\"tendencyShiftOnCastEnabled\", true)";
		int first = config.indexOf(define);
		assertTrue(first >= 0 && config.indexOf(define, first + 1) < 0,
				"tendencyShiftOnCastEnabled must be defined once, default true");
	}
}
