package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.BossSceneTracks;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.render.cinematic.TitleCard;
import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules.Beat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

/**
 * The waking in the Nursery, through the player's own eyes: lids opening in heavy blinks under a letterbox, the body
 * lying, kneeling and rising while the fibers pull taut and snap strand by strand, control back at CONTROL, and her
 * name over the roar. The HUD stays hidden while the player is bound.
 */
public final class MycophantWakingScene implements BossScene {
	private static final int CONTROL = MycophantIntroRules.tickOf(Beat.CONTROL, false);
	private static final int ROAR = MycophantIntroRules.tickOf(Beat.ROAR, false);
	private static final TitleCard CARD = TitleCard.of("mycophant", ROAR, 40, MycophantScenes.AMBER);

	@Override
	public int length() {
		return MycophantIntroClientState.WAKING_LENGTH;
	}

	@Override
	public float letterbox(float t) {
		return BossSceneTracks.keys(t, 0, 1, CONTROL, 1, CONTROL + 12, 0);
	}

	@Override
	public boolean hudHidden(float t) {
		return t < CONTROL;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < CONTROL;
	}

	@Override
	public boolean keepsInputAfterSkip(float t) {
		return t < CONTROL;
	}

	@Override
	public TitleCard card(@Nullable Entity boss) {
		return CARD;
	}

	@Override
	public CameraOffsets offsets(float t) {
		return MycophantScenes.offsets(t);
	}

	@Override
	public void tick(int t) {
		MycophantScenes.holdBody();
		if (!MycophantScenes.STATE.waking()) return;
		for (int f = 0; f < MycophantIntroClientState.FIBERS; f++) {
			if (MycophantScenes.STATE.elapsed() != MycophantIntroClientState.fiberSnapTick(f)) continue;
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WEEPING_VINES_BREAK,
					0.55F + (f % 4) * 0.08F, 0.7F));
		}
	}

	@Override
	public void end() {
		MycophantScenes.release();
	}

	@Override
	public void overlay(GuiGraphics graphics, int width, int height, float t, float time) {
		MycophantScenes.overlay(graphics, width, height, t, time);
	}
}
