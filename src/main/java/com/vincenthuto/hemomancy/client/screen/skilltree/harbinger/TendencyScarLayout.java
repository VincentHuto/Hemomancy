package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Places the short cerebral-scar chains just beyond their manipulation families. */
final class TendencyScarLayout {
	private static final int FAMILY_CLEARANCE = 70;
	private static final int TIER_SPACING = 50;
	private static final int SIDE_BRANCH_OFFSET = 40;
	private static final int AUTHORED_CENTER_X = 480;
	private static final int AUTHORED_CENTER_Y = 480;
	private static final int AUTHORED_FIRST_RADIUS = 200;

	private TendencyScarLayout() {
	}

	static Map<String, Point> arrange(int centerX, int centerY,
			Map<EnumBloodTendency, Integer> outerRadiusByTendency, List<Node> nodes) {
		Map<String, Point> points = new LinkedHashMap<>();
		for (Node node : nodes) {
			double angle = Math.toRadians(-90.0 + node.tendency().ordinal() * 45.0);
			int authoredX = node.authoredX() != null ? node.authoredX() : fallbackX(node, angle);
			int authoredY = node.authoredY() != null ? node.authoredY() : fallbackY(node, angle);
			int shift = outerRadiusByTendency.getOrDefault(node.tendency(), 0)
					+ FAMILY_CLEARANCE - AUTHORED_FIRST_RADIUS;
			int x = centerX + authoredX - AUTHORED_CENTER_X + (int) Math.round(Math.cos(angle) * shift);
			int y = centerY + authoredY - AUTHORED_CENTER_Y + (int) Math.round(Math.sin(angle) * shift);
			points.put(node.id(), new Point(x, y));
		}
		return Map.copyOf(points);
	}

	private static int fallbackX(Node node, double angle) {
		int radius = AUTHORED_FIRST_RADIUS + Math.max(0, node.tier() - 1) * TIER_SPACING;
		return AUTHORED_CENTER_X + (int) Math.round(Math.cos(angle) * radius)
				+ (node.sideBranch() ? (int) Math.round(-Math.sin(angle) * SIDE_BRANCH_OFFSET) : 0);
	}

	private static int fallbackY(Node node, double angle) {
		int radius = AUTHORED_FIRST_RADIUS + Math.max(0, node.tier() - 1) * TIER_SPACING;
		return AUTHORED_CENTER_Y + (int) Math.round(Math.sin(angle) * radius)
				+ (node.sideBranch() ? (int) Math.round(Math.cos(angle) * SIDE_BRANCH_OFFSET) : 0);
	}

	record Node(String id, EnumBloodTendency tendency, int tier, boolean sideBranch,
			Integer authoredX, Integer authoredY) {
		Node(String id, EnumBloodTendency tendency, int tier, boolean sideBranch) {
			this(id, tendency, tier, sideBranch, null, null);
		}
	}

	record Point(int x, int y) {
	}
}
