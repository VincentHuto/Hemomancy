package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * The Ringmaster's finale opening: the camera starts on the sawdust and tilts up through the pavilion to find him
 * perched in the rafters, then pulls back to take in the whole big top as his name lands and the first knife
 * comes down. The view comes back as the rafters phase opens.
 */
public final class CircusRingmasterScene implements BossScene {
	private static final int OPEN = CircusRingmasterEntity.INTRO_TICKS - HANDBACK;
	private static final TitleCard CARD = TitleCard.of("circus_ringmaster", 20, 48, 0xC02A34);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 8, 2.4F, -2.2F, 0.3F),
			new Key(22, 30, 3.8F, 0.6F, 0.9F), new Key(OPEN, 42, 6.2F, 1.4F, 1.6F));

	@Override
	public int length() {
		return CircusRingmasterEntity.INTRO_TICKS + 30;
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
		// The first knife striking the sawdust.
		return CameraOffsets.shake(BossSceneTracks.keys(t, 0, 0, OPEN - 14, 0, OPEN - 12, 0.7F, OPEN - 4, 0));
	}
}
