package com.vincenthuto.hemomancy.client.render.item.unstained;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.unstained.AbsolutionDaggerModel;
import com.vincenthuto.hemomancy.client.model.item.unstained.SilthmereGlaiveModel;
import com.vincenthuto.hemomancy.client.model.item.unstained.UnstainedWarhammerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class UnstainedWeaponItemRenderer extends BlockEntityWithoutLevelRenderer {
	public enum Kind {
		WARHAMMER(Hemomancy.rloc("textures/block/pale_silver_bell_body.png")),
		GLAIVE(Hemomancy.rloc("textures/block/pale_silver_block.png")),
		DAGGER(Hemomancy.rloc("textures/block/pale_silver_block.png"));

		private final ResourceLocation texture;

		Kind(ResourceLocation texture) {
			this.texture = texture;
		}
	}

	private final Kind kind;
	private Model model;

	public UnstainedWeaponItemRenderer(Kind kind, BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		this.kind = kind;
		if (modelSet != null) {
			this.model = createModel(modelSet);
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
			MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		Model weaponModel = model();

		boolean gui = displayContext == ItemDisplayContext.GUI;
		if (gui) {
			Lighting.setupForEntityInInventory();
		}

		poseStack.pushPose();
		applyDisplayTransform(displayContext, poseStack);
		VertexConsumer vertexConsumer = bufferSource.getBuffer(weaponModel.renderType(kind.texture));
		weaponModel.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
		poseStack.popPose();

		if (gui) {
			Lighting.setupFor3DItems();
		}
	}

	private Model model() {
		if (this.model == null) {
			this.model = createModel(Minecraft.getInstance().getEntityModels());
		}
		return this.model;
	}

	private Model createModel(EntityModelSet modelSet) {
		return switch (kind) {
			case WARHAMMER -> new UnstainedWarhammerModel(modelSet.bakeLayer(UnstainedWarhammerModel.LAYER_LOCATION));
			case GLAIVE -> new SilthmereGlaiveModel(modelSet.bakeLayer(SilthmereGlaiveModel.LAYER_LOCATION));
			case DAGGER -> new AbsolutionDaggerModel(modelSet.bakeLayer(AbsolutionDaggerModel.LAYER_LOCATION));
		};
	}

	private void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

		switch (displayContext) {
			case GUI -> {
				poseStack.translate(0.0F, -0.08F, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(42.0F));
				poseStack.mulPose(Axis.YP.rotationDegrees(42.0F));
				poseStack.scale(scaleForGui(), scaleForGui(), scaleForGui());
			}
			case GROUND -> {
				poseStack.translate(0.0F, -0.15F, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
				poseStack.scale(0.32F, 0.32F, 0.32F);
			}
			case FIXED -> {
				poseStack.translate(0.0F, -0.25F, 0.0F);
				poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
				poseStack.scale(0.43F, 0.43F, 0.43F);
			}
			case FIRST_PERSON_LEFT_HAND -> {
				poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
				applyHandAnchor(displayContext, poseStack);
				poseStack.scale(scaleForHand(), scaleForHand(), scaleForHand());
				applyHeldWeaponScale(poseStack);
			}
			case FIRST_PERSON_RIGHT_HAND -> {
				poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
				applyHandAnchor(displayContext, poseStack);
				poseStack.scale(scaleForHand(), scaleForHand(), scaleForHand());
				applyHeldWeaponScale(poseStack);
			}
			case THIRD_PERSON_LEFT_HAND -> {
				poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
				applyHandAnchor(displayContext, poseStack);
				poseStack.scale(scaleForThirdPerson(), scaleForThirdPerson(), scaleForThirdPerson());
				applyHeldWeaponScale(poseStack);
			}
			case THIRD_PERSON_RIGHT_HAND -> {
				poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
				applyHandAnchor(displayContext, poseStack);
				poseStack.scale(scaleForThirdPerson(), scaleForThirdPerson(), scaleForThirdPerson());
				applyHeldWeaponScale(poseStack);
			}
			default -> {
				poseStack.translate(0.0F, -0.2F, 0.0F);
				poseStack.scale(0.45F, 0.45F, 0.45F);
			}
		}
	}

	private void applyHeldWeaponScale(PoseStack poseStack) {
		if (kind != Kind.DAGGER) {
			poseStack.scale(1.25F, 1.25F, 1.25F);
		} else {
			poseStack.translate(0.0F, 0.3F, 0.0F);
		}
	}

	private void applyHandAnchor(ItemDisplayContext displayContext, PoseStack poseStack) {
		switch (displayContext) {
			case FIRST_PERSON_LEFT_HAND -> poseStack.translate(0.42F, -0.38F, -0.42F);
			case FIRST_PERSON_RIGHT_HAND -> poseStack.translate(-0.42F, -0.38F, -0.42F);
			case THIRD_PERSON_LEFT_HAND -> poseStack.translate(0.5F, -0.5F, -0.5F);
			case THIRD_PERSON_RIGHT_HAND -> poseStack.translate(-0.5F, -0.5F, -0.5F);
			default -> {
			}
		}
	}

	private float scaleForGui() {
		return switch (kind) {
			case WARHAMMER -> 0.52F;
			case GLAIVE -> 0.48F;
			case DAGGER -> 0.7F;
		};
	}

	private float scaleForHand() {
		return switch (kind) {
			case WARHAMMER -> 0.36F;
			case GLAIVE -> 0.34F;
			case DAGGER -> 0.48F;
		};
	}

	private float scaleForThirdPerson() {
		return switch (kind) {
			case WARHAMMER -> 0.42F;
			case GLAIVE -> 0.38F;
			case DAGGER -> 0.52F;
		};
	}
}
