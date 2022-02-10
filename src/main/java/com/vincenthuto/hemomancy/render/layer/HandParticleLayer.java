//
//package com.vincenthuto.hemomancy.render.layer;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.math.Matrix4f;
//import com.mojang.math.Vector3f;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.model.ArmedModel;
//import net.minecraft.client.model.EntityModel;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.block.model.ItemTransforms;
//import net.minecraft.client.renderer.entity.RenderLayerParent;
//import net.minecraft.client.renderer.entity.layers.RenderLayer;
//import net.minecraft.core.particles.ParticleOptions;
//import net.minecraft.core.particles.ParticleType;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.entity.HumanoidArm;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.phys.Vec3;
//
//public class HandParticleLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
//	public HandParticleLayer(RenderLayerParent<T, M> rendererIn) {
//		super(rendererIn);
//	}
//
//	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn,
//			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
//			float headPitch) {
//		ItemStack leftHandItem;
//		if (entitylivingbaseIn.isInvisible()) {
//			return;
//		}
//		boolean playerIsRightHanded = entitylivingbaseIn.getMainArm() == HumanoidArm.RIGHT;
//		boolean itemIsInUse = entitylivingbaseIn.getUseItemRemainingTicks() > 0;
//		InteractionHand activeHand = entitylivingbaseIn.getUsedItemHand();
//		ItemStack rightHandItem = playerIsRightHanded ? entitylivingbaseIn.getOffhandItem()
//				: entitylivingbaseIn.getMainHandItem();
//		ItemStack itemStack = leftHandItem = playerIsRightHanded ? entitylivingbaseIn.getMainHandItem()
//				: entitylivingbaseIn.getOffhandItem();
//		if (!rightHandItem.isEmpty() || !leftHandItem.isEmpty()) {
//			matrixStackIn.pushPose();
//			if (this.getParentModel().young) {
//				matrixStackIn.translate(0.0, 0.75, 0.0);
//				matrixStackIn.scale(0.5f, 0.5f, 0.5f);
//			}
//			if (!itemIsInUse || playerIsRightHanded && activeHand == InteractionHand.OFF_HAND
//					|| !playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND) {
//				this.renderHandParticle((LivingEntity) entitylivingbaseIn, leftHandItem,
//						ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStackIn,
//						bufferIn, packedLightIn);
//			}
//			if (!itemIsInUse || !playerIsRightHanded && activeHand == InteractionHand.OFF_HAND
//					|| playerIsRightHanded && activeHand == InteractionHand.MAIN_HAND) {
//				this.renderHandParticle((LivingEntity) entitylivingbaseIn, rightHandItem,
//						ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStackIn, bufferIn,
//						packedLightIn);
//			}
//			matrixStackIn.popPose();
//		}
//	}
//
//	private void renderHandParticle(LivingEntity living, ItemStack stack, ItemTransforms.TransformType transformType,
//			HumanoidArm side, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
//		if (Minecraft.getInstance().isPaused()) {
//			return;
//		}
//		if (!stack.isEmpty() && stack.getItem() instanceof ItemSpell) {
//			boolean leftHand;
//			matrixStack.pushPose();
//			Matrix4f curMatrix = matrixStack.last().pose();
//			Matrix4f inverted = curMatrix.copy();
//			inverted.invert();
//			curMatrix.multiply(inverted);
//			matrixStack.mulPose(Vector3f.YP.rotationDegrees(-living.yBodyRot));
//			matrixStack.mulPose(Vector3f.XN.rotationDegrees(-90.0f));
//			((ArmedModel) this.getParentModel()).translateToHand(side, matrixStack);
//			boolean bl = leftHand = side == HumanoidArm.LEFT;
//			if (leftHand) {
//				matrixStack.translate(0.225, 0.65, -0.95);
//			} else {
//				matrixStack.translate(-0.225, 0.65, -0.95);
//			}
//			this.spawnParticleFromMatrix(affs[(int) (Math.random() * (double) affs.length)], matrixStack, living,
//					transformType);
//			matrixStack.popPose();
//		}
//	}
//
//	private void spawnParticleFromMatrix(Affinity affinity, PoseStack matrixStackIn, LivingEntity entitylivingbaseIn,
//			ItemTransforms.TransformType type) {
//		Vec3 playerPos = entitylivingbaseIn.position();
//		Matrix4f curMatrix = matrixStackIn.last().pose();
//		Vec3 particlePos = playerPos
//				.add(new Vec3((double) curMatrix.m03, (double) curMatrix.m13, (double) curMatrix.m23));
//		switch (affinity) {
//		case ARCANE: {
//			Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
//			Vec3 offset = new Vec3(entitylivingbaseIn.level.random.nextGaussian(),
//					entitylivingbaseIn.level.random.nextGaussian(), entitylivingbaseIn.level.random.nextGaussian())
//							.normalize().scale((double) 0.3f);
//			origin = origin.add(offset);
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.ARCANE_LERP.get())),
//					origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
//			break;
//		}
//		case EARTH: {
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.DUST.get())),
//					particlePos.x, particlePos.y, particlePos.z, (double) -0.005f + Math.random() * (double) 0.01f,
//					(double) 0.03f, (double) -0.005f + Math.random() * (double) 0.01f);
//			break;
//		}
//		case ENDER: {
//			Vec3 origin = new Vec3(particlePos.x, particlePos.y, particlePos.z);
//			Vec3 offset = new Vec3(entitylivingbaseIn.level.random.nextGaussian(),
//					entitylivingbaseIn.level.random.nextGaussian(), entitylivingbaseIn.level.random.nextGaussian())
//							.normalize().scale((double) 0.3f);
//			origin = origin.add(offset);
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.ENDER.get())),
//					origin.x, origin.y, origin.z, particlePos.x, particlePos.y, particlePos.z);
//			break;
//		}
//		case FIRE: {
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.FLAME.get())),
//					particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
//			break;
//		}
//		case HELLFIRE: {
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.HELLFIRE.get())),
//					particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
//			break;
//		}
//		case LIGHTNING: {
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.LIGHTNING_BOLT.get())),
//					particlePos.x, particlePos.y, particlePos.z,
//					particlePos.x - (double) 0.4f + Math.random() * (double) 0.8f,
//					particlePos.y - (double) 0.4f + Math.random() * (double) 0.8f,
//					particlePos.z - (double) 0.4f + Math.random() * (double) 0.8f);
//			break;
//		}
//		case WATER: {
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.WATER.get())),
//					particlePos.x, particlePos.y, particlePos.z, -0.05 * Math.random() * 0.1, Math.random() * 0.05,
//					-0.05 * Math.random() * 0.1);
//			break;
//		}
//		case ICE: {
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.FROST.get())),
//					particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
//			break;
//		}
//		case WIND: {
//			entitylivingbaseIn.level.addParticle(
//					(ParticleOptions) new MAParticleType(
//							(ParticleType<MAParticleType>) ((ParticleType) ParticleInit.AIR_ORBIT.get())),
//					particlePos.x, particlePos.y, particlePos.z, (double) 0.3f, (double) 0.01f, (double) 0.05f);
//			break;
//		}
//		}
//	}
//}
