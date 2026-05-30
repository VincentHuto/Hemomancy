package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingStaffPlayerFocusSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingStaffPlayerFocusSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java");
		String rules = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffFocusRules.java");

		assertContains("living staff can be dispelled", staff, "implements IDispellable");
		assertContains("living staff reads player progress", staff, "HemoCapabilityAccess.getLivingStaffProgress(player)");
		assertContains("living staff reads skill levels", staff, "LivingStaffFocusProfile.fromPlayer(player");
		assertContains("living staff commits handled blood to player progress", staff, "progress.addBloodHandled(handled)");
		assertNotContains("living staff no longer writes handled blood to item stack", staff,
				"LivingStaffFocusRules.addBloodHandled(stack");
		assertNotContains("living staff no longer reads stack handled blood", staff,
				"LivingStaffFocusRules.getBloodHandled(stack)");
		assertNotContains("living staff no longer reads stack vesper state", staff,
				"LivingStaffFocusRules.isVesperAwakened(stack)");
		assertContains("focus rules accept staff skill profile", rules, "LivingStaffFocusProfile focus");
		assertNotContains("focus rules no longer use handled blood milestones", rules, "FOCUS_BLOOD_HANDLED");
		assertNotContains("focus rules no longer read blood handled", rules, "getBloodHandled");
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
