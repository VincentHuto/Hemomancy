package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HeartyCompassItem;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.HeartyCompassRules;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.LastDeathMemory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HeartyCompassItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/item/hearty_compass_renderer.png");

	public HeartyCompassItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
							 MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (!(stack.getItem() instanceof HeartyCompassItem)) {
			return;
		}

		poseStack.pushPose();
		applyDisplayTransform(displayContext, poseStack);

		VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		PoseStack.Pose pose = poseStack.last();
		drawQuad(consumer, pose, -0.5F, -0.5F, 0.5F, 0.5F, 0.0F,
				0.0F, 0.0F, 0.5F, 1.0F, packedLight, 255);

		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(needleRotationDegrees()));
		PoseStack.Pose needlePose = poseStack.last();
		drawQuad(consumer, needlePose, -0.5F, -0.5F, 0.5F, 0.5F, 0.03F,
				0.5F, 0.0F, 1.0F, 1.0F, packedLight, 255);
		poseStack.popPose();

		poseStack.popPose();
	}

	private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		poseStack.translate(0.5F, 0.5F, 0.5F);
		if (displayContext == ItemDisplayContext.GUI) {
			poseStack.scale(1.02F, 1.02F, 1.02F);
			return;
		}
		if (displayContext == ItemDisplayContext.GROUND) {
			poseStack.scale(0.48F, 0.48F, 0.48F);
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			return;
		}
		if (displayContext == ItemDisplayContext.FIXED) {
			poseStack.scale(0.84F, 0.84F, 0.84F);
			return;
		}
		if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
				|| displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
			poseStack.scale(0.76F, 0.76F, 0.76F);
			poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
			return;
		}
		poseStack.scale(0.62F, 0.62F, 0.62F);
		poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
	}

	private static float needleRotationDegrees() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (mc.level == null || player == null) {
			return wobble(mc, 0.0F);
		}

		LastDeathMemory memory = HemoCapabilityAccess.getLastDeathMemory(player);
		if (!memory.hasMemory() || !memory.isSameDimension(mc.level.dimension().location().toString())) {
			return wobble(mc, memory.hasMemory() ? 91.0F : 0.0F);
		}

		double targetX = LastDeathMemory.unpackX(memory.packedPosition()) + 0.5D;
		double targetZ = LastDeathMemory.unpackZ(memory.packedPosition()) + 0.5D;
		float partial = mc.getTimer().getGameTimeDeltaPartialTick(false);
		double playerX = Mth.lerp(partial, player.xOld, player.getX());
		double playerZ = Mth.lerp(partial, player.zOld, player.getZ());
		float yaw = Mth.lerp(partial, player.yRotO, player.getYRot());
		return HeartyCompassRules.relativeNeedleDegrees(playerX, playerZ, yaw, targetX, targetZ);
	}

	private static float wobble(Minecraft mc, float offset) {
		float time = mc.level == null ? 0.0F : mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(false);
		return offset + Mth.sin(time * 0.23F) * 28.0F + Mth.sin(time * 0.071F) * 12.0F;
	}

	private static void drawQuad(VertexConsumer consumer, PoseStack.Pose pose,
								 float x0, float y0, float x1, float y1, float z,
								 float u0, float v0, float u1, float v1, int packedLight, int alpha) {
		consumer.addVertex(pose.pose(), x0, y1, z)
				.setColor(255, 255, 255, alpha)
				.setUv(u0, v1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(packedLight)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
		consumer.addVertex(pose.pose(), x1, y1, z)
				.setColor(255, 255, 255, alpha)
				.setUv(u1, v1)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(packedLight)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
		consumer.addVertex(pose.pose(), x1, y0, z)
				.setColor(255, 255, 255, alpha)
				.setUv(u1, v0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(packedLight)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
		consumer.addVertex(pose.pose(), x0, y0, z)
				.setColor(255, 255, 255, alpha)
				.setUv(u0, v0)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(packedLight)
				.setNormal(pose, 0.0F, 0.0F, 1.0F);
	}
}
