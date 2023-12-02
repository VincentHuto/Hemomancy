package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.event.ClientTickHandler;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRenderRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hutoslib.common.item.ItemArmBanner;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class RenderRunesLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {

	public RenderRunesLayer(LivingEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int packedLightIn, Player player,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
			float scale) {
		IRunesItemHandler inv = player.getCapability(RunesCapabilities.RUNES).orElseThrow(NullPointerException::new);
//
//		matrixStack.pushPose();
//
//		int items = 0;
//		for (int i = 0; i < inv.getSlots(); i++) {
//			if (!inv.getStackInSlot(i).isEmpty()) {
//				items++;
//			}
//		}
//		float[] angles = new float[inv.getSlots()];
//
//		float anglePer = 360F / items;
//		float totalAngle = 0F;
//		for (int i = 0; i < angles.length; i++) {
//			angles[i] = totalAngle += anglePer;
//		}
//
//		double time = ClientTickHandler.ticksInGame + partialTicks;
//		matrixStack.translate(0, -1.25F, 0);
//		matrixStack.mulPose(Vector3.ZP.rotationDegrees(180F).toMoj());
//
//		for (int i = 0; i <= items; i++) {
//
//			matrixStack.mulPose(Vector3.YP.rotationDegrees(angles[i] + (float) time).toMoj());
//			matrixStack.mulPose(Vector3.YP.rotationDegrees(90F).toMoj());
//			matrixStack.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
//			ItemStack stack = inv.getStackInSlot(i);
//			Minecraft mc = Minecraft.getInstance();
//			if (!stack.isEmpty()) {
//				mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, packedLightIn,
//						OverlayTexture.NO_OVERLAY, matrixStack, iRenderTypeBuffer, player.level(), 0);
//			}
//		}
//		matrixStack.popPose();

		matrixStack.pushPose();

		this.dispatchRenders(matrixStack, packedLightIn, iRenderTypeBuffer, inv, player, IRenderRune.RenderType.BODY,
				partialTicks);
		matrixStack.popPose();

		matrixStack.pushPose();
		float yaw = player.yHeadRotO + (player.yHeadRot - player.yHeadRotO) * partialTicks;
		float yawOffset = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO) * partialTicks;
		float xRot = ObfuscationReflectionHelper.getPrivateValue(Entity.class, player, "f_19858_");
		float pitch = player.xRotO + (xRot - player.xRotO) * partialTicks;
		matrixStack.mulPose(new Quaternion(Vector3.YN, yawOffset, true).toMoj());
		matrixStack.mulPose(new Quaternion(Vector3.YP, yaw - 270, true).toMoj());
		matrixStack.mulPose(new Quaternion(Vector3.ZP, pitch, true).toMoj());
		this.dispatchRenders(matrixStack, packedLightIn, iRenderTypeBuffer, inv, player, IRenderRune.RenderType.HEAD,
				partialTicks);
		matrixStack.popPose();

	}

	private void dispatchRenders(PoseStack matrix, int packedLightIn, MultiBufferSource iRenderTypeBuffer,
			IRunesItemHandler inv, Player player, IRenderRune.RenderType type, float partialTicks) {
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				stack.getCapability(RunesCapabilities.ITEM_RUNE).ifPresent(rune -> {
					if (rune instanceof IRenderRune) {
						matrix.pushPose();
						((IRenderRune) rune).onPlayerRuneRender(matrix, stack, packedLightIn, iRenderTypeBuffer, player,
								type, partialTicks);
						matrix.popPose();

					}
				});
			}
		}
	}
}