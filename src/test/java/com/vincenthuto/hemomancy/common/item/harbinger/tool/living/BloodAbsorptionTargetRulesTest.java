package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.util.List;

public final class BloodAbsorptionTargetRulesTest {
	private BloodAbsorptionTargetRulesTest() {
	}

	public static void main(String[] args) {
		bareAbsorptionChoosesNearestValidTarget();
		invalidTargetsAreIgnored();
	}

	private static void bareAbsorptionChoosesNearestValidTarget() {
		List<Candidate> candidates = List.of(
				new Candidate("near", false, true, 1.0),
				new Candidate("looked_at", true, true, 9.0));

		Candidate selected = BloodAbsorptionTargetRules.chooseBareTarget(candidates,
				Candidate::valid, Candidate::lookedAt, Candidate::distanceSqr).orElseThrow();
		assertEquals("nearest valid target wins", "near", selected.id());
	}

	private static void invalidTargetsAreIgnored() {
		List<Candidate> candidates = List.of(
				new Candidate("invalid_looked_at", true, false, 1.0),
				new Candidate("valid_far", false, true, 9.0));

		Candidate selected = BloodAbsorptionTargetRules.chooseBareTarget(candidates,
				Candidate::valid, Candidate::lookedAt, Candidate::distanceSqr).orElseThrow();
		assertEquals("invalid looked-at candidate is skipped", "valid_far", selected.id());
	}

	private static void assertEquals(String label, String expected, String actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private record Candidate(String id, boolean lookedAt, boolean valid, double distanceSqr) {
	}
}
