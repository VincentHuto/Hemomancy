
package com.huto.hemomancy.render.layer;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.tool.living.ICellHand;
import com.huto.hemomancy.item.tool.living.ItemBloodAbsorption;
import com.huto.hemomancy.model.entity.armor.ModelBloodArm;
import com.huto.hemomancy.particle.factory.AbsrobedBloodCellParticleFactory;
import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.huto.hemomancy.particle.util.EntityParticleUtils;
import com.hutoslib.client.ClientUtils;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.hutoslib.math.Vector3;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.potion.Effects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RenderCellHandLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public RenderCellHandLayer(RenderLayerParent<T, M> rendererIn) {
		super(rendererIn);
	}

	public final ResourceLocation skinTexture = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/item/hardened_skin.png");

	@SuppressWarnings("unused")
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn,
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

		// Right Hand only
		if (rightHandItem.getItem() instanceof ICellHand && !(leftHandItem.getItem() instanceof ICellHand)) {
			model.rightArm = playerrenderer.getEntityModel().bipedRightArm;
			model.leftArm = playerrenderer.getEntityModel().bipedLeftArm;
			model.leftArm.showModel = false;
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(skinTexture));
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0, 100);
			PlayerModel<AbstractClientPlayerEntity> playermodel = playerrenderer.getEntityModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, rightHandItem,
					ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, matrixStackIn, bufferIn,
					packedLightIn);
			// Left Hand only
		} else if (leftHandItem.getItem() instanceof ICellHand && !(rightHandItem.getItem() instanceof ICellHand)) {
			model.rightArm = playerrenderer.getEntityModel().bipedRightArm;
			model.leftArm = playerrenderer.getEntityModel().bipedLeftArm;
			model.rightArm.showModel = false;
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(skinTexture));
			model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0, 100);
			PlayerModel<AbstractClientPlayerEntity> playermodel = playerrenderer.getEntityModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, leftHandItem,
					ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, matrixStackIn, bufferIn,
					packedLightIn);
			// Both Hands
		} else if (leftHandItem.getItem() instanceof ICellHand && rightHandItem.getItem() instanceof ICellHand) {
			model.rightArm = playerrenderer.getEntityModel().bipedRightArm;
			model.leftArm = playerrenderer.getEntityModel().bipedLeftArm;
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(model.getRenderType(skinTexture));
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
		if (!stack.isEmpty() && stack.getItem() instanceof ICellHand) {
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
		int globalPartCount = 20;

		boolean playerIsRightHanded = player.getPrimaryHand() == HandSide.RIGHT;
		boolean itemIsInUse = player.getItemInUseCount() > 0;
		Hand activeHand = player.getActiveHand();
		Random rand = new Random();
		Vector3d vec = particlePos;
		if (itemIsInUse) {
			if (player.getHeldItem(activeHand).getItem() instanceof ItemBloodAbsorption) {
				List<Entity> targets = player.world.getEntitiesWithinAABBExcludingEntity(player,
						player.getBoundingBox().grow(5.0));
				if (targets.size() > 0) {
					for (int i = 0; i < targets.size(); ++i) {
						Entity target = targets.get(i);
						if (target instanceof LivingEntity) {
							LivingEntity livingTarget = (LivingEntity) target;
							Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
							Vector3d finalPos = vec.subtract(targetVec.x, targetVec.y, targetVec.z).inverse();
							Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(target);
							ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
							world.addParticle(AbsrobedBloodCellParticleFactory.createData(targetColor), (double) vec.x,
									(double) vec.y + 1.05D, (double) vec.z,
									(double) ((float) finalPos.x + rand.nextFloat()) - 0.5D,
									(double) ((float) finalPos.y - rand.nextFloat() - 0F),
									(double) ((float) finalPos.z + rand.nextFloat()) - 0.5D);
						}
					}
				}

			} else {
				RayTraceResult trace = player.pick(5, ClientUtils.getPartialTicks(), true);
				if (trace.getType() == Type.BLOCK) {
					Vector3d hitVec = trace.getHitVec();
					Vector3d finalPos = hitVec.subtract(particlePos.x, particlePos.y, particlePos.z).inverse();

					world.addParticle(AbsrobedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
							(double) hitVec.x, (double) hitVec.y + 1.05D, (double) hitVec.z,
							(double) ((float) finalPos.x + rand.nextFloat()) - 0.5D,
							(double) ((float) finalPos.y - rand.nextFloat() - 0.5F),
							(double) ((float) finalPos.z + rand.nextFloat()) - 0.5D);

				}
			}
			Vector3d[] fibboSphere = ParticleUtils.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] corona = ParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vector3d[] inversedSphere = ParticleUtils.inversedSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15,
					false);

			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						particlePos.getX() + inversedSphere[i].x, particlePos.getY() + inversedSphere[i].y,
						particlePos.getZ() + inversedSphere[i].z, 0, 0.00, 0);

			}
		}
	}
}
