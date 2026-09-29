package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.FirstDrawsProgress;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstDrawsProgressTest {
	@Test
	void fiveCollectionsNeedThreeCommonSpecies() {
		var cow = ResourceLocation.parse("minecraft:cow");
		var pig = ResourceLocation.parse("minecraft:pig");
		var sheep = ResourceLocation.parse("minecraft:sheep");
		var progress = FirstDrawsProgress.EMPTY;
		for (int i = 0; i < 5; i++) progress = progress.record(cow);
		assertEquals(5, progress.samples());
		assertFalse(progress.complete());
		progress = progress.record(pig).record(sheep);
		assertEquals(7, progress.samples());
		assertTrue(progress.complete());
	}

	@Test
	void unsupportedSourcesNeverCount() {
		var progress = FirstDrawsProgress.EMPTY.record(ResourceLocation.parse("minecraft:zombie"))
				.record(ResourceLocation.parse("minecraft:player"))
				.record(ResourceLocation.parse("hemomancy:crimson_doe"));
		assertEquals(0, progress.samples());
		assertFalse(progress.complete());
	}
}
