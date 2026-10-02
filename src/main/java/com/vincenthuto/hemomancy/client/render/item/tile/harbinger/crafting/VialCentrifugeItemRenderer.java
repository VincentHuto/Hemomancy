package com.vincenthuto.hemomancy.client.render.item.tile.harbinger.crafting;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.CentrifugeArmsModel;
import com.vincenthuto.hemomancy.client.model.tile.crafting.CentrifugeStandModel;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hutoslib.client.HlClientTickHandler;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class VialCentrifugeItemRenderer extends BlockEntityWithoutLevelRenderer {

	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_centrifuge_arms.png");

	private CentrifugeArmsModel model;
	private CentrifugeStandModel standModel;

	public VialCentrifugeItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		if (modelSet != null) {
			this.model = new CentrifugeArmsModel(
					modelSet.bakeLayer(CentrifugeArmsModel.LAYER_LOCATION));
			this.standModel = new CentrifugeStandModel(
					modelSet.bakeLayer(CentrifugeStandModel.LAYER_LOCATION));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

		if (this.model == null) {
			EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
			this.model = new CentrifugeArmsModel(
					modelSet.bakeLayer(CentrifugeArmsModel.LAYER_LOCATION));
			this.standModel = new CentrifugeStandModel(
					modelSet.bakeLayer(CentrifugeStandModel.LAYER_LOCATION));
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		var state = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY)
				.apply(BlockInit.vial_centrifuge.get().defaultBlockState())
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
		int stage = StationTierProperty.stage(state);

		boolean isGui = displayContext == ItemDisplayContext.GUI;
		if (isGui) {
			Lighting.setupForEntityInInventory();
			poseStack.mulPose(new Quaternion(Vector3.YN, 90, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.ZN, 30, true).toMoj());
			poseStack.translate(-1.25, -0.75, -1.25);
			poseStack.scale(1.5f,1.5f,1.5f);
		}

		poseStack.pushPose();
		poseStack.translate(0.5, stage == 0 ? 1.25 : 1.3025, 0.5);
		poseStack.scale(0.45f, 0.45f, 0.45f);
		poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
		poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());

		// Hide all vials for the item form — only show the ring/arms structure
		model.vial1.visible = false;
		model.vial1Empty.visible = false;
		model.vial2.visible = false;
		model.vial2Empty.visible = false;
		model.vial3.visible = false;
		model.vial3Empty.visible = false;
		model.vial4.visible = false;
		model.vial4Empty.visible = false;
		model.vial5.visible = false;
		model.vial5Empty.visible = false;
		model.vial6.visible = false;
		model.vial6Empty.visible = false;
		model.vial7.visible = false;
		model.vial7Empty.visible = false;
		model.vial8.visible = false;
		model.vial8Empty.visible = false;

		// Gentle spin animation for the centrifuge ring
		double ticks = HlClientTickHandler.ticksInGame + HlClientTickHandler.partialTicks;
		poseStack.mulPose(Vector3.YP.rotationDegrees((float) ticks * 2.0f).toMoj());

		model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(TEXTURE)), combinedLight,
				OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		// Keep the same miniature origin for the baked body and moving rotor.
		poseStack.pushPose();
		if (stage == 0) {
			poseStack.translate(0.5, 1.1, 0.5);
			poseStack.scale(0.45f, 0.45f, 0.45f);
			poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
			poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
			standModel.renderToBuffer(poseStack, buffer.getBuffer(standModel.renderType(TEXTURE)), combinedLight,
					OverlayTexture.NO_OVERLAY, -1);
		} else {
			poseStack.translate(0.5, .425, 0.5);
			poseStack.scale(0.45f, 0.45f, 0.45f);
			poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
			poseStack.translate(-0.5, 0, -0.5);
			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffer,
					combinedLight, OverlayTexture.NO_OVERLAY);
		}
		poseStack.popPose();

		if (isGui) {
			Lighting.setupFor3DItems();
		}
	}
}

