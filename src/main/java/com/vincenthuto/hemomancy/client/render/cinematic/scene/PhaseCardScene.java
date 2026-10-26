package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import net.minecraft.world.entity.Entity;

/**
 * A boss turning into its next phase: its renamed title card and a jolt, nothing more. No letterbox: mid-fight it
 * would cover the hotbar and the boss bars. The camera, the HUD and the player's hands stay theirs.
 */
public final class PhaseCardScene implements BossScene {
	public static final int LENGTH = 50;
	private final TitleCard card;

	public PhaseCardScene(String cardId, int ruleColor) {
		this.card = TitleCard.of(cardId, 4, LENGTH - 6, ruleColor);
	}

	@Override
	public int length() {
		return LENGTH;
	}

	@Override
	public CameraOffsets offsets(float t) {
		return CameraOffsets.shake(BossSceneTracks.keys(t, 0, 1.0F, 12, 0));
	}

	@Override
	public TitleCard card(@Nullable Entity boss) {
		return card;
	}

	/** A card holds nothing back, so there is nothing to skip. */
	@Override
	public boolean skippable(float t) {
		return false;
	}
}
