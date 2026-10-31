package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules;
import net.minecraft.world.entity.Entity;

/**
 * The Bloom Warden uprooting (Brined Court Track 10, Tier 2): the camera starts low at the tier floor as the roots
 * tear free, rises with the Warden as it stands out of its moss, and holds on it as the fronds open, where its name
 * lands. The view comes back as the opening ends and the Warden can fight.
 */
public final class BloomWardenScene implements BossScene {
	private static final int OPEN = BloomWardenRules.INTRO - HANDBACK;
	private static final TitleCard CARD = TitleCard.of("bloom_warden", 22, 46, 0xFCC448);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 35, 3.6F, 0.4F, 0.5F),
			new Key(24, 18, 4.6F, 1.1F, 1.3F), new Key(OPEN, 6, 5.4F, 1.6F, 1.8F));

	@Override
	public int length() {
		return BloomWardenRules.INTRO + 30;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 8, 1, OPEN, 1, OPEN + 12, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < OPEN;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < OPEN;
	}

	@Nullable
	@Override
	public TitleCard card(@Nullable Entity boss) {
		return CARD;
	}

	@Override
	public CinematicShot shot() {
		return SHOT;
	}

	@Override
	public boolean shotActive(float t) {
		return t < OPEN;
	}

	@Override
	public CameraOffsets offsets(float t) {
		// The roots tearing out of the tier floor.
		return CameraOffsets.shake(BossSceneTracks.keys(t, 0, 0.5F, 10, 0.9F, 24, 0.2F, 32, 0));
	}
}
