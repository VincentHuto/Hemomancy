package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEndingRules;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * Seraphae contained: the bars come in as her anchors flare, the HUD steps aside while the chains reel her home, and
 * the card lands as she sinks. No camera move and no input lock: the room is the player's to watch, and the light going
 * out of it is the scene. The length follows her ending, so the bars leave as she is gone.
 */
public final class SeraphaeContainedScene implements BossScene {
	private static final int LENGTH = SeraphaeEndingRules.LENGTH;
	private static final int CARD_START = SeraphaeEndingRules.SINK_START;
	private static final int RULE = 0xFFF8EE;
	private final TitleCard card = TitleCard.of("seraphae_contained", CARD_START, LENGTH - CARD_START - 8, RULE);

	@Override
	public int length() {
		return LENGTH;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 12, 1, LENGTH - 16, 1, LENGTH, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < LENGTH - 16;
	}

	@Override
	@Nullable
	public TitleCard card(@Nullable Entity boss) {
		return card;
	}

	/** The ending runs on the server whatever the client shows, so a skip only drops the bars. */
	@Override
	public boolean skippable(float t) {
		return true;
	}
}
