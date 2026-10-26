package com.vincenthuto.hemomancy.client.render.cinematic.scene;

import com.vincenthuto.hemomancy.client.render.cinematic.CameraOffsets;
import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import com.vincenthuto.hemomancy.client.screen.overlay.MycophantMyceliumOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

/**
 * What the Mycophant's capture and waking scenes share: the camera tilt, drift and shake, the forced pose (lying,
 * kneeling, standing), and the mycelium art, all timed by {@link MycophantIntroClientState}.
 */
final class MycophantScenes {
	static final MycophantIntroClientState STATE = MycophantIntroClientState.INSTANCE;
	static final int AMBER = 0xFFA828;

	private static boolean posed;

	private MycophantScenes() {
	}

	/** The dazed roll, the pitch of lying on one's back or being pulled down, a slow drift, and the shake. */
	static CameraOffsets offsets(float t) {
		float partial = t - Mth.floor(t);
		float sway = STATE.swayDegrees(partial);
		return new CameraOffsets(STATE.rollDegrees(partial) + sway * Mth.sin(t * 0.07F),
				-STATE.pitchDegrees(partial) + sway * 0.6F * Mth.sin(t * 0.05F + 1.3F),
				sway * 0.8F * Mth.sin(t * 0.043F + 2.1F), STATE.shakeAmplitude(partial));
	}

	static void overlay(GuiGraphics graphics, int width, int height, float t, float time) {
		MycophantMyceliumOverlay.renderScene(graphics, width, height, t - Mth.floor(t), time, STATE);
	}

	/** Lying in the nectar, then kneeling, then up: the eye height follows the pose, so the view rises with them. */
	static void holdBody() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		int stage = STATE.bodyStage();
		if (stage == MycophantIntroClientState.STAND) {
			release();
			return;
		}
		player.setForcedPose(stage == MycophantIntroClientState.LIE ? Pose.SWIMMING : Pose.CROUCHING);
		posed = true;
	}

	static void release() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (posed && player != null) player.setForcedPose(null);
		posed = false;
	}
}
