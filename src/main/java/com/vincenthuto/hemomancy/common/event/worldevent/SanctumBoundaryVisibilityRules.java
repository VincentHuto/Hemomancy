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
		if (viewerDegree > 5) {
			return SanctumBoundaryRelation.RIVAL_ELDER;
		}
		return viewerDegree >= 1 ? SanctumBoundaryRelation.OUTSIDER : SanctumBoundaryRelation.MUNDANE_OUTSIDER;
	}

	public static SanctumBoundaryRelation strongerInsideEffect(SanctumBoundaryRelation current,
			SanctumBoundaryRelation candidate) {
		return priority(candidate) > priority(current) ? candidate : current;
	}

	private static int priority(SanctumBoundaryRelation relation) {
		return switch (relation) {
			case OUTSIDER -> 4;
			case RIVAL_ELDER -> 3;
			case MUNDANE_OUTSIDER -> 2;
			case MEMBER -> 1;
		};
	}
}
