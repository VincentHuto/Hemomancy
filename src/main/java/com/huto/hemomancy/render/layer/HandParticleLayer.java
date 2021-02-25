
package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.item.ItemParticleItem;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleUtil;
import com.huto.hemomancy.particle.data.BloodCellParticleData;
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
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class HandParticleLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
	public HandParticleLayer(IEntityRenderer<T, M> rendererIn) {
		super(rendererIn);
	}

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private void renderHandParticle(LivingEntity living, ItemStack stack,
			ItemCameraTransforms.TransformType transformType, HandSide side, MatrixStack matrixStack,
			IRenderTypeBuffer buffer, int packedLight) {
		if (Minecraft.getInstance().isGamePaused()) {
			return;
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ItemParticleItem) {
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
		World world = player.world;
		Matrix4f curMatrix = matrixStackIn.getLast().getMatrix();
		Vector3d particlePos = playerPos

				.add(new Vector3d((double) curMatrix.m03, (double) curMatrix.m13, (double) curMatrix.m23));
		Vector3d origin = new Vector3d(particlePos.x, particlePos.y + 0.1, particlePos.z);
		int globalPartCount = 128;
		Vector3d[] fibboSphere = ParticleUtil.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
		Vector3d[] corona = ParticleUtil.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
		Vector3d[] inversedSphere = ParticleUtil.inversedSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15,
				false);
		for (int i = 0; i < globalPartCount; i++) {
			world.addParticle(
					BloodCellParticleData.createData(new ParticleColor(
							Math.max((int) (inversedSphere[i].x * 255), (int) (inversedSphere[i].z * 255)), 0, 0)),
					origin.getX() + inversedSphere[i].x, origin.getY() + inversedSphere[i].y,
					origin.getZ() + inversedSphere[i].z, 0, 0.00, 0);

		}

	}
}
