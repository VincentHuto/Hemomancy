package com.vincenthuto.hemomancy.common.item.rune;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRenderRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.RuneType;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemContractRune extends ItemRune implements IRenderRune {


	public ItemContractRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.RARE;
	}

	@Override
	public boolean isFoil(ItemStack stack) {

		return true;
	}

	@Override
	public RuneType getRuneType() {
		return RuneType.CONTRACT;
	}
	@Override
	public void onPlayerRuneRender(PoseStack matrix, ItemStack stack, int packedLight,
			MultiBufferSource iRenderTypeBuffer, Player player, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
	
			matrix.pushPose();
			matrix.mulPose(Vector3.ZP.rotationDegrees(180).toMoj()); // Edit

			matrix.mulPose(Vector3.YP.rotationDegrees(player.level().getGameTime()).toMoj()); // Edit
			matrix.translate(0.025F, 0.75F, 0.025F);
			matrix.mulPose(Vector3.YP.rotationDegrees(90f).toMoj()); // Edit Radius Movement
			matrix.scale(0.25f, 0.25f, 0.25f);
			if (!stack.isEmpty()) {
				mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
						matrix, iRenderTypeBuffer, player.level(), 0);
			}
			matrix.popPose();
	}



}
