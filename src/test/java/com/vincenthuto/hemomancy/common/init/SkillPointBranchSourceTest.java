package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SkillPointBranchSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private SkillPointBranchSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String init = read("src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java");
		String skillPoint = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPoint.java");
		String skillsTab = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillsTabController.java");
		String core = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CoreSkillBranch.java");
		String scars = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/ScarSkillBranch.java");
		String summons = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/SummonSkillBranch.java");
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/LivingStaffSkillBranch.java");

		assertContains("SkillPointInit delegates core branch", init, "CoreSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates scar branch", init, "ScarSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates summon branch", init, "SummonSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates living staff branch", init, "LivingStaffSkillBranch.register(BASE)");
		assertNotContains("SkillPointInit no longer owns raw skill declarations", init, "new SkillPoint(");

		assertContains("core branch has editor marker", core, "<skill-editor branch=\"core\">");
		assertContains("scar branch has editor marker", scars, "<skill-editor branch=\"scars\">");
		assertContains("summon branch has editor marker", summons, "<skill-editor branch=\"summons\">");
		assertContains("living staff branch has editor marker", staff, "<skill-editor branch=\"living_staff\">");
		assertContains("skill point exposes tree position builder", skillPoint, "setTreePosition(int x, int y)");
		assertContains("skill point exposes tree position presence", skillPoint, "hasTreePosition()");
		assertContains("skill point exposes tree x getter", skillPoint, "getTreeX()");
		assertContains("skill point exposes tree y getter", skillPoint, "getTreeY()");
		assertContains("skill point defaults branch metadata", skillPoint, "String branch = \"core\"");
		assertContains("skill point exposes branch builder", skillPoint, "setBranch(String branch)");
		assertContains("skill point exposes branch getter", skillPoint, "getBranch()");
		assertContains("skills tab prefers explicit skill positions", skillsTab, "sp.hasTreePosition()");
		assertContains("skills tab defines core trace color", skillsTab, "TRACE_CORE");
		assertContains("skills tab defines scar trace color", skillsTab, "TRACE_SCARS");
		assertContains("skills tab defines summon trace color", skillsTab, "TRACE_SUMMONS");
		assertContains("skills tab defines living staff trace color", skillsTab, "TRACE_LIVING_STAFF");
		assertContains("skills tab colors traces by child branch", skillsTab, "branchTraceColor(sp)");
		assertContains("skills tab dims locked branch traces", skillsTab, "dimTraceColor(branchTraceColor(sp))");
		assertContains("core branch keeps base skill", core, "SkillPointInit.base_skill");
		assertContains("scar branch keeps scar mastery", scars, "SkillPointInit.skill_scar_mastery");
		assertContains("summon branch keeps puppet skills", summons, "SkillPointInit.skill_puppet_skein");
		assertContains("living staff branch keeps staff skills", staff, "SkillPointInit.skill_crimson_projection");
		assertContains("core branch marks skills for in-game traces", core, ".setBranch(\"core\")");
		assertContains("scar branch marks skills for in-game traces", scars, ".setBranch(\"scars\")");
		assertContains("summon branch marks skills for in-game traces", summons, ".setBranch(\"summons\")");
		assertContains("living staff branch marks skills for in-game traces", staff, ".setBranch(\"living_staff\")");
		assertContains("core branch keeps a centered base trunk", core, "setTreePosition(360, 424)");
		assertContains("summon branch stays in the left lane", summons, "setTreePosition(80, 280)");
		assertContains("living staff branch has its own right lane", staff, "setTreePosition(630, 352)");
		assertContains("scar branch has a far-right lane", scars, "setTreePosition(700, 136)");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
