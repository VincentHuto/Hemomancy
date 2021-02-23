package com.huto.hemomancy.render.item;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.ItemParticleItem;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleUtil;
import com.huto.hemomancy.particle.data.GlowParticleData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
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
	private final ResourceLocation location = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/item/drudge_submission_device_texture");

	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType,
			MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		if (transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
				&& transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
			if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
				this.renderArm(matrixStack, buffer, combinedLight, HandSide.RIGHT);
				this.spawnFirstPersonParticlesForStack(stack, HandSide.RIGHT);
			} else if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
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
			} else {
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

	private void renderArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, HandSide side) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(mc.player);
		matrixStackIn.push();
		if (side == HandSide.RIGHT) {
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(12.0f));
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-35.0f));
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(5.0f));
			matrixStackIn.translate(1.0, -0.4, 0.8);
			playerrenderer.renderRightArm(matrixStackIn, bufferIn, combinedLightIn,
					(AbstractClientPlayerEntity) mc.player);
		} else {
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-12.0f));
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-35.0f));
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(5.0f));
			matrixStackIn.translate(0.0, -0.3, 0.6);
			playerrenderer.renderLeftArm(matrixStackIn, bufferIn, combinedLightIn,
					(AbstractClientPlayerEntity) mc.player);
		}
		matrixStackIn.pop();
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
			int globalPartCount = 128;
			Vector3d[] fibboSphere = ParticleUtil.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] corona = ParticleUtil.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] inversedSphere = ParticleUtil.inversedSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15,
					false);
			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(
						GlowParticleData.createData(new ParticleColor((int) (inversedSphere[i].y * 255), 0, 0)),
						origin.getX() + inversedSphere[i].x, origin.getY() + inversedSphere[i].y,
						origin.getZ() + inversedSphere[i].z, 0, 0.00, 0);

			}

		}
	}
}