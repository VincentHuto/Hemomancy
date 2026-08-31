package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

final class ManipulationCastSounds {
	private ManipulationCastSounds() {
	}

	static void play(Level level, Player player, BloodManipulation manipulation) {
		Profile profile = profileFor(manipulation.getName());
		if (profile == null) return;
		var sound = switch (profile.cue()) {
			case LLAMA_SPIT -> SoundEvents.LLAMA_SPIT;
			case SKELETON_SHOOT -> SoundEvents.SKELETON_SHOOT;
			case PLAYER_ATTACK_SWEEP -> SoundEvents.PLAYER_ATTACK_SWEEP;
			case EVOKER_CAST_SPELL -> SoundEvents.EVOKER_CAST_SPELL;
			case WITHER_SPAWN -> SoundEvents.WITHER_SPAWN;
			case ILLUSIONER_CAST_SPELL -> SoundEvents.ILLUSIONER_CAST_SPELL;
			case TRIDENT_THUNDER -> SoundEvents.TRIDENT_THUNDER.value();
			case TRIDENT_RETURN -> SoundEvents.TRIDENT_RETURN;
			case SHIELD_BLOCK -> SoundEvents.SHIELD_BLOCK;
			case BEACON_ACTIVATE -> SoundEvents.BEACON_ACTIVATE;
			case FIRECHARGE_USE -> SoundEvents.FIRECHARGE_USE;
			case POWDER_SNOW_BREAK -> SoundEvents.POWDER_SNOW_BREAK;
			case GLASS_PLACE -> SoundEvents.GLASS_PLACE;
			case ANVIL_USE -> SoundEvents.ANVIL_USE;
			case HOGLIN_CONVERTED_TO_ZOMBIFIED -> SoundEvents.HOGLIN_CONVERTED_TO_ZOMBIFIED;
			case BELL_BLOCK -> SoundEvents.BELL_BLOCK;
			case SCULK_BLOCK_SPREAD -> SoundEvents.SCULK_BLOCK_SPREAD;
			case ENDERMAN_TELEPORT -> SoundEvents.ENDERMAN_TELEPORT;
			case WARDEN_SONIC_BOOM -> SoundEvents.WARDEN_SONIC_BOOM;
		};
		level.playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS,
				profile.volume(), profile.pitch());
	}

	static Profile profileFor(String manipulation) {
		return switch (manipulation) {
			case "blood_shot" -> new Profile(Cue.LLAMA_SPIT, 0.65F, 0.85F);
			case "blood_needle" -> new Profile(Cue.SKELETON_SHOOT, 0.65F, 1.25F);
			case "blood_rush" -> new Profile(Cue.PLAYER_ATTACK_SWEEP, 0.60F, 0.75F);
			case "summon_avatar" -> new Profile(Cue.EVOKER_CAST_SPELL, 0.65F, 0.85F);
			case "crimson_coronation" -> new Profile(Cue.WITHER_SPAWN, 0.40F, 1.50F);
			case "deadly_gaze" -> new Profile(Cue.ILLUSIONER_CAST_SPELL, 0.60F, 0.70F);
			case "activation_potential" -> new Profile(Cue.TRIDENT_THUNDER, 0.25F, 1.25F);
			case "synaptic_storm" -> new Profile(Cue.TRIDENT_THUNDER, 0.35F, 0.75F);
			case "living_circuit" -> new Profile(Cue.TRIDENT_RETURN, 0.50F, 1.40F);
			case "sanguine_ward" -> new Profile(Cue.SHIELD_BLOCK, 0.60F, 0.80F);
			case "white_verdict" -> new Profile(Cue.BEACON_ACTIVATE, 0.65F, 1.60F);
			case "furnace_veins" -> new Profile(Cue.FIRECHARGE_USE, 0.70F, 0.90F);
			case "absolute_stillness" -> new Profile(Cue.POWDER_SNOW_BREAK, 0.65F, 0.65F);
			case "rimebound_sentence" -> new Profile(Cue.GLASS_PLACE, 0.70F, 0.75F);
			case "iron_choir" -> new Profile(Cue.ANVIL_USE, 0.50F, 1.40F);
			case "conjure_staff", "blood_absorption", "blood_projection" ->
					new Profile(Cue.HOGLIN_CONVERTED_TO_ZOMBIFIED, 0.20F, 0.95F);
			case "funeral_bell" -> new Profile(Cue.BELL_BLOCK, 0.90F, 0.45F);
			case "carrion_communion" -> new Profile(Cue.SCULK_BLOCK_SPREAD, 0.55F, 0.65F);
			case "penumbral_drift" -> new Profile(Cue.ENDERMAN_TELEPORT, 0.40F, 1.55F);
			case "eclipse_well" -> new Profile(Cue.WARDEN_SONIC_BOOM, 0.40F, 0.65F);
			default -> null;
		};
	}

	enum Cue {
		LLAMA_SPIT,
		SKELETON_SHOOT,
		PLAYER_ATTACK_SWEEP,
		EVOKER_CAST_SPELL,
		WITHER_SPAWN,
		ILLUSIONER_CAST_SPELL,
		TRIDENT_THUNDER,
		TRIDENT_RETURN,
		SHIELD_BLOCK,
		BEACON_ACTIVATE,
		FIRECHARGE_USE,
		POWDER_SNOW_BREAK,
		GLASS_PLACE,
		ANVIL_USE,
		HOGLIN_CONVERTED_TO_ZOMBIFIED,
		BELL_BLOCK,
		SCULK_BLOCK_SPREAD,
		ENDERMAN_TELEPORT,
		WARDEN_SONIC_BOOM
	}

	record Profile(Cue cue, float volume, float pitch) {
	}
}
