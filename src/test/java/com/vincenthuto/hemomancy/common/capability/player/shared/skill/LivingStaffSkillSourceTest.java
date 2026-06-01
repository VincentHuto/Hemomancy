package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingStaffSkillSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingStaffSkillSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String skillInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java");
		String staffBranch = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/LivingStaffSkillBranch.java");
		String skillHelper = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPointHelper.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillsTabController.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String docs = read("docs/HEMOMANCY_REFERENCE.md");

		assertContains("living conduit skill field", skillInit, "skill_living_conduit");
		assertContains("vascular draw skill field", skillInit, "skill_vascular_draw");
		assertContains("crimson projection skill field", skillInit, "skill_crimson_projection");
		assertContains("weapons master skill field", skillInit, "skill_weapons_master");
		assertContains("living conduit id", staffBranch, "new SkillPoint(21, \"skill_living_conduit\"");
		assertContains("vascular draw id", staffBranch, "new SkillPoint(22, \"skill_vascular_draw\"");
		assertContains("crimson projection id", staffBranch, "new SkillPoint(23, \"skill_crimson_projection\"");
		assertContains("weapons master id", staffBranch, "new SkillPoint(39, \"skill_weapons_master\"");
		assertContains("living conduit degree gate", staffBranch, "setRequiredDegree(1)");
		assertContains("vascular draw degree gate", staffBranch, "setRequiredDegree(2)");
		assertContains("crimson projection degree gate", staffBranch, "setRequiredDegree(3)");
		assertContains("weapons master degree gate", staffBranch, "setRequiredDegree(4)");

		assertContains("living conduit helper", skillHelper, "getLivingConduitLevel(Player player)");
		assertContains("vascular draw helper", skillHelper, "getVascularDrawLevel(Player player)");
		assertContains("crimson projection helper", skillHelper, "getCrimsonProjectionLevel(Player player)");
		assertContains("weapons master helper", skillHelper, "getWeaponsMasterLevel(Player player)");
		assertContains("hot swap cost helper", skillHelper, "getLivingStaffHotSwapCost(Player player)");

		assertContains("living conduit screen initial", screen, "skill_living_conduit");
		assertContains("vascular draw screen initial", screen, "skill_vascular_draw");
		assertContains("crimson projection screen initial", screen, "skill_crimson_projection");
		assertContains("weapons master screen initial", screen, "skill_weapons_master");

		assertContains("living conduit lang", lang, "skill.hemomancy.skill_living_conduit.desc");
		assertContains("vascular draw lang", lang, "skill.hemomancy.skill_vascular_draw.desc");
		assertContains("crimson projection lang", lang, "skill.hemomancy.skill_crimson_projection.desc");
		assertContains("weapons master lang", lang, "skill.hemomancy.skill_weapons_master.desc");

		assertContains("living conduit docs", docs, "Living Conduit");
		assertContains("vascular draw docs", docs, "Vascular Draw");
		assertContains("crimson projection docs", docs, "Crimson Projection");
		assertContains("weapons master docs", docs, "Weapons Master");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
