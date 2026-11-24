package com.vincenthuto.hemomancy.common.block.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class DendriticDistributorSynapticLoadoutSourceTest {

	private DendriticDistributorSynapticLoadoutSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String block = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/DendriticDistributorBlock.java");
		String recipe = read("src/main/resources/data/hemomancy/recipe/cardinal_rite/working/dendritic_distributor.json");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SynapticLoadoutScreen.java");
		String clientSync = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/KnownManipulationClientPacket.java");
		String action = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/SynapticLoadoutActionPacket.java");
		String skillInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java");
		String coreBranch = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CoreSkillBranch.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

		assertContains("Distributor asks the client bridge for the synaptic screen", block, "ClientBridge.get().openSynapticLoadout(pos)");
		assertContains("Distributor opens synaptic loadout screen",
				read("src/main/java/com/vincenthuto/hemomancy/client/ClientBridgeImpl.java"), "SynapticLoadoutScreen.openScreen(pos)");
		assertNotContains("Distributor no longer opens old progress screen", block, "HarbingerProgressScreen.openScreen()");
		assertContains("Distributor recipe is Degree 5", recipe, "\"required_degree\": 5");
		assertContains("skill init declares synaptic memory", skillInit, "skill_synaptic_memory");
		assertContains("core branch registers synaptic memory", coreBranch, "\"skill_synaptic_memory\"");
		assertContains("synaptic skill has lang description", lang, "\"skill.hemomancy.skill_synaptic_memory.desc\"");
		assertContains("Synaptic screen suppresses vanilla menu blur", screen, "public void renderBackground(GuiGraphics");
		assertContains("Synaptic screen documents blur suppression", screen, "Screen#renderBackground applies menu blur");
		assertContains("Synaptic screen uses a painted Distributor background", screen, "textures/gui/synaptic_loadouts.png");
		assertContains("Synaptic screen draws equipped and saved previews independently", screen, "drawPreview(gfx, current");
		assertContains("Synaptic screen draws the selected pattern alongside equipped memories", screen, "drawPreview(gfx, pattern");
		assertContains("Each dendrite socket is directly selectable", screen, "new PatternButton(i)");
		assertContains("Locked slots cannot be selected", screen, "button.active = button.slot < slots");
		assertContains("Synaptic screen scales the chassis into the available viewport", screen, "width - EDGE_MARGIN * 2");
		assertContains("Synaptic save price uses server constants", screen, "SynapticLoadoutActionPacket.SAVE_BLOOD_COST");
		assertContains("Cursor keys remain available for text editing", screen, "!nameField.isFocused()");
		assertContains("Occupied patterns still require overwrite confirmation", screen,
				"!currentLoadout().isEmpty() && !confirmOverwrite");
		assertContains("Synaptic screen reads client skill sync cache for slot count", screen, "getClientUnlockedSlots");
		assertContains("Synaptic screen tracks synced slot count changes", screen, "lastRenderedSlots");
		assertContains("Synaptic screen refreshes controls when slot count changes", screen, "lastRenderedSlots != slots");
		assertContains("Synaptic unlocked slots beyond saved data display empty", screen,
				"slotIndex >= known.getLoadouts().size()");
		assertContains("Synaptic render uses display loadouts for missing slots", screen, "displayLoadout");
		assertContains("Synaptic screen open refreshes skill sync", clientSync, "PacketSyncSkills");
		assertContains("Synaptic skill sync is sent with manipulation sync", clientSync, "HemoCapabilityAccess.getSkillProgress(sender).toSyncTag()");
		assertContains("Synaptic save captures memorized wheel entries", action, "memorizedEquippedNames");
		assertNotContains("Synaptic save is not selected-only or normal-only", action, "normalEquippedNames");
		assertContains("Synaptic apply counts only normal entries against capacity", action,
				"countNormalEquippedNames(loadout.manipNames())");
	}
}
