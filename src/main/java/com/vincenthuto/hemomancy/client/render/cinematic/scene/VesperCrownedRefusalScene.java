package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import net.minecraft.world.entity.Entity;

/**
 * Vesper arrives: a long look down the arena at the Archon on his mount, then the camera drops low and close as the
 * mount rears, forelegs pawing, and his name lands at the height of it. It settles, and the view comes back as he
 * starts to fight.
 */
public final class VesperCrownedRefusalScene implements BossScene {
	private static final int LENGTH = VesperCombatRules.ARRIVAL_TICKS;
	private static final int PEAK = VesperCombatRules.ARRIVAL_REAR_PEAK;
	private static final int FREE = LENGTH - HANDBACK;
	private static final TitleCard CARD = TitleCard.of("vesper_crowned_refusal", PEAK, 40, 0xE00018);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 0, 16.0F, 3.0F, 2.4F),
			new Key(VesperCombatRules.ARRIVAL_REAR_START, 22, 10.0F, 1.4F, 3.0F), new Key(PEAK, 28, 8.5F, 0.9F, 3.6F),
			new Key(VesperCombatRules.ARRIVAL_REAR_HOLD, 16, 10.5F, 1.8F, 3.0F), new Key(LENGTH, 0, 13.0F, 2.8F, 2.4F));

	@Override
	public int length() {
		return LENGTH;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.window(t, 0, 8, FREE, 10);
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
		return CameraOffsets.shake(BossSceneTracks.keys(t, PEAK - 1, 0, PEAK, 1.8F, PEAK + 16, 0));
	}
}
