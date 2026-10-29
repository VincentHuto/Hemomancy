package com.vincenthuto.hemomancy.common.encounter;

/** Where the Rite of Visitation sets an Apotheos down. */
public final class VisitationRules {
	private VisitationRules() {}

	/** On the node only when it still beats and nothing has been built over it; otherwise near it on safe ground. */
	public static boolean landOnNode(boolean nodePresent, boolean feetClear, boolean headClear) {
		return nodePresent && feetClear && headClear;
	}
}
