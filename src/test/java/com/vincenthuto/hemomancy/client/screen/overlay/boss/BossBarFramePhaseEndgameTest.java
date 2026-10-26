package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.NaeglerophaeonFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.VesperCrownedRefusalFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.VesperEveningStarFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BossBarFramePhaseEndgameTest {
	@Test
	void naeglerophaeonPhaseFromColor() {
		assertEquals(1, NaeglerophaeonFrame.phaseOf(BossEvent.BossBarColor.RED));
		assertEquals(2, NaeglerophaeonFrame.phaseOf(BossEvent.BossBarColor.PURPLE));
		assertEquals(3, NaeglerophaeonFrame.phaseOf(BossEvent.BossBarColor.YELLOW));
		assertEquals(4, NaeglerophaeonFrame.phaseOf(BossEvent.BossBarColor.WHITE));
	}

	@Test
	void crownedRefusalCountsOpenWounds() {
		assertEquals(1, VesperCrownedRefusalFrame.phaseOf(0.8F));
		assertEquals(2, VesperCrownedRefusalFrame.phaseOf(0.72F));
		assertEquals(3, VesperCrownedRefusalFrame.phaseOf(0.3F));
		assertEquals(4, VesperCrownedRefusalFrame.phaseOf(0.1F));
	}

	@Test
	void eveningStarActsAndShame() {
		Component name = Component.translatable("entity.hemomancy.vesper_evening_star");
		assertEquals(1, VesperEveningStarFrame.phaseOf(name, 0.9F));
		assertEquals(2, VesperEveningStarFrame.phaseOf(name, 0.5F));
		assertEquals(3, VesperEveningStarFrame.phaseOf(name, 0.2F));
		Component shamed = Component.translatable(
				"message.hemomancy.vesper_the_evening_star_entity.vesper_the_shamed_evening_star");
		assertEquals(VesperEveningStarFrame.SHAMED, VesperEveningStarFrame.phaseOf(shamed, 0.9F));
	}

	@Test
	void endgameFramesHaveTheirOwnArt() {
		assertInstanceOf(NaeglerophaeonFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_NAEGLEROPHAEON));
		assertInstanceOf(VesperCrownedRefusalFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_VESPER_CROWNED_REFUSAL));
		assertInstanceOf(VesperEveningStarFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_VESPER_EVENING_STAR));
	}
}
