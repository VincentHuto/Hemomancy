package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.world.SanguineFormationProjectionRenderer;
import com.vincenthuto.hemomancy.common.entity.utility.AwakenedIchorianSigilEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilOrganicGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class AwakenedIchorianSigilRenderer extends EntityRenderer<AwakenedIchorianSigilEntity> {
	private static final float SHAPE_SCALE = 0.42F;
	private static final int VESSEL_SEGMENTS = 7;

	public AwakenedIchorianSigilRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(AwakenedIchorianSigilEntity entity, float yaw, float partialTick, PoseStack stack,
			MultiBufferSource buffers, int packedLight) {
		ResourceLocation sigilId = entity.getSigilId();
		if (sigilId == null) return;
		IchorianSigilDefinition sigil = IchorianSigilRegistry.get(sigilId);
		if (sigil == null || sigil.nodes().isEmpty()) return;

		float time = entity.tickCount + partialTick;
		float peel = entity.getPeelProgress(partialTick);
		int color = sigil.color();
		float red = ((color >> 16) & 255) / 255.0F;
		float green = ((color >> 8) & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		red = Math.min(1.0F, red * 0.62F + 0.30F);
		green *= 0.55F;
		blue *= 0.55F;

		stack.pushPose();
		float heartbeat = (float) Math.sin(time * 0.14F);
		stack.mulPose(Axis.YP.rotationDegrees(
				(time * 0.62F + (float) Math.sin(time * 0.045F) * 12.0F) * peel));
		stack.mulPose(Axis.XP.rotationDegrees(90.0F * peel));
		stack.mulPose(Axis.ZP.rotationDegrees(heartbeat * 4.5F * peel));
		float breath = 1.0F + heartbeat * 0.035F;
		stack.scale(SHAPE_SCALE * breath, SHAPE_SCALE, SHAPE_SCALE * breath);
		renderShape(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), stack.last().pose(),
				sigil, time, red, green, blue, true);
		renderShape(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), stack.last().pose(),
				sigil, time, red, green, blue, false);
		stack.popPose();
		super.render(entity, yaw, partialTick, stack, buffers, packedLight);
	}

	private static void renderShape(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilDefinition sigil, float time,
			float red, float green, float blue, boolean glow) {
		float halfWidth = glow ? 0.18F : 0.065F;
		float alpha = glow ? 0.28F : 0.88F;
		for (int index = 1; index < sigil.nodes().size(); index++) {
			IchorianSigilDefinition.Node start = sigil.nodes().get(index - 1);
			IchorianSigilDefinition.Node end = sigil.nodes().get(index);
			renderBand(consumer, matrix, start.x(), start.z(), end.x(), end.z(),
					halfWidth, time, sigil.id().hashCode() * 31L + index,
					red, green, blue, alpha);
		}
		for (int index = 0; index < sigil.nodes().size(); index++) {
			IchorianSigilDefinition.Node node = sigil.nodes().get(index);
			float nodePulse = IchorianSigilOrganicGeometry.nodePulse(
					time, sigil.id().hashCode(), index);
			Matrix4f translated = new Matrix4f(matrix).translate(
					(float) node.x(), 0.0F, (float) node.z());
			SanguineFormationProjectionRenderer.renderSphere(
					consumer, translated, (glow ? 0.28F : 0.17F) * nodePulse,
					time, sigil.id().hashCode() * 31L + index,
					red, green, blue, alpha);
		}
	}

	private static void renderBand(VertexConsumer consumer, Matrix4f matrix,
			double startX, double startZ, double endX, double endZ, float halfWidth,
			float time, long seed,
			float red, float green, float blue, float alpha) {
		IchorianSigilOrganicGeometry.Sample previous = IchorianSigilOrganicGeometry.sample(
				startX, 0.0D, startZ, endX, 0.0D, endZ,
				time, seed, 0, VESSEL_SEGMENTS, halfWidth);
		for (int step = 1; step <= VESSEL_SEGMENTS; step++) {
			IchorianSigilOrganicGeometry.Sample next = IchorianSigilOrganicGeometry.sample(
					startX, 0.0D, startZ, endX, 0.0D, endZ,
					time, seed, step, VESSEL_SEGMENTS, halfWidth);
			renderVesselSection(consumer, matrix, previous, next, red, green, blue, alpha);
			previous = next;
		}
	}

	private static void renderVesselSection(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilOrganicGeometry.Sample start, IchorianSigilOrganicGeometry.Sample end,
			float red, float green, float blue, float alpha) {
		double dx = end.x() - start.x();
		double dz = end.z() - start.z();
		double length = Math.hypot(dx, dz);
		if (length < 0.001D) return;
		float normalX = (float) (-dz / length);
		float normalZ = (float) (dx / length);
		consumer.addVertex(matrix,
				(float) start.x() - normalX * start.halfWidth(), (float) start.y(),
				(float) start.z() - normalZ * start.halfWidth()).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix,
				(float) start.x() + normalX * start.halfWidth(), (float) start.y(),
				(float) start.z() + normalZ * start.halfWidth()).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix,
				(float) end.x() + normalX * end.halfWidth(), (float) end.y(),
				(float) end.z() + normalZ * end.halfWidth()).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix,
				(float) end.x() - normalX * end.halfWidth(), (float) end.y(),
				(float) end.z() - normalZ * end.halfWidth()).setColor(red, green, blue, alpha);
	}

	@Override
	public ResourceLocation getTextureLocation(AwakenedIchorianSigilEntity entity) {
		return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
	}
}
