package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FungalScarBehaviorSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private FungalScarBehaviorSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		talaromycesNoLongerGrantsHasteButKeepsVeinMiningHook();
		newScarHooksRouteThroughHelpersAndCirculation();
		removedDeathTetherAndOrchardHooksAreGone();
	}

	private static void talaromycesNoLongerGrantsHasteButKeepsVeinMiningHook() throws IOException {
		String talaromyces = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/fungal/TalaromycesMinusItem.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsEntityEventHandler.java");
		assertDoesNotContain("Talaromyces no Haste", talaromyces, "MobEffects.DIG_SPEED");
		assertDoesNotContain("Talaromyces no tick effect", talaromyces, "player.addEffect");
		assertContains("vein mining hook remains", events, "VeinMinerHelper.tryVeinMine");
		assertContains("vein mining stays tied to Talaromyces", events, "ItemInit.talaromyces_minus");
	}

	private static void newScarHooksRouteThroughHelpersAndCirculation() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsEntityEventHandler.java");
		String manipulation = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java");
		String vascular = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/vascular/VascularSystemEvents.java");

		assertContains("Rhizovitta tick hook", events, "RootedStateHelper.tick");
		assertContains("Putrivora tick hook", events, "AfflictionDigestHelper.tick");
		assertContains("Cryostroma tick hook", events, "ConserveStateHelper.tick");
		assertContains("scar passive income routed through circulation", events, "IncomeChannel.SCAR");
		assertContains("Rhizovitta refund post-cast", manipulation, "RootedStateHelper.refundManipulationCost");
		assertContains("Cryostroma vascular multiplier", vascular, "ConserveStateHelper.vascularHealMultiplier");
	}

	private static void removedDeathTetherAndOrchardHooksAreGone() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsEntityEventHandler.java");
		assertDoesNotContain("Thanomyces death save removed", events, "ThanomycesResurgensItem");
		assertDoesNotContain("Sanguiflora orchard removed", events, "SanguifloraeCadensItem");
		assertDoesNotContain("Anastocordyceps tether hook removed", events, "AnastocordycepsNexusItem");
		assertDoesNotContain("No Last Rite coupling for Cryostroma", events, "LastRite");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": found " + unexpected);
		}
	}
}
