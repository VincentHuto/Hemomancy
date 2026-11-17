package com.vincenthuto.hemomancy.common.manipulation;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** UN-12: each manipulation has one {@code manipulation.<id>.cast} event, standing in with today's vanilla cue. */
class ManipulationCastSoundsTest {
	@Test
	void everyPlayerCastKeepsItsCueThroughItsOwnEvent() {
		Map<String, Expected> expected = Map.ofEntries(
				entry("blood_shot", "entity.llama.spit", 0.65F, 0.85F),
				entry("blood_needle", "entity.skeleton.shoot", 0.65F, 1.25F),
				entry("blood_rush", "entity.player.attack.sweep", 0.60F, 0.75F),
				entry("summon_avatar", "entity.evoker.cast_spell", 0.65F, 0.85F),
				entry("crimson_coronation", "entity.wither.spawn", 0.40F, 1.50F),
				entry("deadly_gaze", "entity.illusioner.cast_spell", 0.60F, 0.70F),
				entry("activation_potential", "item.trident.thunder", 0.25F, 1.25F),
				entry("synaptic_storm", "item.trident.thunder", 0.35F, 0.75F),
				entry("living_circuit", "item.trident.return", 0.50F, 1.40F),
				entry("sanguine_ward", "item.shield.block", 0.60F, 0.80F),
				entry("white_verdict", "block.beacon.activate", 0.65F, 1.60F),
				entry("furnace_veins", "item.firecharge.use", 0.70F, 0.90F),
				entry("absolute_stillness", "block.powder_snow.break", 0.65F, 0.65F),
				entry("rimebound_sentence", "block.glass.place", 0.70F, 0.75F),
				entry("iron_choir", "block.anvil.use", 0.50F, 1.40F),
				entry("conjure_staff", "entity.hoglin.converted_to_zombified", 0.20F, 0.95F),
				entry("blood_absorption", "entity.hoglin.converted_to_zombified", 0.20F, 0.95F),
				entry("blood_projection", "entity.hoglin.converted_to_zombified", 0.20F, 0.95F),
				entry("funeral_bell", "block.bell.use", 0.90F, 0.45F),
				entry("carrion_communion", "block.sculk.spread", 0.55F, 0.65F),
				entry("penumbral_drift", "entity.enderman.teleport", 0.40F, 1.55F),
				entry("eclipse_well", "entity.warden.sonic_boom", 0.40F, 0.65F));

		TreeMap<String, String> wrong = new TreeMap<>();
		for (Map.Entry<String, Expected> entry : expected.entrySet()) {
			assertTrue(ManipulationCastSounds.PLAYER_CASTS.contains(entry.getKey()), entry.getKey() + " plays from the cast table");
			Expected actual = alias(entry.getKey());
			if (!entry.getValue().equals(actual)) wrong.put(entry.getKey(), "expected " + entry.getValue() + " but got " + actual);
		}
		assertEquals(new TreeMap<>(), wrong, "a cast event keeps today's cue until its file arrives");
	}

	@Test
	void everyRegisteredCastEventIsInSoundsJsonAndNoOther() {
		TreeSet<String> json = new TreeSet<>();
		for (String key : sounds().keySet()) {
			if (key.startsWith("manipulation.") && key.endsWith(".cast")) {
				json.add(key.substring("manipulation.".length(), key.length() - ".cast".length()));
			}
		}
		assertEquals(new TreeSet<>(ManipulationCastSounds.CAST_EVENTS), json);
		assertTrue(json.containsAll(ManipulationCastSounds.PLAYER_CASTS), "every player cast has its event");
	}

	@Test
	void everyCastEventPlaysAtASafeLevel() {
		TreeMap<String, Expected> unsafe = new TreeMap<>();
		for (String manipulation : ManipulationCastSounds.CAST_EVENTS) {
			Expected cue = alias(manipulation);
			if (cue.volume() <= 0.0F || cue.volume() > 1.0F || cue.pitch() < 0.4F || cue.pitch() > 2.0F) unsafe.put(manipulation, cue);
		}
		assertEquals(new TreeMap<>(), unsafe);
	}

	@Test
	void leavesExistingPassiveAndRetiredAudioAlone() {
		for (String manipulation : new String[] { "blood_binding", "lignum_mortis", "hematic_flare",
				"blackhearted", "sovereign_instinct", "blood_lamp" }) {
			assertFalse(ManipulationCastSounds.PLAYER_CASTS.contains(manipulation), "Unexpected cast-table sound for " + manipulation);
		}
	}

	/** Umbral Rend sweeps from its own code; the cast table no longer doubles it. */
	@Test
	void umbralRendIsNotDoubled() {
		assertFalse(ManipulationCastSounds.PLAYER_CASTS.contains("umbral_rend"));
		assertTrue(ManipulationCastSounds.CAST_EVENTS.contains("umbral_rend"));
	}

	private static JsonObject sounds() {
		return ResourceReader.asset("sounds.json");
	}

	private static Expected alias(String manipulation) {
		JsonObject event = sounds().getAsJsonObject("manipulation." + manipulation + ".cast");
		if (event == null) return null;
		JsonObject sound = event.getAsJsonArray("sounds").get(0).getAsJsonObject();
		assertEquals("event", sound.get("type").getAsString(), manipulation + " aliases a vanilla event");
		return new Expected(sound.get("name").getAsString().replace("minecraft:", ""),
				sound.has("volume") ? sound.get("volume").getAsFloat() : 1.0F,
				sound.has("pitch") ? sound.get("pitch").getAsFloat() : 1.0F);
	}

	private static Map.Entry<String, Expected> entry(String id, String event, float volume, float pitch) {
		return Map.entry(id, new Expected(event, volume, pitch));
	}

	private record Expected(String event, float volume, float pitch) {
	}
}
