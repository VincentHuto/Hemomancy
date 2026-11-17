package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * Succession's yield (chunk 15 T14): the camera comes down low beside the Ringmaster as he kneels on the sawdust, holds
 * on the bare staff once the topper is off, and gives the view back as he speaks. It runs as long as the beat.
 */
public final class CircusYieldScene implements BossScene {
	private static final int OPEN = CircusRingmasterEntity.YIELD_TICKS - HANDBACK;
	private static final TitleCard CARD = TitleCard.of("circus_yield", 10, 40, 0xE8DCC4);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 35, 4.2F, 1.6F, 1.2F),
			new Key(24, 20, 3.0F, 0.9F, 0.9F), new Key(OPEN, 10, 2.6F, 0.7F, 0.8F));

	@Override
	public int length() {
		return CircusRingmasterEntity.YIELD_TICKS;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 8, 1, OPEN, 1, OPEN + 8, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < OPEN;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < OPEN;
	}

	@Override
	public boolean skippable(float t) {
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
}
