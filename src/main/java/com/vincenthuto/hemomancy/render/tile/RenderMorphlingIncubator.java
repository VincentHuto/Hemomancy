package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.tile.BlockEntityMorphlingIncubator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RenderMorphlingIncubator implements BlockEntityRenderer<BlockEntityMorphlingIncubator> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderMorphlingIncubator(BlockEntityRendererProvider.Context p_173636_) {
	}

	@Override
	public void render(BlockEntityMorphlingIncubator te, float partialTicks, PoseStack ms, MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		ms.pushPose();

//		int items = 0;
//		for (int i = 0; i < te.getSizeInventory(); i++) {
//			if (te.getItemHandler().getStackInSlot(i).isEmpty()) {
//				break;
//			} else {
//				items++;
//			}
//		}
//		float[] angles = new float[te.getSizeInventory()];
//
//		float anglePer = 360F / items;
//		float totalAngle = 0F;
//		for (int i = 0; i < angles.length; i++) {
//			angles[i] = totalAngle += anglePer;
//		}
//
//		double time = ClientTickHandler.ticksInGame + partialTicks;
//
//		for (int i = 0; i < te.getSizeInventory(); i++) {
//			ms.pushPose();
//			ms.translate(0.5F, 1.25F, 0.5F);
//			ms.mulPose(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
//			ms.translate(1.125F, 0F, 0.25F);
//			ms.mulPose(Vector3f.YP.rotationDegrees(90F));
//			ms.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
//			ItemStack stack = te.getItemHandler().getStackInSlot(i);
//			Minecraft mc = Minecraft.getInstance();
//			if (!stack.isEmpty()) {
//				mc.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GROUND, combinedLightIn,
//						combinedOverlayIn, ms, bufferIn, 0);
//			}
//			ms.popPose();
//		}

		ms.popPose();

	}
}
