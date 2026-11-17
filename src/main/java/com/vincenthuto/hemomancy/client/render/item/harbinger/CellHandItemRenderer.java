package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.StrandClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.registry.ModelRegistrar;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

public class CellHandItemRenderer extends BlockEntityWithoutLevelRenderer {

	public BakedModel location;

	public final ResourceLocation skinTexture = Hemomancy.rloc("textures/entity/hardened_skin.png");

	public CellHandItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
	}

	public void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, HumanoidArm side) {
		renderArm(matrixStackIn, bufferIn, combinedLightIn, playerIn, side, ItemStack.EMPTY);
	}

	private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, HumanoidArm side, ItemStack stack) {
		if (side == HumanoidArm.RIGHT) {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
			renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (playerrenderer.getModel()).rightArm,
					(playerrenderer.getModel()).rightSleeve, side, stack);
		} else {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
			this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (playerrenderer.getModel()).leftArm,
					(playerrenderer.getModel()).leftSleeve, side, stack);
		}

	}

	private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, HumanoidArm side,
			ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
		matrixStackIn.pushPose();
		applyCastingArmTransform(matrixStackIn, side);
		applyFirstPersonArmTransform(matrixStackIn, side);
		renderArm(matrixStackIn, bufferIn, combinedLightIn, mc.player, side, stack);
		spawnFirstPersonParticlesForStack(stack, side, matrixStackIn);
		matrixStackIn.popPose();
	}

	public static void applyFirstPersonArmTransform(PoseStack matrixStackIn, HumanoidArm side) {
		if (side == HumanoidArm.RIGHT) {
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(12.0F).toMoj());
			matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-35.0F).toMoj());
			matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(5.0F).toMoj());
			matrixStackIn.translate(1.0, -0.4, 0.8);
		} else {
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(-12.0F).toMoj());
			matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-35.0F).toMoj());
			matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(5.0F).toMoj());
			matrixStackIn.translate(0.0, -0.3, 0.6);
		}
	}

	private void applyCastingArmTransform(PoseStack matrixStackIn, HumanoidArm side) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !player.isUsingItem() || !(player.getUseItem().getItem() instanceof ICellHand)) {
			return;
		}

		HumanoidArm activeArm = player.getUsedItemHand() == InteractionHand.MAIN_HAND
				? player.getMainArm()
				: player.getMainArm().getOpposite();
		StrandClientState.Strand strand = StrandClientState.of(player.getId());
		boolean strandOut = strand != null && strand.cue != null && strand.released < 0;
		// With a twin (Second Strand) the off hand's cell hand works the second strand the same way.
		if (strandOut && activeArm != side && strand.cue.twin()) {
			applyStrandArmTransform(matrixStackIn, player, strand, side, true);
			return;
		}
		if (activeArm != side) {
			return;
		}
		if (strandOut) {
			applyStrandArmTransform(matrixStackIn, player, strand, side, false);
			return;
		}

		ItemStack useStack = player.getUseItem();
		float useTicks = useStack.getUseDuration(player) - player.getUseItemRemainingTicks()
				+ HLClientUtils.getPartialTicks();
		float sideSign = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		float wave = Mth.sin(useTicks * 0.35F);
		float pulse = Mth.sin(useTicks * 0.18F);

		matrixStackIn.translate(sideSign * 0.03F * wave, 0.03F * pulse, -0.04F * pulse);
		matrixStackIn.mulPose(Vector3.YP.rotationDegrees(sideSign * (6.0F + 4.0F * wave)).toMoj());
		matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(sideSign * 6.0F * pulse).toMoj());
		matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-8.0F - 5.0F * wave).toMoj());
	}

	/**
	 * The first-person arm while a strand is out (core-loop round two P5), in place of the idle wave. A lash's arm leads
	 * the stroke: it turns toward the chain's second node, which lags the look, so the arm swings ahead and follows
	 * through after a reversal. A jet's arm is straight and braced and trembles as the pressure builds. A spray's palm
	 * opens and rolls side to side. A plant's palm pitches down toward the point. A brand's arm draws slowly back as it
	 * takes. Applied in view space before the arm's own first-person transform, so the drawn palm (the strand's root)
	 * moves with it.
	 */
	private static void applyStrandArmTransform(PoseStack pose, LocalPlayer player, StrandClientState.Strand strand,
			HumanoidArm side, boolean twin) {
		float partial = AnimationTime.partialTick();
		double time = AnimationTime.gameTime(player.level(), (double) partial);
		float sideSign = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		switch (strand.form()) {
			case LASH -> {
				var lash = twin ? strand.twinLash : strand.lash;
				if (!lash.seeded()) return;
				var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
				Vec3 right = new Vec3(camera.getLeftVector()).scale(-1.0D), up = new Vec3(camera.getUpVector()),
						ahead = new Vec3(camera.getLookVector());
				Vec3 lead = lash.previous(2).lerp(lash.node(2), partial).subtract(camera.getPosition());
				double forward = Math.max(0.2D, lead.dot(ahead));
				float yaw = Mth.clamp((float) Math.toDegrees(Math.atan2(lead.dot(right), forward)) * 1.4F, -25.0F, 25.0F);
				float pitch = Mth.clamp((float) Math.toDegrees(Math.atan2(lead.dot(up), forward)) * 1.2F, -18.0F, 18.0F);
				// The arm swings with the lag; a reversal throws the wrist through before it comes back.
				float through = strand.reversed ? 6.0F * Math.signum(yaw) : 0.0F;
				pose.translate(0.12F * yaw / 25.0F, 0.04F * pitch / 18.0F, -0.05F);
				pose.mulPose(Vector3.YP.rotationDegrees(-(yaw + through)).toMoj());
				pose.mulPose(Vector3.XP.rotationDegrees(pitch - 6.0F).toMoj());
				pose.mulPose(Vector3.ZP.rotationDegrees(-sideSign * yaw * 0.5F).toMoj());
			}
			case JET -> {
				double pressure = strand.jet.pressure();
				float tremor = (float) (pressure * pressure);
				float shakeX = (float) (Math.sin(time * 2.9D) * 0.5D + Math.sin(time * 5.3D + 1.1D) * 0.5D) * 0.012F * tremor;
				float shakeY = (float) (Math.sin(time * 3.7D + 0.4D) * 0.5D + Math.sin(time * 6.1D) * 0.5D) * 0.010F * tremor;
				// Straight out and braced: the arm thrust forward, level with the look.
				pose.translate(-sideSign * 0.06F + shakeX, 0.06F + shakeY, -0.16F - 0.04F * (float) pressure);
				pose.mulPose(Vector3.XP.rotationDegrees(-14.0F - 4.0F * (float) pressure).toMoj());
				pose.mulPose(Vector3.YP.rotationDegrees(sideSign * 4.0F).toMoj());
			}
			case SPRAY -> {
				float roll = (float) Math.sin(time * 0.32D) * 16.0F;
				float sway = (float) Math.sin(time * 0.32D + 0.6D) * 5.0F;
				pose.translate(0.0F, 0.03F, -0.1F);
				pose.mulPose(Vector3.YP.rotationDegrees(sideSign * (4.0F + sway)).toMoj());
				pose.mulPose(Vector3.XP.rotationDegrees(-10.0F).toMoj());
				pose.mulPose(Vector3.ZP.rotationDegrees(roll).toMoj());
			}
			case PLANT -> {
				float fill = (float) strand.cue.param();
				float throb = (float) Math.sin(time * 0.9D) * 1.5F;
				// The palm pitches down toward the ground it sinks into, pressing lower as the plant fills.
				pose.translate(0.0F, -0.06F - 0.05F * fill, -0.06F);
				pose.mulPose(Vector3.XP.rotationDegrees(22.0F + 10.0F * fill + throb).toMoj());
				pose.mulPose(Vector3.YP.rotationDegrees(sideSign * 3.0F).toMoj());
			}
			case BRAND -> {
				double progress = com.vincenthuto.hemomancy.common.manipulation.verb.strand.BrandRules.progress(strand.brand,
						com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSkills.brandSetTicks(strand.cue.skill()));
				float pull = (float) progress;
				float strain = pull > 0.7F ? (float) Math.sin(time * 4.3D) * 0.006F * (pull - 0.7F) / 0.3F : 0.0F;
				// A slow pull back toward the body as the brand takes, the wrist cocking up with it.
				pose.translate(strain, 0.02F * pull, 0.14F * pull - 0.04F);
				pose.mulPose(Vector3.XP.rotationDegrees(-6.0F - 10.0F * pull).toMoj());
				pose.mulPose(Vector3.YP.rotationDegrees(sideSign * (2.0F + 4.0F * pull)).toMoj());
			}
		}
	}

	// ModelBloodArm model = new ModelBloodArm(1.0f);

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext ItemDisplayContext, PoseStack matrixStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (ItemDisplayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				&& ItemDisplayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
			if (ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
				if (!(stack.getItem() instanceof ICellHand)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.RIGHT, stack);
			} else if (ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
				if (!(stack.getItem() instanceof ICellHand)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.LEFT, stack);
			} else {
				this.renderDefaultItem(stack, ItemDisplayContext, matrixStack, buffer, combinedLight, combinedOverlay);
			}
		}
	}

	private void renderDefaultItem(ItemStack stack, ItemDisplayContext ItemDisplayContext, PoseStack matrixStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		VertexConsumer buffers = buffer.getBuffer(Sheets.cutoutBlockSheet());
		if (stack.getItem() instanceof ICellHand) {
			location = ModelRegistrar.cellHandModel(stack.getItem());
			int color = Minecraft.getInstance().getItemColors().getColor(stack, 1);
			if (this.location == null) {
				float r = (color >> 16 & 0xFF) / 255F;
				float g = (color >> 8 & 0xFF) / 255F;
				float b = (color & 0xFF) / 255F;
				Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), buffers,
						null, location, r, g, b, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
						RenderType.solid());
			} else if (!stack.isEmpty()) {
				if (this.location.isGui3d()) {
					Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(),
							buffers, null, location, 255, 255, 255, 0x000000, combinedOverlay, ModelData.EMPTY,
							RenderType.solid());
				} else {
					Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(),
							buffers, null, location, 255, 255, 255, combinedLight, combinedOverlay, ModelData.EMPTY,
							RenderType.solid());
					matrixStack.popPose();
					matrixStack.pushPose();
					Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext,
							ItemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND, matrixStack, buffer,
							ItemDisplayContext == ItemDisplayContext.GUI ? 0xF000F0 : combinedLight,
							ItemDisplayContext == ItemDisplayContext.GUI ? OverlayTexture.NO_OVERLAY : combinedOverlay,
							this.location);
				}
			}
		}
	}

	private void renderItem(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn, HumanoidArm side,
			ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();

		mc.getTextureManager().bindForSetup(mc.player.getSkin().texture());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
		PlayerModel<AbstractClientPlayer> playermodel = playerrenderer.getModel();
		playermodel.attackTime = 0.0F;
		playermodel.crouching = false;
		playermodel.swimAmount = 0.0F;
		playermodel.setupAnim(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		rendererArmIn.xRot = 0.0F;
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(playermodel.renderType(skinTexture));
		rendererArmIn.render(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY);
		rendererArmwearIn.xRot = 0.0F;
		rendererArmwearIn.render(matrixStackIn,
				bufferIn.getBuffer(RenderType.entityTranslucent(playerIn.getSkin().texture())), combinedLightIn,
				OverlayTexture.NO_OVERLAY);
	}

	private void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand, PoseStack poseStack) {
		Minecraft mc = Minecraft.getInstance();
		PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
		ModelPart arm = hand == HumanoidArm.RIGHT
				? playerRenderer.getModel().rightArm
				: playerRenderer.getModel().leftArm;
		// The arm is posed in view space; the palm is carried into the world through the camera so particles and the
		// cast geometry both leave the drawn palm whichever way the player faces.
		FirstPersonPalm.record(poseStack.last().pose(), arm, hand);
		Vec3 origin = FirstPersonPalm.world(hand);
		if (origin != null) CellHandParticleEffects.spawnFirstPersonParticlesForStack(stack, hand, origin);
	}
}
