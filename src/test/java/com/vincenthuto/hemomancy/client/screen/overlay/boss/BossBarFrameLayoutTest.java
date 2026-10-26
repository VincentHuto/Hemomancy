package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossBarFrameLayoutTest {
	@Test
	void everyFrameIdHasArt() {
		for (ResourceLocation id : BossPresentationIds.FRAMES) assertNotNull(BossBarFrames.get(id), id.toString());
	}

	@Test
	void stackedFramesDoNotOverlap() {
		for (ResourceLocation id : BossPresentationIds.FRAMES) {
			BossBarFrame frame = BossBarFrames.get(id);
			assertTrue(frame.increment() >= BossBarFramePainter.SPAN + 2, id + " leaves room for the next bar");
		}
	}

	@Test
	void notchesAreSortedInsideTheBar() {
		for (ResourceLocation id : BossPresentationIds.FRAMES) {
			float[] notches = BossBarFrames.get(id).notches();
			float last = 0.0F;
			for (float notch : notches) {
				assertTrue(notch > last && notch < 1.0F, id + " notch " + notch);
				last = notch;
			}
		}
	}
}
