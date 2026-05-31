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
		assertContains("reliquary should fall back to the conjured item stack",
				screen, "manip instanceof ConjurationManip conjuration");
		assertContains("reliquary should render conjuration manipulations with their conjured item",
				screen, "new ItemStack(conjuration.getItem().get())");
		assertContains("known manipulation rendering should use fallback icon lookup",
				screen, "ItemStack stack = iconStackFor(icon.manip);");
		assertContains("drag rendering should use fallback icon lookup",
				screen, "ItemStack stack = iconStackFor(draggingManip);");
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
