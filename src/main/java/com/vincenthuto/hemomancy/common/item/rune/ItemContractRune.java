package com.vincenthuto.hemomancy.common.item.rune;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRenderRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRenderRune.RenderType;
import com.vincenthuto.hemomancy.common.capability.player.rune.RuneType;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemContractRune extends Item implements IRenderRune {

	RenderType renderType;
	EnumBloodTendency assignedTendency;
	float deepenAmount;

	public ItemContractRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties);
		this.renderType = RenderType.HEAD;
		this.assignedTendency = tendencyIn;
		this.deepenAmount = deepenAmountIn;
	}

	public ItemContractRune(Properties properties, RenderType renderTypeIn, EnumBloodTendency tendencyIn,
			float deepenAmountIn) {
		super(properties);
		this.renderType = renderTypeIn;
		this.assignedTendency = tendencyIn;
		this.deepenAmount = deepenAmountIn;
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
			MultiBufferSource iRenderTypeBuffer, Player player, RenderType type, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (type == RenderType.BODY) {
	
			matrix.pushPose();
			matrix.mulPose(Vector3.YP.rotationDegrees(player.level().getGameTime()).toMoj()); // Edit
			matrix.translate(0.025F, -0.75F, 0.025F);
			matrix.mulPose(Vector3.YP.rotationDegrees(90f).toMoj()); // Edit Radius Movement
			matrix.scale(0.25f, 0.25f, 0.25f);
			if (!stack.isEmpty()) {
//				mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
//						matrix, iRenderTypeBuffer, player.level(), 0);
			}
			matrix.popPose();
		
		
		
		}

	}

	@Override
	public RenderType getRenderType() {
		return this.renderType;
	}


}
