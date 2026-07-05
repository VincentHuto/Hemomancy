package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillCombatRedesignSourceTest {
	private static final String[] REQUIRED_KIT_IDS = {
			"blood_shot", "blood_needle", "blood_aneurysm", "blood_cloud",
			"crimson_flame_conjuration", "sanguine_ignition", "scalding_updraft", "vitric_combustion",
			"deadly_gaze", "hemolymphal_pulse", "activation_potential",
			"hematic_flare", "crimson_sight", "prismatic_reproof", "unclosing_eye",
			"hemorrhage", "exsanguinate", "bloom_of_rot",
			"cryogenic_pulse", "glacial_grasp", "glacial_bastion", "glacial_rampart",
			"pyretic_forge", "void_shroud", "gloam_laceration", "umbral_step", "blood_eclipse"
	};

	private WillCombatRedesignSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String will = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java");
		String caster = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/WillManipulationCaster.java");
		String support = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityManipulationEffects.java");
		String context = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationCastContext.java");
		String entityCastable = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityCastableManipulation.java");
		assertContains("entity cast marker exists", entityCastable, "boolean castFromEntity(ManipulationCastContext context)");
		assertContains("will context factory exists", context, "forWill");
		assertContains("player context factory exists", context, "forPlayer");
		assertContains("will caster requires entity-castable manipulations", caster, "instanceof EntityCastableManipulation");
		assertContains("will caster builds will context", caster, "ManipulationCastContext.forWill");
		assertContains("broken wills use cast controller", will, "selectBrokenCast");
		assertContains("sent wills use cast controller", will, "selectSentCast");
		assertNotContains("will does not cast through drudge bridge", will, "MobManipCaster");
		for (String id : REQUIRED_KIT_IDS) {
			assertContains("entity support covers " + id, support, "\"" + id + "\"");
		}
		assertContains("entity support dispatches Lux flare", support,
				"case \"hematic_flare\" -> hematicFlare(context);");
		assertContains("entity support dispatches Tenebris slash", support,
				"case \"gloam_laceration\" -> gloamLaceration(context);");
		assertContains("drudge caster still exists", read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/MobManipCaster.java"),
				"class MobManipCaster");
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path));
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}

	private static void assertNotContains(String label, String source, String unexpected) {
		if (source.contains(unexpected)) {
			throw new AssertionError(label + ": found `" + unexpected + "`");
		}
	}
}
