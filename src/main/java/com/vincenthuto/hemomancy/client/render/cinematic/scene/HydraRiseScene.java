package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead;
import net.minecraft.world.entity.Entity;

/**
 * Hydra's largest head rising for a player the Head-tender has called it for (Brined Court Track 10). The camera
 * starts low on the player's side of the chimney and climbs with the head as it rises, and Hydra's name lands at the
 * top of the rise. As the neck arches over, the shot hands back to the player's own view, tipped up, so they watch the
 * crown come down over them from underneath. No boss bar: Hydra is never fought.
 */
public final class HydraRiseScene implements BossScene {
	private static final int RISE = HydraRisingHead.RISE;
	private static final int OVER = RISE + HydraRisingHead.REACH / 2;
	private static final int HELD = RISE + HydraRisingHead.REACH + HydraRisingHead.HOLD;
	private static final int FREE = HELD - HANDBACK;
	// The camera stays well clear of the vent water at the chimney's foot, where the shot read dark (2026-10-30 review).
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 30, 8.5F, 3.5F, 2.0F),
			new Key(RISE, 42, 12.0F, 6.5F, HydraRisingHead.RISE_HEIGHT - 1.0F), new Key(OVER, 50, 12.5F, 8.5F, 9.0F));
	private static final TitleCard CARD = TitleCard.of("hydra", RISE - 14, 70, 0xE8642C);

	@Override
	public int length() {
		return HELD + 10;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 10, 1, FREE, 1, FREE + 10, 0);
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
	public CinematicShot shot() {
		return SHOT;
	}

	@Override
	public boolean shotActive(float t) {
		return t < OVER;
	}

	@Override
	public CameraOffsets offsets(float t) {
		// Back in their own eyes, the player is tipped up toward the crown overhead, but not so far that its tentacles fill
		// the frame: the crown's rim and the neck behind it both stay in view. The vent's rumble under it all.
		float pitch = BossSceneTracks.keys(t, OVER, -24, HELD - 14, -20, FREE + 10, 0);
		float shake = BossSceneTracks.keys(t, 0, 0.25F, RISE - 6, 0.6F, RISE + 4, 0.15F, HELD, 0);
		return new CameraOffsets(0.0F, t < OVER ? 0.0F : pitch, 0.0F, shake);
	}
}
