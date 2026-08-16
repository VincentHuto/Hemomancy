package com.vincenthuto.hemomancy.common.mission;

public final class RedTaxonomyRewardRules {

	private RedTaxonomyRewardRules() {}

	public static boolean grantsFirstFieldVial(int recordedSpecimensBefore, boolean uniqueSubmission) {
		return uniqueSubmission && recordedSpecimensBefore == 0;
	}
}
