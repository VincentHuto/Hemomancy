package com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.HematicArmatureRenderer;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hutoslib.client.HlClientTickHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/** Draws the saved stage's whole frame shrunk into one block, so a moved Armature keeps its look. */
public class HematicArmatureItemRenderer extends BlockEntityWithoutLevelRenderer {
	/** The frame is five blocks wide and tall; this fits it to the item's unit cube. */
	private static final float FIT = .2F;

	public HematicArmatureItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		var state = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)
				.apply(BlockInit.hematic_armature.get().defaultBlockState())
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
		var mc = Minecraft.getInstance();
		float time = HlClientTickHandler.ticksInGame + HlClientTickHandler.partialTicks;
		poseStack.pushPose();
		poseStack.translate(.5, .5, .5);
		poseStack.scale(FIT, FIT, FIT);
		// Centre the frame: x spans -2..3 blocks, y 0..5 and z -1..1 around the controller.
		poseStack.translate(-.5, -2.45, -.0625);
		mc.getBlockRenderer().renderSingleBlock(state, poseStack, buffer, combinedLight, combinedOverlay);
		HematicArmatureRenderer.renderFrame(mc, StationTierProperty.stage(state), time, poseStack, buffer,
				combinedLight, combinedOverlay);
		poseStack.popPose();
	}
}
