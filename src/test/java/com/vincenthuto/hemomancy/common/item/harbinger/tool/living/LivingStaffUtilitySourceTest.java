package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingStaffUtilitySourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingStaffUtilitySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String staff = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffItem.java");
		String layer = read("src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/CellHandLayer.java");
		String useMethod = staff.substring(staff.indexOf("public InteractionResultHolder<ItemStack> use"));
		String absorbWithStaff = between(staff, "private static void absorbWithStaff", "private static void projectWithStaff");
		String projectWithStaff = between(staff, "private static void projectWithStaff", "private static void recordUtilityBloodHandled");

		assertBefore("staff starts using before the server-only branch so client hold ticks and particles run",
				useMethod, "playerIn.startUsingItem(handIn);", "if (!worldIn.isClientSide)");
		assertContains("staff utility selection uses the shared name rule",
				staff, "LivingStaffUtilitySelectionRules.isSelectedUtility");
		assertContains("staff emits the shared cell-hand first-person particles while held",
				staff, "CellHandParticleEffects.spawnFirstPersonParticlesForStack");
		assertContains("third-person blood hand layer recognizes active living staff utility use",
				layer, "LivingStaffItem.isLivingStaffUtilityUse");
		assertNotContains("staff absorption pulses do not rewrite stack custom data during held use",
				absorbWithStaff, "LivingStaffFocusRules.addBloodHandled");
		assertNotContains("staff projection pulses do not rewrite stack custom data during held use",
				projectWithStaff, "LivingStaffFocusRules.addBloodHandled");
		assertContains("staff records handled blood away from the held item during utility use",
				staff, "recordUtilityBloodHandled(player,");
		assertContains("staff commits handled blood once when utility use is released",
				staff, "commitUtilityBloodHandled(player, stack);");
		assertContains("staff still preserves long-term BloodHandled focus progression",
				staff, "LivingStaffFocusRules.addBloodHandled(stack, handled);");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
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

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (unexpected '" + unexpected + "')");
		}
	}

	private static String between(String text, String start, String end) {
		int startIndex = text.indexOf(start);
		int endIndex = text.indexOf(end, startIndex);
		if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
			throw new AssertionError("Unable to find source section from '" + start + "' to '" + end + "'");
		}
		return text.substring(startIndex, endIndex);
	}
}
