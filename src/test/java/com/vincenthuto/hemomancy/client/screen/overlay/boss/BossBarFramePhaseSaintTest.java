package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.HemorathFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.PutricielFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.SeraphaeFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.VelorumFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BossBarFramePhaseSaintTest {
	@Test
	void hemorathEmptiedFromName() {
		assertEquals(2, HemorathFrame.phaseOf(Component.translatable("message.hemomancy.hemorath.saint_hemorath_the_vessel_empties")));
		assertEquals(1, HemorathFrame.phaseOf(Component.translatable("entity.hemomancy.hemorath")));
		assertEquals(3, HemorathFrame.phaseOf(Component.translatable("message.hemomancy.hemorath.saint_hemorath_the_last_thirst")));
	}

	@Test
	void velorumFrostsInThreePhases() {
		assertEquals(1, VelorumFrame.phaseOf(0.7F));
		assertEquals(2, VelorumFrame.phaseOf(0.6F));
		assertEquals(2, VelorumFrame.phaseOf(0.3F));
		assertEquals(3, VelorumFrame.phaseOf(0.25F));
	}

	@Test
	void saintFramesHaveTheirOwnArt() {
		assertInstanceOf(HemorathFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_HEMORATH));
		assertInstanceOf(PutricielFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_PUTRICIEL));
		assertInstanceOf(VelorumFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_VELORUM));
		assertInstanceOf(SeraphaeFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_SERAPHAE));
	}
}
