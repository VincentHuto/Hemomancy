package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.boss.MycophantGlimpse;
import com.vincenthuto.hemomancy.client.boss.MycophantIntroClientState;
import com.vincenthuto.hemomancy.client.screen.overlay.MycophantMyceliumOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The player's side of being taken by the Mycophant: held to their knees and then down during the capture, lying,
 * kneeling and rising at the waking, the dazed drift and the shakes, input held while bound, each fiber's snap, the
 * hunt's glimpse of her, and the mycelium screen layer ({@link MycophantMyceliumOverlay}). All timing comes from
 * {@link MycophantIntroClientState}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MycophantIntroClientEvents {
	private static final MycophantIntroClientState STATE = MycophantIntroClientState.INSTANCE;
	private static boolean posed;

	private MycophantIntroClientEvents() {
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (minecraft.level == null || player == null) {
			STATE.start(MycophantIntroClientState.CLEAR, 0);
			MycophantGlimpse.clear();
			posed = false;
			return;
		}
		STATE.tick();
		MycophantGlimpse.tick();
		holdBody(player);
		if (STATE.waking()) {
			for (int f = 0; f < MycophantIntroClientState.FIBERS; f++) {
				if (STATE.elapsed() != MycophantIntroClientState.fiberSnapTick(f)) continue;
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WEEPING_VINES_BREAK,
						0.55F + (f % 4) * 0.08F, 0.7F));
			}
		}
	}

	/** Lying in the nectar, then kneeling, then up: the eye height follows the pose, so the view rises with them. */
	private static void holdBody(LocalPlayer player) {
		int stage = STATE.bodyStage();
		if (stage == MycophantIntroClientState.STAND) {
			if (posed) player.setForcedPose(null);
			posed = false;
			return;
		}
		player.setForcedPose(stage == MycophantIntroClientState.LIE ? Pose.SWIMMING : Pose.CROUCHING);
		posed = true;
	}

	@SubscribeEvent
	public static void tiltCamera(ViewportEvent.ComputeCameraAngles event) {
		if (!STATE.isActive()) return;
		float partial = (float) event.getPartialTick();
		float time = (Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount) + partial;
		// a slow dazed drift, and a quicker shake made of two detuned waves so it never reads as a metronome
		float sway = STATE.swayDegrees(partial);
		float shake = STATE.shakeAmplitude(partial);
		float shakeRoll = shake * (Mth.sin(time * 1.31F + STATE.seed() * 0.37F) * 0.6F + Mth.sin(time * 2.17F) * 0.4F);
		float shakePitch = shake * (Mth.cos(time * 1.53F) * 0.6F + Mth.sin(time * 2.71F + 1.0F) * 0.4F);
		event.setRoll(event.getRoll() + STATE.rollDegrees(partial) + sway * Mth.sin(time * 0.07F) + shakeRoll * 0.5F);
		event.setPitch(event.getPitch() - STATE.pitchDegrees(partial) + sway * 0.6F * Mth.sin(time * 0.05F + 1.3F)
				+ shakePitch * 0.45F);
		event.setYaw(event.getYaw() + sway * 0.8F * Mth.sin(time * 0.043F + 2.1F));
	}

	@SubscribeEvent
	public static void holdInput(MovementInputUpdateEvent event) {
		if (!STATE.inputBound()) return;
		Input input = event.getInput();
		input.forwardImpulse = 0.0F;
		input.leftImpulse = 0.0F;
		input.up = input.down = input.left = input.right = false;
		input.jumping = false;
		input.shiftKeyDown = false;
	}

	@SubscribeEvent
	public static void renderGlimpse(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
		MycophantGlimpse.render(event.getPoseStack(), event.getCamera().getPosition(),
				event.getPartialTick().getGameTimeDeltaPartialTick(true));
	}

	/** Drawn above the HUD from ClientEvents' GUI layer registration. */
	public static void renderOverlay(GuiGraphics graphics, int width, int height, float partial) {
		if (!STATE.isActive()) return;
		Minecraft minecraft = Minecraft.getInstance();
		float time = (minecraft.player == null ? 0 : minecraft.player.tickCount) + partial;
		// every quad below shares one flush instead of a draw call each
		graphics.drawManaged(() -> MycophantMyceliumOverlay.render(graphics, width, height, partial, time, STATE));
	}
}
