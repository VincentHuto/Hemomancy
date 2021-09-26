package com.vincenthuto.hemomancy.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.capa.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.rune.RunesCapabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RenderRunesLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {

	public RenderRunesLayer(RenderLayerParent<T, M> render) {
		super(render);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int packedLight, Player player,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
			float scale) {
		player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
			dispatchRenders(matrixStack, packedLight, iRenderTypeBuffer, inv, player, IRenderRunes.RenderType.BODY,
					partialTicks);

			matrixStack.pushPose();
			EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
			EntityModel<?> model = ((RenderLayerParent<?, ?>) renderer).getModel();
			if (model instanceof HumanoidModel<?>) {
				HumanoidModel<?> biModel = (HumanoidModel<?>) model;
				biModel.head.translateAndRotate(matrixStack);
			}
			dispatchRenders(matrixStack, packedLight, iRenderTypeBuffer, inv, player, IRenderRunes.RenderType.HEAD,
					partialTicks);
			matrixStack.popPose();
		});
	}

	private void dispatchRenders(PoseStack matrix, int packedLightIn, MultiBufferSource iRenderTypeBuffer,
			IRunesItemHandler inv, Player player, IRenderRunes.RenderType type, float partialTicks) {
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				stack.getCapability(RunesCapabilities.ITEM_RUNE).ifPresent(rune -> {
					if (rune instanceof IRenderRunes) {
						matrix.pushPose();
					//	if (!(stack.getItem() instanceof ItemArmBanner)) {
							((IRenderRunes) rune).onPlayerRuneRender(matrix, packedLightIn, iRenderTypeBuffer, player,
									type, partialTicks);
//						} else {
//							if (type != IRenderRunes.RenderType.HEAD) {
//								ItemArmBanner arm = (ItemArmBanner) stack.getItem();
//								arm.onPlayerRuneRender(matrix, stack, packedLightIn, iRenderTypeBuffer, player, type,
//										partialTicks);
//							}
//						}
						matrix.popPose();

					}
				});
			}
		}
	}
}