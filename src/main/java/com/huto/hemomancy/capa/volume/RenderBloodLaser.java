package com.huto.hemomancy.capa.volume;

import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.init.RenderTypeInit;
import com.huto.hemomancy.util.Vector3;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class RenderBloodLaser {

	public static void renderLaser(RenderWorldLastEvent event, PlayerEntity player, float ticks) {
		Vector3 centerVec = Vector3.fromEntityCenter(player);
		if (player.getActivePotionEffect(PotionInit.blood_binding.get()) != null) {
			if (player.world.isRemote) {
				Vector3 playerPos = Vector3.fromEntityCenter(player);
				Vector3d playerVec = new Vector3d(playerPos.x, playerPos.y, playerPos.z);
				Vector3d part1 = new Vector3d(centerVec.x + Math.sin(player.ticksExisted * 0.1 + Math.toRadians(30)),
						centerVec.y, centerVec.z + Math.cos(player.ticksExisted * 0.1 + Math.toRadians(30)));
				Vector3d part2 = new Vector3d(centerVec.x - Math.sin(player.ticksExisted * 0.1 + Math.toRadians(90)),
						centerVec.y, centerVec.z - Math.cos(player.ticksExisted * 0.1 + Math.toRadians(90)));
				Vector3d part3 = new Vector3d(centerVec.x - (Math.sin(player.ticksExisted * 0.1 + Math.toRadians(-30))),
						centerVec.y, centerVec.z - (Math.cos(player.ticksExisted * 0.1 + Math.toRadians(-30))));

				drawLasers(event.getMatrixStack(), playerVec, part1, 255 / 255f, 255 / 255f, 0);
				drawLasers(event.getMatrixStack(), playerVec, part2, 255 / 255f, 0 / 255f, 0);
				drawLasers(event.getMatrixStack(), playerVec, part3, 255 / 255f, 0 / 255f, 255 / 255);

			}
		}
	}

	public static void drawLasers(MatrixStack matrixStackIn, Vector3d to, Vector3d from, float r, float g, float b) {
		final Minecraft mc = Minecraft.getInstance();
		World world = mc.world;
		IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vector3d view = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
		matrixStackIn.push();
		matrixStackIn.translate(-view.getX(), -view.getY(), -view.getZ());
		IVertexBuilder builder = buffer.getBuffer(RenderTypeInit.LASER_MAIN_ADDITIVE);
		matrixStackIn.push();
		matrixStackIn.translate(from.getX(), from.getY(), from.getZ());
		float diffX = (float) (to.getX() - from.getX());
		float diffY = (float) (to.getY() - from.getY());
		float diffZ = (float) (to.getZ() - from.getZ());
		Vector3f startLaser = new Vector3f(0, 0, 0);
		Vector3f endLaser = new Vector3f(diffX, diffY, diffZ);
		Vector3f sortPos = new Vector3f((float) from.getX(), (float) from.getY(), (float) from.getZ());
		Matrix4f positionMatrix = matrixStackIn.getLast().getMatrix();
		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, 1f, 0.025f, v, v + diffY * -5.5, sortPos);
		matrixStackIn.pop();

		matrixStackIn.pop();
		buffer.finish(RenderTypeInit.LASER_MAIN_ADDITIVE);
	}

	public static Vector3f adjustBeamToEyes(Vector3f from, Vector3f to, Vector3f sortPos) {
		PlayerEntity player = Minecraft.getInstance().player;
		Vector3f P = new Vector3f((float) player.getPosX() - sortPos.getX(),
				(float) player.getPosYEye() - sortPos.getY(), (float) player.getPosZ() - sortPos.getZ());

		Vector3f PS = from.copy();
		PS.sub(P);
		Vector3f SE = to.copy();
		SE.sub(from);

		Vector3f adjustedVec = PS.copy();
		adjustedVec.cross(SE);
		adjustedVec.normalize();
		return adjustedVec;
	}

	public static void drawLaser(IVertexBuilder builder, Matrix4f positionMatrix, Vector3f from, Vector3f to, float r,
			float g, float b, float alpha, float thickness, double v1, double v2, Vector3f sortPos) {
		Vector3f adjustedVec = adjustBeamToEyes(from, to, sortPos);
		adjustedVec.mul(thickness); // Determines how thick the beam is
		Vector3f p1 = from.copy();
		p1.add(adjustedVec);
		Vector3f p2 = from.copy();
		p2.sub(adjustedVec);
		Vector3f p3 = to.copy();
		p3.add(adjustedVec);
		Vector3f p4 = to.copy();
		p4.sub(adjustedVec);
		builder.pos(positionMatrix, p1.getX(), p1.getY(), p1.getZ()).color(r, g, b, alpha).tex(1, (float) v1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).endVertex();
		builder.pos(positionMatrix, p3.getX(), p3.getY(), p3.getZ()).color(r, g, b, alpha).tex(1, (float) v2)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).endVertex();
		builder.pos(positionMatrix, p4.getX(), p4.getY(), p4.getZ()).color(r, g, b, alpha).tex(0, (float) v2)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).endVertex();
		builder.pos(positionMatrix, p2.getX(), p2.getY(), p2.getZ()).color(r, g, b, alpha).tex(0, (float) v1)
				.overlay(OverlayTexture.NO_OVERLAY).lightmap(15728880).endVertex();
	}

}