package com.vincenthuto.hemomancy.render.tile;

import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hemomancy.tile.BlockEntityVisceralRecaller;
import com.vincenthuto.hutoslib.client.ClientUtils;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class RenderVisceralRecaller implements BlockEntityRenderer<BlockEntityVisceralRecaller> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderVisceralRecaller(BlockEntityRendererProvider.Context p_173636_) {
	}

	@Override
	public void render(BlockEntityVisceralRecaller te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		// Vec3 startVec = new Vec3(te.getBlockPos().getX(), te.getBlockPos().getY(),
		// te.getBlockPos().getZ());
		// drawCenter(matrixStackIn, bufferIn, te, startVec, combinedLightIn,
		// combinedOverlayIn);
		// renderLine(startVec, endVec, partialTicks, matrixStackIn, bufferIn,
		// combinedOverlayIn);
//		WorldRenderUtils.renderBeam(ClientUtils.getWorld(), ClientUtils.getPartialTicks(), matrixStackIn, bufferIn,
//				combinedLightIn, endVec, startVec, 1, ParticleColor.BLOOD, RenderTypeInit.RADIANT_RENDER_TYPE);
	}

	private void drawCenter(PoseStack stack, MultiBufferSource bufferIn, BlockEntityVisceralRecaller te, Vec3 from,
			int combinedLightIn, int combinedOverlayIn) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();
		double centerOffset = 0.5;
		double cx = 0;
		double cy = 0;
		float rotAngle = -90f;
		int diameter = 15;
		float spikeBaseWidth = 23.5f;
		double xOff = from.x;
		double yOff = from.z;
		double yLevel = from.y;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {

			double cx1 = (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + xOff;
			double cx2 = (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + xOff;
			double cy1 = (cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + yOff;
			double cy2 = (cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + yOff;
			double depthDist = ((diameter) * (affs.get(tend) * 0.5) + diameter) * 1;
			double lx = (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist) + xOff;
			double ly = (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist) + yOff;
			double displace = ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			Vec3 vec1 = new Vec3(lx + centerOffset, yLevel + centerOffset, cy1 + centerOffset);
			Vec3 vec2 = new Vec3(cx + centerOffset, yLevel + centerOffset, cy2 + centerOffset);
			Vec3 vec3 = new Vec3(cx1 + centerOffset, yLevel + centerOffset, cy1 + centerOffset);
			Vec3 vec4 = new Vec3(cx1 + centerOffset, yLevel + centerOffset, cy1 + centerOffset);
			fracLine(stack, bufferIn, vec1, vec3, tend.getColor(), displace, 1.1, combinedOverlayIn);
			fracLine(stack, bufferIn, vec1, vec2, tend.getColor(), displace, 1.1, combinedOverlayIn);
			fracLine(stack, bufferIn, vec4, vec1, tend.getColor(), displace, 0.8, combinedOverlayIn);
			fracLine(stack, bufferIn, vec2, vec1, tend.getColor(), displace, 0.8, combinedOverlayIn);

			rotAngle += 45;
		}
	}

	public static void fracLine(PoseStack matrix, MultiBufferSource buffer, Vec3 from, Vec3 to, ParticleColor color,
			double displace, double detail, int combinedLightIn) {
		if (displace < detail) {

			WorldRenderUtils.renderBeam(ClientUtils.getWorld(), ClientUtils.getPartialTicks(), matrix, buffer,
					combinedLightIn, to, from, 1f, color, RenderTypeInit.RADIANT_RENDER_TYPE);
			Vec3 velo = to.subtract(from).scale(0.1);
			ClientUtils.getWorld().addParticle(GlowParticleFactory.createData(ParticleColor.PURPLE), to.x, to.y, to.z,
					velo.x, velo.y, velo.z);
//
//			VertexConsumer vertexBuilder = buffer.getBuffer(RenderTypeInit.RADIANT_RENDER_TYPE);
//			PoseStack.Pose activeStack = matrix.last();
//			Matrix4f renderMatrix = activeStack.pose();
//			Matrix3f normalMatrix = activeStack.normal();
//			vertexBuilder.vertex(renderMatrix, (float) from.x(), (float) from.y(), (float) from.z())
//					.color(1.0F, 1.0F, 1.0F, 1.0F).normal(normalMatrix, 1, 1, 1).endVertex();
//			vertexBuilder.vertex(renderMatrix, (float) to.x(), (float) to.y(), (float) to.z())
//					.color(1.0F, 1.0F, 1.0F, 1.0F).normal(normalMatrix, 1, 1, 1).endVertex();

		} else {

			Random rand = new Random();
			double offset = 0.15;
			Vec3 mid = new Vec3(((from.x + to.x) / 2) + (rand.nextFloat() - offset) * displace * offset,
					((from.y + to.y) / 2) + (rand.nextFloat() - offset) * displace * offset,
					((from.z + to.z) / 2) + (rand.nextFloat() - offset) * displace * offset);
			fracLine(matrix, buffer, to, mid, color, (displace / 2), detail, combinedLightIn);
			fracLine(matrix, buffer, from, mid, color, (displace / 2), detail, combinedLightIn);

		}
	}

//	public void drawLine(VertexConsumer vertexBuilder, Matrix4f renderMatrix, Vec3 start, Vec3 end) {
//		vertexBuilder.vertex(renderMatrix, (float) start.x(), (float) start.y(), (float) start.z())
//				.color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
//		vertexBuilder.vertex(renderMatrix, (float) end.x(), (float) end.y(), (float) end.z())
//				.color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
//	}

}
