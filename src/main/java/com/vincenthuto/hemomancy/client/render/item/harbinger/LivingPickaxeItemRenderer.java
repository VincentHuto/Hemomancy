package com.vincenthuto.hemomancy.client.render.item.harbinger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.LivingPickaxeModel;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The Living Staff's pickaxe form (Track 8), drawn from the generated {@link LivingPickaxeModel}. The model is laid
 * along the item cube's diagonal, pommel low and head high, the way a vanilla pickaxe's sprite lies, so the item
 * model's vanilla handheld display transforms hold it like any pickaxe in every view.
 */
public final class LivingPickaxeItemRenderer extends BlockEntityWithoutLevelRenderer {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_living_pickaxe.png");
	/** Model pixels from the pommel's foot (y 18.5) to the spike's top (y -4.5), centred on y 7. */
	private static final float SPAN_CENTRE = 7.0F / 16.0F;
	private static final float SCALE = 0.8F;
	private final LivingPickaxeModel model;

	public LivingPickaxeItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
		super(dispatcher, models);
		this.model = new LivingPickaxeModel(models.bakeLayer(LivingPickaxeModel.LAYER_LOCATION));
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
			MultiBufferSource buffers, int light, int overlay) {
		poseStack.pushPose();
		poseStack.translate(0.5D, 0.5D, 0.5D);
		// Head up and to the right, pommel down and to the left, across the cube like a tool's sprite.
		poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
		poseStack.scale(SCALE, SCALE, SCALE);
		poseStack.translate(0.0D, SPAN_CENTRE, 0.0D);
		// Model space runs down the haft; turn it upright.
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
		VertexConsumer base = buffers.getBuffer(model.renderType(TEXTURE));
		model.renderToBuffer(poseStack, base, light, OverlayTexture.NO_OVERLAY, -1);
		if (!LivingStaffMorphRenderer.isMorphBuffer(buffers)) {
			VertexConsumer glint = buffers.getBuffer(RenderTypeInit.getCrimsonGlint());
			model.renderToBuffer(poseStack, glint, light, OverlayTexture.NO_OVERLAY, -1);
		}
		poseStack.popPose();
	}
}
