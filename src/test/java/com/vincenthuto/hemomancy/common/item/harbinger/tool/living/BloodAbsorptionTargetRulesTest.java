package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;
import java.util.List;

public final class BloodAbsorptionTargetRulesTest {
	private BloodAbsorptionTargetRulesTest() {
	}

	@Test
	void allChecksPass() {
		bareAbsorptionChoosesNearestTarget();
		aBrandedTargetComesBeforeAnythingNearer();
		brandedTargetsKeepTheirOwnDistanceOrder();
	}

	private static void bareAbsorptionChoosesNearestTarget() {
		List<Candidate> candidates = List.of(
				new Candidate("far", false, 9.0),
				new Candidate("near", false, 1.0));
		assertEquals("nearest target wins", "near", order(candidates).getFirst().id());
	}

	private static void aBrandedTargetComesBeforeAnythingNearer() {
		List<Candidate> candidates = List.of(
				new Candidate("near", false, 1.0),
				new Candidate("branded_far", true, 64.0));
		assertEquals("the drawer's brand is picked first (core-loop Track 12)", "branded_far", order(candidates).getFirst().id());
	}

	private static void brandedTargetsKeepTheirOwnDistanceOrder() {
		List<Candidate> candidates = List.of(
				new Candidate("plain", false, 1.0),
				new Candidate("branded_far", true, 49.0),
				new Candidate("branded_near", true, 16.0));
		List<Candidate> ordered = order(candidates);
		assertEquals("brands first, nearest brand first", "branded_near", ordered.get(0).id());
		assertEquals("then the farther brand", "branded_far", ordered.get(1).id());
		assertEquals("then the unbranded", "plain", ordered.get(2).id());
	}

	private static List<Candidate> order(List<Candidate> candidates) {
		return BloodAbsorptionTargetRules.orderTargets(candidates, Candidate::branded, Candidate::distanceSqr);
	}

	private static void assertEquals(String label, String expected, String actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private record Candidate(String id, boolean branded, double distanceSqr) {
	}
}
