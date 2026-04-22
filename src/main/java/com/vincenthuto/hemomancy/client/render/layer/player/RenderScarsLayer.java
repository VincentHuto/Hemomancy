package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.scar.IRenderScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScarsItemHandler;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RenderScarsLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {

	public RenderScarsLayer(LivingEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int packedLightIn, Player player,
			float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
			float scale) {
		IScarsItemHandler inv = HemoCapabilityAccess.requireScars(player);

		matrixStack.pushPose();
		this.dispatchRenders(matrixStack, packedLightIn, iRenderTypeBuffer, inv, player, 
				partialTicks);
		matrixStack.popPose();

	}

	private void dispatchRenders(PoseStack matrix, int packedLightIn, MultiBufferSource iRenderTypeBuffer,
			IScarsItemHandler inv, Player player, float partialTicks) {
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				HemoCapabilityAccess.getScar(stack).ifPresent(scar -> {
					if (scar instanceof IRenderScar) {
						matrix.pushPose();
						((IRenderScar) scar).onPlayerScarRender(matrix, stack, packedLightIn, iRenderTypeBuffer, player,
								 partialTicks);
						matrix.popPose();

					}
				});
			}
		}
	}
}