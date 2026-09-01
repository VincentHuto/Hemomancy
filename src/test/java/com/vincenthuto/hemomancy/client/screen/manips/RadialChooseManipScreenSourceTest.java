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
		String loadout = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SynapticLoadoutScreen.java");
		String menu = read("src/main/java/com/vincenthuto/hemomancy/client/screen/radial/GenericRadialMenu.java");
		String itemStackRadial = read("src/main/java/com/vincenthuto/hemomancy/client/screen/radial/ItemStackRadialMenuItem.java");
		String reliquary = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MnemonicReliquaryScreen.java");
		String iconResolver = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/ManipulationIconResolver.java");

		assertContains("radial screen imports fixed mechanical helper", screen,
				"ManipulationEquipHelper");
		assertContains("radial screen adds mechanical entries to the inner band", screen,
				"this.menu.addAllInner(this.cachedMechanicalItems);");
		assertContains("radial screen defines the selected manipulation slice tint", screen,
				"SELECTED_MANIP_SLICE_TINT");
		assertContains("radial screen defines a distinct recharging armor ability tint", screen,
				"RECHARGING_ABILITY_SLICE_TINT");
		assertContains("radial screen compares item names to the selected manipulation", screen,
				"manipulation.getName().equals(selectedManipName)");
		assertContains("selected radial item applies the tint to the whole slice", screen,
				"item.setBackgroundColor(SELECTED_MANIP_SLICE_TINT);");
		assertContains("shared resolver maps conjure blade to the living blade memory overlay", iconResolver,
				"case \"conjure_blade\" -> \"memory_living_blade_overlay\"");
		assertContains("shared resolver maps conjure staff to the living staff memory overlay", iconResolver,
				"case \"conjure_staff\" -> \"memory_living_staff_overlay\"");
		assertContains("shared resolver maps Lignum Mortis to its item model overlay", iconResolver,
				"case \"lignum_mortis\" -> \"memory_hemorrhage_overlay\"");
		assertContains("radial item construction uses the shared memory overlay resolver", screen,
				"ManipulationIconResolver.overlay(manipulation.getName())");
		assertContains("saved loadouts use the shared memory overlay resolver", loadout,
				"ManipulationIconResolver.overlay(ref.id())");
		assertContains("generic radial reads per-item background colors", menu,
				"item.getBackgroundColor");
		assertContains("absorption is added before projection for the top inner half", screen,
				"ManipulationEquipHelper.BLOOD_ABSORPTION");
		assertContains("projection is added to the second inner half", screen,
				"ManipulationEquipHelper.BLOOD_PROJECTION");
		assertContains("outer ring skips fixed mechanical manipulations", screen,
				"ManipulationEquipHelper.isFixedMechanicalManip(c.getName())");
		assertContains("item-stack radial entries can render custom tooltip lines", itemStackRadial,
				"customTooltip");
		assertContains("armor ability radial entry uses ability tooltip instead of helmet tooltip", screen,
				"ability.tooltip()");
		assertContains("armor ability tooltip is generated dynamically while hovered", screen,
				"armorAbilityTooltip(ability)");
		assertContains("armor ability tooltip shows live recharge remaining text", screen,
				"ability.hemomancy.armor_set.recharging");
		assertContains("armor ability radial reads client-synced cooldown state", screen,
				"getClientCooldownUntil");
		assertContains("armor ability wedge computes the recharge tint while cooldown remains", screen,
				"getBackgroundColor(int fallbackColor)");
		assertContains("item-stack radial entries support dynamic tooltip suppliers", itemStackRadial,
				"Supplier<List<Component>> customTooltip");
		assertNotContains("armor ability radial entry does not duplicate the tooltip title as central hover text", screen,
				"item.setCentralText(ability.displayName().copy());");
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

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (still contains '" + unexpected + "')");
		}
	}
}
