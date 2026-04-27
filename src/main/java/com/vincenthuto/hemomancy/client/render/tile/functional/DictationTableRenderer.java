package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.functional.DictationTableBlock;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.tile.functional.DictationTableBlockEntity;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;

import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DictationTableRenderer implements BlockEntityRenderer<DictationTableBlockEntity> {
	private static final ResourceLocation FALLBACK_TEXTURE = Hemomancy.rloc("textures/entity/liber_sanguinum.png");

	private final BookModel bookModel;

	public DictationTableRenderer(BlockEntityRendererProvider.Context context) {
		this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
	}

	@Override
	public void render(DictationTableBlockEntity table, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		ItemStack liber = table.getLiber();
		if (!MemoHelper.isLiber(liber)) {
			return;
		}

		Direction facing = table.getBlockState().getValue(DictationTableBlock.FACING);
		ResourceLocation texture = liber.getItem() instanceof ItemGuideBook guideBook && guideBook.getTexture() != null
				? guideBook.getTexture()
				: FALLBACK_TEXTURE;
		poseStack.pushPose();
		poseStack.translate(0.5D, 1.085D, 0.5D);
		poseStack.mulPose(Axis.YP.rotationDegrees(rotationForFacing(facing) + 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
		poseStack.scale(0.62F, 0.62F, 0.62F);
		bookModel.setupAnim(0.0F, 0.08F, 0.92F, 1.0F);
		VertexConsumer vertexConsumer = bufferSource.getBuffer(bookModel.renderType(texture));
		bookModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();
	}

	private static float rotationForFacing(Direction facing) {
		return switch (facing) {
		case NORTH -> 0.0F;
		case EAST -> -90.0F;
		case WEST -> 90.0F;
		default -> 180.0F;
		};
	}
}
