package com.vincenthuto.hemomancy.common.capability.player.volume;

import org.joml.Matrix4f;
import org.joml.Vector3d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.registry.RenderTypeInit;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class RenderBloodLaser {

	public static Vector3 adjustBeamToEyes(Vector3 from, Vector3 to, Vector3 sortPos) {
		Player player = Minecraft.getInstance().player;
		Vector3 P = new Vector3((float) player.getX() - sortPos.x, (float) player.getEyeY() - sortPos.y,
				(float) player.getZ() - sortPos.z);

		Vector3 PS = from.copy();
		PS.subtract(P);
		Vector3 SE = to.copy();
		SE.subtract(from);

		Vector3 adjustedVec = PS.copy();
		adjustedVec.crossProduct(SE);
		adjustedVec.normalize();
		return adjustedVec;
	}

	public static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix, Vector3 from, Vector3 to, float r,
			float g, float b, float alpha, float thickness, double v1, double v2, Vector3 sortPos) {
		Vector3 adjustedVec = adjustBeamToEyes(from, to, sortPos);
		adjustedVec.multiply(thickness); //
		// Determines how thick the beam is V
		Vector3 p1 = from.copy();
		p1.add(adjustedVec);
		Vector3 p2 = from.copy();
		p2.subtract(adjustedVec);
		Vector3 p3 = to.copy();
		p3.add(adjustedVec);
		Vector3 p4 = to.copy();
		p4.subtract(adjustedVec);
		builder.vertex(positionMatrix, p1.x, p1.y, p1.z).color(r, g, b, alpha).uv(1, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p3.x, p3.y, p3.z).color(r, g, b, alpha).uv(1, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p4.x, p4.y, p4.z).color(r, g, b, alpha).uv(0, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p2.x, p2.y, p2.z).color(r, g, b, alpha).uv(0, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
	}

	public static void drawLasers(PoseStack matrixStackIn, Vector3d to, Vector3d from, float r, float g, float b) {
		final Minecraft mc = Minecraft.getInstance();
		Level world = mc.level;
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);
		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.LASER_MAIN_ADDITIVE);
		matrixStackIn.pushPose();
		matrixStackIn.translate(from.x, from.y, from.z);
		float diffX = (float) (to.x - from.x);
		float diffY = (float) (to.y - from.y);
		float diffZ = (float) (to.z - from.z);
		Vector3 startLaser = new Vector3(0, 0, 0);
		Vector3 endLaser = new Vector3(diffX, diffY, diffZ);
		Vector3 sortPos = new Vector3((float) from.x, (float) from.y, (float) from.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();
		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, 1f, 0.025f, v, v + diffY * -5.5, sortPos);
		matrixStackIn.popPose();

		matrixStackIn.popPose();
		buffer.endBatch(RenderTypeInit.LASER_MAIN_ADDITIVE);
	}

	public static void renderLaser(RenderLevelStageEvent event, Player player, float ticks) {
		Vector3 centerVec = Vector3.fromEntityCenter(player);
//		if (player.getEffect(PotionInit.blood_binding.get()) != null) {
//			if (player.level().isClientSide) {
//				Vector3 playerPos = Vector3.fromEntityCenter(player);
//				Vector3d playerVec = new Vector3d(playerPos.x, playerPos.y, playerPos.z);
//				Vector3d part1 = new Vector3d(centerVec.x + Math.sin(player.tickCount * 0.1 + Math.toRadians(30)),
//						centerVec.y, centerVec.z + Math.cos(player.tickCount * 0.1 + Math.toRadians(30)));
//				Vector3d part2 = new Vector3d(centerVec.x - Math.sin(player.tickCount * 0.1 + Math.toRadians(90)),
//						centerVec.y, centerVec.z - Math.cos(player.tickCount * 0.1 + Math.toRadians(90)));
//				Vector3d part3 = new Vector3d(centerVec.x - (Math.sin(player.tickCount * 0.1 + Math.toRadians(-30))),
//						centerVec.y, centerVec.z - (Math.cos(player.tickCount * 0.1 + Math.toRadians(-30))));
//
//				drawLasers(event.getPoseStack(), playerVec, part1, 255 / 255f, 255 / 255f, 0);
//				drawLasers(event.getPoseStack(), playerVec, part2, 255 / 255f, 0 / 255f, 0);
//				drawLasers(event.getPoseStack(), playerVec, part3, 255 / 255f, 0 / 255f, 255 / 255);
//
//			}
//		}
	}

}
