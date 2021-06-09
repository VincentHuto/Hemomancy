package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.event.ClientTickHandler;
import com.huto.hemomancy.tile.TileEntityMorphlingIncubator;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.math.vector.Vector3f;

public class RenderMorphlingIncubator extends TileEntityRenderer<TileEntityMorphlingIncubator> {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

	public RenderMorphlingIncubator(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TileEntityMorphlingIncubator te, float partialTicks, MatrixStack ms, IRenderTypeBuffer bufferIn,
			int combinedLightIn, int combinedOverlayIn) {

		ms.push();

		int items = 0;
		for (int i = 0; i < te.getSizeInventory(); i++) {
			if (te.getItemHandler().getStackInSlot(i).isEmpty()) {
				break;
			} else {
				items++;
			}
		}
		float[] angles = new float[te.getSizeInventory()];

		float anglePer = 360F / items;
		float totalAngle = 0F;
		for (int i = 0; i < angles.length; i++) {
			angles[i] = totalAngle += anglePer;
		}

		double time = ClientTickHandler.ticksInGame + partialTicks;

		for (int i = 0; i < te.getSizeInventory(); i++) {
			ms.push();
			ms.translate(0.5F, 1.25F, 0.5F);
			ms.rotate(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
			ms.translate(1.125F, 0F, 0.25F);
			ms.rotate(Vector3f.YP.rotationDegrees(90F));
			ms.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
			ItemStack stack = te.getItemHandler().getStackInSlot(i);
			Minecraft mc = Minecraft.getInstance();
			if (!stack.isEmpty()) {
				mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, combinedLightIn,
						combinedOverlayIn, ms, bufferIn);
			}
			ms.pop();
		}

		ms.pop();

	}
}
