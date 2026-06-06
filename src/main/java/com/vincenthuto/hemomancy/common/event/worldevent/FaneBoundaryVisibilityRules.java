package com.vincenthuto.hemomancy.common.event.worldevent;

public final class FaneBoundaryVisibilityRules {
	private FaneBoundaryVisibilityRules() {
	}

	public static FaneBoundaryRelation classifyViewer(boolean memberOfOwnerBloodline, int viewerDegree) {
		return classifyViewer(memberOfOwnerBloodline, viewerDegree, false);
	}

	public static FaneBoundaryRelation classifyViewer(boolean memberOfOwnerBloodline, int viewerDegree,
			boolean viewerHasBegunUnstainedPath) {
		if (viewerHasBegunUnstainedPath) {
			return FaneBoundaryRelation.OUTSIDER;
		}
		if (memberOfOwnerBloodline) {
			return FaneBoundaryRelation.MEMBER;
		}
		if (viewerDegree > 5) {
			return FaneBoundaryRelation.RIVAL_ELDER;
		}
		return viewerDegree >= 1 ? FaneBoundaryRelation.OUTSIDER : FaneBoundaryRelation.MUNDANE_OUTSIDER;
	}

	public static FaneBoundaryRelation strongerInsideEffect(FaneBoundaryRelation current,
			FaneBoundaryRelation candidate) {
		return priority(candidate) > priority(current) ? candidate : current;
	}

	private static int priority(FaneBoundaryRelation relation) {
		return switch (relation) {
			case OUTSIDER -> 4;
			case RIVAL_ELDER -> 3;
			case MUNDANE_OUTSIDER -> 2;
			case MEMBER -> 1;
		};
	}
}
