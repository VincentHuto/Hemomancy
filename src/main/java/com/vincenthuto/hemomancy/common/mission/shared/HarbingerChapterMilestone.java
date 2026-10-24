package com.vincenthuto.hemomancy.common.mission.shared;

import java.util.Locale;
import java.util.Set;

import net.minecraft.network.chat.Component;

/** The single defining chapter proof which certifies each public Harbinger rank. */
public enum HarbingerChapterMilestone {
	FIRST_BLOODCRAFT(2),
	FIRST_SEPARATION(-1),
	FIRST_DISTILLATION(3),
	WOVEN_VESSEL(4),
	VEIN_MASON(5),
	COVENANT_WRITTEN_IN_PLACE(6),
	LIVING_COVENANT(7);

	private final int targetDegree;

	HarbingerChapterMilestone(int targetDegree) {
		this.targetDegree = targetDegree;
	}

	public int targetDegree() {
		return targetDegree;
	}

	public Component chapterName() {
		return Component.translatable("milestone.hemomancy.chapter." + name().toLowerCase(Locale.ROOT));
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
