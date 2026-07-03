package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PowerGuardrailStateTest {
	private PowerGuardrailStateTest() {
	}

	public static void main(String[] args) throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/PowerGuardrailState.java"));
		serializesAndDeserializesGuardrailState(source);
		clampsDegenerateStateValues(source);
	}

	private static void serializesAndDeserializesGuardrailState(String source) {
		assertContains("circulation window start is serialized", source,
				"tag.putLong(CIRCULATION_WINDOW_START_KEY, circulationWindowStart)");
		assertContains("circulation window used is serialized", source,
				"tag.putDouble(CIRCULATION_WINDOW_USED_KEY, circulationWindowUsed)");
		assertContains("borrowed blood is serialized", source,
				"tag.putDouble(BORROWED_BLOOD_KEY, borrowedBlood)");
		assertContains("armed source is serialized", source,
				"tag.putString(LAST_RITE_ARMED_SOURCE_KEY, lastRiteArmedSource)");
		assertContains("cooldown is serialized", source,
				"tag.putLong(LAST_RITE_COOLDOWN_UNTIL_KEY, lastRiteCooldownUntil)");
		assertContains("circulation window start is deserialized", source,
				"setCirculationWindowStart(nbt.getLong(CIRCULATION_WINDOW_START_KEY))");
		assertContains("circulation window used is deserialized", source,
				"setCirculationWindowUsed(nbt.getDouble(CIRCULATION_WINDOW_USED_KEY))");
		assertContains("borrowed blood is deserialized", source,
				"setBorrowedBlood(nbt.getDouble(BORROWED_BLOOD_KEY))");
		assertContains("armed source is deserialized", source,
				"setLastRiteArmedSource(nbt.getString(LAST_RITE_ARMED_SOURCE_KEY))");
		assertContains("cooldown is deserialized", source,
				"setLastRiteCooldownUntil(nbt.getLong(LAST_RITE_COOLDOWN_UNTIL_KEY))");
	}

	private static void clampsDegenerateStateValues(String source) {
		assertContains("window start clamps to zero", source,
				"this.circulationWindowStart = Math.max(0L, circulationWindowStart)");
		assertContains("window used clamps to zero", source,
				"this.circulationWindowUsed = Math.max(0.0D, circulationWindowUsed)");
		assertContains("borrowed blood clamps to zero", source,
				"this.borrowedBlood = Math.max(0.0D, borrowedBlood)");
		assertContains("cooldown clamps to zero", source,
				"this.lastRiteCooldownUntil = Math.max(0L, lastRiteCooldownUntil)");
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}
}
