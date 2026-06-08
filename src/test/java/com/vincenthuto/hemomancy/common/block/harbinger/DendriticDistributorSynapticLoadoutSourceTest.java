package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DendriticDistributorSynapticLoadoutSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private DendriticDistributorSynapticLoadoutSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/DendriticDistributorBlock.java");
		String recipe = read("src/main/resources/data/hemomancy/recipe/blood_structure/dendritic_distributor.json");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SynapticLoadoutScreen.java");
		String clientSync = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/KnownManipulationClientPacket.java");
		String action = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/SynapticLoadoutActionPacket.java");
		String skillInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java");
		String coreBranch = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CoreSkillBranch.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

		assertContains("Distributor imports synaptic screen", block, "SynapticLoadoutScreen");
		assertContains("Distributor opens synaptic loadout screen", block, "SynapticLoadoutScreen.openScreen(pos)");
		assertDoesNotContain("Distributor no longer opens old progress screen", block, "HarbingerProgressScreen.openScreen()");
		assertContains("Distributor recipe is Degree 5", recipe, "\"required_degree\": 5");
		assertContains("skill init declares synaptic memory", skillInit, "skill_synaptic_memory");
		assertContains("core branch registers synaptic memory", coreBranch, "\"skill_synaptic_memory\"");
		assertContains("synaptic skill has lang description", lang, "\"skill.hemomancy.skill_synaptic_memory.desc\"");
		assertContains("Synaptic screen suppresses vanilla menu blur", screen, "public void renderBackground(GuiGraphics");
		assertContains("Synaptic screen documents blur suppression", screen, "Screen#renderBackground applies menu blur");
		assertContains("Synaptic controls use left-side rail", screen, "CONTROL_RAIL_W");
		assertContains("Synaptic buttons are narrower than the rail", screen, "CONTROL_BUTTON_W");
		assertContains("Synaptic side cards use compact spacing", screen, "SIDE_GAP");
		assertContains("Synaptic carousel center is bounded", screen, "boundedCarouselCenter");
		assertContains("Synaptic arrows sit in the gaps", screen, "arrowGapCenter");
		assertContains("Synaptic control rail has compact height", screen, "CONTROL_RAIL_H");
		assertContains("Synaptic rail is raised near top", screen, "controlY = frameY + 38");
		assertContains("Synaptic carousel avoids rail overlap when cramped", screen, "return min");
		assertDoesNotContain("Synaptic carousel keeps side cards visible", screen, "showSidePanels");
		assertContains("Synaptic carousel uses a rail gutter", screen, "CONTROL_CAROUSEL_GUTTER");
		assertContains("Synaptic side cards shrink responsively", screen, "sidePanelSize(frameW)");
		assertContains("Synaptic side gaps shrink responsively", screen, "sideGap(frameW)");
		assertContains("Synaptic left preview is bounded after controls", screen, "leftSidePanelX(centerX, sidePanel, gap)");
		assertContains("Synaptic frame is wide enough for side cards", screen, "760");
		assertContains("Synaptic carousel is raised inside the frame", screen, "carouselCenterY");
		assertContains("Synaptic control rail has visible bottom border", screen, "y + h - 2");
		assertContains("Synaptic footer wraps long cost text", screen, "font.split");
		assertContains("Synaptic side preview radius stays inside side cards", screen, "sidePreviewRadius");
		assertContains("Synaptic focused preview radius stays inside main card", screen, "focusedPreviewRadius");
		assertContains("Synaptic screen uses a neuronal yellow vein background", screen, "renderNeuralVeinBackground");
		assertContains("Synaptic screen seeds animated neural veins", screen, "NEURAL_VEIN_COUNT");
		assertContains("Synaptic screen has neuronal yellow border accents", screen, "NEURAL_BORDER_OUTER");
		assertContains("Synaptic name field is placed in rail", screen, "controlX + 8");
		assertContains("Synaptic action buttons stack vertically", screen, "controlY + 74");
		assertContains("Synaptic screen reads client skill sync cache for slot count", screen, "getClientUnlockedSlots");
		assertContains("Synaptic screen open refreshes skill sync", clientSync, "PacketSyncSkills");
		assertContains("Synaptic skill sync is sent with manipulation sync", clientSync, "requireSkillProgress(sender).toSyncTag()");
		assertContains("Synaptic save captures memorized wheel entries", action, "memorizedEquippedNames");
		assertDoesNotContain("Synaptic save is not selected-only or normal-only", action, "normalEquippedNames");
		assertContains("Synaptic apply counts only normal entries against capacity", action,
				"countNormalEquippedNames(loadout.manipNames())");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}
}
