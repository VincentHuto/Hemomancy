package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BloodAvatarModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class BloodAvatarLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
	static final int BLOOD_TRANSITION_COLOR = 0x80B02018;
	static final int FIRST_PERSON_BLOOD_TRANSITION_COLOR = 0x408A1812;
	private final BloodAvatarModel<T> modelBloodAvatar;
	ResourceLocation glowTexture = Hemomancy.rloc("textures/models/armor/avatar_glow.png");

	private static int packColor(float red, float green, float blue, float alpha) {
		int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
		int r = Mth.clamp((int) (red * 255.0F), 0, 255);
		int g = Mth.clamp((int) (green * 255.0F), 0, 255);
		int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public BloodAvatarLayer(RenderLayerParent<T, M> p_117346_) {
		super(p_117346_);
		modelBloodAvatar = new BloodAvatarModel<>(
				Minecraft.getInstance().getEntityModels().bakeLayer(BloodAvatarModel.layer));
	}

	public void render(ItemStack pItemStack, VertexConsumer swirlConsumer, ItemDisplayContext pItemDisplayContext,
			boolean pLeftHand, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight,
			int pCombinedOverlay, BakedModel pModel) {
		// Legacy baked-quad tint hook kept dormant; the active 1.21 path delegates to ItemRenderer below.
	}

	@Override
	public void render(PoseStack ms, MultiBufferSource pBuffer, int pPackedLight, T ent, float pLimbSwing,
			float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		if (!(ent instanceof Player player)) return;
		String activeForm = HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> known.getActiveAvatarForm()).orElse("");
		AvatarManifestationTransition.Sample transition = AvatarManifestationTransition.sample(
				player.getUUID(), activeForm, player.tickCount, pPartialTicks);
		if (!transition.renders()) return;
		var stats = AvatarManifestationRules.stats(transition.form()).orElse(null);
		if (stats == null) return;

		float age = ent.tickCount + pPartialTicks;
		modelBloodAvatar.prepareMobModel(ent, pLimbSwing, pLimbSwingAmount, pPartialTicks);
		this.getParentModel().copyPropertiesTo(modelBloodAvatar);
		modelBloodAvatar.setStage(stats.stage());
		modelBloodAvatar.setupAnim(ent, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
		ms.pushPose();
		ms.translate(0.0F, stats.playerChestLift() * transition.presence(), 0.0F);
		ms.translate(0.0F, 1.501F, 0.0F);
		float avatarScale = stats.avatarVisualScale();
		ms.scale(avatarScale, avatarScale, avatarScale);
		ms.translate(0.0F, -1.501F, 0.0F);
		applyEmergencePose(ms, transition, avatarScale);
		RenderType renderType = transition.warping()
				? bloodTransitionType(player, ms, age, transition)
				: RenderType.energySwirl(glowTexture, this.xOffset(age) % 4.0F, age * .01F % 2.0F);
		VertexConsumer consumer = pBuffer.getBuffer(renderType);
		modelBloodAvatar.renderToBuffer(ms, consumer, pPackedLight, OverlayTexture.NO_OVERLAY,
				transition.warping() ? BLOOD_TRANSITION_COLOR : packColor(0.5F, 0.5F, 0.5F, 0.3F));

		if (transition.phase() == AvatarManifestationTransition.Phase.ACTIVE && stats.stage() >= 1) {
			renderArmWithItemPlayer(player, consumer, player.getItemInHand(InteractionHand.MAIN_HAND),
					player.getMainArm(), ms, pBuffer, pPackedLight);
			renderArmWithItemPlayer(player, consumer, player.getItemInHand(InteractionHand.OFF_HAND),
					player.getMainArm().getOpposite(), ms, pBuffer, pPackedLight);
		}
		ms.popPose();
	}

	static void applyEmergencePose(PoseStack poseStack, AvatarManifestationTransition.Sample transition,
			float avatarScale) {
		if (transition.phase() != AvatarManifestationTransition.Phase.SUMMONING) return;
		float scale = transition.emergenceScale(avatarScale);
		poseStack.translate(transition.swimOffset(), 1.8F, -transition.swimOffset() * 0.35F);
		poseStack.scale(scale, scale, scale);
		poseStack.translate(0.0F, -1.8F, 0.0F);
	}

	static RenderType bloodTransitionType(Player player, PoseStack poseStack, float age,
			AvatarManifestationTransition.Sample transition) {
		Vector3f top = poseStack.last().pose().transformPosition(new Vector3f(0.0F, 0.0F, 0.0F));
		Vector3f ground = poseStack.last().pose().transformPosition(new Vector3f(0.0F, 1.5F, 0.0F));
		return HemoRenderTypes.cardinalStaffBloodMelt(age, player.getId() * 0.137F,
				0.05F + (1.0F - transition.progress()) * 0.09F,
				(top.x + ground.x) * 0.5F, (top.y + ground.y) * 0.5F, (top.z + ground.z) * 0.5F,
				transition.meltProgress(), ground.y, Math.max(0.1F, Math.abs(top.y - ground.y)));
	}

	protected void renderArmWithItem(LivingEntity entity, VertexConsumer swirlConsumer, ItemStack stack,
			HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight) {
		if (!stack.isEmpty()) {
			poseStack.pushPose();
			modelBloodAvatar.translateToHand(arm, poseStack);
			AvatarHeldItemTransform.apply(poseStack, arm);
			boolean flag = arm == HumanoidArm.LEFT;
			renderItem(entity, swirlConsumer, stack, AvatarHeldItemTransform.displayContext(arm), flag,
					poseStack, buffer, pCombinedLight);
			poseStack.popPose();
		}
	}

	protected void renderArmWithItemPlayer(LivingEntity entity, VertexConsumer swirlConsumer, ItemStack stack,
			HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight) {
		if (stack.is(Items.SPYGLASS) && entity.getUseItem() == stack && entity.swingTime == 0) {
			this.renderArmWithSpyglass(entity, swirlConsumer, stack, arm, poseStack, buffer, pCombinedLight);
		} else {
			renderArmWithItem(entity, swirlConsumer, stack, arm, poseStack, buffer, pCombinedLight);
		}

	}

	private void renderArmWithSpyglass(LivingEntity entity, VertexConsumer swirlConsumer, ItemStack stack,
			HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int pCombinedLight) {
		poseStack.pushPose();
		ModelPart modelpart = this.getParentModel().getHead();
		float f = modelpart.xRot;
		modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float) Math.PI / 6F), ((float) Math.PI / 2F));
		modelpart.translateAndRotate(poseStack);
		modelpart.xRot = f;
		CustomHeadLayer.translateToHead(poseStack, false);
		boolean flag = arm == HumanoidArm.LEFT;
		poseStack.translate((flag ? -2.5F : 2.5F) / 16.0F, -0.0625D, 0.0D);
		renderItem(entity, swirlConsumer, stack, ItemDisplayContext.HEAD, false, poseStack, buffer,
				pCombinedLight);
		poseStack.popPose();
	}

	public void renderItem(LivingEntity pLivingEntity, VertexConsumer swirlConsumer, ItemStack pItemStack,
			ItemDisplayContext pItemDisplayContext, boolean pLeftHand, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pCombinedLight) {
		if (!pItemStack.isEmpty()) {
			Minecraft.getInstance().getItemRenderer().renderStatic(
					pLivingEntity, pItemStack, pItemDisplayContext, pLeftHand,
					pMatrixStack, pBuffer, pLivingEntity.level(),
					pCombinedLight, OverlayTexture.NO_OVERLAY,
					pLivingEntity.getId() + pItemDisplayContext.ordinal());
		}
	}

	private void renderModelLists(BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay,
			PoseStack pMatrixStack, VertexConsumer pBuffer) {
		RandomSource random = RandomSource.create();

		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState) null, direction, random), pStack,
					pCombinedLight, pCombinedOverlay);
		}

		random.setSeed(42L);
		this.renderQuadList(pMatrixStack, pBuffer, pModel.getQuads((BlockState) null, (Direction) null, random), pStack,
				pCombinedLight, pCombinedOverlay);
	}

	public void renderQuadList(PoseStack pMatrixStack, VertexConsumer pBuffer, List<BakedQuad> pQuads,
			ItemStack pItemStack, int pCombinedLight, int pCombinedOverlay) {
		boolean flag = !pItemStack.isEmpty();
		PoseStack.Pose posestack$pose = pMatrixStack.last();

		for (BakedQuad bakedquad : pQuads) {
			int i = -1;
			if (flag && bakedquad.isTinted()) {
			}

			float f = (i >> 16 & 255) / 255.0F;
			float f1 = (i >> 8 & 255) / 255.0F;
			float f2 = (i & 255) / 255.0F;
			pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, 1.0F, pCombinedLight, pCombinedOverlay);
		}

	}

	public void renderStatic(@Nullable LivingEntity p_174243_, VertexConsumer swirlConsumer, ItemStack p_174244_,
			ItemDisplayContext p_174245_, boolean p_174246_, PoseStack p_174247_, MultiBufferSource p_174248_,
			@Nullable Level p_174249_, int p_174250_, int p_174251_, int p_174252_) {
		if (!p_174244_.isEmpty()) {
			BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(p_174244_, p_174249_, p_174243_,
					p_174252_);
			this.render(p_174244_, swirlConsumer, p_174245_, p_174246_, p_174247_, p_174248_, p_174250_, p_174251_,
					bakedmodel);
		}
	}

	protected float xOffset(float p_116683_) {
		return p_116683_ * 0.01F;
	}
}
