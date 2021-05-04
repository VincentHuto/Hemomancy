package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.capa.rune.IRunesItemHandler;
import com.huto.hemomancy.capa.rune.RunesCapabilities;
import com.huto.hemomancy.event.ClientTickHandler;
import com.huto.hemomancy.tile.TileEntityRuneModStation;
import com.hutoslib.util.ClientUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.math.vector.Vector3f;

public class RenderRuneModStation extends TileEntityRenderer<TileEntityRuneModStation> {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

	public RenderRuneModStation(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(TileEntityRuneModStation te, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		PlayerEntity player = ClientUtils.getClientPlayer();
		if (player.isAlive()) {
			GlStateManager.pushMatrix();
			GlStateManager.color4f(1F, 1F, 1F, 1F);

			IRunesItemHandler runes = player.getCapability(RunesCapabilities.RUNES)
					.orElseThrow(IllegalArgumentException::new);
			int items = 0;
			for (int i = 0; i < runes.getSlots(); i++) {
				items++;
			}
			float[] angles = new float[runes.getSlots()];
			float anglePer = 360F / items;
			float totalAngle = 0F;
			for (int i = 0; i < angles.length; i++) {
				angles[i] = totalAngle += anglePer;
			}

			double time = ClientTickHandler.ticksInGame + partialTicks;
			Minecraft mc = Minecraft.getInstance();
			matrixStackIn.push();
			matrixStackIn.translate(0.5F, 1.25F, 0.5F);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angles[0] + (float) time));
			// Edit True Radius
			matrixStackIn.translate(0.025F, -0.3F, 0.025F);
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90f));
			// Edit Radius Movement
			matrixStackIn.translate(0D, 0.175D + 0 * 0.55, 0F);
			// Block/Item Scale
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			matrixStackIn.scale(0.25f, 0.25f, 0.25f);

			ItemStack stack = runes.getStackInSlot(0);
			if (!stack.isEmpty()) {
				mc.getItemRenderer().renderItem(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
						matrixStackIn, bufferIn);
			}
			matrixStackIn.pop();

			for (int i = 1; i < runes.getSlots(); i++) {
				matrixStackIn.push();
				matrixStackIn.translate(0.5F, 1F, 0.5F);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
				matrixStackIn.translate(0.025F, -0.25F, 0.025F);
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90f));
				matrixStackIn.translate(0.35, 0.3175D, 0F);
				matrixStackIn.scale(0.1f, 0.1f, 0.1f);
				ItemStack stack2 = runes.getStackInSlot(i);
				mc.getItemRenderer().renderItem(stack2, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
						matrixStackIn, bufferIn);
				matrixStackIn.pop();
			}
			GlStateManager.popMatrix();
		}
	}
}
