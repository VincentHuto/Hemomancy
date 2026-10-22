package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.ItemFungalScar;
import com.vincenthuto.hutoslib.math.Vector3;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
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
		if (!isFungalScarLayerVisible(player)) {
			return;
		}
		var scars = HemoCapabilityAccess.getScarState(player);
		ItemStack stack = scars.getFungalScar();
		if (!stack.isEmpty()) {
			if (stack.getItem() instanceof ItemFungalScar) {
				matrix.pushPose();
				renderFungalScar(matrix, stack, packedLightIn, iRenderTypeBuffer, player, partialTicks);
				matrix.popPose();
			}
		}
	}

	private static void renderFungalScar(PoseStack matrix, ItemStack stack, int packedLight, MultiBufferSource iRenderTypeBuffer,
			Player player, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();

		matrix.pushPose();
		matrix.mulPose(Vector3.ZP.rotationDegrees(180).toMoj());
		matrix.mulPose(Vector3.YP.rotationDegrees(player.level().getGameTime()).toMoj());
		matrix.translate(0.025F, 0.75F, 0.025F);
		matrix.mulPose(Vector3.YP.rotationDegrees(90f).toMoj());
		matrix.scale(0.25f, 0.25f, 0.25f);
		if (!stack.isEmpty()) {
			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
					matrix, iRenderTypeBuffer, player.level(), 0);
		}
		matrix.popPose();
	}

	private boolean isFungalScarLayerVisible(Player player) {
		return HemoCapabilityAccess.getEquipment(player).isRenderLayerVisible(IScars.FUNGAL_SLOT);
	}
}
