package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import net.minecraft.nbt.ListTag;

public final class SkillProgressClientCache {
	private static final SkillProgress CURRENT = new SkillProgress();
	private static long revision;

	private SkillProgressClientCache() {}

	public static SkillProgress current() {
		return CURRENT;
	}

	public static long revision() {
		return revision;
	}

	public static void apply(ListTag data) {
		CURRENT.readSyncTag(data);
		revision++;
	}
}
