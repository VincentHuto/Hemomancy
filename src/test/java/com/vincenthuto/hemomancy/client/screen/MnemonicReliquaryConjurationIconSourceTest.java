package com.vincenthuto.hemomancy.client.screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MnemonicReliquaryConjurationIconSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private MnemonicReliquaryConjurationIconSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MnemonicReliquaryScreen.java");

		assertContains("reliquary should import conjuration manip for non-memory icons",
				screen, "import com.vincenthuto.hemomancy.common.manipulation.ferric.ConjurationManip;");
		assertContains("reliquary should centralize manipulation icon stack lookup",
				screen, "private ItemStack iconStackFor(BloodManipulation manip)");
		assertContains("reliquary should cache hidden active-manipulation memory icons",
				screen, "!ManipulationRetirementRules.isRetiredManipulation(manip)");
		assertContains("reliquary should fall back to the conjured item stack",
				screen, "manip instanceof ConjurationManip conjuration");
		assertContains("reliquary should render conjuration manipulations with their conjured item",
				screen, "new ItemStack(conjuration.getItem().get())");
		assertContains("known manipulation rendering should use fallback icon lookup",
				screen, "renderManipulationIcon(graphics, icon.manip, icon.x, icon.y);");
		assertContains("drag rendering should use fallback icon lookup",
				screen, "renderManipulationIcon(graphics, draggingManip");
		assertContains("the shared renderer should retain conjuration item fallback",
				screen, "ItemStack stack = iconStackFor(manip);");
		assertContains("family variants should use a compact two-column dropdown",
				screen, "int cols = Math.min(2, forms.size());");
		assertContains("family variants should wrap beneath the baseline icon",
				screen, "int y = heldFamilyIcon.y + (i / cols + 1) * (currentIconSize + gap);");
		assertContains("the family dropdown should hide already memorized forms",
				screen, "private List<BloodManipulation> unmemorizedFamilyForms(BloodManipulation baseline)");
		assertContains("the family dropdown should only add an unmemorized baseline",
				screen, "if (!equippedNames.contains(heldFamilyIcon.manip.getName())) forms.add(0, heldFamilyIcon.manip);");
		assertContains("family holds should leave enough time for an ordinary manipulation drag",
				screen, "FAMILY_HOLD_MILLIS = 600L;");
		assertContains("the family dropdown should render above the central memory ring",
				screen, "graphics.pose().translate(0.0F, 0.0F, 300.0F);");
		assertContains("the family dropdown should have a continuous dark red backdrop",
				screen, "FAMILY_DROPDOWN_BACKGROUND = 0xFF2A080D;");
		assertContains("the family dropdown backdrop should cover the full wrapped grid",
				screen, "graphics.fill(minX - padding, minY - padding, maxX + padding, maxY + padding,");
		assertContains("the family dropdown should suppress underlying hover targets",
				screen, "if (familyFanOpen) {");
		assertContains("the family dropdown should consume clicks while open",
				screen, "if (familyFanOpen) return true;");
		assertNotContains("family variants should not fan upward around the baseline icon",
				screen, "double angle = -Math.PI / 2.0D");
		assertNotContains("known manipulation rendering should not only look in memory cache",
				screen, "ItemStack stack = manipItemCache.get(icon.manip.getName());");
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
