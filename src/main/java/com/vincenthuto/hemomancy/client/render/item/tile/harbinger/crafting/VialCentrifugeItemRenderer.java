package com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.VialCentrifugeRenderer;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty;
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

/** Draws the saved stage's body with its empty rotor idling, so a moved centrifuge keeps its look. */
public class VialCentrifugeItemRenderer extends BlockEntityWithoutLevelRenderer {

	public VialCentrifugeItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		var state = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)
				.apply(BlockInit.vial_centrifuge.get().defaultBlockState())
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
		int stage = StationTierProperty.stage(state);
		var mc = Minecraft.getInstance();
		float time = HlClientTickHandler.ticksInGame + HlClientTickHandler.partialTicks;
		mc.getBlockRenderer().renderSingleBlock(state, poseStack, buffer, combinedLight, combinedOverlay);
		VialCentrifugeRenderer.renderMachine(mc, stage, time * 1.5F, time * .375F, 0, time, null, poseStack, buffer,
				combinedLight, combinedOverlay);
	}
}
