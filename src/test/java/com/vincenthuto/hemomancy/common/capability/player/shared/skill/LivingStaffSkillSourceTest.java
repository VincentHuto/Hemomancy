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
		String skillHelper = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPointHelper.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillsTabController.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String docs = read("docs/HEMOMANCY_REFERENCE.md");

		assertContains("living conduit skill field", skillInit, "skill_living_conduit");
		assertContains("vascular draw skill field", skillInit, "skill_vascular_draw");
		assertContains("crimson projection skill field", skillInit, "skill_crimson_projection");
		assertContains("living conduit id", skillInit, "new SkillPoint(21, \"skill_living_conduit\"");
		assertContains("vascular draw id", skillInit, "new SkillPoint(22, \"skill_vascular_draw\"");
		assertContains("crimson projection id", skillInit, "new SkillPoint(23, \"skill_crimson_projection\"");
		assertContains("living conduit degree gate", skillInit, "setRequiredDegree(1)");
		assertContains("vascular draw degree gate", skillInit, "setRequiredDegree(2)");
		assertContains("crimson projection degree gate", skillInit, "setRequiredDegree(3)");

		assertContains("living conduit helper", skillHelper, "getLivingConduitLevel(Player player)");
		assertContains("vascular draw helper", skillHelper, "getVascularDrawLevel(Player player)");
		assertContains("crimson projection helper", skillHelper, "getCrimsonProjectionLevel(Player player)");

		assertContains("living conduit screen initial", screen, "skill_living_conduit");
		assertContains("vascular draw screen initial", screen, "skill_vascular_draw");
		assertContains("crimson projection screen initial", screen, "skill_crimson_projection");

		assertContains("living conduit lang", lang, "skill.hemomancy.skill_living_conduit.desc");
		assertContains("vascular draw lang", lang, "skill.hemomancy.skill_vascular_draw.desc");
		assertContains("crimson projection lang", lang, "skill.hemomancy.skill_crimson_projection.desc");

		assertContains("living conduit docs", docs, "Living Conduit");
		assertContains("vascular draw docs", docs, "Vascular Draw");
		assertContains("crimson projection docs", docs, "Crimson Projection");
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
