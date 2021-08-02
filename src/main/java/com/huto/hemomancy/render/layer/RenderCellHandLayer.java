
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
import com.hutoslib.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

		if (entitylivingbaseIn.getEffect(MobEffects.INVISIBILITY) != null) {
			return;
		}
		boolean playerIsRightHanded = entitylivingbaseIn.getMainArm() == HumanoidArm.RIGHT;
		boolean itemIsInUse = entitylivingbaseIn.getUseItemRemainingTicks() > 0;
		InteractionHand activeHand = entitylivingbaseIn.getUsedItemHand();
		ItemStack rightHandItem = playerIsRightHanded ? entitylivingbaseIn.getMainHandItem()
				: entitylivingbaseIn.getOffhandItem();
		ItemStack leftHandItem = playerIsRightHanded ? entitylivingbaseIn.getOffhandItem()
				: entitylivingbaseIn.getMainHandItem();
		matrixStackIn.pushPose();
		if (this.getParentModel().young) {
			matrixStackIn.translate(0.0, 0.75, 0.0);
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
		}
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindForSetup(mc.player.getSkinTextureLocation());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);

		// Right InteractionHand only
		if (rightHandItem.getItem() instanceof ICellHand && !(leftHandItem.getItem() instanceof ICellHand)) {
			model.rightArm = playerrenderer.getModel().rightArm;
			model.leftArm = playerrenderer.getModel().leftArm;
			model.leftArm.visible = false;
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(skinTexture));
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0,
					100);
			PlayerModel<AbstractClientPlayer> playermodel = playerrenderer.getModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, rightHandItem,
					ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStackIn, bufferIn,
					packedLightIn);
			// Left InteractionHand only
		} else if (leftHandItem.getItem() instanceof ICellHand && !(rightHandItem.getItem() instanceof ICellHand)) {
			model.rightArm = playerrenderer.getModel().rightArm;
			model.leftArm = playerrenderer.getModel().leftArm;
			model.rightArm.visible = false;
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(skinTexture));
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0,
					100);
			PlayerModel<AbstractClientPlayer> playermodel = playerrenderer.getModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, leftHandItem,
					ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStackIn, bufferIn,
					packedLightIn);
			// Both Hands
		} else if (leftHandItem.getItem() instanceof ICellHand && rightHandItem.getItem() instanceof ICellHand) {
			model.rightArm = playerrenderer.getModel().rightArm;
			model.leftArm = playerrenderer.getModel().leftArm;
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(skinTexture));
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0,
					100);
			PlayerModel<AbstractClientPlayer> playermodel = playerrenderer.getModel();
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, rightHandItem,
					ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStackIn, bufferIn,
					packedLightIn);
			this.renderHandParticle((LivingEntity) entitylivingbaseIn, leftHandItem,
					ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStackIn, bufferIn,
					packedLightIn);

		}
		matrixStackIn.popPose();
	}

	@SuppressWarnings("unused")
	private void renderHandParticle(LivingEntity living, ItemStack stack, ItemTransforms.TransformType transformType,
			HumanoidArm side, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		if (Minecraft.getInstance().isPaused()) {
			return;
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ICellHand) {
			boolean leftHand;
			matrixStack.pushPose();
			Matrix4f curMatrix = matrixStack.last().pose();
			Matrix4f inverted = curMatrix.copy();
			inverted.invert();
			curMatrix.multiply(inverted);
			matrixStack.mulPose(Vector3f.YP.rotationDegrees(-living.yBodyRot));
			matrixStack.mulPose(Vector3f.XN.rotationDegrees(-90.0f));
			((ArmedModel) this.getParentModel()).translateToHand(side, matrixStack);
			boolean bl = leftHand = side == HumanoidArm.LEFT;
			if (leftHand) {
				matrixStack.translate(0.225, 0.65, -0.95);
			} else {
				matrixStack.translate(-0.225, 0.65, -0.95);
			}
			this.spawnParticleFromMatrix(matrixStack, living, transformType);
			matrixStack.popPose();
		}
	}

	@SuppressWarnings("unused")
	private void spawnParticleFromMatrix(PoseStack matrixStackIn, LivingEntity player,
			ItemTransforms.TransformType type) {
		Vec3 playerPos = player.position();
		Level world = player.level;
		Matrix4f curMatrix = matrixStackIn.last().pose();
		Vec3 particlePos = playerPos
				.add(new Vec3((double) curMatrix.m03, (double) curMatrix.m13, (double) curMatrix.m23));
		Vec3 origin = new Vec3(particlePos.x, particlePos.y + 0.1, particlePos.z);
		int globalPartCount = 20;

		boolean playerIsRightHanded = player.getMainArm() == HumanoidArm.RIGHT;
		boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
		InteractionHand activeHand = player.getUsedItemHand();
		Random rand = new Random();
		Vec3 vec = particlePos;
		if (itemIsInUse) {
			if (player.getItemInHand(activeHand).getItem() instanceof ItemBloodAbsorption) {
				List<Entity> targets = player.level.getEntities(player, player.getBoundingBox().inflate(5.0));
				if (targets.size() > 0) {
					for (int i = 0; i < targets.size(); ++i) {
						Entity target = targets.get(i);
						if (target instanceof LivingEntity) {
							LivingEntity livingTarget = (LivingEntity) target;
							Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
							Vector3d finalPos = vec.subtract(targetVec.x, targetVec.y, targetVec.z).reverse();
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
					Vector3d hitVec = trace.getLocation();
					Vector3d finalPos = hitVec.subtract(particlePos.x, particlePos.y, particlePos.z).reverse();

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
						particlePos.x() + inversedSphere[i].x, particlePos.y() + inversedSphere[i].y,
						particlePos.z() + inversedSphere[i].z, 0, 0.00, 0);

			}
		}
	}
}
