package com.vincenthuto.hemomancy.common.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ArmatureUpgradeRules {
	public static final int BOWL_STACK_LIMIT = 1;

	private ArmatureUpgradeRules() {
	}

	public enum ArmatureSlot {
		HEAD,
		CHEST,
		LEGS,
		FEET
	}

	public record Candidate(ArmatureSlot slot, int bowlSlot, boolean baseMatches, boolean reagentMatches,
			boolean persistentGateMatches, int requiredDegree, int playerDegree, double bloodCost) {
		public boolean hasMatchingInputs() {
			return baseMatches && reagentMatches && persistentGateMatches && playerDegree >= requiredDegree;
		}
	}

	public record ProcessResult(List<ArmatureSlot> upgradedSlots, List<Integer> consumedBowlSlots,
			double bloodSpent) {
	}

	public static int bowlSlotFor(ArmatureSlot slot) {
		return switch (slot) {
			case HEAD -> 0;
			case CHEST -> 1;
			case LEGS -> 2;
			case FEET -> 3;
		};
	}

	public static ArmatureSlot bowlSlotForLocalHit(double localX, double localZ) {
		boolean east = localX >= 0.5D;
		boolean south = localZ >= 0.5D;
		if (!east && !south) {
			return ArmatureSlot.HEAD;
		}
		if (east && !south) {
			return ArmatureSlot.CHEST;
		}
		return east ? ArmatureSlot.FEET : ArmatureSlot.LEGS;
	}

	public static List<ArmatureSlot> processingOrder() {
		return List.of(ArmatureSlot.HEAD, ArmatureSlot.CHEST, ArmatureSlot.LEGS, ArmatureSlot.FEET);
	}

	public static List<Integer> bowlSlotsInInsertionOrder() {
		return List.of(
				bowlSlotFor(ArmatureSlot.HEAD),
				bowlSlotFor(ArmatureSlot.CHEST),
				bowlSlotFor(ArmatureSlot.LEGS),
				bowlSlotFor(ArmatureSlot.FEET));
	}

	public static List<Integer> bowlSlotsInWithdrawalOrder() {
		return List.of(
				bowlSlotFor(ArmatureSlot.FEET),
				bowlSlotFor(ArmatureSlot.LEGS),
				bowlSlotFor(ArmatureSlot.CHEST),
				bowlSlotFor(ArmatureSlot.HEAD));
	}

	public static int insertedCountForBowl(boolean bowlEmpty, int remainingItems) {
		return bowlEmpty && remainingItems > 0 ? BOWL_STACK_LIMIT : 0;
	}

	public static int nextInsertableBowlSlot(List<Boolean> occupiedSlots) {
		for (int slot : bowlSlotsInInsertionOrder()) {
			if (slot < occupiedSlots.size() && !occupiedSlots.get(slot)) {
				return slot;
			}
		}
		return -1;
	}

	public static ProcessResult process(List<Candidate> candidates, int playerDegree, double availableBlood) {
		List<ArmatureSlot> upgradedSlots = new ArrayList<>();
		List<Integer> consumedBowlSlots = new ArrayList<>();
		double spent = 0;

		List<Candidate> ordered = candidates.stream()
				.sorted(Comparator.comparingInt(Candidate::bowlSlot))
				.toList();

		for (ArmatureSlot slot : processingOrder()) {
			for (Candidate candidate : ordered) {
				if (candidate.slot() != slot) {
					continue;
				}
				Candidate checked = new Candidate(candidate.slot(), candidate.bowlSlot(), candidate.baseMatches(),
						candidate.reagentMatches(), candidate.persistentGateMatches(),
						candidate.requiredDegree(), playerDegree, candidate.bloodCost());
				if (!checked.hasMatchingInputs()) {
					continue;
				}
				if (spent + checked.bloodCost() > availableBlood) {
					return new ProcessResult(List.copyOf(upgradedSlots), List.copyOf(consumedBowlSlots), spent);
				}
				upgradedSlots.add(slot);
				consumedBowlSlots.add(checked.bowlSlot());
				spent += checked.bloodCost();
				break;
			}
		}

		return new ProcessResult(List.copyOf(upgradedSlots), List.copyOf(consumedBowlSlots), spent);
	}
}
