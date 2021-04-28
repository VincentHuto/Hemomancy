package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.capa.rune.IRunesItemHandler;
import com.huto.hemomancy.capa.rune.RunesCapabilities;
import com.huto.hemomancy.item.armor.ItemArmBanner;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class RenderRunesLayer<T extends PlayerEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {

	public RenderRunesLayer(IEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int packedLight,
			PlayerEntity player, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
			dispatchRenders(matrixStack, packedLight, iRenderTypeBuffer, inv, player, IRenderRunes.RenderType.BODY,
					partialTicks);

			matrixStack.push();
			EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(player);
			EntityModel<?> model = ((IEntityRenderer<?, ?>) renderer).getEntityModel();
			if (model instanceof BipedModel<?>) {
				BipedModel<?> biModel = (BipedModel<?>) model;
				biModel.bipedHead.translateRotate(matrixStack);
			}
			dispatchRenders(matrixStack, packedLight, iRenderTypeBuffer, inv, player, IRenderRunes.RenderType.HEAD,
					partialTicks);
			matrixStack.pop();
		});
	}

	private void dispatchRenders(MatrixStack matrix, int packedLightIn, IRenderTypeBuffer iRenderTypeBuffer,
			IRunesItemHandler inv, PlayerEntity player, IRenderRunes.RenderType type, float partialTicks) {
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				stack.getCapability(RunesCapabilities.ITEM_RUNE).ifPresent(rune -> {
					if (rune instanceof IRenderRunes) {
						matrix.push();
						if (!(stack.getItem() instanceof ItemArmBanner)) {
							((IRenderRunes) rune).onPlayerRuneRender(matrix, packedLightIn, iRenderTypeBuffer, player,
									type, partialTicks);
						} else {
							if (type != IRenderRunes.RenderType.HEAD) {
								ItemArmBanner arm = (ItemArmBanner) stack.getItem();
								arm.onPlayerRuneRender(matrix, stack, packedLightIn, iRenderTypeBuffer, player, type,
										partialTicks);
							}
						}
						matrix.pop();

					}
				});
			}
		}
	}
}