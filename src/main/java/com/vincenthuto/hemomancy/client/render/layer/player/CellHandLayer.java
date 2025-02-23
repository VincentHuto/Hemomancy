
package com.vincenthuto.hemomancy.client.render.layer.player;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.client.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.util.EntityParticleUtils;
import com.vincenthuto.hemomancy.common.item.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.tool.living.ICellHand;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class CellHandLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	@SuppressWarnings("rawtypes")
	public static BloodArmModel model;
	public final ResourceLocation skinTexture = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/entity/hardened_skin.png");

	public CellHandLayer(RenderLayerParent<T, M> rendererIn) {
		super(rendererIn);
		model = new BloodArmModel<T>(Minecraft.getInstance().getEntityModels().bakeLayer((BloodArmModel.blood_arm)));

	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {

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
			this.renderHandParticle(entitylivingbaseIn, rightHandItem,
					ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStackIn, bufferIn,
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
			this.renderHandParticle(entitylivingbaseIn, leftHandItem,
					ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStackIn, bufferIn,
					packedLightIn);
			// Both Hands
		} else if (leftHandItem.getItem() instanceof ICellHand && rightHandItem.getItem() instanceof ICellHand) {
			model.rightArm = playerrenderer.getModel().rightArm;
			model.leftArm = playerrenderer.getModel().leftArm;
			VertexConsumer ivertexbuilder = bufferIn.getBuffer(model.renderType(skinTexture));
			model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 255, 0, 0,
					100);
			PlayerModel<AbstractClientPlayer> playermodel = playerrenderer.getModel();
			this.renderHandParticle(entitylivingbaseIn, rightHandItem,
					ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, matrixStackIn, bufferIn,
					packedLightIn);
			this.renderHandParticle(entitylivingbaseIn, leftHandItem,
					ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, matrixStackIn, bufferIn,
					packedLightIn);

		}
		matrixStackIn.popPose();
	}

	@SuppressWarnings("unused")
	private void renderHandParticle(LivingEntity living, ItemStack stack, ItemDisplayContext ItemDisplayContext,
			HumanoidArm side, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		if (Minecraft.getInstance().isPaused()) {
			return;
		}
		if (!stack.isEmpty() && stack.getItem() instanceof ICellHand) {
			boolean leftHand;
			matrixStack.pushPose();
			Matrix4f curMatrix = matrixStack.last().pose();
			Matrix4f inverted = new Matrix4f(curMatrix);
			inverted.invert();
			curMatrix.mul(inverted);
			matrixStack.mulPose(Vector3.YP.rotationDegrees(-living.yBodyRot).toMoj());
			matrixStack.mulPose(Vector3.XN.rotationDegrees(-90.0f).toMoj());
			((ArmedModel) this.getParentModel()).translateToHand(side, matrixStack);
			boolean bl = leftHand = side == HumanoidArm.LEFT;
			if (leftHand) {
				matrixStack.translate(0.225, 0.65, -0.95);
			} else {
				matrixStack.translate(-0.225, 0.65, -0.95);
			}
			this.spawnParticleFromMatrix(matrixStack, living, ItemDisplayContext);
			matrixStack.popPose();
		}
	}

	@SuppressWarnings("unused")
	private void spawnParticleFromMatrix(PoseStack matrixStackIn, LivingEntity player,
			ItemDisplayContext type) {
		Vec3 playerPos = player.position();
		Level world = player.level();
		Matrix4f curMatrix = matrixStackIn.last().pose();
		Vec3 particlePos = playerPos.add(new Vec3(curMatrix.m03(), curMatrix.m13(), curMatrix.m23()));
		Vec3 origin = new Vec3(particlePos.x, particlePos.y + 0.1, particlePos.z);
		int globalPartCount = 20;

		boolean playerIsRightHanded = player.getMainArm() == HumanoidArm.RIGHT;
		boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
		InteractionHand activeHand = player.getUsedItemHand();
		Random rand = new Random();
		Vec3 vec = particlePos;
		if (itemIsInUse) {
			if (player.getItemInHand(activeHand).getItem() instanceof BloodAbsorptionItem) {
				List<Entity> targets = player.level().getEntities(player, player.getBoundingBox().inflate(5.0));
				if (targets.size() > 0) {
					for (Entity target : targets) {
						if (target instanceof LivingEntity) {
							LivingEntity livingTarget = (LivingEntity) target;
							Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
							Vec3 finalPos = vec.subtract(targetVec.x, targetVec.y, targetVec.z).reverse();
							Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(target);
							ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
							world.addParticle(AbsrobedBloodCellParticleFactory.createData(targetColor), vec.x,
									vec.y + 1.05D, vec.z, (float) finalPos.x + rand.nextFloat() - 0.5D,
									(float) finalPos.y - rand.nextFloat() - 0F,
									(float) finalPos.z + rand.nextFloat() - 0.5D);
						}
					}
				}

			} else {
				HitResult trace = player.pick(5, HLClientUtils.getPartialTicks(), true);
				if (trace.getType() == Type.BLOCK) {
					Vec3 hitVec = trace.getLocation();
					Vec3 finalPos = hitVec.subtract(vec.x, vec.y, vec.z).reverse();
					world.addParticle(AbsrobedBloodCellParticleFactory.createData(ParticleColor.BLOOD), hitVec.x,
							hitVec.y + 1.05D, hitVec.z, (float) finalPos.x + rand.nextFloat() - 0.5D,
							(float) finalPos.y - rand.nextFloat() - 0.5F, (float) finalPos.z + rand.nextFloat() - 0.5D);

				}
			}
			Vec3[] fibboSphere = HLParticleUtils.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vec3[] corona = HLParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15,
					false);
			// particlePos = particlePos.reverse();

			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						particlePos.x() + inversedSphere[i].x, particlePos.y() + inversedSphere[i].y,
						particlePos.z() + inversedSphere[i].z, 0, 0.00, 0);

			}
		}
	}
}