package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.CircusJointedPerformerModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.PerformerHandAnchors;
import com.vincenthuto.hemomancy.client.screen.overlay.CircusPerceptionOverlay;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusStiltWalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CircusPerformerRenderer<T extends CircusPerformerEntity, M extends HumanoidModel<T>>
		extends MobRenderer<T, M> {
	private final ResourceLocation[] textures;
	/** The right arm's grip in model pixels, and the frame the dispatcher placed this entity in. */
	private final Vector3f grip;
	private final Matrix4f entityRoot = new Matrix4f();
	/** The Disturbed afterimage: a stutter this many ticks long, at most once this often per figure. */
	private static final float STUTTER_TICKS = 6.0F;
	private static final float STUTTER_EVERY = 120.0F;

	public CircusPerformerRenderer(EntityRendererProvider.Context context, M model, String textureName,
			float shadowRadius) {
		super(context, model, shadowRadius);
		addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
		addLayer(new PerceptionEchoLayer());
		addLayer(new StringHandLayer());
		grip = model instanceof CircusJointedPerformerModel<?> ? new Vector3f()
				: PerformerHandAnchors.gripOf(model.rightArm);
		textures = new ResourceLocation[] {
				Hemomancy.rloc("textures/entity/npc/harbinger/circus/" + textureName + "_0.png"),
				Hemomancy.rloc("textures/entity/npc/harbinger/circus/" + textureName + "_1.png")
		};
	}

	/** Records where the posed right hand was drawn, after every flip, spin, act pose and swing. */
	private final class StringHandLayer extends RenderLayer<T, M> {
		private StringHandLayer() {
			super(CircusPerformerRenderer.this);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
				float netHeadYaw, float headPitch) {
			poseStack.pushPose();
			if (getParentModel() instanceof CircusJointedPerformerModel<?> rig) {
				rig.translateToHand(HumanoidArm.RIGHT, poseStack);
			} else {
				getParentModel().rightArm.translateAndRotate(poseStack);
			}
			Vector3f hand = poseStack.last().pose().transformPosition(
					grip.x / 16.0F, grip.y / 16.0F, grip.z / 16.0F, new Vector3f());
			poseStack.popPose();
			PerformerHandAnchors.record(entity, PerformerHandAnchors.worldOffset(entityRoot, hand));
		}
	}

	private final class PerceptionEchoLayer extends RenderLayer<T, M> {
		private PerceptionEchoLayer() {
			super(CircusPerformerRenderer.this);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
				float netHeadYaw, float headPitch) {
			CircusProgressRules.Stage stage = CircusPerceptionOverlay.isRuined() ? CircusProgressRules.Stage.DISTURBED
					: CircusPerceptionOverlay.stage();
			int alpha = CircusPerceptionOverlay.isActive() ? stage.motionEchoAlpha() : 0;
			if (alpha == 0) return;
			// Disturbed: now and then a brief afterimage stutters off the figure and snaps back.
			float time = AnimationTime.age(entity, partialTick) + entity.getId() * 37.0F;
			float within = time % STUTTER_EVERY;
			if (within > STUTTER_TICKS) return;
			float offset = 0.09F * (float) Math.sin(within / STUTTER_TICKS * Math.PI);
			poseStack.pushPose();
			poseStack.translate(Math.sin(entity.getId() * 1.7D) * offset, 0.0D, Math.cos(entity.getId() * 1.7D) * offset);
			VertexConsumer echo = buffer.getBuffer(RenderType.entityTranslucentEmissive(getTextureLocation(entity)));
			getParentModel().renderToBuffer(poseStack, echo, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, alpha << 24 | 0x9A143E);
			poseStack.popPose();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return textures[Math.floorMod(entity.getVariant(), textures.length)];
	}

	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		entityRoot.set(poseStack.last().pose());
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	protected void setupRotations(T entity, PoseStack poseStack, float bob, float bodyYaw,
			float partialTick, float scale) {
		super.setupRotations(entity, poseStack, bob, bodyYaw, partialTick, scale);
		if (entity instanceof CircusAcrobatEntity acrobat) {
			float progress = acrobat.getFlipProgress(partialTick);
			if (progress > 0.0F) {
				float pivot = entity.getBbHeight() * 0.5F;
				poseStack.translate(0.0F, pivot, 0.0F);
				poseStack.mulPose(Axis.XP.rotationDegrees(progress * 360.0F));
				poseStack.translate(0.0F, -pivot, 0.0F);
			}
		}
		if (entity instanceof CircusStiltWalkerEntity stiltWalker) {
			float progress = stiltWalker.getSpinProgress(partialTick);
			if (progress > 0.0F) {
				poseStack.translate(-0.12F, 0.0F, 0.0F);
				poseStack.mulPose(Axis.YP.rotationDegrees(progress * 720.0F));
				poseStack.translate(0.12F, 0.0F, 0.0F);
			}
		}
	}
}
