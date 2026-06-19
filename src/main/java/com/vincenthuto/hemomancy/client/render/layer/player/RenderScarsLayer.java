package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IRenderHarbingerEquipment;

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
		matrixStack.pushPose();
		this.dispatchRenders(matrixStack, packedLightIn, iRenderTypeBuffer, player, partialTicks);
		matrixStack.popPose();

	}

	private void dispatchRenders(PoseStack matrix, int packedLightIn, MultiBufferSource iRenderTypeBuffer,
	                             Player player, float partialTicks) {
		HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
			ItemStack stack = scars.getFungalScar();
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof IRenderHarbingerEquipment renderer) {
					matrix.pushPose();
					renderer.onPlayerScarRender(matrix, stack, packedLightIn, iRenderTypeBuffer, player, partialTicks);
					matrix.popPose();
				}
			}
		});
	}
}
