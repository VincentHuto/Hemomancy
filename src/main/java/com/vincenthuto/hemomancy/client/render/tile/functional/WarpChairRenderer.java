package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.tile.functional.WarpChairBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class WarpChairRenderer implements BlockEntityRenderer<WarpChairBlockEntity> {
	public WarpChairRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(WarpChairBlockEntity chair, float partialTick, PoseStack stack,
			MultiBufferSource buffers, int light, int overlay) {
		BlockState state = chair.getBlockState();
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = blockRenderer.getBlockModel(state);
		ModelData modelData = chair.getModelData();
		float green = chair.isPaired() ? 160.0F / 255.0F : 1.0F;
		stack.pushPose();
		for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(42L), modelData)) {
			blockRenderer.getModelRenderer().renderModel(stack.last(),
					buffers.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false)),
					state, model, 1.0F, green, 1.0F, light, overlay, modelData, renderType);
		}
		stack.popPose();
	}

	@Override
	public AABB getRenderBoundingBox(WarpChairBlockEntity chair) {
		return new AABB(chair.getBlockPos()).expandTowards(0.0D, 1.0D, 0.0D);
	}
}
