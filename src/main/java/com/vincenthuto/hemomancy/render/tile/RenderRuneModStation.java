package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.capa.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.event.ClientTickHandler;
import com.vincenthuto.hemomancy.tile.BlockEntityRuneModStation;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RenderRuneModStation implements BlockEntityRenderer<BlockEntityRuneModStation> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderRuneModStation(BlockEntityRendererProvider.Context p_173636_) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(BlockEntityRuneModStation te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		Player player = HLClientUtils.getClientPlayer();
		if (player.isAlive()) {
			// GlStateManager._pushMatrix();
			// GlStateManager._color4f(1F, 1F, 1F, 1F);

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
			matrixStackIn.pushPose();
			matrixStackIn.translate(0.5F, 1.25F, 0.5F);
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angles[0] + (float) time));
			// Edit True Radius
			matrixStackIn.translate(0.025F, -0.3F, 0.025F);
			matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90f));
			// Edit Radius Movement
			matrixStackIn.translate(0D, 0.175D + 0 * 0.55, 0F);
			// Block/Item Scale
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			matrixStackIn.scale(0.25f, 0.25f, 0.25f);

			ItemStack stack = runes.getStackInSlot(0);
			if (!stack.isEmpty()) {
				mc.getItemRenderer().renderStatic(stack, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
						matrixStackIn, bufferIn, 0);
			}
			matrixStackIn.popPose();

			for (int i = 1; i < runes.getSlots(); i++) {
				matrixStackIn.pushPose();
				matrixStackIn.translate(0.5F, 1F, 0.5F);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
				matrixStackIn.translate(0.025F, -0.25F, 0.025F);
				matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(90f));
				matrixStackIn.translate(0.35, 0.3175D, 0F);
				matrixStackIn.scale(0.1f, 0.1f, 0.1f);
				ItemStack stack2 = runes.getStackInSlot(i);
				mc.getItemRenderer().renderStatic(stack2, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
						matrixStackIn, bufferIn, 0);
				matrixStackIn.popPose();
			}
			// GlStateManager._popMatrix();
		}
	}
}
