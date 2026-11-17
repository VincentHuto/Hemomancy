package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

/**
 * Each manipulation's {@code manipulation.<id>.cast} event (UN-12). The event's volume, pitch and stand-in vanilla
 * cue live in {@code sounds.json}, so a sourced file replaces the cue without a code change. Player casts and
 * {@link EntityCasts} read the same event.
 */
public final class ManipulationCastSounds {
	/** Every manipulation with a cast event, registered by {@link SoundInit#MANIPULATION_CAST}. */
	public static final List<String> CAST_EVENTS = List.of(
			"absolute_stillness", "activation_potential", "blood_absorption", "blood_aneurysm", "blood_cloud",
			"blood_eclipse", "blood_needle", "blood_needle_fan", "blood_needle_lance", "blood_projection", "blood_rush",
			"blood_shot", "carrion_communion", "conductive_mark", "conjure_staff", "crimson_coronation",
			"crimson_flame_conjuration", "cruor_javelin", "cryogenic_pulse", "deadly_gaze", "eclipse_well",
			"exsanguinate", "fruiting_contagion", "funeral_bell", "furnace_veins", "glacial_grasp", "glacial_rampart",
			"gloam_laceration", "grave_debt", "guided_blood_shot", "hematic_beacon", "hematic_flare", "hematic_mortar",
			"hemolymphal_pulse", "hemorrhage", "insatiable_hunger", "iron_choir", "iron_retort", "living_circuit",
			"lodestone_collapse", "penumbral_drift", "prismatic_reproof", "pyretic_forge", "radiant_wake",
			"rimebound_sentence", "sanguine_halo", "sanguine_ignition", "sanguine_ward", "scalding_updraft",
			"searing_lash", "summon_avatar", "summon_avatar_armor", "summon_avatar_arms", "summon_avatar_complete",
			"summon_avatar_legs", "synaptic_jolt", "synaptic_storm", "umbral_rend", "umbral_step", "vitric_combustion",
			"void_shroud", "white_verdict");

	/** Manipulations whose player cast plays its event from here; the others sound from their own code. */
	static final Set<String> PLAYER_CASTS = Set.of(
			"blood_shot", "guided_blood_shot", "sanguine_halo", "hematic_mortar",
			"blood_needle", "blood_needle_fan", "blood_needle_lance", "blood_rush",
			"summon_avatar", "summon_avatar_arms", "summon_avatar_armor", "summon_avatar_legs", "summon_avatar_complete",
			"crimson_coronation", "deadly_gaze", "activation_potential", "synaptic_storm", "living_circuit",
			"sanguine_ward", "white_verdict", "furnace_veins", "absolute_stillness", "rimebound_sentence", "iron_choir",
			"conjure_staff", "blood_absorption", "blood_projection", "funeral_bell", "carrion_communion",
			"penumbral_drift", "eclipse_well", "searing_lash", "radiant_wake", "lodestone_collapse", "cruor_javelin",
			"fruiting_contagion");

	private ManipulationCastSounds() {
	}

	public static void play(Level level, Player player, BloodManipulation manipulation) {
		if (!PLAYER_CASTS.contains(manipulation.getName())) return;
		play(level, player.blockPosition(), SoundSource.PLAYERS, manipulation.getName(), 1.0F);
	}

	/** An entity's cast of {@code manipulation}, heard where the cast lands. */
	public static void playEntityCast(ManipulationCastContext context, BlockPos at, String manipulation) {
		playEntityCast(context, at, manipulation, 1.0F);
	}

	public static void playEntityCast(ManipulationCastContext context, BlockPos at, String manipulation, float pitch) {
		play(context.level(), at, SoundSource.HOSTILE, manipulation, pitch);
	}

	public static void play(Level level, BlockPos at, SoundSource source, String manipulation, float pitch) {
		var event = SoundInit.MANIPULATION_CAST.get(manipulation);
		if (event == null) throw new IllegalArgumentException("No cast sound event for " + manipulation);
		level.playSound(null, at, event.get(), source, 1.0F, pitch);
	}
}
