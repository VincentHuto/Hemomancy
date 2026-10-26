package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules;
import net.minecraft.world.entity.Entity;

/**
 * The cure tearing the infection out of Annetta. The player watches her drink with their own eyes; at the tear the
 * camera takes over, low and close as the infection pulls free, and draws back as it rises. Its name lands once it
 * stands, and the view comes back then: it cannot strike while it rises.
 */
public final class LatentInfectionScene implements BossScene {
	private static final int TEAR = LatentCombatRules.EMERGE_HIDDEN;
	private static final int RISEN = LatentCombatRules.EMERGE_END;
	private static final int SPORE = 0xC8AA60;
	private static final TitleCard CARD = TitleCard.of("latent_annetta_infection", RISEN, 40, SPORE);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(TEAR, 22, 4.2F, 0.6F, 0.8F),
			new Key(RISEN, 6, 6.6F, 1.6F, 1.6F));

	@Override
	public int length() {
		return RISEN + 40;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, TEAR - 12, 0, TEAR - 2, 1, RISEN - HANDBACK, 1, RISEN - HANDBACK + 12, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t >= TEAR - 6 && t < RISEN - HANDBACK;
	}

	@Override
	public boolean inputLocked(float t) {
		return t >= TEAR && t < RISEN - HANDBACK;
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
		return t >= TEAR && t < RISEN - HANDBACK;
	}

	@Override
	public CameraOffsets offsets(float t) {
		return CameraOffsets.shake(BossSceneTracks.keys(t, TEAR - 1, 0, TEAR, 1.8F, TEAR + 18, 0));
	}
}
