package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SkillProgressClientCacheTest {

	@Test
	void synchronizedProgressAdvancesTheRenderRevision() {
		long before = SkillProgressClientCache.revision();

		SkillProgressClientCache.apply(new ListTag());

		assertEquals(before + 1L, SkillProgressClientCache.revision());
	}
}
