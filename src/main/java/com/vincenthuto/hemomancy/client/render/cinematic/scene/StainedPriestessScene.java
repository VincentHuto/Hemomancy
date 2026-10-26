package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules;
import net.minecraft.world.entity.Entity;

/**
 * The Stained Priestess rising out of Annetta's pool: the camera starts low at the blood as she pushes up, rises with
 * her through the kneel and the lancea's drawing, and holds on her scream as she stands, where her name lands. The view
 * comes back when her emergence ends and she can fight.
 */
public final class StainedPriestessScene implements BossScene {
	private static final int EMERGE = PriestessCombatRules.EMERGE_LENGTH - HANDBACK;
	private static final int STAND = PriestessCombatRules.STAND_END;
	private static final int GLOW = 0xDC2834;
	private static final TitleCard CARD = TitleCard.of("stained_priestess", STAND, 40, GLOW);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 25, 4.6F, 0.35F, 0.4F),
			new Key(PriestessCombatRules.KNEEL_END, 12, 5.2F, 1.0F, 1.2F),
			new Key(STAND, 0, 6.4F, 1.4F, 2.1F));

	@Override
	public int length() {
		return STAND + 40;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 8, 1, EMERGE, 1, EMERGE + 12, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < EMERGE;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < EMERGE;
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
		return t < EMERGE;
	}

	@Override
	public CameraOffsets offsets(float t) {
		// the scream as she stands
		return CameraOffsets.shake(BossSceneTracks.keys(t, STAND - 1, 0, STAND, 1.6F, STAND + 14, 0));
	}
}
