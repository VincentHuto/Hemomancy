package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Core loop Track 4: Temper, the spendable pool beside Temperament, has one rule and one home. A cast or a Sculpting
 * pulse is charged in {@code BloodManipulation} through {@code TemperCharge} (worked out before the blood is priced,
 * settled after it is paid), and only the temperament package, the sync row and the HUD and Scrying readers touch the
 * pool. A new source of Temper (a vat, a food) goes through {@code TemperCharge} or {@code BloodTemper.add} in the
 * temperament package, or is added here with its reason.
 */
class TemperGuardrailTest {
	private static final String TEMPERAMENT = "src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/temperament/";
	private static final Set<String> POOL_READERS = Set.of(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/HemoCapabilityAccess.java",
			"src/main/java/com/vincenthuto/hemomancy/common/capability/PlayerStateSync.java",
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketSyncBloodTemper.java",
			"src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/TemperMarksOverlay.java",
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/ScryingDiagnosticsScreen.java",
			// The dev-only /hemo temperpool fixture (round two P11) sets the pool full or empty and puts it back.
			"src/main/java/com/vincenthuto/hemomancy/common/command/CoreLoopFixtures.java",
			// CQ-63: the Alchemist's one Titration Bandolier is flagged beside the pool; the gift never reads or fills it.
			"src/main/java/com/vincenthuto/hemomancy/common/mission/alchemist/TitrationBandolierGift.java");

	@Test
	void onlyTheTemperamentPackageAndItsReadersTouchThePool() throws IOException {
		TreeSet<String> outside = new TreeSet<>();
		try (var files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (var file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String display = RepoPaths.display(file);
				if (display.startsWith(TEMPERAMENT) || POOL_READERS.contains(display)) continue;
				if (SourceAssertions.read(file).contains("getBloodTemper(")) outside.add(display);
			}
		}
		assertEquals(Set.of(), outside, "fill or spend Temper through TemperCharge in the temperament package");
	}

	@Test
	void castsAndPulsesAreChargedBeforePricingAndSettledAfterPayment() {
		String manipulation = SourceAssertions.readMainJava("common/manipulation/BloodManipulation.java");
		String cast = SourceAssertions.section(manipulation, "TemperCharge.forCast(", "TemperCharge.commit(");
		SourceAssertions.assertContains("the cast's Temper line is in the ledger that prices it", cast,
				"ManipulationCostLedger.collect(player, this, costMultiplier,");
		SourceAssertions.assertContains("the cast settles Temper right after the blood is paid",
				SourceAssertions.section(manipulation, "BloodAccounting.pay(player, effectiveCost, CAST);", "HemorathEntity"),
				"TemperCharge.commit((ServerPlayer) player, temperCharge);");
		String pulse = SourceAssertions.methodBody(manipulation, "public OptionalDouble tryPerformVerbPulse(");
		SourceAssertions.assertContains("a pulse plans its charge by its own price band (LP-14)", pulse,
				"TemperCharge.forPulse(player, this, sculpting.cadence(), basePrice)");
		SourceAssertions.assertContains("a pulse's Temper line prices it", pulse,
				"ManipulationCostLedger.collect(player, this, costMultiplier, temperCharge)");
		SourceAssertions.assertContains("a dry pulse works softer", pulse, "pulse.workScale() * strength");
		// CL-12 (NQ-03-1 A): an unread kind fills its schools at the unread share.
		SourceAssertions.assertContains("absorbing from a creature fills its schools",
				SourceAssertions.readMainJava("common/item/harbinger/tool/living/BloodAbsorptionItem.java"),
				".fillFromCreature(serverPlayer, target, absorbed * BloodResearch.temperShare(read))");
	}
}
