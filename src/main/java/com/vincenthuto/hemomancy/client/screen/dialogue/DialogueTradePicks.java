package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals.Grade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The Trade box's choice of stacks, over a plain view of the player's main inventory so it can be tested without a
 * game. A {@link Stack} is one slot that matches at least one part of the want, graded per part. {@link #predict}
 * mirrors the server's plan (slots in pick order, each slot paying one part, the lowest grade winning), so the Offer
 * button lights only when the server would find enough; the server still checks everything again.
 */
public final class DialogueTradePicks {
	public enum Carried { NONE, SOME, ENOUGH }

	/** Main-inventory {@code slot} holding {@code count}; {@code byPart[p]} is its grade for part p, null if no match. */
	public record Stack(int slot, int count, Grade[] byPart) {
		public boolean matches(int part) {
			return part >= 0 && part < byPart.length && byPart[part] != null;
		}

		public boolean matchesAny() {
			for (Grade grade : byPart) if (grade != null) return true;
			return false;
		}

		/** The grade shown on its well: for the first part it matches. */
		public Grade shownGrade() {
			for (Grade grade : byPart) if (grade != null) return grade;
			return Grade.FAIR;
		}
	}

	private DialogueTradePicks() {}

	/**
	 * The stacks the client picks before the player touches anything: for each part in order, the best-graded unused
	 * stacks (then the largest, then the lowest slot) until the count is covered. Poor stacks are never pre-picked,
	 * since one would sink the whole trade.
	 */
	public static List<Integer> prePick(List<Stack> stacks, int[] counts) {
		List<Integer> picked = new ArrayList<>();
		for (int part = 0; part < counts.length; part++) {
			int p = part;
			List<Stack> candidates = stacks.stream()
					.filter(stack -> stack.matches(p) && stack.byPart()[p] != Grade.POOR && stack.count() > 0
							&& !picked.contains(stack.slot()))
					.sorted(Comparator.comparing((Stack stack) -> stack.byPart()[p]).reversed()
							.thenComparing(Comparator.comparingInt(Stack::count).reversed())
							.thenComparingInt(Stack::slot))
					.toList();
			int remaining = counts[part];
			for (Stack stack : candidates) {
				if (remaining <= 0) break;
				picked.add(stack.slot());
				remaining -= stack.count();
			}
		}
		return picked;
	}

	/**
	 * What the server would make of {@code picked}: the trade's grade, or empty when the slots don't cover every
	 * part. {@code POOR} as soon as a stack that would be taken is poor.
	 */
	public static Optional<Grade> predict(List<Stack> stacks, int[] counts, List<Integer> picked) {
		List<Integer> used = new ArrayList<>();
		Grade grade = Grade.FINE;
		for (int part = 0; part < counts.length; part++) {
			int remaining = counts[part];
			for (int slot : picked) {
				if (remaining <= 0) break;
				if (used.contains(slot)) continue;
				Stack stack = find(stacks, slot);
				if (stack == null || stack.count() <= 0 || !stack.matches(part)) continue;
				grade = grade.min(stack.byPart()[part]);
				if (grade == Grade.POOR) return Optional.of(Grade.POOR);
				used.add(slot);
				remaining -= stack.count();
			}
			if (remaining > 0) return Optional.empty();
		}
		return Optional.of(grade);
	}

	/** Enough when the pre-pick would trade; some when any stack matches; otherwise none. */
	public static Carried carried(List<Stack> stacks, int[] counts) {
		if (counts.length == 0) return Carried.NONE;
		Optional<Grade> grade = predict(stacks, counts, prePick(stacks, counts));
		if (grade.isPresent() && grade.get() != Grade.POOR) return Carried.ENOUGH;
		return stacks.stream().anyMatch(Stack::matchesAny) ? Carried.SOME : Carried.NONE;
	}

	/** {@code picked} with {@code slot} dropped if it was picked, or added at the end if it wasn't. */
	public static List<Integer> toggle(List<Integer> picked, int slot) {
		List<Integer> next = new ArrayList<>(picked);
		if (!next.remove(Integer.valueOf(slot))) next.add(slot);
		return next;
	}

	/** {@code picked} without slots that no longer hold a matching stack (the inventory moved under the strip). */
	public static List<Integer> retain(List<Integer> picked, List<Stack> stacks) {
		return picked.stream().filter(slot -> {
			Stack stack = find(stacks, slot);
			return stack != null && stack.count() > 0 && stack.matchesAny();
		}).toList();
	}

	private static Stack find(List<Stack> stacks, int slot) {
		for (Stack stack : stacks) if (stack.slot() == slot) return stack;
		return null;
	}
}
