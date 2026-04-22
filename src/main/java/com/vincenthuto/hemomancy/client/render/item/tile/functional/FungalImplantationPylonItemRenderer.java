package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.FungalImplantationPylonModel;
import com.vincenthuto.hemomancy.client.render.tile.functional.FungalImplantationPylonRenderer.FungalImplantationPylonAnimContext;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FungalImplantationPylonItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/fungal_implantation_pylon/fungal_implantation_pylon.png");

	private FungalImplantationPylonModel model;
	private final FungalImplantationPylonAnimContext animCtx = new FungalImplantationPylonAnimContext(new AnimationState());

	public FungalImplantationPylonItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new FungalImplantationPylonModel(
					modelSet.bakeLayer(FungalImplantationPylonModel.LAYER_LOCATION));
		}
		animCtx.state().start(0);
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new FungalImplantationPylonModel(
					modelSet.bakeLayer(FungalImplantationPylonModel.LAYER_LOCATION));
		}

		// Ensure no stale shader color tints the model dark
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		// GUI contexts need the inventory lighting setup; other contexts (ground,
		// hand, frame) already have correct 3-D lighting from the item pipeline.
		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		poseStack.translate(0.5, 0.5, 0.5);
		poseStack.scale(0.3f, 0.3f, 0.3f);
		poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());

		// Drive the wiggle animation using the client level's game time
		if (Minecraft.getInstance().level != null) {
			float partialTicks = Minecraft.getInstance().getPartialTick();
			model.setupAnimation(Minecraft.getInstance().level, partialTicks, animCtx);
		}

		model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight,
				OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();

		// Restore 3-D item lighting so subsequent renders are unaffected
		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}

