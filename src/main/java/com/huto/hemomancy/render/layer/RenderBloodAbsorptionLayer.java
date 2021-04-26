
package com.huto.hemomancy.render.layer;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.ItemBloodAbsorption;
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
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class RenderBloodAbsorptionLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
	public RenderBloodAbsorptionLayer(IEntityRenderer<T, M> rendererIn) {
		super(rendererIn);
	}

	@SuppressWarnings("unused")
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {
		ModelBloodArm model = new ModelBloodArm(0.25f);

		if (entitylivingbaseIn.getActivePotionEffect(Effects.INVISIBILITY) != null) {
			return;
		}
		boolean playerIsRightHanded = entitylivingbaseIn.getPrimaryHand() == HandSide.RIGHT;
		boolean itemIsInUse = entitylivingbaseIn.getItemInUseCount() > 0;
		Hand activeHand = entitylivingbaseIn.getActiveHand();
		ItemStack rightHandItem = playerIsRightHanded ? entitylivingbaseIn.getHeldItemMainhand()
				: entitylivingbaseIn.getHeldItemOffhand();
		ItemStack leftHandItem = playerIsRightHanded ? entitylivingbaseIn.getHeldItemOffhand()
				: entitylivingbaseIn.getHeldItemMainhand();
		matrixStackIn.push();
		if (this.getEntityModel().isChild) {
			matrixStackIn.translate(0.0, 0.75, 0.0);
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
		}
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindTexture(mc.player.getLocationSkin());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getRenderManager().getRenderer(mc.player);
		if (rightHandItem.getItem() == ItemInit.blood_absorption.get()
				&& leftHandItem.getItem() != ItemInit.blood_absorption.get()) {
			model.rightArm = playerrenderer.getEntityModel().bipedRightArm;
			model.leftArm = playerrenderer.getEntityModel().bipedLeftArm;
			model.leftArm.showModel = false;
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(
					model.getRenderType(new ResourceLocation(Hemomancy.MOD_ID + ":textures/item/the_greed.png")));
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0, 100);
			PlayerModel<AbstractClientPlayerEntity> playermodel = playerrenderer.getEntityModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, rightHandItem,
					ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStackIn, bufferIn,
					packedLightIn);
		} else if (leftHandItem.getItem() == ItemInit.blood_absorption.get()
				&& rightHandItem.getItem() != ItemInit.blood_absorption.get()) {
			model.rightArm = playerrenderer.getEntityModel().bipedRightArm;
			model.leftArm = playerrenderer.getEntityModel().bipedLeftArm;
			model.rightArm.showModel = false;
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(
					model.getRenderType(new ResourceLocation(Hemomancy.MOD_ID + ":textures/item/the_greed.png")));
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0, 100);
			PlayerModel<AbstractClientPlayerEntity> playermodel = playerrenderer.getEntityModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, leftHandItem,
					ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStackIn, bufferIn,
					packedLightIn);
		} else if (leftHandItem.getItem() == ItemInit.blood_absorption.get()
				&& rightHandItem.getItem() == ItemInit.blood_absorption.get()) {
			model.rightArm = playerrenderer.getEntityModel().bipedRightArm;
			model.leftArm = playerrenderer.getEntityModel().bipedLeftArm;
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(
					model.getRenderType(new ResourceLocation(Hemomancy.MOD_ID + ":textures/item/the_greed.png")));
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0, 100);
			PlayerModel<AbstractClientPlayerEntity> playermodel = playerrenderer.getEntityModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, rightHandItem,
					ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStackIn, bufferIn,
					packedLightIn);
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, leftHandItem,
					ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStackIn, bufferIn,
					packedLightIn);

		}
		matrixStackIn.pop();
	}

	@SuppressWarnings("unused")
	private void renderHandParticle(LivingEntity living, ItemStack stack,
			ItemCameraTransforms.TransformType transformType, HandSide side, MatrixStack matrixStack,
			IRenderTypeBuffer buffer, int packedLight) {
		if (Minecraft.getInstance().isGamePaused()) {
			return;
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ItemBloodAbsorption) {
			boolean leftHand;
			matrixStack.push();
			Matrix4f curMatrix = matrixStack.getLast().getMatrix();
			Matrix4f inverted = curMatrix.copy();
			inverted.invert();
			curMatrix.mul(inverted);
			matrixStack.rotate(Vector3f.YP.rotationDegrees(-living.renderYawOffset));
			matrixStack.rotate(Vector3f.XN.rotationDegrees(-90.0f));
			((IHasArm) this.getEntityModel()).translateHand(side, matrixStack);
			boolean bl = leftHand = side == HandSide.LEFT;
			if (leftHand) {
				matrixStack.translate(0.225, 0.65, -0.95);
			} else {
				matrixStack.translate(-0.225, 0.65, -0.95);
			}
			this.spawnParticleFromMatrix(matrixStack, living, transformType);
			matrixStack.pop();
		}
	}

	@SuppressWarnings("unused")
	private void spawnParticleFromMatrix(MatrixStack matrixStackIn, LivingEntity player,
			ItemCameraTransforms.TransformType type) {
		Vector3d playerPos = player.getPositionVec();
		// System.out.println(player.getName());
		World world = player.world;
		Matrix4f curMatrix = matrixStackIn.getLast().getMatrix();
		Vector3d particlePos = playerPos
				.add(new Vector3d((double) curMatrix.m03, (double) curMatrix.m13, (double) curMatrix.m23));
		Vector3d origin = new Vector3d(particlePos.x, particlePos.y + 0.1, particlePos.z);
		int globalPartCount = 20;
		// RayTraceResult trace = player.pick(2,
		// ClientEventSubscriber.getPartialTicks(), true);
		// System.out.println(trace);
		boolean playerIsRightHanded = player.getPrimaryHand() == HandSide.RIGHT;
		boolean itemIsInUse = player.getItemInUseCount() > 0;
		Hand activeHand = player.getActiveHand();
		Random rand = new Random();
		Vector3d vec = particlePos;
		if (itemIsInUse) {
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

			Vector3d[] fibboSphere = ParticleUtil.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] corona = ParticleUtil.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] inversedSphere = ParticleUtil.inversedSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15,
					false);

			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						particlePos.getX() + inversedSphere[i].x, particlePos.getY() + inversedSphere[i].y,
						particlePos.getZ() + inversedSphere[i].z, 0, 0.00, 0);

			}

		}
	}
}
