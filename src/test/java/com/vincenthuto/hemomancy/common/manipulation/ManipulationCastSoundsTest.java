package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import java.util.Map;

class ManipulationCastSoundsTest {
	@Test
	void mapsEverySilentActiveCastToItsApprovedCue() {
		Map<String, Expected> expected = Map.ofEntries(
				entry("blood_shot", ManipulationCastSounds.Cue.LLAMA_SPIT, 0.65F, 0.85F),
				entry("blood_needle", ManipulationCastSounds.Cue.SKELETON_SHOOT, 0.65F, 1.25F),
				entry("blood_rush", ManipulationCastSounds.Cue.PLAYER_ATTACK_SWEEP, 0.60F, 0.75F),
				entry("summon_avatar", ManipulationCastSounds.Cue.EVOKER_CAST_SPELL, 0.65F, 0.85F),
				entry("crimson_coronation", ManipulationCastSounds.Cue.WITHER_SPAWN, 0.40F, 1.50F),
				entry("deadly_gaze", ManipulationCastSounds.Cue.ILLUSIONER_CAST_SPELL, 0.60F, 0.70F),
				entry("activation_potential", ManipulationCastSounds.Cue.TRIDENT_THUNDER, 0.25F, 1.25F),
				entry("synaptic_storm", ManipulationCastSounds.Cue.TRIDENT_THUNDER, 0.35F, 0.75F),
				entry("living_circuit", ManipulationCastSounds.Cue.TRIDENT_RETURN, 0.50F, 1.40F),
				entry("sanguine_ward", ManipulationCastSounds.Cue.SHIELD_BLOCK, 0.60F, 0.80F),
				entry("white_verdict", ManipulationCastSounds.Cue.BEACON_ACTIVATE, 0.65F, 1.60F),
				entry("furnace_veins", ManipulationCastSounds.Cue.FIRECHARGE_USE, 0.70F, 0.90F),
				entry("absolute_stillness", ManipulationCastSounds.Cue.POWDER_SNOW_BREAK, 0.65F, 0.65F),
				entry("rimebound_sentence", ManipulationCastSounds.Cue.GLASS_PLACE, 0.70F, 0.75F),
				entry("iron_choir", ManipulationCastSounds.Cue.ANVIL_USE, 0.50F, 1.40F),
				entry("conjure_staff", ManipulationCastSounds.Cue.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.20F, 0.95F),
				entry("blood_absorption", ManipulationCastSounds.Cue.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.20F, 0.95F),
				entry("blood_projection", ManipulationCastSounds.Cue.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.20F, 0.95F),
				entry("funeral_bell", ManipulationCastSounds.Cue.BELL_BLOCK, 0.90F, 0.45F),
				entry("carrion_communion", ManipulationCastSounds.Cue.SCULK_BLOCK_SPREAD, 0.55F, 0.65F),
				entry("penumbral_drift", ManipulationCastSounds.Cue.ENDERMAN_TELEPORT, 0.40F, 1.55F),
				entry("eclipse_well", ManipulationCastSounds.Cue.WARDEN_SONIC_BOOM, 0.40F, 0.65F));

		for (Map.Entry<String, Expected> entry : expected.entrySet()) {
			ManipulationCastSounds.Profile actual = ManipulationCastSounds.profileFor(entry.getKey());
			Expected wanted = entry.getValue();
			if (actual == null || actual.cue() != wanted.cue()
					|| actual.volume() != wanted.volume() || actual.pitch() != wanted.pitch()) {
				throw new AssertionError(entry.getKey() + ": expected " + wanted + " but got " + actual);
			}
			if (actual.volume() <= 0.0F || actual.volume() > 1.0F
					|| actual.pitch() < 0.4F || actual.pitch() > 2.0F) {
				throw new AssertionError(entry.getKey() + " has an unsafe sound profile: " + actual);
			}
		}
	}

	@Test
	void leavesExistingPassiveAndRetiredAudioAlone() {
		for (String manipulation : new String[] { "blood_binding", "lignum_mortis", "hematic_flare",
				"blackhearted", "sovereign_instinct", "blood_lamp" }) {
			if (ManipulationCastSounds.profileFor(manipulation) != null) {
				throw new AssertionError("Unexpected fallback sound for " + manipulation);
			}
		}
	}

	private static Map.Entry<String, Expected> entry(String id, ManipulationCastSounds.Cue cue,
			float volume, float pitch) {
		return Map.entry(id, new Expected(cue, volume, pitch));
	}

	private record Expected(ManipulationCastSounds.Cue cue, float volume, float pitch) {
	}
}
