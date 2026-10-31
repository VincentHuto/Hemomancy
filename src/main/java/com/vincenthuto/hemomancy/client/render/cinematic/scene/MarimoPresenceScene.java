package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import net.minecraft.world.entity.Entity;

/**
 * The first time a player comes down to Marimo (Brined Court Track 10). Marimo is his moss, not a creature, so there
 * is no shot: the player's own view settles and dips toward the pool while the letterbox closes, the moss pulses round
 * them, and his name lands. No boss bar, and nothing is held once the card has gone.
 */
public final class MarimoPresenceScene implements BossScene {
	private static final int LENGTH = 120;
	private static final int FREE = LENGTH - HANDBACK - 12;
	private static final TitleCard CARD = TitleCard.of("marimo", 22, 72, 0xD8B464);

	@Override
	public int length() {
		return LENGTH;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 14, 1, FREE, 1, FREE + 12, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < FREE;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < FREE;
	}

	@Nullable
	@Override
	public TitleCard card(@Nullable Entity boss) {
		return CARD;
	}

	@Override
	public CameraOffsets offsets(float t) {
		// A slow look down toward the moss island, and the faintest sway, as of breathing.
		float pitch = BossSceneTracks.keys(t, 0, 0, 30, 14, FREE, 14, FREE + 12, 0);
		float roll = (float) Math.sin(t * 0.07F) * 0.8F * BossSceneTracks.window(t, 0, 20, FREE + 12, 12);
		return new CameraOffsets(roll, pitch, 0.0F, 0.0F);
	}
}
