package com.huto.hemomancy.render.item;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.huto.hemomancy.ClientProxy;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.tool.living.ItemBloodAbsorption;
import com.huto.hemomancy.model.entity.armor.ModelBloodArm;
import com.huto.hemomancy.particle.factory.AbsrobedBloodCellParticleFactory;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.huto.hemomancy.particle.util.ParticleColor;
import com.huto.hemomancy.particle.util.ParticleUtil;
import com.huto.hemomancy.util.Vector3;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Atlases;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class RenderBloodAbsorption extends ItemStackTileEntityRenderer {

	public IBakedModel location = ClientProxy.bloodAbsorptionModel;

	@Override
	public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType,
			MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		if (transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
				&& transformType != ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
			if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
				if (!(stack.getItem() instanceof ItemBloodAbsorption)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HandSide.RIGHT);
				this.spawnFirstPersonParticlesForStack(stack, HandSide.RIGHT);
			} else if (transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
				if (!(stack.getItem() instanceof ItemBloodAbsorption)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HandSide.LEFT);
				this.spawnFirstPersonParticlesForStack(stack, HandSide.LEFT);
			} else {
				this.renderDefaultItem(stack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
			}
		}
	}

	private void renderDefaultItem(ItemStack stack, ItemCameraTransforms.TransformType transformType,
			MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		IVertexBuilder buffers = buffer.getBuffer(Atlases.getCutoutBlockType());
		IBakedModel bakedModel = ClientProxy.bloodAbsorptionModel;
		int color = Minecraft.getInstance().getItemColors().getColor(stack, 1);
		if (this.location == null) {
			float r = (color >> 16 & 0xFF) / 255F;
			float g = (color >> 8 & 0xFF) / 255F;
			float b = (color & 0xFF) / 255F;
			Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(
					matrixStack.getLast(), buffers, null, bakedModel, r, g, b, 0xF000F0, OverlayTexture.NO_OVERLAY);
		} else if (!stack.isEmpty()) {
			if (this.location.isLayered()) {
				ForgeHooksClient.drawItemLayered((ItemRenderer) Minecraft.getInstance().getItemRenderer(),
						(IBakedModel) this.location, (ItemStack) stack, (MatrixStack) matrixStack,
						(IRenderTypeBuffer) buffer, (int) combinedLight, (int) combinedOverlay, (boolean) true);
				Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(
						matrixStack.getLast(), buffers, null, bakedModel, 255, 255, 255, 0x000000, combinedOverlay);
			} else {
				Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(
						matrixStack.getLast(), buffers, null, bakedModel, 255, 255, 255, combinedLight,
						combinedOverlay);
				matrixStack.pop();
				matrixStack.push();
				Minecraft.getInstance().getItemRenderer().renderItem(stack, transformType,
						transformType == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, matrixStack, buffer,
						transformType == ItemCameraTransforms.TransformType.GUI ? 0xF000F0 : combinedLight,
						transformType == ItemCameraTransforms.TransformType.GUI ? OverlayTexture.NO_OVERLAY
								: combinedOverlay,
						this.location);
			}
		}
	}

	ModelBloodArm model = new ModelBloodArm(1.0f);

	private void renderArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, HandSide side) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());

		matrixStackIn.push();
		if (side == HandSide.RIGHT) {
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(12.0f));
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-35.0f));
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(5.0f));
			matrixStackIn.translate(1.0, -0.4, 0.8);
			renderArm(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayerEntity) mc.player, side);

		} else {
			matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-12.0f));
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-35.0f));
			matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(5.0f));
			matrixStackIn.translate(0.0, -0.3, 0.6);
			renderArm(matrixStackIn, bufferIn, combinedLightIn, (AbstractClientPlayerEntity) mc.player, side);
		}
		matrixStackIn.pop();
	}

	public void renderArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			AbstractClientPlayerEntity playerIn, HandSide side) {
		if (side == HandSide.RIGHT) {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(mc.player);
			renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn,
					(playerrenderer.getEntityModel()).bipedRightArm,
					(playerrenderer.getEntityModel()).bipedRightArmwear);
		} else {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(mc.player);
			this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn,
					(playerrenderer.getEntityModel()).bipedLeftArm, (playerrenderer.getEntityModel()).bipedLeftArmwear);
		}

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
		if (!(stack.getItem() instanceof ItemBloodAbsorption)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		boolean playerIsRightHanded = player.getPrimaryHand() == HandSide.RIGHT;
		boolean itemIsInUse = player.getItemInUseCount() > 0;
		Hand activeHand = player.getActiveHand();
		World world = player.world;

		if (itemIsInUse) {
			Vector3d particlePos = player.getPositionVec().add(0.0, (double) (player.getEyeHeight() - 0.2f), 0.0);
			Vector3d look = player.getLookVec().normalize().scale(0.5);
			Vector3d perp = look.crossProduct(new Vector3d(0.0, 1.0, 0.0)).normalize()
					.scale(hand == HandSide.LEFT ? (double) -0.4f : (double) 0.4f);
			particlePos = particlePos.add(look).add(perp);

			Vector3d origin = new Vector3d(particlePos.x, particlePos.y + 0.1, particlePos.z);
			Vector3d vec = particlePos;
			Random rand = new Random();
			List<Entity> targets = player.world.getEntitiesWithinAABBExcludingEntity(player,
					player.getBoundingBox().grow(5.0));
			if (targets.size() > 0) {
				for (int i = 0; i < targets.size(); ++i) {
					Entity target = targets.get(i);
					if (target instanceof LivingEntity) {
						LivingEntity livingTarget = (LivingEntity) target;
						Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
						Vector3d finalPos = vec.subtract(targetVec.x, targetVec.y, targetVec.z).inverse();
						Predicate<Entity> targetPred = ParticleColor.getEntityPredicate(target);
						ParticleColor targetColor = ParticleColor.getColorFromPredicate(targetPred);
						world.addParticle(AbsrobedBloodCellParticleFactory.createData(targetColor), (double) vec.x,
								(double) vec.y + 1.05D, (double) vec.z,
								(double) ((float) finalPos.x + rand.nextFloat()) - 0.5D,
								(double) ((float) finalPos.y - rand.nextFloat() - 0F),
								(double) ((float) finalPos.z + rand.nextFloat()) - 0.5D);
					}
				}
			}

			int globalPartCount = 20;
			Vector3d[] fibboSphere = ParticleUtil.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] corona = ParticleUtil.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] inversedSphere = ParticleUtil.inversedSphere(globalPartCount, -world.getGameTime() * 0.016, 0.15,
					false);
			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						origin.getX() + inversedSphere[i].x, origin.getY() + inversedSphere[i].y,
						origin.getZ() + inversedSphere[i].z, 0, 0.00, 0);

			}

		}
	}
}