package com.vincenthuto.hemomancy.common.mission.shared;

import java.util.Set;

/** The single defining chapter proof which certifies each public Harbinger rank. */
public enum HarbingerChapterMilestone {
	FIRST_BLOODCRAFT(2, "First Bloodcraft"),
	FIRST_SEPARATION(3, "The First Separation"),
	WOVEN_VESSEL(4, "The Woven Vessel"),
	VEIN_MASON(5, "The Vein-Mason"),
	COVENANT_WRITTEN_IN_PLACE(6, "A Covenant Written in Place"),
	LIVING_COVENANT(7, "The Living Covenant");

	private final int targetDegree;
	private final String chapterName;

	HarbingerChapterMilestone(int targetDegree, String chapterName) {
		this.targetDegree = targetDegree;
		this.chapterName = chapterName;
	}

	public int targetDegree() {
		return targetDegree;
	}

	public String chapterName() {
		return chapterName;
	}

	public static HarbingerChapterMilestone requiredForTargetDegree(int targetDegree) {
		for (HarbingerChapterMilestone milestone : values()) {
			if (milestone.targetDegree == targetDegree) {
				return milestone;
			}
		}
		return null;
	}

	public static boolean isRankUnlocked(int targetDegree, Set<HarbingerChapterMilestone> completed) {
		HarbingerChapterMilestone required = requiredForTargetDegree(targetDegree);
		return required == null || completed.contains(required);
	}
}
