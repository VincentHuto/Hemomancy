package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.boss.MycophantGlimpse;
import com.vincenthuto.hemomancy.client.data.MycophantIntroClientState;
import com.vincenthuto.hemomancy.client.screen.overlay.MycophantMyceliumOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * The parts of being hunted by the Mycophant that play outside her boss scenes: the hunt's watching pulses and glimpse,
 * and the Pulp Bloom veil. The capture and the waking (pose, camera, input, lids, fibers) are boss scenes now
 * ({@code MycophantCaptureScene}, {@code MycophantWakingScene}); all timing comes from {@link MycophantIntroClientState}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class MycophantIntroClientEvents {
	private static final MycophantIntroClientState STATE = MycophantIntroClientState.INSTANCE;

	private MycophantIntroClientEvents() {
	}

	@SubscribeEvent
	public static void tickClient(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || minecraft.player == null) {
			STATE.start(MycophantIntroClientState.CLEAR, 0);
			MycophantGlimpse.clear();
			return;
		}
		STATE.tick();
		MycophantGlimpse.tick();
	}

	public static void renderGlimpse(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
		MycophantGlimpse.render(event.getPoseStack(), event.getCamera().getPosition(),
				event.getPartialTick().getGameTimeDeltaPartialTick(true));
	}

	/** Drawn above the HUD from GuiLayerRegistrar. */
	public static void renderOverlay(GuiGraphics graphics, int width, int height, float partial) {
		if (!STATE.isActive()) return;
		Minecraft minecraft = Minecraft.getInstance();
		float time = (minecraft.player == null ? 0 : minecraft.player.tickCount) + partial;
		// every quad below shares one flush instead of a draw call each
		graphics.drawManaged(() -> MycophantMyceliumOverlay.renderAmbient(graphics, width, height, partial, time, STATE));
	}
}
