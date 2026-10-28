package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaCombatRules;
import net.minecraft.world.entity.Entity;

/**
 * The arterial fluke rearing out of the ichor: the camera sits low on the surface while the vessel throbs under it,
 * climbs with the head as it rears, and the name lands at the top of the rear. Two heartbeats thud through the view
 * before it, one hard beat on it, and the view comes back as the fluke drops to hunt.
 */
public final class PhlegethosomaScene implements BossScene {
	private static final int LENGTH = PhlegethosomaCombatRules.INTRO_TICKS;
	private static final int FREE = LENGTH - HANDBACK;
	private static final int REAR = PhlegethosomaCombatRules.INTRO_REAR_AT;
	private static final int[] BEATS = { 6, 18 };
	private static final TitleCard CARD = TitleCard.of("phlegethosoma", REAR, 40, 0xC84330);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 28, 9.0F, 0.4F, 0.3F),
			new Key(REAR, 14, 6.5F, 1.8F, 1.6F), new Key(LENGTH, 6, 7.5F, 1.4F, 1.0F));

	@Override
	public int length() {
		return REAR + 40;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 0, 8, 1, FREE, 1, FREE + 10, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < FREE;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < FREE;
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
		return t < FREE;
	}

	@Override
	public CameraOffsets offsets(float t) {
		float jolt = BossSceneTracks.keys(t, REAR - 1, 0, REAR, 1.4F, REAR + 12, 0);
		for (int beat : BEATS)
			jolt = Math.max(jolt, BossSceneTracks.keys(t, beat - 1, 0, beat, 0.6F, beat + 6, 0));
		return CameraOffsets.shake(jolt);
	}
}
