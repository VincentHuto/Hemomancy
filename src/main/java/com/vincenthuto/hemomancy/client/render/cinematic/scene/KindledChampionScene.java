package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules;
import net.minecraft.world.entity.Entity;

/**
 * The Kindled Champion stepping out of the elders to meet a challenger (Brined Court Track 10, Tier 2): the camera
 * comes round its flank above the vent water, climbs its neck as it rears, and holds under the crown as it flares,
 * where its name lands. The chimneys rumble behind. The view comes back as the opening ends.
 */
public final class KindledChampionScene implements BossScene {
	private static final int OPEN = KindledChampionRules.INTRO - HANDBACK;
	private static final TitleCard CARD = TitleCard.of("kindled_champion", 34, 56, 0xEE621E);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 70, 6.5F, 2.0F, 1.2F),
			new Key(30, 40, 6.0F, 2.6F, 2.4F), new Key(OPEN, 15, 5.8F, 2.2F, 2.8F));

	@Override
	public int length() {
		return KindledChampionRules.INTRO + 30;
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
		// The chimneys' rumble, and the crown flaring as it rears.
		return CameraOffsets.shake(BossSceneTracks.keys(t, 0, 0.2F, 30, 0.3F, 34, 1.1F, 48, 0.2F, OPEN, 0));
	}
}
