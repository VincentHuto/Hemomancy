package com.vincenthuto.hemomancy.client.render.world;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Renders glowing red boundary circles in the world for active cardinal rites.
 * Uses the same beam-segment rendering approach as the MorphlingIncubator blood ring.
 */
public class CardinalRiteBoundaryRenderer {

	private static final int SEGMENTS = 64;

	public static void render(PoseStack poseStack, float partialTick) {
		List<ActiveRiteClientData.RiteEntry> rites = ActiveRiteClientData.getActiveRites();
		if (rites.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		float currentTime = mc.level.getGameTime() + partialTick;

		for (ActiveRiteClientData.RiteEntry rite : rites) {
			drawBoundaryCircle(poseStack, buffer, rite, currentTime);
		}

		buffer.endBatch(RenderTypeInit.RECALLER_BEAM_CORE);
		buffer.endBatch(RenderTypeInit.RECALLER_BEAM_GLOW);
	}

	private static void drawBoundaryCircle(PoseStack stack, MultiBufferSource buffer,
			ActiveRiteClientData.RiteEntry rite, float currentTime) {

		BlockPos center = rite.getCenter();
		// Boundary radius = riteSize / 2 + 1 (the +2 total buffer)
		double radius = rite.getRiteSize() / 2.0 + 1.0;
		double cx = center.getX() + 0.5;
		double cy = center.getY() + 0.1; // Slightly above ground
		double cz = center.getZ() + 0.5;

		// Pulsing brightness
		double pulse = (Math.sin(currentTime * 0.1) + 1.0) * 0.5;
		float coreR = (float) (0.6f + 0.2f * pulse);
		float coreG = 0.04f;
		float coreB = 0.04f;
		float glowR = (float) (0.7f + 0.3f * pulse);
		float glowG = 0.1f;
		float glowB = 0.06f;

		for (int i = 0; i < SEGMENTS; i++) {
			float angle1 = (360f / SEGMENTS) * i;
			float angle2 = (360f / SEGMENTS) * (i + 1);

			// Subtle wave for organic feel
			double wave1 = Math.sin(Math.toRadians(angle1 * 3) + currentTime * 0.08) * 0.02;
			double wave2 = Math.sin(Math.toRadians(angle2 * 3) + currentTime * 0.08) * 0.02;

			double x1 = cx + Math.cos(Math.toRadians(angle1)) * radius;
			double z1 = cz + Math.sin(Math.toRadians(angle1)) * radius;
			double x2 = cx + Math.cos(Math.toRadians(angle2)) * radius;
			double z2 = cz + Math.sin(Math.toRadians(angle2)) * radius;

			Vec3 from = new Vec3(x1, cy + wave1, z1);
			Vec3 to = new Vec3(x2, cy + wave2, z2);

			drawBeamSegment(stack, buffer, from, to, coreR, coreG, coreB, 1.0f, 0.035f);
			drawBeamSegmentGlow(stack, buffer, from, to, glowR, glowG, glowB, 0.45f, 0.08f);
		}
	}

	// ========================== BEAM DRAWING (same as MorphlingIncubatorRenderer) ==========================

	private static void drawBeamSegment(PoseStack matrixStackIn, MultiBufferSource buffer, Vec3 from, Vec3 to,
			float r, float g, float b, float alpha, float thickness) {
		final Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);

		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.RECALLER_BEAM_CORE);
		matrixStackIn.pushPose();
		matrixStackIn.translate(to.x, to.y, to.z);

		float diffX = (float) (from.x - to.x);
		float diffY = (float) (from.y - to.y);
		float diffZ = (float) (from.z - to.z);

		Vector3f startLaser = new Vector3f(0, 0, 0);
		Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
		Vector3f sortPos = new Vector3f((float) to.x, (float) to.y, (float) to.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();

		drawLaser(builder, positionMatrix, endLaser, startLaser, 0, 0, 0, alpha * 0.6f, thickness * 1.5f,
				v, v + diffY * -5.5, new Vector3f(sortPos.x(), sortPos.y() - 0.05f, sortPos.z()));
		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness,
				v, v + diffY * -5.5, sortPos);

		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}

	private static void drawBeamSegmentGlow(PoseStack matrixStackIn, MultiBufferSource buffer, Vec3 from, Vec3 to,
			float r, float g, float b, float alpha, float thickness) {
		final Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);

		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.RECALLER_BEAM_GLOW);
		matrixStackIn.pushPose();
		matrixStackIn.translate(to.x, to.y, to.z);

		float diffX = (float) (from.x - to.x);
		float diffY = (float) (from.y - to.y);
		float diffZ = (float) (from.z - to.z);

		Vector3f startLaser = new Vector3f(0, 0, 0);
		Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
		Vector3f sortPos = new Vector3f((float) to.x, (float) to.y, (float) to.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();

		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, alpha, thickness,
				v, v + diffY * -5.5, sortPos);

		matrixStackIn.popPose();
		matrixStackIn.popPose();
	}

	private static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to,
			float r, float g, float b, float alpha, float thickness, double v1, double v2, Vector3f sortPos) {
		Player player = Minecraft.getInstance().player;
		if (player == null) return;

		Vector3f SE = new Vector3f(to);
		SE.sub(from);
		float seLen = SE.length();
		if (seLen < 1e-6f) return;

		Vector3f P = new Vector3f((float) player.getX() - sortPos.x(), (float) player.getEyeY() - sortPos.y(),
				(float) player.getZ() - sortPos.z());
		Vector3f PS = new Vector3f(from);
		PS.sub(P);
		Vector3f adjustedVec = new Vector3f(PS);
		adjustedVec.cross(SE);
		float len = adjustedVec.length();
		if (len < 1e-6f) {
			adjustedVec.set(0, 1, 0);
			adjustedVec.cross(SE);
			len = adjustedVec.length();
			if (len < 1e-6f) {
				adjustedVec.set(1, 0, 0);
				adjustedVec.cross(SE);
				len = adjustedVec.length();
				if (len < 1e-6f) return;
			}
		}
		adjustedVec.mul(thickness / len);

		Vector3f perp2 = new Vector3f(adjustedVec);
		Vector3f seNorm = new Vector3f(SE);
		seNorm.normalize();
		perp2.cross(seNorm);
		float len2 = perp2.length();
		if (len2 > 1e-6f) {
			perp2.mul(thickness / len2);
		} else {
			perp2.set(adjustedVec);
		}

		drawQuad(builder, positionMatrix, from, to, adjustedVec, r, g, b, alpha, v1, v2);
		drawQuad(builder, positionMatrix, from, to, perp2, r, g, b, alpha, v1, v2);
	}

	private static void drawQuad(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to,
			Vector3f offset, float r, float g, float b, float alpha, double v1, double v2) {
		Vector3f p1 = new Vector3f(from).add(offset);
		Vector3f p2 = new Vector3f(from).sub(offset);
		Vector3f p3 = new Vector3f(to).add(offset);
		Vector3f p4 = new Vector3f(to).sub(offset);

		builder.vertex(positionMatrix, p1.x(), p1.y(), p1.z()).color(r, g, b, alpha).uv(1, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p3.x(), p3.y(), p3.z()).color(r, g, b, alpha).uv(1, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p4.x(), p4.y(), p4.z()).color(r, g, b, alpha).uv(0, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p2.x(), p2.y(), p2.z()).color(r, g, b, alpha).uv(0, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
	}
}
