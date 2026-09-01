package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BloodAvatarModel;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.manipulation.animus.AvatarManifestationRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class AvatarFirstPersonRenderer {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
			Hemomancy.MOD_ID, "textures/models/armor/avatar_glow.png");
	private static BloodAvatarModel<AbstractClientPlayer> model;

	private AvatarFirstPersonRenderer() {
	}

	static boolean shouldRender(boolean firstPerson, String activeAvatarForm) {
		return firstPerson && AvatarManifestationRules.isAvatarForm(activeAvatarForm);
	}

	static boolean shouldRenderHelmet(int stage) {
		return stage >= 4;
	}

	static boolean shouldRenderHeldItems(int stage) {
		return stage >= 1;
	}

	@SubscribeEvent
	public static void render(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
		Minecraft minecraft = Minecraft.getInstance();
		AbstractClientPlayer player = minecraft.player;
		if (player == null || !(player.level() instanceof ClientLevel)
				|| !minecraft.options.getCameraType().isFirstPerson()) return;
		String activeForm = HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> known.getActiveAvatarForm()).orElse("");
		float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
		AvatarManifestationTransition.Sample transition = AvatarManifestationTransition.sample(
				player.getUUID(), activeForm, player.tickCount, partialTick);
		if (!transition.renders()) return;
		var stats = AvatarManifestationRules.stats(transition.form()).orElse(null);
		if (stats == null) return;
		if (model == null) {
			model = new BloodAvatarModel<>(minecraft.getEntityModels().bakeLayer(BloodAvatarModel.layer));
		}

		float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
		float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot) - bodyYaw;
		float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
		float limbAmount = Math.min(1.0F, player.walkAnimation.speed(partialTick));
		float limbSwing = player.walkAnimation.position(partialTick);
		float age = player.tickCount + partialTick;
		model.attackTime = player.getAttackAnim(partialTick);
		model.riding = player.isPassenger();
		model.young = false;
		model.prepareMobModel(player, limbSwing, limbAmount, partialTick);
		model.setupAnim(player, limbSwing, limbAmount, age, headYaw, pitch);
		model.setStage(stats.stage());

		Vec3 camera = event.getCamera().getPosition();
		PoseStack poseStack = event.getPoseStack();
		poseStack.pushPose();
		poseStack.translate(
				Mth.lerp(partialTick, player.xOld, player.getX()) - camera.x,
				Mth.lerp(partialTick, player.yOld, player.getY()) - camera.y,
				Mth.lerp(partialTick, player.zOld, player.getZ()) - camera.z);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
		float scale = player.getScale();
		poseStack.scale(-scale, -scale, scale);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		BloodAvatarLayer.applyEmergencePose(poseStack, transition, stats.avatarVisualScale());

		RenderType renderType = transition.warping()
				? BloodAvatarLayer.bloodTransitionType(player, poseStack, age, transition)
				: RenderTypeInit.firstPersonEnergySwirl(TEXTURE, age * .01F % 4.0F, age * .01F % 2.0F);
		MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
		VertexConsumer consumer = buffer.getBuffer(renderType);
		int packedLight = LevelRenderer.getLightColor(player.level(), player.blockPosition());
		model.head.visible = shouldRenderHelmet(stats.stage());
		model.renderToBuffer(poseStack, consumer, packedLight, 0,
				transition.warping() ? BloodAvatarLayer.FIRST_PERSON_BLOOD_TRANSITION_COLOR : 0x4D7F7F7F);
		if (transition.phase() == AvatarManifestationTransition.Phase.ACTIVE
				&& shouldRenderHeldItems(stats.stage())) {
			renderHeldItem(player, player.getMainHandItem(), player.getMainArm(), poseStack, buffer, packedLight);
			renderHeldItem(player, player.getOffhandItem(), player.getMainArm().getOpposite(), poseStack, buffer,
					packedLight);
		}
		model.head.visible = true;
		poseStack.popPose();
		buffer.endBatch(renderType);
	}

	private static void renderHeldItem(AbstractClientPlayer player, ItemStack stack, HumanoidArm arm,
			PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (stack.isEmpty()) return;
		poseStack.pushPose();
		model.translateToHand(arm, poseStack);
		AvatarHeldItemTransform.apply(poseStack, arm);
		boolean left = arm == HumanoidArm.LEFT;
		Minecraft.getInstance().getItemRenderer().renderStatic(player, stack,
				AvatarHeldItemTransform.displayContext(arm),
				left, poseStack, buffer, player.level(), packedLight, 0,
				player.getId() + (left ? 1 : 0));
		poseStack.popPose();
	}
}
