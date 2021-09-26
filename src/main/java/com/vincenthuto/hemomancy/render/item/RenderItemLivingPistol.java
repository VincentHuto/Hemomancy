package com.vincenthuto.hemomancy.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.item.tool.living.ItemLivingPistol;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class RenderItemLivingPistol extends BlockEntityWithoutLevelRenderer {

	public RenderItemLivingPistol(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transform, PoseStack ms,
			MultiBufferSource buffers, int light, int overlay) {
		System.out.println("rerwr");
		if (stack.getItem()instanceof ItemLivingPistol pistol) {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
			InteractionHand activeHand = player.getUsedItemHand();
			if (itemIsInUse) {
				if (activeHand == InteractionHand.MAIN_HAND) {
					System.out.println("t");
					if (transform == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
						ms.mulPose(new Quaternion(Vector3f.XP, -20, true));
						ms.translate(-.55, 0, 0.);

					}
				} else {
					if (transform == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
						ms.mulPose(new Quaternion(Vector3f.XP, -20, true));
						ms.translate(.55, 0, 0.);

					}
				}

			}
		}

	}
}