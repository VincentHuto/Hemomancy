package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.client.render.world.AnimusMortemRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.rite.RiteSurface;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * A thin, pulsing thread of blood standing over each required support socket whose sigil still sleeps, so the
 * caster can see what the circle still lacks without a panel. It is the anchor jet's thread at a smaller scale.
 */
public final class RiteSocketMarkerRenderer {
	private static final RiteSurface.View VIEW = new RiteSurface.View();
	private static final RiteSurface.Path PATH = new RiteSurface.Path();
	private static final double MAX_DISTANCE_SQR = 64.0D * 64.0D;
	private static final double HEIGHT = 1.4D;
	private static final int STATIONS = 7;

	private RiteSocketMarkerRenderer() {
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		double time = mc.level.getGameTime() + partialTick;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		VertexConsumer blood = null;
		for (ActiveRiteData.RiteEntry rite : ActiveRiteClientData.getActiveRites()) {
			if (rite.isUnstained()) continue;
			for (ActiveRiteData.SocketMarker marker : rite.getSocketMarkers()) {
				Vec3 at = Vec3.atBottomCenterOf(marker.pos());
				if (camera.distanceToSqr(at) > MAX_DISTANCE_SQR) continue;
				if (blood == null) {
					AnimusMortemRenderTypes.begin(time);
					blood = buffers.getBuffer(AnimusMortemRenderTypes.ANIMUS);
				}
				drawThread(poseStack, blood, mc, camera, at, time, marker.sigil().hashCode());
			}
		}
		if (blood != null) buffers.endBatch(AnimusMortemRenderTypes.ANIMUS);
	}

	private static void drawThread(PoseStack poseStack, VertexConsumer consumer, Minecraft mc, Vec3 camera, Vec3 at,
			double time, int seed) {
		VIEW.set(camera.x, camera.y, camera.z, at.x, at.y, at.z, mc.gameRenderer.getMainCamera().rotation());
		poseStack.pushPose();
		poseStack.translate(at.x - camera.x, at.y - camera.y, at.z - camera.z);
		Matrix4f matrix = poseStack.last().pose();
		double beat = CardinalRiteMath.heartbeat((float) (time + (seed & 15)));
		double height = HEIGHT * (0.75D + 0.35D * beat);
		PATH.clear();
		for (int s = 0; s < STATIONS; s++) {
			double t = s / (STATIONS - 1.0D);
			double waver = Math.sin(time * 0.5D + t * 7.0D + seed) * 0.02D * t;
			PATH.add(waver, t * height, waver * 0.6D, 0.03D * (1.0D - 0.6D * t));
		}
		RiteSurface.ribbon(matrix, consumer, VIEW, PATH, RiteSurface.RIBBON, seed, RiteSurface.TINT, 0.85F);
		RiteSurface.card(matrix, consumer, VIEW, 0.0D, height, 0.0D, 0.04D, 0.04D, RiteSurface.FOCUS, seed + 1,
				RiteSurface.CORE, 0.5F + 0.4F * (float) beat);
		poseStack.popPose();
	}
}
