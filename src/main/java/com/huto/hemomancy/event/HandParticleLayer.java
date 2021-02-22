
package com.huto.hemomancy.event;

import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.item.ItemDSD;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.data.GlowParticleData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class HandParticleLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
	public HandParticleLayer(IEntityRenderer<T, M> rendererIn) {
		super(rendererIn);
	}

	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {
		ItemStack leftHandItem;
		if (entitylivingbaseIn.getActivePotionEffect(Effects.INVISIBILITY) != null) {
			return;
		}
		boolean playerIsRightHanded = entitylivingbaseIn.getPrimaryHand() == HandSide.RIGHT;
		boolean itemIsInUse = entitylivingbaseIn.getItemInUseCount() > 0;
		Hand activeHand = entitylivingbaseIn.getActiveHand();
		ItemStack rightHandItem = playerIsRightHanded ? entitylivingbaseIn.getHeldItemOffhand()
				: entitylivingbaseIn.getHeldItemMainhand();
		ItemStack itemStack = leftHandItem = playerIsRightHanded ? entitylivingbaseIn.getHeldItemMainhand()
				: entitylivingbaseIn.getHeldItemOffhand();
		if (!rightHandItem.isEmpty() || !leftHandItem.isEmpty()) {
			matrixStackIn.push();
			if (this.getEntityModel().isChild) {
				matrixStackIn.translate(0.0, 0.75, 0.0);
				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
			}
			if (!itemIsInUse || playerIsRightHanded && activeHand == Hand.OFF_HAND
					|| !playerIsRightHanded && activeHand == Hand.MAIN_HAND) {
				this.renderHandParticle((LivingEntity) entitylivingbaseIn, leftHandItem,
						ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStackIn,
						bufferIn, packedLightIn);
			}
			if (!itemIsInUse || !playerIsRightHanded && activeHand == Hand.OFF_HAND
					|| playerIsRightHanded && activeHand == Hand.MAIN_HAND) {
				this.renderHandParticle((LivingEntity) entitylivingbaseIn, rightHandItem,
						ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStackIn,
						bufferIn, packedLightIn);
			}
			matrixStackIn.pop();
		}
	}

	private void renderHandParticle(LivingEntity living, ItemStack stack,
			ItemCameraTransforms.TransformType transformType, HandSide side, MatrixStack matrixStack,
			IRenderTypeBuffer buffer, int packedLight) {
		if (Minecraft.getInstance().isGamePaused()) {
			return;
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ItemDSD) {
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

	private void spawnParticleFromMatrix(MatrixStack matrixStackIn, LivingEntity entitylivingbaseIn,
			ItemCameraTransforms.TransformType type) {
		Vector3d playerPos = entitylivingbaseIn.getPositionVec();
		Matrix4f curMatrix = matrixStackIn.getLast().getMatrix();
		Vector3d particlePos = playerPos
				.add(new Vector3d((double) curMatrix.m03, (double) curMatrix.m13, (double) curMatrix.m23));
		Vector3d origin = new Vector3d(particlePos.x, particlePos.y, particlePos.z);
		Vector3d offset = new Vector3d(entitylivingbaseIn.world.rand.nextGaussian(),
				entitylivingbaseIn.world.rand.nextGaussian(), entitylivingbaseIn.world.rand.nextGaussian()).normalize()
						.scale((double) 0.3f);
		origin = origin.add(offset);
		entitylivingbaseIn.world.addParticle(GlowParticleData.createData(new ParticleColor(255, 100, 50)), origin.x, origin.y,
				origin.z, 0, 0, 0);
		entitylivingbaseIn.world.addParticle(GlowParticleData.createData(new ParticleColor(255, 255, 50)), origin.x, origin.y,
				origin.z, 0, 0, 0);
		entitylivingbaseIn.world.addParticle(GlowParticleData.createData(new ParticleColor(100, 100, 50)), origin.x, origin.y,
				origin.z, 0, 0, 0);	}
}
