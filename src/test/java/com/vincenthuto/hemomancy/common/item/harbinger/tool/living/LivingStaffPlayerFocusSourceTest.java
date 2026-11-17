package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class LivingStaffPlayerFocusSourceTest {

	private LivingStaffPlayerFocusSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java");
		String rules = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffFocusRules.java");

		assertContains("living staff can be dispelled", staff, "implements IDispellable");
		assertContains("living staff reads player progress", staff, "HemoCapabilityAccess.getLivingStaffProgress(player)");
		assertContains("living staff reads skill levels", staff, "LivingStaffFocusProfile.fromPlayer(player");
		assertContains("living staff commits handled blood to player progress through its growth (Track 8)", staff,
				"LivingStaffGrowth.record(serverPlayer, ringBlood)");
		assertNotContains("living staff no longer writes handled blood to item stack", staff,
				"LivingStaffFocusRules.addBloodHandled(stack");
		assertNotContains("living staff no longer reads stack handled blood", staff,
				"LivingStaffFocusRules.getBloodHandled(stack)");
		assertNotContains("living staff no longer reads stack vesper state", staff,
				"LivingStaffFocusRules.isVesperAwakened(stack)");
		assertContains("focus rules accept staff skill profile", rules, "LivingStaffFocusProfile focus");
		assertNotContains("focus rules no longer use handled blood milestones", rules, "FOCUS_BLOOD_HANDLED");
		assertNotContains("focus rules no longer read blood handled", rules, "getBloodHandled");
		assertContains("focus rules read the growth ring the profile carries (Track 8)", rules, "growthRing()");
	}
}
