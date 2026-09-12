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
		assertNotContains("selected manipulation no longer shares a flat slice tint with cooldown", screen,
				"SELECTED_MANIP_SLICE_TINT");
		assertContains("radial screen defines a distinct recharging armor ability tint", screen,
				"RECHARGING_ABILITY_SLICE_TINT");
		assertContains("radial screen compares item names to the selected manipulation", screen,
				"manipulation.getName().equals(selectedManipName)");
		assertContains("radial screen resolves selected manipulation directly from its memory reference", screen,
				"selectedMemory.id()");
		assertContains("selected radial item enables its veiny slice border", screen,
				"item.setVeinyBorder(true);");
		assertContains("selecting a manipulation updates the open wheel immediately", screen,
				"menu.selectVeinyBorder(this);");
		assertContains("generic radial can move the selected border between existing items", menu,
				"public void selectVeinyBorder(RadialMenuItem selected)");
		assertContains("generic radial renders selected slice vein borders", menu,
				"drawVeinyBorders");
		assertContains("selected vein border is not hidden by the radial depth buffer", menu,
				"RenderSystem.disableDepthTest();");
		assertContains("shared resolver maps conjure blade to the living blade memory overlay", iconResolver,
				"case \"conjure_blade\" -> \"memory_living_blade_overlay\"");
		assertContains("shared resolver maps conjure staff to the living staff memory overlay", iconResolver,
				"case \"conjure_staff\" -> \"memory_living_staff_overlay\"");
		assertContains("shared resolver uses dedicated manipulation overlays", iconResolver,
				"default -> \"memory_\" + manipulationId + \"_overlay\"");
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
		assertContains("outer ring uses the shared memorized wheel order", screen,
				"ManipulationWheelOrder.resolve(allManips, equippedNames)");
		assertContains("reliquary uses the shared memorized wheel order", reliquary,
				"ManipulationWheelOrder.resolve(known.getManipList(), equipped)");
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
		assertContains("manipulation wedges read their own client cooldown", screen,
				"ClientManipulationCooldowns.remainingTicks(manipulation.getName(), now)");
		assertContains("cooling manipulation wedges reuse the red recharge tint", screen,
				"? RECHARGING_ABILITY_SLICE_TINT : super.getBackgroundColor(fallbackColor)");
		assertContains("cooling manipulation hover text shows its remaining time", screen,
				"seconds + \"s cooldown\"");
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
