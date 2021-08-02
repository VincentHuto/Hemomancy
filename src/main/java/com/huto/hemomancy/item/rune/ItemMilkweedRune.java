package com.huto.hemomancy.item.rune;

import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.IRunesItemHandler;
import com.huto.hemomancy.capa.rune.RunesCapabilities;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.event.ClientTickHandler;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.render.layer.IRenderRunes;
import com.mojang.blaze3d.platform.//GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemMilkweedRune extends ItemContractRune implements IRune, IRenderRunes {

	public ItemMilkweedRune(Properties properties, EnumBloodTendency tendencyIn, int deepenAmount) {
		super(properties, tendencyIn, deepenAmount);

	}

	@Override
	public void onPlayerRuneRender(PoseStack matrix, int packedLightIn, MultiBufferSource buffer, Player player,
			RenderType type, float partialTicks) {

		if (type == RenderType.HEAD) {
			Lighting.turnBackOn();
			matrix.mulPose(Vector3f.XN.rotationDegrees(180f));
			matrix.scale(0.5f, 0.5f, 0.5f);
			matrix.translate(0, 0.5, 0.5);
			Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(ItemInit.rune_milkweed_c.get()),
					TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrix, buffer);

		}

		if (type == RenderType.HEAD) {
			if (player.isAlive()) {
				//GlStateManager._pushMatrix();
				//GlStateManager._color4f(1F, 1F, 1F, 1F);

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

				for (int i = 1; i < runes.getSlots(); i++) {
					matrix.pushPose();
					matrix.mulPose(Vector3f.YP.rotationDegrees(angles[i] + (float) time));
					matrix.translate(0.5F, 0.4f, 0.5F);
					matrix.mulPose(Vector3f.XP.rotationDegrees(90f));
					matrix.mulPose(Vector3f.YN.rotationDegrees(90f));
					matrix.translate(0.2, 0.375D, 0F);
					ItemStack stack2 = runes.getStackInSlot(i);
					mc.getItemRenderer().renderStatic(stack2, TransformType.FIXED, packedLightIn,
							OverlayTexture.NO_OVERLAY, matrix, buffer);
					matrix.popPose();
				}
				//GlStateManager._popMatrix();
			}
		}
	}

	@Override
	public void onPlayerRuneRender(PoseStack matrix, ItemStack stack, int packedLight,
			MultiBufferSource iRenderTypeBuffer, Player player, RenderType type, float partialTicks) {

	}
}
