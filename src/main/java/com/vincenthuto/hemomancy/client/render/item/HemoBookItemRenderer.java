package com.vincenthuto.hemomancy.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.item.book.HemoBookBinding;
import com.vincenthuto.hemomancy.client.model.item.book.HemoBookLayers;
import com.vincenthuto.hemomancy.client.model.item.book.HemoBookModel;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hutoslib.common.item.BookAnimState;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Draws the Hemomancy books with their own {@link HemoBookModel}s. In the hands a book opens, flips and turns from
 * HutosLib's per-player {@link BookAnimState}, posed as HutosLib's guide-book renderer poses it; in a slot, on the
 * ground and in a frame it is shown shut, front cover out.
 */
public final class HemoBookItemRenderer extends BlockEntityWithoutLevelRenderer {
	/** Pixels the shut book's longest side fills in each context (a slot is 16). */
	private static final float GUI_SIZE = 15.0F;
	private static final float GROUND_SIZE = 8.0F;
	private static final float FIXED_SIZE = 16.0F;
	/**
	 * A held book is drawn at about vanilla's held-book size, on top of the meshes' own half scale (they are built at
	 * twice block size so their atlases are 32 px per block).
	 */
	private static final float HELD_SCALE = 0.6F * HemoBookLayers.MODEL_SCALE;

	private final Map<HemoBookBinding, HemoBookModel> models;

	public HemoBookItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
		super(dispatcher, models);
		this.models = HemoBookBinding.bakeAll(models);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
			int light, int overlay) {
		if (!(stack.getItem() instanceof ItemGuideBook item)) return;
		HemoBookBinding binding = HemoBookBinding.of(item);
		if (binding == null) return;
		HemoBookModel model = models.get(binding);
		pose.pushPose();
		if (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
				|| context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
			poseHeld(pose, model, item);
			pose.scale(HELD_SCALE, HELD_SCALE, HELD_SCALE);
		} else {
			pose.translate(0.5D, context == ItemDisplayContext.GROUND ? 0.35D : 0.5D, 0.5D);
			if (context == ItemDisplayContext.GUI) {
				// A quarter turn toward the spine so a slot shows its bands, hinges or tail as well as the cover.
				pose.mulPose(Axis.YP.rotationDegrees(24.0F));
				pose.mulPose(Axis.XP.rotationDegrees(-8.0F));
			}
			frameShut(pose, binding, switch (context) {
				case GUI -> GUI_SIZE;
				case GROUND -> GROUND_SIZE;
				default -> FIXED_SIZE;
			});
			model.setupAnim(0.0F, 0.0F, 1.0F, 0.0F);
		}
		model.renderToBuffer(pose, buffers.getBuffer(binding.solid()), light, OverlayTexture.NO_OVERLAY, -1);
		pose.popPose();
	}

	/** Head up and front cover toward +z, scaled so the longest side fills {@code pixels} and centred. */
	public static void frameShut(PoseStack pose, HemoBookBinding binding, float pixels) {
		pose.mulPose(Axis.XP.rotationDegrees(180.0F));
		float scale = pixels / binding.span();
		pose.scale(scale, scale, scale);
		pose.translate(-binding.centre(0) / 16.0F, -binding.centre(1) / 16.0F, -binding.centre(2) / 16.0F);
	}

	private static void poseHeld(PoseStack pose, HemoBookModel model, ItemGuideBook item) {
		// BlockEntityWithoutLevelRenderer is not told whose hand this is, so the local player's state drives it.
		Player player = Minecraft.getInstance().player;
		BookAnimState s = ItemGuideBook.getOrCreateState(player != null ? player.getUUID() : null, item);
		float partialTick = AnimationTime.partialTick();
		float time = player != null ? AnimationTime.gameTime(player.level(), partialTick) : 0.0F;
		pose.translate(0.5D, 0.5D, 0.5D);
		pose.mulPose(Axis.XP.rotationDegrees(45.0F));
		pose.mulPose(Axis.ZP.rotationDegrees(15.0F));
		pose.mulPose(Axis.YP.rotationDegrees(-60.0F));
		pose.translate(0.0D, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0D);
		float turn = Mth.wrapDegrees((s.nextPageAngle - s.pageAngle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
		pose.mulPose(Axis.YP.rotation(-(s.pageAngle + turn * partialTick)));
		pose.mulPose(Axis.ZP.rotationDegrees(80.0F));
		// HutosLib's pose suits vanilla's head-foot symmetric book; turn ours about the opening so the head is up.
		pose.mulPose(Axis.XP.rotationDegrees(180.0F));
		float flip = Mth.lerp(partialTick, s.oFlip, s.flip);
		float rightFlip = Mth.clamp(Mth.frac(flip + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
		float leftFlip = Mth.clamp(Mth.frac(flip + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
		model.setupAnim(time, rightFlip, leftFlip, s.close);
	}
}
