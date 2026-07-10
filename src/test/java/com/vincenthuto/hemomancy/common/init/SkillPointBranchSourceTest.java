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
		String layerRules = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillTreeLayerRules.java");
		String harbingerScreen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/HarbingerProgressScreen.java");
		String veinBackground = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/VeinBackgroundRenderer.java");
		String scarCraftPacket = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/scars/PacketScarCraftingEvent.java");
		String skillPointHelper = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/skill/SkillPointHelper.java");
		String core = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CoreSkillBranch.java");
		String scars = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/ScarSkillBranch.java");
		String summons = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/SummonSkillBranch.java");
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/LivingStaffSkillBranch.java");
		String covenant = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CovenantSkillBranch.java");
		String mycelial = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/MycelialSkillBranch.java");

		assertContains("SkillPointInit delegates core branch", init, "CoreSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates scar branch", init, "ScarSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates summon branch", init, "SummonSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates living staff branch", init, "LivingStaffSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates covenant branch", init, "CovenantSkillBranch.register(BASE)");
		assertContains("SkillPointInit delegates mycelial branch", init, "MycelialSkillBranch.register(BASE)");
		assertBefore("SkillPointInit registers mycelial before scar cross-branch parents resolve", init,
				"MycelialSkillBranch.register(BASE);", "ScarSkillBranch.register(BASE);");
		assertNotContains("SkillPointInit no longer owns raw skill declarations", init, "new SkillPoint(");
		assertContains("SkillPointInit stores editable degree labels", init, "setDegreeLabelPosition(int degree, int x, int y)");
		assertContains("SkillPointInit exposes editable degree labels", init, "getDegreeLabelPosition(int degree)");
		assertContains("SkillPointInit stores labels through degree eight", init, "{90, 380}");
		assertContains("SkillPointInit exposes deep base field", init, "deep_base_skill");
		assertContains("SkillPointInit exposes covenant skill field", init, "skill_bloodline_concord");
		assertContains("SkillPointInit exposes mycelial skill field", init, "skill_primal_morphogenesis");

		assertContains("core branch has editor marker", core, "<skill-editor branch=\"core\">");
		assertContains("scar branch has editor marker", scars, "<skill-editor branch=\"scars\">");
		assertContains("summon branch has editor marker", summons, "<skill-editor branch=\"summons\">");
		assertContains("living staff branch has editor marker", staff, "<skill-editor branch=\"living_staff\">");
		assertContains("covenant branch has editor marker", covenant, "<skill-editor branch=\"covenant\">");
		assertContains("mycelial branch has editor marker", mycelial, "<skill-editor branch=\"mycelial\">");
		assertContains("skill point exposes tree position builder", skillPoint, "setTreePosition(int x, int y)");
		assertContains("skill point exposes tree position presence", skillPoint, "hasTreePosition()");
		assertContains("skill point exposes tree x getter", skillPoint, "getTreeX()");
		assertContains("skill point exposes tree y getter", skillPoint, "getTreeY()");
		assertContains("skill point defaults branch metadata", skillPoint, "String branch = \"core\"");
		assertContains("skill point exposes branch builder", skillPoint, "setBranch(String branch)");
		assertContains("skill point exposes branch getter", skillPoint, "getBranch()");
		assertContains("skill point exposes branch color builder", skillPoint, "setBranchColor(int color)");
		assertContains("skill point exposes branch color getter", skillPoint, "getBranchColor()");
		assertContains("skill point falls back to branch color if no explicit color exists", skillPoint, "hasExplicitBranchColor ? branchColor : defaultBranchColor(branch)");
		assertContains("skill point exposes multiple parents", skillPoint, "getParents()");
		assertContains("skill point exposes additional parent builder", skillPoint, "addParents(SkillPoint... additionalParents)");
		assertContains("skills tab prefers explicit skill positions", skillsTab, "sp.hasTreePosition()");
		assertContains("skills tab colors nodes from saved branch color", skillsTab, "sp.getBranchColor()");
		assertContains("skills tab keeps locked branch-colored nodes readable during fades", skillsTab, "Math.round(0xAA * alpha)");
		assertContains("skills tab owns a surface cached trace layer", skillsTab, "SkillTraceLayerCache surfaceTraceCache");
		assertContains("skills tab owns a deep cached trace layer", skillsTab, "SkillTraceLayerCache deepTraceCache");
		assertContains("skills tab owns independent surface navigation", skillsTab, "PanZoomState surfacePanZoom");
		assertContains("skills tab owns independent deep navigation", skillsTab, "PanZoomState deepPanZoom");
		assertContains("skills tab owns latched dive state", skillsTab, "SkillTreeDiveState diveState");
		assertContains("skills tab rebuilds surface cached traces on state changes", skillsTab, "surfaceTraceCache.rebuildIfNeeded");
		assertContains("skills tab rebuilds deep cached traces on state changes", skillsTab, "deepTraceCache.rebuildIfNeeded");
		assertContains("skills tab renders cached surface trace texture", skillsTab, "surfaceTraceCache.render(gfx, ctx, surfacePanZoom");
		assertContains("skills tab renders cached deep trace texture", skillsTab, "deepTraceCache.render(gfx, ctx, deepView");
		assertContains("skills tab renders heartbeat over cached surface trace texture", skillsTab, "surfaceTraceCache.renderHeartbeat(gfx, ctx, surfacePanZoom");
		assertContains("skills tab renders heartbeat over cached deep trace texture", skillsTab, "deepTraceCache.renderHeartbeat(gfx, ctx, deepView");
		assertContains("skills tab renders lightweight animated trace flow", skillsTab, "surfaceTraceCache.renderFlow(gfx, ctx, surfacePanZoom");
		assertContains("skills tab filters skills through layer rules", skillsTab, "SkillTreeLayerRules.layerForDegree");
		assertContains("skills tab tracks deep fade", skillsTab, "deepFade");
		assertContains("skills tab latches deep view after surface dive", skillsTab, "diveState.updateSurfaceDive");
		assertContains("skills tab exits deep view by zooming out", skillsTab, "diveState.updateDeepZoom");
		assertContains("skills tab exposes the active layer navigation state", skillsTab, "return activePanZoom();");
		assertContains("skills tab gates deep interaction until fade midpoint", skillsTab, "deepFade >= 0.5f");
		assertContains("skills tab exposes current deep fade for the background", skillsTab, "updateAndGetDeepFade(ProgressScreenContext ctx)");
		assertContains("layer rules split surface and deep degrees", layerRules, "SURFACE_MAX_DEGREE = 4");
		assertContains("layer rules gates the deep tree at degree five", layerRules, "DIVE_UNLOCK_DEGREE = 5");
		assertContains("layer rules requires zoom and center focus", layerRules, "diveProgress(int playerDegree, float zoom, float centerFocus)");
		assertContains("harbinger screen passes fresh deep fade to the vein background", harbingerScreen, "skills.updateAndGetDeepFade(ctx)");
		assertContains("vein background accepts deep tint fade", veinBackground, "render(GuiGraphics gfx, int gx, int gy, int gw, int gh, float deepFade)");
		assertContains("vein background lerps to bruised purple deep colors", veinBackground, "lerpArgb(0xFF0A0204, 0xFF0E020D, fade)");
		assertContains("vein background keeps deep tendrils purple-blood instead of hot red", veinBackground, "34 + 24 * brightness");
		assertContains("vein background gives deep specks a muted purple channel", veinBackground, "int sb = (int)(fade * (6 + speckRand.nextInt(7)))");
		assertContains("trace cache colors traces from saved branch color", traceCache, "sp.getBranchColor()");
		assertContains("trace cache can anchor missing deep parents to the heart", traceCache, "anchorMissingParents");
		assertContains("trace cache supports alpha-rendered layer fades", traceCache, "float alpha");
		assertContains("trace cache keeps degree rings visually behind colored traces", traceCache, "TRACE_DEGREE_RING = 0x1858231F");
		assertNotContains("trace cache no longer uses hardcoded core trace color", traceCache, "TRACE_CORE");
		assertNotContains("trace cache no longer uses hardcoded scar trace color", traceCache, "TRACE_SCARS");
		assertNotContains("trace cache no longer uses hardcoded summon trace color", traceCache, "TRACE_SUMMONS");
		assertNotContains("trace cache no longer uses hardcoded living staff trace color", traceCache, "TRACE_LIVING_STAFF");
		assertContains("trace cache draws traces for every parent", traceCache, "for (SkillPoint parent : sp.getParents())");
		assertContains("trace cache stops traces at node rims", traceCache, "trimTraceEndpoint");
		assertContains("trace cache gives straight traces a subtle organic sway", traceCache, "organicSway");
		assertContains("trace cache caps organic trace sway for readability", traceCache, "TRACE_ORGANIC_SWAY_MAX");
		assertContains("trace cache colors each trace from its parent skill", traceCache, "branchTraceColor(parent)");
		assertContains("trace cache dims locked branch traces", traceCache, "dimTraceColor(branchTraceColor(parent))");
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
		assertContains("trace cache anchors transition pulse through tree pan zoom", traceCache,
				"renderScreenTransitionPulse(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState panZoom");
		assertNotContains("trace cache does not anchor transition pulse to screen center", traceCache,
				"ctx.guiLeft() + ctx.guiWidth() / 2");
		assertContains("skills tab renders transition pulse from the active tree center", skillsTab,
				"renderScreenTransitionPulse(gfx, ctx, transitionPulseView(), transitionPulse");
		assertNotContains("transition pulse avoids a detached glowing core", traceCache,
				"fillCenterCircle(gfx, centerX, centerY, coreRadius");
		assertContains("skills tab stops deep portal fill after deep mode latches", skillsTab,
				"if (!diveState.isDeepActive() && deepFade > 0.01f) deepTraceCache.renderPortalPulse");
		assertContains("skills tab stacks degree labels away from the center tree", skillsTab, "DEGREE_LABEL_TOP_Y");
		assertContains("skills tab keeps degree labels in a left-side column", skillsTab, "DEGREE_LABEL_X");
		assertContains("skills tab reads saved degree label positions", skillsTab, "SkillPointInit.getDegreeLabelPosition(degree)");
		assertContains("skills tab keeps degree-locked branch outlines colored during fades", skillsTab, "Math.round(0xCC * alpha)");
		assertNotContains("trace cache no longer uses square center pulse boxes", traceCache, "fillCenterBox");
		assertNotContains("trace cache no longer red-tints branch heartbeat texture", traceCache, "RenderSystem.setShaderColor(1.0f, 0.15f, 0.12f");
		assertNotContains("skills tab avoids expensive layered traces", skillsTab, "ScreenDrawUtils.drawTendrilTrace");
		assertNotContains("skills tab avoids per-frame cubic rasterizing", skillsTab, "ScreenDrawUtils.drawFineCubicTrace");
		assertNotContains("skills tab avoids per-frame degree-ring rasterizing", skillsTab, "ScreenDrawUtils.drawCircleTrace");
		assertContains("core branch keeps base skill", core, "SkillPointInit.base_skill");
		assertContains("core branch adds degree five deep base skill", core, "new SkillPoint(38, \"deep_base\", 0, 1, EnumSkillStates.UNLOCKED, null)");
		assertContains("core branch degree-gates deep base", core, "setRequiredDegree(5).setTreePosition(480, 480)");
		assertContains("core deep skills depend on deep base", core, "addParents(SkillPointInit.deep_base_skill)");
		assertContains("scar branch keeps scar mastery", scars, "SkillPointInit.skill_scar_mastery");
		assertContains("scar deep skills depend on deep base", scars, "addParents(SkillPointInit.deep_base_skill)");
		assertContains("summon branch keeps puppet skills", summons, "SkillPointInit.skill_puppet_skein");
		assertContains("living staff branch keeps staff skills", staff, "SkillPointInit.skill_crimson_projection");
		assertContains("living staff deep skills depend on deep base", staff, "addParents(SkillPointInit.skill_vascular_draw, SkillPointInit.deep_base_skill)");
		assertContains("living staff branch adds hematic focus", staff, "SkillPointInit.skill_hematic_focus");
		assertContains("living staff branch adds vespers refusal", staff, "SkillPointInit.skill_vespers_refusal");
		assertContains("summon branch adds thread economy", summons, "SkillPointInit.skill_thread_economy");
		assertContains("summon branch adds bound command", summons, "SkillPointInit.skill_bound_command");
		assertContains("scar branch adds deep inscription", scars, "SkillPointInit.skill_deep_inscription");
		assertContains("scar branch adds fungal symbiosis", scars, "SkillPointInit.skill_fungal_symbiosis");
		assertContains("fungal symbiosis keeps hyphal cultivation as a live parent", scars,
				"addParents(SkillPointInit.skill_hyphal_cultivation)");
		assertContains("covenant branch adds fane suture", covenant, "SkillPointInit.skill_fane_suture");
		assertContains("covenant deep skills depend on deep base", covenant, "addParents(SkillPointInit.deep_base_skill)");
		assertContains("mycelial branch adds sporitic attunement", mycelial, "SkillPointInit.skill_sporitic_attunement");
		assertContains("mycelial deep skills depend on deep base", mycelial, "addParents(SkillPointInit.deep_base_skill)");
		assertContains("core branch marks skills for in-game traces", core, ".setBranch(\"core\")");
		assertContains("scar branch marks skills for in-game traces", scars, ".setBranch(\"scars\")");
		assertContains("summon branch marks skills for in-game traces", summons, ".setBranch(\"summons\")");
		assertContains("living staff branch marks skills for in-game traces", staff, ".setBranch(\"living_staff\")");
		assertContains("covenant branch marks skills for in-game traces", covenant, ".setBranch(\"covenant\")");
		assertContains("mycelial branch marks skills for in-game traces", mycelial, ".setBranch(\"mycelial\")");
		assertContains("core branch saves branch trace color", core, ".setBranchColor(0xFFD00000)");
		assertContains("core branch saves degree label positions", core, "SkillPointInit.setDegreeLabelPosition(0,");
		assertContains("scar branch saves branch trace color", scars, ".setBranchColor(0xFF9A9A9F)");
		assertContains("summon branch saves branch trace color", summons, ".setBranchColor(0xFF2370DB)");
		assertContains("living staff branch saves branch trace color", staff, ".setBranchColor(0xFFD9AD28)");
		assertContains("covenant branch saves branch trace color", covenant, ".setBranchColor(0xFFA54569)");
		assertContains("mycelial branch saves branch trace color", mycelial, ".setBranchColor(0xFF6E8F3A)");
		assertContains("core branch keeps center root", core, "setTreePosition(480, 480)");
		assertContains("core branch reaches north on deep layer", core, "setTreePosition(455, 310)");
		assertContains("summon branch grows east", summons, "setTreePosition(650, 480)");
		assertContains("living staff branch grows west", staff, "setTreePosition(360, 480)");
		assertContains("scar branch grows south on deep layer", scars, "setTreePosition(585, 750)");
		assertContains("covenant branch grows northeast on deep layer", covenant, "setTreePosition(581, 411)");
		assertContains("mycelial branch grows southwest", mycelial, "setTreePosition(289, 671)");
		assertContains("skill helper exposes hematic focus", skillPointHelper, "getHematicFocusLevel");
		assertContains("skill helper exposes vesper refusal", skillPointHelper, "getVespersRefusalLevel");
		assertContains("skill helper exposes thread economy", skillPointHelper, "getThreadEconomyLevel");
		assertContains("skill helper exposes bound command", skillPointHelper, "getBoundCommandLevel");
		assertContains("skill helper exposes deep inscription", skillPointHelper, "hasDeepInscription");
		assertContains("skill helper exposes covenant routing", skillPointHelper, "getFaneSutureLevel");
		assertContains("skill helper exposes mycelial capstone", skillPointHelper, "getPrimalMorphogenesisLevel");
		assertContains("scar crafting checks deep inscription for tier three scars", scarCraftPacket,
				"SkillPointHelper.isUnlocked(player, SkillPointInit.skill_deep_inscription)");
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

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0) {
			throw new AssertionError(label + " (missing first marker '" + first + "')");
		}
		if (secondIndex < 0) {
			throw new AssertionError(label + " (missing second marker '" + second + "')");
		}
		if (firstIndex > secondIndex) {
			throw new AssertionError(label + " expected '" + first + "' before '" + second + "'");
		}
	}
}
