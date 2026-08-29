package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.entity.projectile.VeinwingFeatherEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class VeinwingFeatherRenderer extends EntityRenderer<VeinwingFeatherEntity> {
	private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/item/feather.png");

	public VeinwingFeatherRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(VeinwingFeatherEntity entity, float yaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffers, int light) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
		if (!entity.isEmbedded()) {
			poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * 36.0F));
		}
		poseStack.scale(0.55F, 0.55F, 0.55F);
		VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		quad(poseStack.last(), out, light);
		poseStack.popPose();
		super.render(entity, yaw, partialTick, poseStack, buffers, light);
	}

	private static void quad(PoseStack.Pose pose, VertexConsumer out, int light) {
		vertex(pose, out, light, -0.5F, -0.5F, 0.0F, 1.0F);
		vertex(pose, out, light, 0.5F, -0.5F, 1.0F, 1.0F);
		vertex(pose, out, light, 0.5F, 0.5F, 1.0F, 0.0F);
		vertex(pose, out, light, -0.5F, 0.5F, 0.0F, 0.0F);
	}

	private static void vertex(PoseStack.Pose pose, VertexConsumer out, int light, float x, float y,
			float u, float v) {
		Matrix4f matrix = pose.pose();
		Matrix3f normals = pose.normal();
		Vector3f normal = new Vector3f(0.0F, 0.0F, 1.0F).mul(normals).normalize();
		out.addVertex(matrix, x, y, 0.0F).setColor(170, 28, 42, 255).setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x(), normal.y(), normal.z());
	}

	@Override
	public ResourceLocation getTextureLocation(VeinwingFeatherEntity entity) {
		return TEXTURE;
	}
}
