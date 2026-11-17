package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class FungalScarBehaviorSourceTest {

	private FungalScarBehaviorSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		talaromycesNoLongerGrantsHasteButKeepsVeinMiningHook();
		newScarHooksRouteThroughHelpersAndCirculation();
		removedDeathTetherAndOrchardHooksAreGone();
	}

	private static void talaromycesNoLongerGrantsHasteButKeepsVeinMiningHook() throws IOException {
		String talaromyces = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/scar/fungal/TalaromycesMinusItem.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsEntityEventHandler.java");
		assertNotContains("Talaromyces no Haste", talaromyces, "MobEffects.DIG_SPEED");
		assertNotContains("Talaromyces no tick effect", talaromyces, "player.addEffect");
		assertContains("vein mining hook remains", events, "VeinMinerHelper.tryVeinMine");
		assertContains("vein mining stays tied to Talaromyces", events, "ItemInit.talaromyces_minus");
	}

	private static void newScarHooksRouteThroughHelpersAndCirculation() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsEntityEventHandler.java");
		String manipulation = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java");
		String vascular = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/vascular/VascularSystemEvents.java");
		String rooted = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/fungal/RootedStateHelper.java");
		String digest = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/fungal/AfflictionDigestHelper.java");
		String conserve = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/fungal/ConserveStateHelper.java");

		assertContains("Rhizovitta tick hook", events, "RootedStateHelper.tick");
		assertContains("Putrivora tick hook", events, "AfflictionDigestHelper.tick");
		assertContains("Cryostroma tick hook", events, "ConserveStateHelper.tick");
		assertContains("rooted scar income routed through circulation", rooted, "IncomeChannel.SCAR");
		assertContains("digest scar income routed through circulation", digest, "IncomeChannel.SCAR");
		assertContains("conserve scar income routed through circulation", conserve, "IncomeChannel.SCAR");
		assertContains("Rhizovitta refund post-cast", manipulation, "RootedStateHelper.refundManipulationCost");
		assertContains("Cryostroma vascular multiplier", vascular, "ConserveStateHelper.vascularHealMultiplier");
	}

	private static void removedDeathTetherAndOrchardHooksAreGone() throws IOException {
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/scar/ScarsEntityEventHandler.java");
		assertNotContains("Thanomyces death save removed", events, "ThanomycesResurgensItem");
		assertNotContains("Sanguiflora orchard removed", events, "SanguifloraeCadensItem");
		assertNotContains("Anastocordyceps tether hook removed", events, "AnastocordycepsNexusItem");
		assertNotContains("No Last Rite coupling for Cryostroma", events, "LastRite");
	}
}
