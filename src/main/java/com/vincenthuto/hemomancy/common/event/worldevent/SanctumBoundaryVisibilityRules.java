package com.vincenthuto.hemomancy.common.event.worldevent;

public final class SanctumBoundaryVisibilityRules {
	private SanctumBoundaryVisibilityRules() {
	}

	public static SanctumBoundaryRelation classifyViewer(boolean memberOfOwnerBloodline, int viewerDegree) {
		return classifyViewer(memberOfOwnerBloodline, viewerDegree, false);
	}

	public static SanctumBoundaryRelation classifyViewer(boolean memberOfOwnerBloodline, int viewerDegree,
			boolean viewerHasBegunUnstainedPath) {
		if (viewerHasBegunUnstainedPath) {
			return SanctumBoundaryRelation.OUTSIDER;
		}
		if (memberOfOwnerBloodline) {
			return SanctumBoundaryRelation.MEMBER;
		}
		return viewerDegree > 5 ? SanctumBoundaryRelation.RIVAL_ELDER : SanctumBoundaryRelation.OUTSIDER;
	}

	public static SanctumBoundaryRelation strongerInsideEffect(SanctumBoundaryRelation current,
			SanctumBoundaryRelation candidate) {
		return priority(candidate) > priority(current) ? candidate : current;
	}

	private static int priority(SanctumBoundaryRelation relation) {
		return switch (relation) {
			case OUTSIDER -> 3;
			case RIVAL_ELDER -> 2;
			case MEMBER -> 1;
		};
	}
}
