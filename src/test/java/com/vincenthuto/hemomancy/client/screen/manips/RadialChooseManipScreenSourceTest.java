package com.vincenthuto.hemomancy.client.screen.manips;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RadialChooseManipScreenSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private RadialChooseManipScreenSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java");
		String menu = read("src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java");
		String reliquary = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MnemonicReliquaryScreen.java");

		assertContains("radial screen imports fixed mechanical helper", screen,
				"ManipulationEquipHelper");
		assertContains("radial screen adds mechanical entries to the inner band", screen,
				"this.menu.addAllInner(this.cachedMechanicalItems);");
		assertContains("radial screen defines the selected manipulation slice tint", screen,
				"SELECTED_MANIP_SLICE_TINT");
		assertContains("radial screen compares item names to the selected manipulation", screen,
				"manipulation.getName().equals(selectedManipName)");
		assertContains("selected radial item applies the tint to the whole slice", screen,
				"item.setBackgroundColor(SELECTED_MANIP_SLICE_TINT);");
		assertContains("generic radial reads per-item background colors", menu,
				"item.getBackgroundColor");
		assertContains("absorption is added before projection for the top inner half", screen,
				"ManipulationEquipHelper.BLOOD_ABSORPTION");
		assertContains("projection is added to the second inner half", screen,
				"ManipulationEquipHelper.BLOOD_PROJECTION");
		assertContains("outer ring skips fixed mechanical manipulations", screen,
				"ManipulationEquipHelper.isFixedMechanicalManip(c.getName())");
		assertContains("generic radial supports inner band items", menu,
				"visibleInnerItems");
		assertContains("generic radial exposes inner add API", menu,
				"addAllInner");
		assertContains("reliquary hides fixed mechanics from normal tendency groups", reliquary,
				"ManipulationEquipHelper.isFixedMechanicalManip(manip.getName())");
		assertContains("reliquary normal-slot counter excludes fixed mechanics", reliquary,
				"equippedManips.size()");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
