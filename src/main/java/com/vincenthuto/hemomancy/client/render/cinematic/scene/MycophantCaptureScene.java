package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.BossScene;
import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The Gardens root claim: hyphae creep across the view, the player is pulled to their knees and down, and their eyes
 * close; the dark holds across the transfer into the Nursery until the waking takes over. The server binds the player
 * throughout, so it can't be skipped.
 */
public final class MycophantCaptureScene implements BossScene {
	@Override
	public int length() {
		return MycophantIntroClientState.CAPTURE_LENGTH;
	}

	@Override
	public boolean inputLocked(float t) {
		return t < length();
	}

	@Override
	public boolean keepsInputAfterSkip(float t) {
		return t < length();
	}

	@Override
	public boolean skippable(float t) {
		return false;
	}

	@Override
	public boolean keepsAcrossDimensions() {
		return true;
	}

	@Override
	public CameraOffsets offsets(float t) {
		return MycophantScenes.offsets(t);
	}

	@Override
	public void tick(int t) {
		MycophantScenes.holdBody();
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
