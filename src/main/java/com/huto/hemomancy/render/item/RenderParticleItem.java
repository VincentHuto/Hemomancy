package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.ItemParticleItem;
import com.huto.hemomancy.model.entity.armor.ModelBloodArm;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.data.BloodCellParticleData;
import com.huto.hemomancy.particle.util.ParticleUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class RenderParticleItem extends ItemStackTileEntityRenderer {

	private IBakedModel defaultSpellModel;
	public ResourceLocation location = new ResourceLocation(Hemomancy.MOD_ID, "item/obsidian_knapper");

	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType,
			MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		if (transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
				&& transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
			if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
				if (!(stack.getItem() instanceof ItemParticleItem)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HandSide.RIGHT);
				this.spawnFirstPersonParticlesForStack(stack, HandSide.RIGHT);
			} else if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
				if (!(stack.getItem() instanceof ItemParticleItem)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HandSide.LEFT);
				this.spawnFirstPersonParticlesForStack(stack, HandSide.LEFT);
			} else {
				this.renderDefaultSpellItem(stack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
			}
		}
	}

	private void renderDefaultSpellItem(ItemStack stack, ItemCameraTransforms.TransformType transformType,
			MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		if (this.defaultSpellModel == null) {
			this.defaultSpellModel = Minecraft.getInstance().getModelManager().getModel(this.location);
		} else if (!stack.isEmpty()) {
			if (this.defaultSpellModel.isLayered()) {
				ForgeHooksClient.drawItemLayered((ItemRenderer) Minecraft.getInstance().getItemRenderer(),
						(IBakedModel) this.defaultSpellModel, (ItemStack) stack, (MatrixStack) matrixStack,
						(IRenderTypeBuffer) buffer, (int) combinedLight, (int) combinedOverlay, (boolean) true);
				this.defaultSpellModel = Minecraft.getInstance().getModelManager().getModel(this.location);

			} else {
				this.defaultSpellModel = Minecraft.getInstance().getModelManager().getModel(this.location);

				matrixStack.pop();
				matrixStack.push();
				Minecraft.getInstance().getItemRenderer().renderItem(stack, transformType,
						transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, matrixStack, buffer,
						transformType == ItemCameraTransforms.TransformType.GUI ? 0xF000F0 : combinedLight,
						transformType == ItemCameraTransforms.TransformType.GUI ? OverlayTexture.NO_OVERLAY
								: combinedOverlay,
						this.defaultSpellModel);
			}
		}
	}

	ModelBloodArm model = new ModelBloodArm(1.0f);

	private void renderArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, HandSide side) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		// PlayerRenderer playerrenderer = (PlayerRenderer)
		// mc.getRenderManager().getRenderer(mc.player);
		matrixStackIn.push();
		if (side == HandSide.RIGHT) {

			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(12.0f));
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-35.0f));
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(5.0f));
			matrixStackIn.translate(1.0, -0.4, 0.8);
			/*
			 * IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model .getRenderType(new
			 * ResourceLocation(Hemomancy.MOD_ID +
			 * ":textures/block/sanguine_tran_pane.png"))); model.render(matrixStackIn,
			 * ivertexbuilder, combinedLightIn, combinedLightIn, 100, 100, 100, 100);
			 */
			renderRightArm(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayerEntity) mc.player);

		} else {
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-12.0f));
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-35.0f));
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(5.0f));
			matrixStackIn.translate(0.0, -0.3, 0.6);
			/*
			 * IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model .getRenderType(new
			 * ResourceLocation(Hemomancy.MOD_ID +
			 * ":textures/block/sanguine_tran_pane.png"))); model.render(matrixStackIn,
			 * ivertexbuilder, combinedLightIn, combinedLightIn, 100, 100, 100, 100);
			 */
			renderLeftArm(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayerEntity) mc.player);
		}
		matrixStackIn.pop();
	}

	public void renderLeftArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			AbstractClientPlayerEntity playerIn) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(mc.player);
		this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn,
				(playerrenderer.getEntityModel()).bipedLeftArm, (playerrenderer.getEntityModel()).bipedLeftArmwear);
	}

	public void renderRightArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			AbstractClientPlayerEntity playerIn) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(mc.player);
		renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (playerrenderer.getEntityModel()).bipedRightArm,
				(playerrenderer.getEntityModel()).bipedRightArmwear);
	}

	private void renderItem(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			AbstractClientPlayerEntity playerIn, ModelRenderer rendererArmIn, ModelRenderer rendererArmwearIn) {
		Minecraft mc = Minecraft.getInstance();

		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(mc.player);
		PlayerModel<AbstractClientPlayerEntity> playermodel = playerrenderer.getEntityModel();
		// playerrenderer.setModelVisibilities(playerIn);
		playermodel.swingProgress = 0.0F;
		playermodel.isSneak = false;
		playermodel.swimAnimation = 0.0F;
		playermodel.setRotationAngles(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		rendererArmIn.rotateAngleX = 0.0F;
		IVertexBuilder ivertexbuilder = bufferIn.getBuffer(
				model.getRenderType(new ResourceLocation(Hemomancy.MOD_ID + ":textures/item/the_greed.png")));
		rendererArmIn.render(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY);
		rendererArmwearIn.rotateAngleX = 0.0F;
		rendererArmwearIn.render(matrixStackIn,
				bufferIn.getBuffer(RenderType.getEntityTranslucent(playerIn.getLocationSkin())), combinedLightIn,
				OverlayTexture.NO_OVERLAY);
	}

	@SuppressWarnings("unused")
	private void spawnFirstPersonParticlesForStack(ItemStack stack, HandSide hand) {
		if (Minecraft.getInstance().isGamePaused()) {
			return;
		}
		if (!(stack.getItem() instanceof ItemParticleItem)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		boolean playerIsRightHanded = player.getPrimaryHand() == HandSide.RIGHT;
		boolean itemIsInUse = player.getItemInUseCount() > 0;
		Hand activeHand = player.getActiveHand();
		World world = player.world;

		if (!itemIsInUse || playerIsRightHanded && activeHand == Hand.MAIN_HAND && hand == HandSide.LEFT
				|| !playerIsRightHanded && activeHand == Hand.MAIN_HAND && hand == HandSide.RIGHT) {
			Vector3d particlePos = player.getPositionVec().add(0.0, (double) (player.getEyeHeight() - 0.2f), 0.0);
			Vector3d look = player.getLookVec().normalize().scale(0.5);
			Vector3d perp = look.crossProduct(new Vector3d(0.0, 1.0, 0.0)).normalize()
					.scale(hand == HandSide.LEFT ? (double) -0.4f : (double) 0.4f);
			particlePos = particlePos.add(look).add(perp);

			Vector3d origin = new Vector3d(particlePos.x, particlePos.y + 0.1, particlePos.z);
			int globalPartCount = 60;
			Vector3d[] fibboSphere = ParticleUtil.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] corona = ParticleUtil.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] inversedSphere = ParticleUtil.inversedSphere(globalPartCount, -world.getGameTime() * 0.016, 0.15,
					false);
			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(
						BloodCellParticleData.createData(new ParticleColor(255, 0, 0)),
						origin.getX() + inversedSphere[i].x, origin.getY() + inversedSphere[i].y,
						origin.getZ() + inversedSphere[i].z, 0, 0.00, 0);

			}

		}
	}
}