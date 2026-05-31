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
		String traceCache = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillTraceLayerCache.java");
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
		assertContains("skills tab owns a cached trace layer", skillsTab, "SkillTraceLayerCache traceCache");
		assertContains("skills tab rebuilds cached traces on state changes", skillsTab, "traceCache.rebuildIfNeeded");
		assertContains("skills tab renders cached trace texture", skillsTab, "traceCache.render(gfx, ctx, panZoom)");
		assertContains("skills tab renders heartbeat over cached trace texture", skillsTab, "traceCache.renderHeartbeat");
		assertContains("skills tab renders lightweight animated trace flow", skillsTab, "traceCache.renderFlow");
		assertContains("trace cache colors traces by child branch", traceCache, "branchTraceColor(sp)");
		assertContains("trace cache dims locked branch traces", traceCache, "dimTraceColor(branchTraceColor(sp))");
		assertContains("trace cache bakes to a dynamic texture", traceCache, "DynamicTexture");
		assertContains("trace cache bakes pixels through NativeImage", traceCache, "NativeImage");
		assertContains("trace cache draws compass degree rings while baking", traceCache, "bakeDegreeRing");
		assertContains("trace cache renders one cached texture layer", traceCache, "gfx.blit(textureLocation");
		assertContains("trace cache defines heartbeat cadence", traceCache, "HEARTBEAT_PERIOD_SECONDS");
		assertContains("trace cache computes a double heartbeat pulse", traceCache, "heartbeatPulse");
		assertContains("trace cache expands the cached layer around the base skill", traceCache, "renderPulsedTexture");
		assertContains("trace cache emphasizes the base skill as heartbeat source", traceCache, "renderCenterHeartbeat");
		assertContains("trace cache uses a center heartbeat source color", traceCache, "CENTER_HEARTBEAT_SOURCE_COLOR");
		assertContains("trace cache draws rounded center pulse rings", traceCache, "drawCenterPulseRing");
		assertContains("trace cache fills rounded center pulse glow", traceCache, "fillCenterCircle");
		assertContains("trace cache uses branch-colored heartbeat texture", traceCache, "heartbeatTextureLocation");
		assertContains("trace cache boosts heartbeat trace alpha", traceCache, "HEARTBEAT_TRACE_ALPHA_BOOST");
		assertContains("trace cache renders branch-colored heartbeat texture", traceCache, "renderBranchHeartbeatTexture");
		assertContains("trace cache keeps heartbeat shader neutral so summons blue can pulse", traceCache, "RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f");
		assertNotContains("trace cache no longer uses square center pulse boxes", traceCache, "fillCenterBox");
		assertNotContains("trace cache no longer red-tints branch heartbeat texture", traceCache, "RenderSystem.setShaderColor(1.0f, 0.15f, 0.12f");
		assertNotContains("skills tab avoids expensive layered traces", skillsTab, "ScreenDrawUtils.drawTendrilTrace");
		assertNotContains("skills tab avoids per-frame cubic rasterizing", skillsTab, "ScreenDrawUtils.drawFineCubicTrace");
		assertNotContains("skills tab avoids per-frame degree-ring rasterizing", skillsTab, "ScreenDrawUtils.drawCircleTrace");
		assertContains("core branch keeps base skill", core, "SkillPointInit.base_skill");
		assertContains("scar branch keeps scar mastery", scars, "SkillPointInit.skill_scar_mastery");
		assertContains("summon branch keeps puppet skills", summons, "SkillPointInit.skill_puppet_skein");
		assertContains("living staff branch keeps staff skills", staff, "SkillPointInit.skill_crimson_projection");
		assertContains("core branch marks skills for in-game traces", core, ".setBranch(\"core\")");
		assertContains("scar branch marks skills for in-game traces", scars, ".setBranch(\"scars\")");
		assertContains("summon branch marks skills for in-game traces", summons, ".setBranch(\"summons\")");
		assertContains("living staff branch marks skills for in-game traces", staff, ".setBranch(\"living_staff\")");
		assertContains("core branch keeps center root", core, "setTreePosition(480, 480)");
		assertContains("core branch reaches north on degree five", core, "setTreePosition(521, 80)");
		assertContains("summon branch grows east", summons, "setTreePosition(670, 480)");
		assertContains("living staff branch grows west", staff, "setTreePosition(360, 480)");
		assertContains("scar branch grows south", scars, "setTreePosition(480, 880)");
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
