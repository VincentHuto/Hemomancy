package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.MycophantFrame;
import com.vincenthuto.hemomancy.client.screen.overlay.boss.frame.StainedPriestessFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Each frame reads its boss's phase from what the client already has: the bar's name key, colour and progress. */
class BossBarFramePhaseTest {
	@Test
	void mycophantPhaseFromJoiningName() {
		Component joining = Component.translatable("entity.hemomancy.mycophant.joining")
				.append(Component.literal("  40%").withStyle(ChatFormatting.GOLD));
		assertEquals(2, MycophantFrame.phaseOf(joining));
		assertEquals(1, MycophantFrame.phaseOf(Component.translatable("entity.hemomancy.mycophant")));
	}

	@Test
	void priestessFlaresBelowHalf() {
		assertEquals(1, StainedPriestessFrame.phaseOf(0.51F));
		assertEquals(2, StainedPriestessFrame.phaseOf(0.5F));
	}

	@Test
	void theAnnettaArcAndMycophantHaveTheirOwnArt() {
		assertInstanceOf(MycophantFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_MYCOPHANT));
		assertEquals("AnnettaKnowlesFrame", BossBarFrames.get(BossPresentationIds.FRAME_ANNETTA_KNOWLES).getClass().getSimpleName());
		assertInstanceOf(StainedPriestessFrame.class, BossBarFrames.get(BossPresentationIds.FRAME_STAINED_PRIESTESS));
		assertEquals("LatentInfectionFrame", BossBarFrames.get(BossPresentationIds.FRAME_LATENT_INFECTION).getClass().getSimpleName());
	}
}
