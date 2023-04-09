package com.vincenthuto.hemomancy.render.tile;

import java.util.Map;
import java.util.Random;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hemomancy.recipe.RecallerRecipe;
import com.vincenthuto.hemomancy.recipe.serializer.RecallerRecipeSerializer;
import com.vincenthuto.hemomancy.tile.VisceralRecallerBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class VisceralRecallerRenderer implements BlockEntityRenderer<VisceralRecallerBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

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

	public static void drawLasers(PoseStack matrixStackIn, MultiBufferSource buffer, Vec3 from, Vec3 to, float r,
			float g, float b) {
		final Minecraft mc = Minecraft.getInstance();

		Level world = mc.level;
		long gameTime = world.getGameTime();
		double v = gameTime * 0.04;
		Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
		matrixStackIn.pushPose();
		matrixStackIn.translate(-view.x, -view.y, -view.z);
		VertexConsumer builder = buffer.getBuffer(RenderTypeInit.LASER_MAIN_CORE);
		VertexConsumer builderBack = buffer.getBuffer(RenderTypeInit.LASER_MAIN_CORE);

		matrixStackIn.pushPose();
		matrixStackIn.translate(to.x, to.y, to.z);
		float diffX = (float) (from.x - to.x);
		float diffY = (float) (from.y - to.y);
		float diffZ = (float) (from.z - to.z);
		Vector3 startLaser = new Vector3(0, 0, 0);
		Vector3 endLaser = new Vector3(diffX, diffY, diffZ);
		Vector3 sortPos = new Vector3((float) to.x, (float) to.y, (float) to.z);
		Matrix4f positionMatrix = matrixStackIn.last().pose();

		Vector3 backPos = new Vector3((float) to.x, (float) to.y - .05f, (float) to.z);

		drawLaser(builderBack, positionMatrix, endLaser, startLaser, 0, 0, 0, 1f, 0.075f, v, v + diffY * -5.5, backPos);
		drawLaser(builder, positionMatrix, endLaser, startLaser, r, g, b, 1f, 0.05f, v, v + diffY * -5.5, sortPos);

		matrixStackIn.popPose();
		matrixStackIn.popPose();

	}

	public static void fracLine(PoseStack matrix, MultiBufferSource buffer, Vec3 from, Vec3 to, ParticleColor color,
			double displace, double detail, int combinedLightIn) {
		if (displace < detail) {
			drawLasers(matrix, buffer, from, to, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255);
		} else {

			Random rand = new Random();
			double offset = 0.25;
			Vec3 mid = new Vec3(((to.x + from.x) / 2) + (rand.nextFloat() - offset) * displace * offset,
					((to.y + from.y) / 2) + (rand.nextFloat() - offset) * displace * offset,
					((to.z + from.z) / 2) + (rand.nextFloat() - offset) * displace * offset);
			fracLine(matrix, buffer, from, mid, color, (displace / 2), detail, combinedLightIn);
			fracLine(matrix, buffer, to, mid, color, (displace / 2), detail, combinedLightIn);

		}
	}

	public VisceralRecallerRenderer(BlockEntityRendererProvider.Context p_173636_) {
	}

	@SuppressWarnings("unused")
	private void drawCenter(PoseStack stack, MultiBufferSource bufferIn, VisceralRecallerBlockEntity te, Vec3 from,
			int combinedLightIn, int combinedOverlayIn) {
		Map<EnumBloodTendency, Float> affs = te.getTendency();
		double centerOffset = 8;
		double cx = from.x;
		double cy = from.z;
		float rotAngle = -90f;
		double diameter = 3;
		double spikeBaseWidth = 22.75f;
		double xOff = from.x;
		double yOff = from.z;
		double yLevel = from.y + 2;
		for (EnumBloodTendency tend : EnumBloodTendency.values()) {
			double cx1 = (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
			double cx2 = (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);
			double cy1 = (cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
			double cy2 = (cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);
			double depthDist = ((65 - diameter) * affs.get(tend) * 0.0625 + diameter);
			double lx = (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist);
			double ly = (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist);
			double displace = ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
					/ 2f);
			Vec3 vec1 = new Vec3(lx, yLevel, ly);
			Vec3 vec2 = new Vec3(cx1, yLevel, cy1);
			Vec3 vec3 = new Vec3(cx2, yLevel, cy2);
			Vec3 vec4 = new Vec3(cx1, yLevel, cy1);
			fracLine(stack, bufferIn, vec1, vec3, tend.getColor(), displace, 1.1, combinedOverlayIn);
			fracLine(stack, bufferIn, vec1, vec2, tend.getColor(), displace, 1.1, combinedOverlayIn);
			fracLine(stack, bufferIn, vec4, vec1, tend.getColor(), displace, 0.8, combinedOverlayIn);
			fracLine(stack, bufferIn, vec2, vec1, tend.getColor(), displace, 0.8, combinedOverlayIn);

			rotAngle += 45;
		}
	}

	@Override
	public void render(VisceralRecallerBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		matrixStackIn.pushPose();
		matrixStackIn.translate(0.5F, 1.75F, 0.5F);
		matrixStackIn.mulPose(Vector3.YP.rotationDegrees(te.getLevel().getGameTime()).toMoj()); // Edit
		matrixStackIn.translate(0.025F, -0.5F, 0.025F);
		matrixStackIn.mulPose(Vector3.YP.rotationDegrees(90f).toMoj()); // Edit Radius Movement
		matrixStackIn.translate(0D, 0.175D * 0.25, 0F); // Block/Item Scale
		matrixStackIn.scale(0.5f, 0.5f, 0.5f);
		ItemStack stack = te.contents.get(0);
		Minecraft mc = Minecraft.getInstance();
		if (!stack.isEmpty()) {
			mc.getItemRenderer().renderStatic(null, stack, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn, null,
					combinedLightIn, combinedOverlayIn, 0);
		}

		ItemStack stack1 = te.contents.get(1);
		if (!stack1.isEmpty()) {
			matrixStackIn.translate(0D, 1f, 0F); // Block/Item Scale
			mc.getItemRenderer().renderStatic(null, stack1, ItemDisplayContext.FIXED, true, matrixStackIn, bufferIn, null,
					combinedLightIn, combinedOverlayIn, 0);
		}

		RecallerRecipe currRecipe = RecallerRecipeSerializer
				.getRecipe(te.getUpdateTag().getString(VisceralRecallerBlockEntity.TAG_RECIPE));
		if (currRecipe != null) {
			matrixStackIn.translate(0D, 1f, 0F); // Block/Item Scale
			mc.getItemRenderer().renderStatic(null, currRecipe.getResultItem(te.getLevel().registryAccess()), ItemDisplayContext.FIXED, true,
					matrixStackIn, bufferIn, null, combinedLightIn, combinedOverlayIn, 0);
		}
		matrixStackIn.popPose();
		double ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks - 1.3 * 0.14;
		float currentTime = te.getLevel().getGameTime() + partialTicks;
		Vector3 startVec = Vector3.fromBlockEntityCenter(te);
		matrixStackIn.scale(0.5f, 0.5f, 0.5f);
		matrixStackIn.translate(0.5f, 0.5f + Mth.sin(currentTime * 0.15f) * 0.15f, 0.5f);
		drawCenter(matrixStackIn, bufferIn, te, startVec.add(0.5).toVec3(), combinedLightIn, combinedOverlayIn);
	}

}