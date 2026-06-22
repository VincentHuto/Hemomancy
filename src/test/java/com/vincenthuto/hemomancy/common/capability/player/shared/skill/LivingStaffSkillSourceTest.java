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
		assertContains("dragging siphon skill field", skillInit, "skill_dragging_siphon");
		assertContains("mobile conduit skill field", skillInit, "skill_mobile_conduit");
		assertContains("blood tolerance skill field", skillInit, "skill_blood_tolerance");
		assertContains("unbound siphon skill field", skillInit, "skill_unbound_siphon");
		assertContains("quickened draw skill field", skillInit, "skill_quickened_draw");
		assertContains("hungry pulse skill field", skillInit, "skill_hungry_pulse");
		assertContains("arterial cadence skill field", skillInit, "skill_arterial_cadence");
		assertContains("living conduit id", staffBranch, "new SkillPoint(21, \"skill_living_conduit\"");
		assertContains("vascular draw id", staffBranch, "new SkillPoint(22, \"skill_vascular_draw\"");
		assertContains("crimson projection id", staffBranch, "new SkillPoint(23, \"skill_crimson_projection\"");
		assertContains("weapons master id", staffBranch, "new SkillPoint(39, \"skill_weapons_master\"");
		assertContains("dragging siphon id", staffBranch, "new SkillPoint(41, \"skill_dragging_siphon\"");
		assertContains("mobile conduit id", staffBranch, "new SkillPoint(42, \"skill_mobile_conduit\"");
		assertContains("blood tolerance id", staffBranch, "new SkillPoint(43, \"skill_blood_tolerance\"");
		assertContains("unbound siphon id", staffBranch, "new SkillPoint(44, \"skill_unbound_siphon\"");
		assertContains("quickened draw id", staffBranch, "new SkillPoint(45, \"skill_quickened_draw\"");
		assertContains("hungry pulse id", staffBranch, "new SkillPoint(46, \"skill_hungry_pulse\"");
		assertContains("arterial cadence id", staffBranch, "new SkillPoint(47, \"skill_arterial_cadence\"");
		assertContains("living conduit degree gate", staffBranch, "setRequiredDegree(1)");
		assertContains("vascular draw degree gate", staffBranch, "setRequiredDegree(2)");
		assertContains("crimson projection degree gate", staffBranch, "setRequiredDegree(3)");
		assertContains("weapons master degree gate", staffBranch, "setRequiredDegree(4)");
		assertContains("dragging siphon degree gate", staffBranch, "skill_dragging_siphon\", 300, 1");
		assertContains("mobile conduit has three tiers", staffBranch, "skill_mobile_conduit\", 375, 3");
		assertContains("blood tolerance is repeatable", staffBranch, "skill_blood_tolerance\", 425, 5");
		assertContains("unbound siphon degree gate", staffBranch, "skill_unbound_siphon\", 600, 1");

		assertContains("living conduit helper", skillHelper, "getLivingConduitLevel(Player player)");
		assertContains("vascular draw helper", skillHelper, "getVascularDrawLevel(Player player)");
		assertContains("crimson projection helper", skillHelper, "getCrimsonProjectionLevel(Player player)");
		assertContains("weapons master helper", skillHelper, "getWeaponsMasterLevel(Player player)");
		assertContains("hot swap cost helper", skillHelper, "getLivingStaffHotSwapCost(Player player)");
		assertContains("client-side skill helper reads synced client cache",
				skillHelper, "player.level().isClientSide");
		assertBefore("client-side skill helper checks cache before attachment",
				skillHelper, "player.level().isClientSide", "HemoCapabilityAccess.getSkillProgress(player)");
		assertContains("dragging siphon helper", skillHelper, "getDraggingSiphonLevel(Player player)");
		assertContains("mobile conduit helper", skillHelper, "getMobileConduitLevel(Player player)");
		assertContains("blood tolerance helper", skillHelper, "getBloodToleranceLevel(Player player)");
		assertContains("unbound siphon helper", skillHelper, "hasUnboundSiphon(Player player)");
		assertContains("absorption cadence helper", skillHelper, "getAbsorptionCadenceLevel(Player player)");

		assertContains("living conduit screen initial", screen, "skill_living_conduit");
		assertContains("vascular draw screen initial", screen, "skill_vascular_draw");
		assertContains("crimson projection screen initial", screen, "skill_crimson_projection");
		assertContains("weapons master screen initial", screen, "skill_weapons_master");
		assertContains("dragging siphon screen initial", screen, "skill_dragging_siphon");
		assertContains("mobile conduit screen initial", screen, "skill_mobile_conduit");
		assertContains("blood tolerance screen initial", screen, "skill_blood_tolerance");
		assertContains("unbound siphon screen initial", screen, "skill_unbound_siphon");
		assertContains("quickened draw screen initial", screen, "skill_quickened_draw");
		assertContains("hungry pulse screen initial", screen, "skill_hungry_pulse");
		assertContains("arterial cadence screen initial", screen, "skill_arterial_cadence");

		assertContains("living conduit lang", lang, "skill.hemomancy.skill_living_conduit.desc");
		assertContains("vascular draw lang", lang, "skill.hemomancy.skill_vascular_draw.desc");
		assertContains("crimson projection lang", lang, "skill.hemomancy.skill_crimson_projection.desc");
		assertContains("weapons master lang", lang, "skill.hemomancy.skill_weapons_master.desc");
		assertContains("dragging siphon lang", lang, "skill.hemomancy.skill_dragging_siphon.desc");
		assertContains("mobile conduit lang", lang, "skill.hemomancy.skill_mobile_conduit.desc");
		assertContains("blood tolerance lang", lang, "skill.hemomancy.skill_blood_tolerance.desc");
		assertContains("unbound siphon lang", lang, "skill.hemomancy.skill_unbound_siphon.desc");
		assertContains("quickened draw lang", lang, "skill.hemomancy.skill_quickened_draw.desc");
		assertContains("hungry pulse lang", lang, "skill.hemomancy.skill_hungry_pulse.desc");
		assertContains("arterial cadence lang", lang, "skill.hemomancy.skill_arterial_cadence.desc");

		assertContains("living conduit docs", docs, "Living Conduit");
		assertContains("vascular draw docs", docs, "Vascular Draw");
		assertContains("crimson projection docs", docs, "Crimson Projection");
		assertContains("weapons master docs", docs, "Weapons Master");
		assertContains("dragging siphon docs", docs, "Dragging Siphon");
		assertContains("mobile conduit docs", docs, "Mobile Conduit");
		assertContains("blood tolerance docs", docs, "Blood Tolerance");
		assertContains("unbound siphon docs", docs, "Unbound Siphon");
		assertContains("quickened draw docs", docs, "Quickened Draw");
		assertContains("hungry pulse docs", docs, "Hungry Pulse");
		assertContains("arterial cadence docs", docs, "Arterial Cadence");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}
}
