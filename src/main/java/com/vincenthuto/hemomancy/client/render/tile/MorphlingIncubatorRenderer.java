package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.event.ClientTickHandler;
import com.vincenthuto.hemomancy.common.tile.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class MorphlingIncubatorRenderer implements BlockEntityRenderer<MorphlingIncubatorBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public MorphlingIncubatorRenderer(BlockEntityRendererProvider.Context p_173636_) {
	}

	@Override
	public void render(MorphlingIncubatorBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		ms.pushPose();

		int items = 0;
		for (int i = 0; i < te.inventorySize(); i++) {
			if (te.getItemHandler().getItem(i).isEmpty()) {
				break;
			} else {
				items++;
			}
		}
		float[] angles = new float[te.inventorySize()];

		float anglePer = 360F / items;
		float totalAngle = 0F;
		for (int i = 0; i < angles.length; i++) {
			angles[i] = totalAngle += anglePer;
		}

		double time = ClientTickHandler.ticksInGame + partialTicks;

		for (int i = 0; i < te.inventorySize(); i++) {
			ms.pushPose();
			ms.translate(0.5F, 1.25F, 0.5F);
			ms.mulPose(Vector3.YP.rotationDegrees(angles[i] + (float) time).toMoj());
			ms.translate(1.125F, 0F, 0.25F);
			ms.mulPose(Vector3.YP.rotationDegrees(90F).toMoj());
			ms.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
			ItemStack stack = te.getItemHandler().getItem(i);
			Minecraft mc = Minecraft.getInstance();
			if (!stack.isEmpty()) {
				mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, combinedLightIn,
						combinedOverlayIn, ms, bufferIn, te.getLevel(), 0);
			}
			ms.popPose();
		}

		ms.popPose();

	}
}
