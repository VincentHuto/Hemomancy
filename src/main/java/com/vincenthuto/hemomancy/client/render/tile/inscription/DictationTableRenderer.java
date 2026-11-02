package com.vincenthuto.hemomancy.client.render.tile.inscription;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.item.book.HemoBookBinding;
import com.vincenthuto.hemomancy.client.model.item.book.HemoBookLayers;
import com.vincenthuto.hemomancy.client.model.item.book.HemoBookModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HarbingerEscritoireBlock;
import com.vincenthuto.hutoslib.common.block.DictationTableBlock;
import com.vincenthuto.hutoslib.common.block.entity.DictationTableBlockEntity;
import com.vincenthuto.hutoslib.common.book.FieldNotes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Lays a Hemomancy book open on a Dictation Table or Harbinger Escritoire in its own model, pulsing while dictation
 * is pending; any other guide book falls back to HutosLib's vanilla-book drawing.
 */
public class DictationTableRenderer extends com.vincenthuto.hutoslib.client.render.block.DictationTableRenderer {
	/** The Escritoire's writing surface, and where the open book's middle sits on it, in block pixels (facing north). */
	private static final float DESK_TOP = 16.5F, DESK_DEPTH = 5.0F;
	/** Open past the held book's spread so it lies nearly flat on the desk instead of standing on its spine. */
	private static final float DESK_OPEN = 1.2F;
	/** How far the flat-open boards sit above the spine's back, in model units: the boards rest on the desk. */
	private static final float BOARD_LIFT = 3.0F;

	private final Map<HemoBookBinding, HemoBookModel> models;

	public DictationTableRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		this.models = HemoBookBinding.bakeAll(context.getModelSet());
	}

	@Override
	public void render(DictationTableBlockEntity table, float partialTicks, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		ItemStack book = table.getBook();
		HemoBookBinding binding = HemoBookBinding.of(book.getItem());
		if (binding == null || !(table.getBlockState().getBlock() instanceof DictationTableBlock block)) {
			super.render(table, partialTicks, pose, buffers, light, overlay);
			return;
		}
		HemoBookModel model = models.get(binding);
		boolean pending = block.supportsDictation() && block.pendingGlowColor() != 0
				&& FieldNotes.hasPending(Minecraft.getInstance().player, book, block);
		Direction facing = table.getBlockState().getValue(DictationTableBlock.FACING);
		pose.pushPose();
		if (block instanceof HarbingerEscritoireBlock) {
			poseOnDesk(pose, facing);
			model.setupAnim(0.0F, 0.1F, 0.9F, DESK_OPEN);
		} else {
			pose.translate(0.5D, block.bookHeight(), 0.5D);
			pose.mulPose(Axis.YP.rotationDegrees(rotationForFacing(facing) + block.bookRotationOffset()));
			pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
			pose.scale(HemoBookLayers.MODEL_SCALE, HemoBookLayers.MODEL_SCALE, HemoBookLayers.MODEL_SCALE);
			model.setupAnim(0.0F, 0.1F, 0.9F, 1.0F);
		}
		model.renderToBuffer(pose, buffers.getBuffer(binding.solid()), light, OverlayTexture.NO_OVERLAY, -1);
		if (pending) {
			float time = table.getLevel() == null ? 0 : AnimationTime.gameTime(table.getLevel(), partialTicks);
			int alpha = Math.round(35 + 25 * (0.5F + 0.5F * Mth.sin(time * 0.08F)));
			model.renderToBuffer(pose, buffers.getBuffer(binding.glow()), 0xF000F0, OverlayTexture.NO_OVERLAY,
					(alpha << 24) | block.pendingGlowColor());
		}
		pose.popPose();
	}

	/**
	 * Lays the book open on the Escritoire's writing surface: pages up, spine running front to back with the head
	 * toward the hutch, so the spread runs along the desk's width in front of the pigeonholes.
	 */
	private static void poseOnDesk(PoseStack pose, Direction facing) {
		pose.translate(0.5D, 0.0D, 0.5D);
		// The blockstate turns the model clockwise by 90 degrees per step from north.
		pose.mulPose(Axis.YP.rotationDegrees(-switch (facing) {
			case EAST -> 90.0F;
			case SOUTH -> 180.0F;
			case WEST -> 270.0F;
			default -> 0.0F;
		}));
		pose.translate(0.0D, DESK_TOP / 16.0F, DESK_DEPTH / 16.0F - 0.5D);
		// Model +x (the way the pages face) up, the foot (+y) toward the reader at the front, the boards across the desk.
		pose.mulPose(Axis.YP.rotationDegrees(-90.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
		pose.scale(HemoBookLayers.MODEL_SCALE, HemoBookLayers.MODEL_SCALE, HemoBookLayers.MODEL_SCALE);
		pose.translate(BOARD_LIFT / 16.0F, 0.0D, 0.0D);
	}

	private static float rotationForFacing(Direction facing) {
		return switch (facing) {
			case NORTH -> 180.0F;
			case EAST -> 90.0F;
			case WEST -> -90.0F;
			default -> 0.0F;
		};
	}
}
