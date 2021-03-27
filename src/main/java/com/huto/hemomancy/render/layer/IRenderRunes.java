package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.capa.rune.IRune;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IRenderRunes extends IRune {

	void onPlayerRuneRender(MatrixStack matrix, int packedLight, IRenderTypeBuffer iRenderTypeBuffer,
			PlayerEntity player, RenderType type, float partialTicks);

	final class Helper {

		public static void rotateIfSneaking(PlayerEntity player) {
			if (player.isSneaking())
				applySneakingRotation();
		}

		@SuppressWarnings("deprecation")
		public static void applySneakingRotation() {
			GlStateManager.translatef(0F, 0.2F, 0F);
			GlStateManager.rotatef(90F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
		}

		@SuppressWarnings("deprecation")
		public static void translateToHeadLevel(PlayerEntity player) {
			GlStateManager.translatef(0, -player.getEyeHeight(), 0);
			if (player.isSneaking())
				GlStateManager.translatef(0.25F * MathHelper.sin(player.rotationPitch * (float) Math.PI / 180),
						0.25F * MathHelper.cos(player.rotationPitch * (float) Math.PI / 180), 0F);
		}

		@SuppressWarnings("deprecation")
		public static void translateToFace() {
			GlStateManager.rotatef(90F, 0F, 1F, 0F);
			GlStateManager.rotatef(180F, 1F, 0F, 0F);
			GlStateManager.translatef(0f, -4.35f, -1.27f);
		}

		@SuppressWarnings("deprecation")
		public static void defaultTransforms() {
			GlStateManager.translatef(0.0f, 3.0f, 1.0f);
			GlStateManager.scalef(0.55f, 0.55f, 0.55f);
		}

		@SuppressWarnings("deprecation")
		public static void translateToChest() {
			GlStateManager.rotatef(180F, 1F, 0F, 0F);
			GlStateManager.translatef(0F, -3.2F, -0.85F);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void doRender(BipedModel<?> bipedModel, ItemStack stack, LivingEntity player, MatrixStack ms,
			IRenderTypeBuffer buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch) {

	}

	enum RenderType {

		BODY,

		HEAD;
	}
}