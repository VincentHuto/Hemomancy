package com.vincenthuto.hemomancy.render.tile;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hemomancy.tile.BlockEntityVisceralRecaller;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class RenderVisceralRecaller implements BlockEntityRenderer<BlockEntityVisceralRecaller> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderVisceralRecaller(BlockEntityRendererProvider.Context p_173636_) {
	}

	@SuppressWarnings("unused")
	@Override
	public void render(BlockEntityVisceralRecaller te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Vector3f startVec = new Vector3f(te.getBlockPos().getX(), te.getBlockPos().getY(), te.getBlockPos().getZ());
	//	drawCenter(matrixStackIn, te, startVec);
	}

	@SuppressWarnings("unused")
	private void drawCenter(PoseStack stack, BlockEntityVisceralRecaller te, Vector3f from) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();
		int cx = (int) from.x(), cz = (int) from.z();
		float rotAngle = -90f;
		int iconDiameter = 1;
		int diameter = 1;
		float spikeBaseWidth = 0.235f;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			int cx1 = (int) (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
			int cx2 = (int) (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);
			int cz1 = (int) (cz + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
			int cz2 = (int) (cz + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);
			double depthDist = ((iconDiameter - diameter) * affs.get(tend) + diameter);
			int lx = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist);
			int lz = (int) (cz + Math.sin(Math.toRadians(rotAngle)) * depthDist);
			int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cz1, cz2) - Math.min(cz1, cz2))
					/ 2f);
			Vector3f vec1 = new Vector3f(lx, from.y() + 1, lz);
			Vector3f vec2 = new Vector3f(cx1, from.y() + 1, cz1);
			Vector3f vec22 = new Vector3f(cx2, from.y() + 1, cz2);
			fracLine(stack, vec1, vec2, tend.getColor(), displace, 1.1);
			fracLine(stack, vec1, vec22, tend.getColor(), displace, 1.1);
			fracLine(stack, vec2, vec1, tend.getColor(), displace, 0.8);
			fracLine(stack, vec22, vec1, tend.getColor(), displace, 0.8);
			rotAngle += 45;
		}
	}

	public static void fracLine(PoseStack matrix, Vector3f from, Vector3f to, ParticleColor color, int displace,
			double detail) {
		if (displace < detail) {
			final Minecraft mc = Minecraft.getInstance();
			Level world = mc.level;
			MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			long gameTime = world.getGameTime();
			double v = gameTime * 0.04;
			VertexConsumer builder = buffer.getBuffer(RenderTypeInit.LASER_MAIN_ADDITIVE);
			Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
			matrix.translate(-view.x(), -view.y(), -view.z());
			Matrix4f positionMatrix = matrix.last().pose();
			float diffZ = to.z() + .5f - from.z();
			Vector3f sortPos = new Vector3f(from.x(), from.y(), from.z());
			drawLaser(builder, positionMatrix, from, to, ParticleColor.RED, 1f, 0.125f, v, v + diffZ * -5.5, sortPos);
			buffer.endBatch(RenderTypeInit.LASER_MAIN_ADDITIVE);
			
		} else {
			Random rand = new Random();
			float mid_x = (to.x() + from.x()) / 2;
			float mid_y = (to.y() + from.y()) / 2;
			mid_x = (int) (mid_x + (rand.nextFloat() - 0.25) * displace * 0.25);
			mid_y = (int) (mid_y + (rand.nextFloat() - 0.25) * displace * 0.25);
			Vector3f midVec = new Vector3f(mid_x, mid_y, from.z() + 1);
			fracLine(matrix, midVec, to, color, (displace / 2), detail);

		}
	}

	public static Vector3f adjustBeamToEyes(Vector3f from, Vector3f to, Vector3f sortPos) {
		Player player = Minecraft.getInstance().player;
		Vector3f P = new Vector3f((float) player.getX() - sortPos.x(), player.getEyeHeight() - sortPos.y(),
				(float) player.getZ() - sortPos.z());
		Vector3f PS = from.copy();
		PS.sub(P);
		Vector3f SE = to.copy();
		SE.sub(from);
		Vector3f adjustedVec = PS.copy();
		adjustedVec.cross(SE);
		adjustedVec.normalize();
		return adjustedVec;
	}

	public static void drawLaser(VertexConsumer builder, Matrix4f positionMatrix, Vector3f from, Vector3f to,
			ParticleColor color, float alpha, float thickness, double v1, double v2, Vector3f sortPos) {
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
		float r = color.getRed();
		float g = color.getGreen();
		float b = color.getBlue();
		builder.vertex(positionMatrix, p1.x(), p1.y(), p1.z()).color(r, g, b, alpha).uv(1, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p3.x(), p3.y(), p3.z()).color(r, g, b, alpha).uv(1, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p4.x(), p4.y(), p4.z()).color(r, g, b, alpha).uv(0, (float) v2)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
		builder.vertex(positionMatrix, p2.x(), p2.y(), p2.z()).color(r, g, b, alpha).uv(0, (float) v1)
				.overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).endVertex();
	}

	public static boolean canAdd(BlockPos sourcePos, BlockPos targetPos) {
		SetMultimap<BlockPos, BlockPos> lasers = HashMultimap.create();
		if (!lasers.containsKey(targetPos))
			return true;
		Set<BlockPos> tempSet = lasers.get(targetPos);
		if (!tempSet.contains(sourcePos))
			return true;
		return false;
	}

}
