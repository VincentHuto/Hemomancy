package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.utility.UnsettledIchorEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * The same undulating 3D blood-sphere language used by Sanguine Formation
 * creation, scaled by the amount of recoverable ichor.
 */
public final class UnsettledIchorRenderer extends EntityRenderer<UnsettledIchorEntity> {
	public UnsettledIchorRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(UnsettledIchorEntity entity, float yaw, float partialTick, PoseStack stack,
			MultiBufferSource buffers, int packedLight) {
		stack.pushPose();
		stack.translate(0.0D, 0.2D + Math.sin((entity.tickCount + partialTick) * 0.18D) * 0.06D, 0.0D);
		float radius = 0.16F + Math.min(0.18F, entity.getBloodMl() / 300.0F);
		Matrix4f matrix = stack.last().pose();
		renderSphere(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), matrix, radius,
				entity.tickCount + partialTick, 0.85F, 0.02F, 0.035F, 0.82F);
		renderSphere(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), matrix, radius + 0.09F,
				entity.tickCount + partialTick, 0.55F, 0.0F, 0.02F, 0.25F);
		stack.popPose();
		super.render(entity, yaw, partialTick, stack, buffers, packedLight);
	}

	private static void renderSphere(VertexConsumer consumer, Matrix4f matrix, float baseRadius, float time,
			float red, float green, float blue, float alpha) {
		for (int lat = 0; lat < 8; lat++) {
			double theta0 = Math.PI * lat / 8.0D;
			double theta1 = Math.PI * (lat + 1) / 8.0D;
			for (int lon = 0; lon < 12; lon++) {
				double phi0 = Math.PI * 2.0D * lon / 12.0D;
				double phi1 = Math.PI * 2.0D * (lon + 1) / 12.0D;
				vertex(consumer, matrix, theta0, phi0, radius(baseRadius, theta0, phi0, time),
						red, green, blue, alpha);
				vertex(consumer, matrix, theta1, phi0, radius(baseRadius, theta1, phi0, time),
						red, green, blue, alpha);
				vertex(consumer, matrix, theta1, phi1, radius(baseRadius, theta1, phi1, time),
						red, green, blue, alpha);
				vertex(consumer, matrix, theta0, phi1, radius(baseRadius, theta0, phi1, time),
						red, green, blue, alpha);
			}
		}
	}

	private static float radius(float base, double theta, double phi, float time) {
		return base + (float) (0.018D * Math.sin(theta * 4.0D + time * 0.17D)
				+ 0.012D * Math.cos(phi * 5.0D + time * 0.11D));
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, double theta, double phi, float radius,
			float red, float green, float blue, float alpha) {
		consumer.addVertex(matrix,
				(float) (Math.sin(theta) * Math.cos(phi)) * radius,
				(float) Math.cos(theta) * radius,
				(float) (Math.sin(theta) * Math.sin(phi)) * radius)
				.setColor(red, green, blue, alpha);
	}

	@Override
	public ResourceLocation getTextureLocation(UnsettledIchorEntity entity) {
		return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
	}
}
