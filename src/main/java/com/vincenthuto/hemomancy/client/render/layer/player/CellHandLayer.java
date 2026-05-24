
package com.vincenthuto.hemomancy.client.render.layer.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.client.particle.AbsorbedBloodCellParticle;
import com.vincenthuto.hemomancy.client.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.util.EntityParticleUtils;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ICellHand;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class CellHandLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	private final BloodArmModel<T> model;
	public final ResourceLocation skinTexture = Hemomancy.rloc("textures/entity/hardened_skin.png");

	public CellHandLayer(RenderLayerParent<T, M> rendererIn) {
		super(rendererIn);
		model = new BloodArmModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BloodArmModel.blood_arm));

	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {

		if (entitylivingbaseIn.getEffect(MobEffects.INVISIBILITY) != null) {
			return;
		}
		boolean playerIsRightHanded = entitylivingbaseIn.getMainArm() == HumanoidArm.RIGHT;
		ItemStack rightHandItem = playerIsRightHanded ? entitylivingbaseIn.getMainHandItem()
				: entitylivingbaseIn.getOffhandItem();
		ItemStack leftHandItem = playerIsRightHanded ? entitylivingbaseIn.getOffhandItem()
				: entitylivingbaseIn.getMainHandItem();
		matrixStackIn.pushPose();
		if (this.getParentModel().young) {
			matrixStackIn.translate(0.0, 0.75, 0.0);
			matrixStackIn.scale(0.5f, 0.5f, 0.5f);
		}

		// Right InteractionHand only
		if (rightHandItem.getItem() instanceof ICellHand && !(leftHandItem.getItem() instanceof ICellHand)) {
			this.renderBloodArm(matrixStackIn, bufferIn, packedLightIn, true, false);
			this.renderHandParticle(entitylivingbaseIn, rightHandItem, HumanoidArm.RIGHT);
			// Left InteractionHand only
		} else if (leftHandItem.getItem() instanceof ICellHand && !(rightHandItem.getItem() instanceof ICellHand)) {
			this.renderBloodArm(matrixStackIn, bufferIn, packedLightIn, false, true);
			this.renderHandParticle(entitylivingbaseIn, leftHandItem, HumanoidArm.LEFT);
			// Both Hands
		} else if (leftHandItem.getItem() instanceof ICellHand && rightHandItem.getItem() instanceof ICellHand) {
			this.renderBloodArm(matrixStackIn, bufferIn, packedLightIn, true, true);
			this.renderHandParticle(entitylivingbaseIn, rightHandItem, HumanoidArm.RIGHT);
			this.renderHandParticle(entitylivingbaseIn, leftHandItem, HumanoidArm.LEFT);

		}
		matrixStackIn.popPose();
	}

	private void renderBloodArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
			boolean renderRightArm, boolean renderLeftArm) {
		copyBloodArmPose(this.getParentModel());
		model.rightArm.visible = renderRightArm;
		model.leftArm.visible = renderLeftArm;
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(skinTexture));
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
	}

	private void copyBloodArmPose(M parentModel) {
		model.setAllVisible(false);
		if (parentModel instanceof HumanoidModel<?> humanoidModel) {
			model.rightArm.copyFrom(humanoidModel.rightArm);
			model.leftArm.copyFrom(humanoidModel.leftArm);
		}
	}

	private void renderHandParticle(LivingEntity living, ItemStack stack, HumanoidArm side) {
		if (Minecraft.getInstance().isPaused()) {
			return;
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ICellHand && living.isUsingItem()) {
			HumanoidArm activeArm = living.getUsedItemHand() == InteractionHand.MAIN_HAND
					? living.getMainArm()
					: living.getMainArm().getOpposite();
			if (activeArm == side) {
				this.spawnParticleFromOrigin(calculateThirdPersonHandOrigin(living, side), living);
			}
		}
	}

	private Vec3 calculateThirdPersonHandOrigin(LivingEntity living, HumanoidArm side) {
		double bodyYaw = Math.toRadians(living.yBodyRot);
		Vec3 forward = new Vec3(-Math.sin(bodyYaw), 0.0D, Math.cos(bodyYaw));
		Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
		double sideOffset = side == HumanoidArm.RIGHT ? 0.36D : -0.36D;

		return living.position()
				.add(0.0D, living.getBbHeight() * 0.72D, 0.0D)
				.add(forward.scale(0.46D))
				.add(right.scale(sideOffset));
	}

	private void spawnParticleFromOrigin(Vec3 origin, LivingEntity player) {
		Level world = player.level();
		int globalPartCount = 20;

		boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
		InteractionHand activeHand = player.getUsedItemHand();
		Random rand = new Random();
		if (itemIsInUse) {
			if (player.getItemInHand(activeHand).getItem() instanceof BloodAbsorptionItem) {
				List<Entity> targets = player.level().getEntities(player, player.getBoundingBox().inflate(5.0));
				if (targets.size() > 0) {
					for (Entity target : targets) {
						if (target instanceof LivingEntity) {
							LivingEntity livingTarget = (LivingEntity) target;
							Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
							Vec3 finalPos = origin.subtract(targetVec.x, targetVec.y, targetVec.z).reverse();
							Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(target);
							ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
							Particle created = Minecraft.getInstance().particleEngine.createParticle(AbsrobedBloodCellParticleFactory.createData(targetColor), origin.x,
									origin.y, origin.z, (float) finalPos.x + rand.nextFloat() - 0.5D,
									(float) finalPos.y - rand.nextFloat() - 0F,
									(float) finalPos.z + rand.nextFloat() - 0.5D);
							if (created instanceof AbsorbedBloodCellParticle particle) {
								particle.setTargetYOffset(0.0D);
							}
						}
					}
				}

			} else {
				HitResult trace = player.pick(5, HLClientUtils.getPartialTicks(), true);
				if (trace.getType() == Type.BLOCK) {
					Vec3 hitVec = trace.getLocation();
					Vec3 projectionTarget = hitVec.add(0.0D, 1.05D, 0.0D);
					Vec3 finalPos = projectionTarget.subtract(origin.x, origin.y, origin.z).reverse();
					world.addParticle(AbsrobedBloodCellParticleFactory.createData(ParticleColor.BLOOD),
							projectionTarget.x, projectionTarget.y, projectionTarget.z,
							(float) finalPos.x + rand.nextFloat() - 0.5D,
							(float) finalPos.y - rand.nextFloat() - 0.5F,
							(float) finalPos.z + rand.nextFloat() - 0.5D);

				}
			}
			Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15,
					false);
			// particlePos = particlePos.reverse();

			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						origin.x() + inversedSphere[i].x, origin.y() + inversedSphere[i].y,
						origin.z() + inversedSphere[i].z, 0, 0.00, 0);

			}
		}
	}
}
