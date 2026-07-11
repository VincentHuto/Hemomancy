package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class IronBrazierRenderer implements BlockEntityRenderer<IronBrazierBlockEntity> {
	private final ItemRenderer itemRenderer;

	public IronBrazierRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(IronBrazierBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		ItemStack displayed = te.getOfferingDisplayStack();
		if (displayed.isEmpty()) {
			return;
		}

		long gameTime = te.getLevel() != null ? te.getLevel().getGameTime() : 0L;
		float time = gameTime + partialTicks;
		float bob = Mth.sin(time * 0.08F) * 0.035F;
		float spin = (time * 2.0F) % 360.0F;

		poseStack.pushPose();
		poseStack.translate(0.5F, 1.05F + bob, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(spin));
		poseStack.mulPose(Axis.XP.rotationDegrees(12.0F));
		poseStack.scale(0.5F, 0.5F, 0.5F);
		itemRenderer.renderStatic(null, displayed, ItemDisplayContext.FIXED, false,
				poseStack, buffer, te.getLevel(), combinedLight, combinedOverlay, 0);
		poseStack.popPose();
	}
}
