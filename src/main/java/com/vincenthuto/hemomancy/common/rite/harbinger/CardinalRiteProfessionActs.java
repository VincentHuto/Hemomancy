package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Authored signature interactions for the eight degree rites.
 */
public final class CardinalRiteProfessionActs {
	private CardinalRiteProfessionActs() {
	}

	public static Act forRite(ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		String handler = recipe.getCeremony().signatureHandler();
		return switch (handler) {
			case "first_circulation" -> ordered(handler,
					"The four pulses must circulate sunwise.",
					List.of(p(0, -2), p(2, 0), p(0, 2), p(-2, 0)));
			case "tendency_response" -> choice(handler,
					"Choose the blood's answer; the direction becomes your declaration.",
					ring(8, 3), -1);
			case "blood_memory" -> ordered(handler,
					"Route the moving memory from wound to heart.",
					List.of(p(-3, 0), p(-2, -1), p(-1, 0), p(0, 1), p(1, 0), p(2, -1), p(3, 0)));
			case "simulacrum_wound" -> new Act(handler,
					"Expose the wound, then stabilize it or make the precise finishing strike.",
					List.of(p(0, -2), p(-2, 1), p(2, 1)), List.of(0, -1));
			case "fungal_root" -> choice(handler,
					"Preserve the living root. Cauterize no counterfeit.",
					ring(6, 3), Math.floorMod(rite.getCenterPos().hashCode(), 6));
			case "covenant_currents" -> ordered(handler,
					"Distribute the covenant current, then recall it without loss.",
					outAndBack());
			case "trifold_judgment" -> choice(handler,
					"Judge the authored tells: Order, blood-memory, or fungal truth.",
					List.of(p(-3, 0), p(0, -3), p(3, 0)),
					Math.floorMod(rite.getRecipeId().hashCode(), 3));
			case "reverse_circulation" -> ordered(handler,
					"Reverse every ring, outermost to innermost. Each anchor demands 50ml.",
					reverseAnchors(recipe));
			default -> ordered(handler, "Make the culminating profession.",
					List.of(p(0, -2), p(2, 0), p(0, 2), p(-2, 0)));
		};
	}

	public static boolean requiresBlood(Act act) {
		return "reverse_circulation".equals(act.id());
	}

	private static Act ordered(String id, String prompt, List<BlockPos> nodes) {
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i++) order.add(i);
		return new Act(id, prompt, nodes, order);
	}

	private static Act choice(String id, String prompt, List<BlockPos> nodes, int correct) {
		return new Act(id, prompt, nodes, List.of(correct));
	}

	private static List<BlockPos> outAndBack() {
		List<BlockPos> outward = new ArrayList<>(List.of(
				p(0, -1), p(2, -2), p(3, 0), p(2, 2), p(0, 3), p(-2, 2)));
		List<BlockPos> result = new ArrayList<>(outward);
		Collections.reverse(outward);
		result.addAll(outward);
		return result;
	}

	private static List<BlockPos> reverseAnchors(CardinalRiteRecipe recipe) {
		List<BlockPos> nodes = new ArrayList<>();
		for (int i = recipe.getCeremony().anchors().size() - 1; i >= 0; i--) {
			nodes.add(recipe.getCeremony().anchors().get(i).offset());
		}
		return nodes;
	}

	private static List<BlockPos> ring(int count, int radius) {
		List<BlockPos> nodes = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			double angle = -Math.PI / 2.0D + Math.PI * 2.0D * i / count;
			nodes.add(p((int) Math.round(Math.cos(angle) * radius),
					(int) Math.round(Math.sin(angle) * radius)));
		}
		return nodes;
	}

	private static BlockPos p(int x, int z) {
		return new BlockPos(x, 1, z);
	}

	public record Act(String id, String prompt, List<BlockPos> nodes, List<Integer> order) {
		public Act {
			nodes = List.copyOf(nodes);
			order = List.copyOf(order);
		}

		public int expectedNode(int step) {
			if (order.isEmpty() || step >= order.size()) return -2;
			return order.get(step);
		}

		public boolean accepts(int step, int touchedNode) {
			int expected = expectedNode(step);
			return expected == -1 || expected == touchedNode;
		}

		public boolean complete(int step) {
			return step >= order.size();
		}
	}
}
