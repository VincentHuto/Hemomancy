package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules.Beat;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * A rematch, kept short: the camera starts at her face while she tends the brood facing away, swings round with her
 * turn and holds on the roar, then gives the player their view back twenty ticks before she rushes.
 */
public final class MycophantRematchScene implements BossScene {
	private static final int ROAR = MycophantIntroRules.tickOf(Beat.ROAR, true);
	private static final int RUSH = MycophantIntroRules.tickOf(Beat.RUSH, true);
	/** The shot hands back the view here, well before she can act. */
	private static final int SHOT_END = RUSH - 20;
	private static final TitleCard CARD = TitleCard.of("mycophant", ROAR, 40, MycophantScenes.AMBER);
	// her spawn facing (+Z) is away from the player; yaw 0 is her face, 180 the player's side
	private static final CinematicShot SHOT = CinematicShot.of(new CinematicShot.Key(0, 30, 9.0F, 4.0F, 2.6F),
			new CinematicShot.Key(14, 110, 9.0F, 3.6F, 2.6F), new CinematicShot.Key(ROAR, 165, 8.0F, 3.0F, 2.8F));

	@Override
	public int length() {
		return RUSH + 1;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 1, SHOT_END, 1, SHOT_END + 12, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < SHOT_END;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < SHOT_END;
	}

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
		return t < SHOT_END;
	}

	@Override
	public CameraOffsets offsets(float t) {
		return CameraOffsets.shake(MycophantScenes.STATE.shakeAmplitude(t - Mth.floor(t)));
	}
}
