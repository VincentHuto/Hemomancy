package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules;
import net.minecraft.world.entity.Entity;

/**
 * Annetta, bitten, turns on the Harbinger: her name lands on her scream, and the scream jolts the view. The Tooth
 * Pecks the jar released hunts any player without Clarity, so this scene takes nothing from the player: no camera, no
 * held input, no letterbox over the hotbar. She is untouchable while she arms herself
 * ({@link AnnettaEncounterRules#HARBINGER_INTRO_TICKS}).
 */
public final class AnnettaKnowlesScene implements BossScene {
	private static final int LENGTH = AnnettaEncounterRules.HARBINGER_INTRO_TICKS;
	private static final int SCREAM = AnnettaEncounterRules.HARBINGER_SCREAM_AT;
	private static final TitleCard CARD = TitleCard.of("annetta_knowles", SCREAM, LENGTH - SCREAM, 0xD6EAF6);

	@Override
	public int length() {
		return LENGTH;
	}

	@Override
	public TitleCard card(@Nullable Entity boss) {
		return CARD;
	}

	@Override
	public CameraOffsets offsets(float t) {
		return CameraOffsets.shake(BossSceneTracks.keys(t, SCREAM - 1, 0, SCREAM, 1.2F, SCREAM + 12, 0));
	}

	/** Nothing is held back, so there is nothing to skip. */
	@Override
	public boolean skippable(float t) {
		return false;
	}
}
