package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public final class CardinalRiteBrazierSignatureRules {
	private CardinalRiteBrazierSignatureRules() {
	}

	public static boolean exactMatch(Map<String, Integer> required, Map<String, Integer> offered) {
		return required != null && offered != null && required.equals(offered);
	}

	/** One lit brazier, by its index in the offered list, filling one slot of the rite. */
	public record Assignment(int offered, int slot) {
	}

	/**
	 * How the lit braziers answer a rite, or null when they do not.
	 * <p>
	 * A rite without optional offerings needs an exact answer: every brazier fills one required slot and every slot
	 * is filled, so an extra brazier refuses the rite as surely as a missing one. Slots index into {@code required}.
	 * <p>
	 * A rite with optional offerings never refuses here: each optional slot takes the first brazier it accepts that
	 * no earlier slot took, and a brazier no slot accepts is simply not counted. Slots index into {@code optional}.
	 */
	public static <S, O> List<Assignment> assign(List<S> required, List<S> optional, List<O> offered,
			BiPredicate<S, O> accepts) {
		if (!optional.isEmpty()) {
			List<Assignment> taken = new ArrayList<>();
			boolean[] used = new boolean[offered.size()];
			for (int slot = 0; slot < optional.size(); slot++) {
				for (int index = 0; index < offered.size(); index++) {
					if (used[index] || !accepts.test(optional.get(slot), offered.get(index))) continue;
					used[index] = true;
					taken.add(new Assignment(index, slot));
					break;
				}
			}
			return List.copyOf(taken);
		}
		if (offered.size() != required.size()) return null;
		int[] slotOf = new int[offered.size()];
		Arrays.fill(slotOf, -1);
		if (!fill(required, offered, accepts, 0, new boolean[offered.size()], slotOf)) return null;
		List<Assignment> assigned = new ArrayList<>(offered.size());
		for (int index = 0; index < offered.size(); index++) assigned.add(new Assignment(index, slotOf[index]));
		return List.copyOf(assigned);
	}

	private static <S, O> boolean fill(List<S> required, List<O> offered, BiPredicate<S, O> accepts, int slot,
			boolean[] used, int[] slotOf) {
		if (slot == required.size()) return true;
		for (int index = 0; index < offered.size(); index++) {
			if (used[index] || !accepts.test(required.get(slot), offered.get(index))) continue;
			used[index] = true;
			slotOf[index] = slot;
			if (fill(required, offered, accepts, slot + 1, used, slotOf)) return true;
			slotOf[index] = -1;
			used[index] = false;
		}
		return false;
	}
}
