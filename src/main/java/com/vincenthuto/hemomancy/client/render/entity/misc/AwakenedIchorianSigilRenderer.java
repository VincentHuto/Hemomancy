package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.render.world.SanguineFormationProjectionRenderer;
import com.vincenthuto.hemomancy.common.entity.utility.AwakenedIchorianSigilEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilBodyAnimation;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilFacing;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilOrganicGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRenderPalette;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPose;
import com.vincenthuto.hemomancy.common.rite.sigil.AwakenedIchorianSigilPoseCalculator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

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

		stack.pushPose();
		stack.mulPose(Axis.YP.rotationDegrees(entity.getRenderFacingYaw(partialTick)));
		stack.mulPose(Axis.XP.rotationDegrees(entity.getRenderFacingPitch(partialTick)));
		stack.mulPose(Axis.ZP.rotationDegrees(entity.getRenderBankRoll(partialTick)));
		sigil.awakenedForm().ifPresent(anatomy -> stack.mulPose(Axis.YP.rotationDegrees(
				AwakenedIchorianSigilFacing.authoredForwardCorrection(
						anatomy.forward().x, anatomy.forward().z))));
		AwakenedIchorianSigilBodyAnimation.BodyPose body =
				AwakenedIchorianSigilBodyAnimation.pose(
						sigilId, time, entity.getRenderMovementSpeed(partialTick));
		stack.translate(body.offsetX(), body.offsetY(), body.offsetZ());
		stack.mulPose(Axis.YP.rotationDegrees(body.yawDegrees()));
		stack.mulPose(Axis.XP.rotationDegrees(body.pitchDegrees()));
		stack.mulPose(Axis.ZP.rotationDegrees(body.rollDegrees()));
		stack.scale(body.scaleX(), body.scaleY(), body.scaleZ());
		if (sigil.awakenedForm().isPresent()) {
			AwakenedIchorianSigilPose pose =
					AwakenedIchorianSigilPoseCalculator.calculate(sigil, time);
			AwakenedIchorianSigilGeometryRenderer.render(
					pose, stack, buffers, time, color, sigil.id().hashCode());
			stack.popPose();
			super.render(entity, yaw, partialTick, stack, buffers, packedLight);
			return;
		}
		stack.mulPose(Axis.XP.rotationDegrees(90.0F * peel));
		stack.scale(SHAPE_SCALE, SHAPE_SCALE, SHAPE_SCALE);
		renderShape(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW), stack.last().pose(),
				sigil, time, color, true);
		renderShape(buffers.getBuffer(RenderTypeInit.RITE_BOUNDARY_CORE), stack.last().pose(),
				sigil, time, color, false);
		stack.popPose();
		super.render(entity, yaw, partialTick, stack, buffers, packedLight);
	}

	@Override
	public boolean shouldRender(AwakenedIchorianSigilEntity entity, Frustum frustum,
			double cameraX, double cameraY, double cameraZ) {
		return frustum.isVisible(entity.getAnatomyRenderBounds());
	}

	private static void renderShape(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilDefinition sigil, float time, int sigilColor, boolean glow) {
		float halfWidth = glow ? 0.18F : 0.065F;
		float alpha = glow ? 0.28F : 0.88F;
		IchorianSigilRenderPalette.Color vesselColor = IchorianSigilRenderPalette.vessel(glow);
		IchorianSigilRenderPalette.Color nodeColor =
				IchorianSigilRenderPalette.node(sigilColor, glow);
		for (int index = 1; index < sigil.nodes().size(); index++) {
			IchorianSigilDefinition.Node start = sigil.nodes().get(index - 1);
			IchorianSigilDefinition.Node end = sigil.nodes().get(index);
			renderBand(consumer, matrix, start.x(), start.z(), end.x(), end.z(),
					halfWidth, time, sigil.id().hashCode() * 31L + index,
					vesselColor.red(), vesselColor.green(), vesselColor.blue(), alpha);
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
					nodeColor.red(), nodeColor.green(), nodeColor.blue(), alpha);
		}
	}

	private static void renderBand(VertexConsumer consumer, Matrix4f matrix,
			double startX, double startZ, double endX, double endZ, float halfWidth,
			float time, long seed,
			float red, float green, float blue, float alpha) {
		List<IchorianSigilOrganicGeometry.Sample> samples = new ArrayList<>(VESSEL_SEGMENTS + 1);
		for (int step = 0; step <= VESSEL_SEGMENTS; step++) {
			samples.add(IchorianSigilOrganicGeometry.sample(
					startX, 0.0D, startZ, endX, 0.0D, endZ,
					time, seed, step, VESSEL_SEGMENTS, halfWidth));
		}
		for (IchorianSigilOrganicGeometry.RibbonSegment segment
				: IchorianSigilOrganicGeometry.ribbonSegments(samples)) {
			renderVesselSection(consumer, matrix, segment, red, green, blue, alpha);
		}
	}

	private static void renderVesselSection(VertexConsumer consumer, Matrix4f matrix,
			IchorianSigilOrganicGeometry.RibbonSegment segment,
			float red, float green, float blue, float alpha) {
		IchorianSigilOrganicGeometry.RibbonJoint start = segment.start();
		IchorianSigilOrganicGeometry.RibbonJoint end = segment.end();
		consumer.addVertex(matrix,
				(float) start.leftX(), (float) start.centerY(),
				(float) start.leftZ()).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix,
				(float) start.rightX(), (float) start.centerY(),
				(float) start.rightZ()).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix,
				(float) end.rightX(), (float) end.centerY(),
				(float) end.rightZ()).setColor(red, green, blue, alpha);
		consumer.addVertex(matrix,
				(float) end.leftX(), (float) end.centerY(),
				(float) end.leftZ()).setColor(red, green, blue, alpha);
	}

	@Override
	public ResourceLocation getTextureLocation(AwakenedIchorianSigilEntity entity) {
		return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
	}
}
