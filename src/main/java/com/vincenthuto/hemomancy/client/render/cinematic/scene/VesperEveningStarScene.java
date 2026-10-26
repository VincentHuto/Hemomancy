package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import net.minecraft.world.entity.Entity;

/**
 * Vesper out of the cocoon as the Evening Star. The cocoon delivers him complete and he fights at once, so this only
 * names him over the burst: a jolt and his new name. The camera and the player's hands stay theirs.
 */
public final class VesperEveningStarScene implements BossScene {
	private static final int LENGTH = 64;
	private static final TitleCard CARD = TitleCard.of("vesper_evening_star", 4, LENGTH - 6, 0xF2F7FF);

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
		return CameraOffsets.shake(BossSceneTracks.keys(t, 0, 1.4F, 16, 0));
	}

	/** A card holds nothing back, so there is nothing to skip. */
	@Override
	public boolean skippable(float t) {
		return false;
	}
}
