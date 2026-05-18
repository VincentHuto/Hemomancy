package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.DictationTableModel;
import com.vincenthuto.hemomancy.common.block.inscription.DictationTableBlock;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.tile.functional.DictationTableBlockEntity;
import com.vincenthuto.hutoslib.common.item.BookAnimState;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class DictationTableRenderer implements BlockEntityRenderer<DictationTableBlockEntity> {
	private static final ResourceLocation FALLBACK_TEXTURE = Hemomancy.rloc("textures/entity/liber_sanguinum.png");
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_dictation_table.png");

	private final BookModel bookModel;
	private final DictationTableModel tableModel;

	public DictationTableRenderer(BlockEntityRendererProvider.Context context) {
		this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
		this.tableModel = new DictationTableModel(context.bakeLayer(DictationTableModel.LAYER_LOCATION));
	}

	@Override
	public void render(DictationTableBlockEntity table, float partialTicks, PoseStack poseStack,
			MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		ItemStack liber = table.getLiber();
		Direction facing = table.getBlockState().getValue(DictationTableBlock.FACING);

		poseStack.pushPose();
		poseStack.translate(0.5D, 1.5D, 0.5D);
		poseStack.mulPose(Axis.YP.rotationDegrees(rotationForFacing(facing) + 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		VertexConsumer vertexTableConsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
		tableModel.renderToBuffer(poseStack, vertexTableConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();


		if (!MemoHelper.isLiber(liber)) {
			return;
		}

		if (!(liber.getItem() instanceof ItemGuideBook item)) {
			return;
		}

		// HutosLib now keeps book animation state per entity instead of on the item
		// singleton, so sample the local client's cached state for page flip motion.
		BookAnimState state = ItemGuideBook.getOrCreateState(
				Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null);
		float f3 = Mth.lerp(partialTicks, state.oFlip, state.flip);
		float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
		float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;

		ResourceLocation texture = item.getTexture() != null ? item.getTexture() : FALLBACK_TEXTURE;
		poseStack.pushPose();
		poseStack.translate(0.5D, 1.15D, 0.5D);
		poseStack.mulPose(Axis.YP.rotationDegrees(rotationForFacing(facing) + 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
		poseStack.scale(1, 1F, 1F);
		// The book is always shown open on the table (close = 1.0F); page-flip angles
		// come from the local client's cached guidebook animation state.
		bookModel.setupAnim(0.0F, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), 1.0F);
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
