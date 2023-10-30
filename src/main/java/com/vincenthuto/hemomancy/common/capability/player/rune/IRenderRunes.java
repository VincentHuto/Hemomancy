package com.vincenthuto.hemomancy.common.capability.player.rune;

//GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IRenderRunes extends IRune {

	enum RenderType {
		BODY, HEAD;
	}

	@OnlyIn(Dist.CLIENT)
	public static void doRender(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity player, PoseStack ms,
			MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch) {
	}

	void onPlayerRuneRender(PoseStack matrix, int packedLight, MultiBufferSource iRenderTypeBuffer, Player player,
			RenderType type, float partialTicks);

	void onPlayerRuneRender(PoseStack matrix, ItemStack stack, int packedLight, MultiBufferSource iRenderTypeBuffer,
			Player player, RenderType type, float partialTicks);
}