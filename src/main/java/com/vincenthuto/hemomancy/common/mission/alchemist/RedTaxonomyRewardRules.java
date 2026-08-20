package com.vincenthuto.hemomancy.common.mission.alchemist;

public final class RedTaxonomyRewardRules {

	private RedTaxonomyRewardRules() {}

	public static boolean grantsFirstFieldVial(int recordedSpecimensBefore, boolean uniqueSubmission) {
		return uniqueSubmission && recordedSpecimensBefore == 0;
	}
}
