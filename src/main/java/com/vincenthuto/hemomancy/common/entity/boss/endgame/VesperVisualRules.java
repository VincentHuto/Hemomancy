package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import java.util.List;
import java.util.Map;

/** Pure visual-language rules shared by both forms of the Vesper encounter. */
public final class VesperVisualRules {
	private static final Map<String, List<String>> FAMILIES = Map.ofEntries(
			Map.entry("crowned_ambient", List.of("dark_glow", "sporitic_spore", "blood_cell")),
			Map.entry("crowned_telegraph", List.of("dark_glow", "blood_cell")),
			Map.entry("throne_wound", List.of("dark_glow", "blood_cell", "lightning")),
			Map.entry("throne_break", List.of("blood_cell", "dark_glow", "tendril", "lightning")),
			Map.entry("transformation", List.of("absorbed_blood_cell", "dark_glow", "tendril", "lightning")),
			Map.entry("evening_ambient", List.of("glow", "dark_glow", "blood_cell")),
			Map.entry("stance_shift", List.of("glow", "dark_glow", "blood_cell")),
			Map.entry("rage", List.of("blood_cell", "dark_glow", "ember", "tendril", "lightning")),
			Map.entry("death", List.of("dark_glow", "blood_cell", "ember", "lightning")),
			Map.entry("blood_blade", List.of("blood_cell", "glow", "dark_glow")),
			Map.entry("living_axe", List.of("contextual_block_debris", "dark_glow", "ember")),
			Map.entry("living_spear", List.of("glow", "blood_cell", "tendril")),
			Map.entry("gloam_claw", List.of("claw_slash", "blood_claw", "dark_glow")),
			Map.entry("crimson_torch", List.of("ember", "glow", "sporitic_spore")),
			Map.entry("glacial_flail", List.of("glow", "blood_cell", "tendril"))
	);

	private VesperVisualRules() { }

	public static List<String> families(String scene) {
		return FAMILIES.getOrDefault(scene, List.of());
	}

	public static String weaponScene(VesperWeaponAction action) {
		return switch (action) {
			case ICHIMONJI, CROSSCUT -> "blood_blade";
			case LEAPING_CLEAVE, REAPER_SWEEP -> "living_axe";
			case SKY_LANCE, LANCE_FLURRY -> "living_spear";
			case TWIN_REND, PREDATOR_POUNCE -> "gloam_claw";
			case BRANDING_THRUSTS, UPDRAFT_IMPALEMENT -> "crimson_torch";
			case CHAIN_SWEEP, HOOK_AND_CRUSH -> "glacial_flail";
			default -> "none";
		};
	}

	public static int tendencyColorRgb(EnumBloodTendency tendency) {
		return switch (tendency) {
			case ANIMUS -> 0xE00018;
			case FLAMMEUS -> 0xFF6508;
			case DUCTILIS -> 0xF2E85C;
			case LUX -> 0xF4F7FF;
			case MORTEM -> 0x15522A;
			case CONGEATIO -> 0x52BFE8;
			case FERRIC -> 0x68646A;
			case TENEBRIS -> 0x63108A;
		};
	}
}
