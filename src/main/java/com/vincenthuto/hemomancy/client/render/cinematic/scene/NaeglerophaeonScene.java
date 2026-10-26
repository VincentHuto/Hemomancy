package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot;
import com.vincenthuto.hemomancy.client.render.cinematic.CinematicShot.Key;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
import net.minecraft.world.entity.Entity;

/**
 * The Vagrant Mind's predator gathering out of a synaptic node: the camera holds on the node while nerve-lightning
 * coils into it, eases in as the eel pours out, and its name lands as it does. It turns on the player, discharges, and
 * the view comes back as it starts to hunt.
 */
public final class NaeglerophaeonScene implements BossScene {
	private static final int LENGTH = NaeglerophaeonCombatRules.INTRO_TICKS;
	private static final int FREE = LENGTH - HANDBACK;
	private static final int EMERGE = NaeglerophaeonCombatRules.INTRO_EMERGE_AT;
	private static final int DISCHARGE = NaeglerophaeonCombatRules.INTRO_DISCHARGE_AT;
	private static final TitleCard CARD = TitleCard.of("naeglerophaeon", EMERGE, 40, 0xE02870);
	private static final CinematicShot SHOT = CinematicShot.toward(new Key(0, 32, 11.0F, 2.5F, 0.4F),
			new Key(EMERGE, 18, 8.5F, 1.6F, 0.8F), new Key(LENGTH, 4, 9.5F, 1.8F, 0.9F));

	@Override
	public int length() {
		return EMERGE + 40;
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
		float jolt = Math.max(BossSceneTracks.keys(t, EMERGE - 1, 0, EMERGE, 1.2F, EMERGE + 10, 0),
				BossSceneTracks.keys(t, DISCHARGE - 1, 0, DISCHARGE, 1.6F, DISCHARGE + 14, 0));
		return CameraOffsets.shake(jolt);
	}
}
