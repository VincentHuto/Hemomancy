package com.huto.hemomancy.item.runes;

import com.huto.hemomancy.capabilities.mindrunes.IRune;
import com.huto.hemomancy.capabilities.mindrunes.IRunesItemHandler;
import com.huto.hemomancy.capabilities.mindrunes.RunesCapabilities;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;
import com.huto.hemomancy.event.ClientTickHandler;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.render.layer.IRenderRunes;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class ItemMilkweedRune extends ItemContractRune implements IRune, IRenderRunes {

	public ItemMilkweedRune(Properties properties, EnumBloodTendency tendencyIn, int deepenAmount) {
		super(properties, tendencyIn, deepenAmount);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPlayerRuneRender(MatrixStack matrix, int packedLightIn, IRenderTypeBuffer buffer, PlayerEntity player,
			RenderType type, float partialTicks) {

		if (type == RenderType.HEAD) {
			RenderHelper.enableStandardItemLighting();
			matrix.rotate(Vector3f.XN.rotationDegrees(180f));
			matrix.scale(0.5f, 0.5f, 0.5f);
			matrix.translate(0, 0.5, 0.5);
			Minecraft.getInstance().getItemRenderer().renderItem(new ItemStack(ItemInit.rune_milkweed_c.get()),
					TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrix, buffer);

		}

		if (type == RenderType.HEAD) {
			if (player.isAlive()) {
				GlStateManager.pushMatrix();
				GlStateManager.color4f(1F, 1F, 1F, 1F);

				IRunesItemHandler runes = player.getCapability(RunesCapabilities.RUNES)
						.orElseThrow(IllegalArgumentException::new);
				int items = 0;
				for (int i = 0; i < 3; i++) {
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

				for (int i = 1; i < runes.getSlots(); i++) {
					matrix.push();
					matrix.rotate(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
					matrix.translate(0.5F, 0.4f, 0.5F);
					matrix.rotate(Vector3f.XP.rotationDegrees(90f));
					matrix.rotate(Vector3f.YN.rotationDegrees(90f));
					matrix.translate(0.2, 0.375D, 0F);
					ItemStack stack2 = runes.getStackInSlot(i);
					mc.getItemRenderer().renderItem(stack2, TransformType.FIXED, packedLightIn,
							OverlayTexture.NO_OVERLAY, matrix, buffer);
					matrix.pop();
				}
				GlStateManager.popMatrix();
			}
		}
	}
}
