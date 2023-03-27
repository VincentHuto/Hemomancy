package com.vincenthuto.hemomancy.render.item;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.item.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.item.tool.living.ICellHand;
import com.vincenthuto.hemomancy.particle.factory.AbsrobedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.particle.util.EntityParticleUtils;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

public class CellHandItemRenderer extends BlockEntityWithoutLevelRenderer {

	public BakedModel location;

	public final ResourceLocation skinTexture = new ResourceLocation(
			Hemomancy.MOD_ID + ":textures/entity/hardened_skin.png");
	public CellHandItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
	}

	public void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, HumanoidArm side) {
		if (side == HumanoidArm.RIGHT) {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindForSetup(mc.player.getSkinTextureLocation());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
			renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (playerrenderer.getModel()).rightArm,
					(playerrenderer.getModel()).rightSleeve);
		} else {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bindForSetup(mc.player.getSkinTextureLocation());
			PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
			this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (playerrenderer.getModel()).leftArm,
					(playerrenderer.getModel()).leftSleeve);
		}

	}

	private void renderArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, HumanoidArm side) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bindForSetup(mc.player.getSkinTextureLocation());
		matrixStackIn.pushPose();
		if (side == HumanoidArm.RIGHT) {
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(12.0f).toMoj());
			matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-35.0f).toMoj());
			matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(5.0f).toMoj());
			matrixStackIn.translate(1.0, -0.4, 0.8);
			renderArm(matrixStackIn, bufferIn, combinedLightIn, mc.player, side);

		} else {
			matrixStackIn.mulPose(Vector3.YP.rotationDegrees(-12.0f).toMoj());
			matrixStackIn.mulPose(Vector3.XP.rotationDegrees(-35.0f).toMoj());
			matrixStackIn.mulPose(Vector3.ZP.rotationDegrees(5.0f).toMoj());
			matrixStackIn.translate(0.0, -0.3, 0.6);
			renderArm(matrixStackIn, bufferIn, combinedLightIn, mc.player, side);
		}
		matrixStackIn.popPose();
	}

	// ModelBloodArm model = new ModelBloodArm(1.0f);

	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if (transformType != ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND
				&& transformType != ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
			if (transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND) {
				if (!(stack.getItem() instanceof ICellHand)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.RIGHT);
				this.spawnFirstPersonParticlesForStack(stack, HumanoidArm.RIGHT);
			} else if (transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND) {
				if (!(stack.getItem() instanceof ICellHand)) {
					return;
				}
				this.renderArm(matrixStack, buffer, combinedLight, HumanoidArm.LEFT);
				this.spawnFirstPersonParticlesForStack(stack, HumanoidArm.LEFT);
			} else {
				this.renderDefaultItem(stack, transformType, matrixStack, buffer, combinedLight, combinedOverlay);
			}
		}
	}

	private void renderDefaultItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack matrixStack,
			MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		VertexConsumer buffers = buffer.getBuffer(Sheets.cutoutBlockSheet());
		if (stack.getItem() instanceof ICellHand) {
			ICellHand cell = (ICellHand) stack.getItem();
			location = cell.getBakedModel();
			int color = Minecraft.getInstance().getItemColors().getColor(stack, 1);
			if (this.location == null) {
				float r = (color >> 16 & 0xFF) / 255F;
				float g = (color >> 8 & 0xFF) / 255F;
				float b = (color & 0xFF) / 255F;
				Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), buffers,
						null, location, r, g, b, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
			} else if (!stack.isEmpty()) {
				if (this.location.isGui3d()) {
//					ForgeHooksClient.drawItemLayered(Minecraft.getInstance().getItemRenderer(), this.location, stack,
//							matrixStack, buffer, combinedLight, combinedOverlay, true);
					Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(),
							buffers, null, location, 255, 255, 255, 0x000000, combinedOverlay, ModelData.EMPTY, RenderType.solid());
				} else {
					Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(),
							buffers, null, location, 255, 255, 255, combinedLight, combinedOverlay, ModelData.EMPTY, RenderType.solid());
					matrixStack.popPose();
					matrixStack.pushPose();
					Minecraft.getInstance().getItemRenderer().render(stack, transformType,
							transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND, matrixStack, buffer,
							transformType == ItemTransforms.TransformType.GUI ? 0xF000F0 : combinedLight,
							transformType == ItemTransforms.TransformType.GUI ? OverlayTexture.NO_OVERLAY
									: combinedOverlay,
							this.location);
				}
			}
		}
	}

	private void renderItem(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
		Minecraft mc = Minecraft.getInstance();

		mc.getTextureManager().bindForSetup(mc.player.getSkinTextureLocation());
		PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
		PlayerModel<AbstractClientPlayer> playermodel = playerrenderer.getModel();
		// playerrenderer.setModelVisibilities(playerIn);
		playermodel.attackTime = 0.0F;
		playermodel.crouching = false;
		playermodel.swimAmount = 0.0F;
		playermodel.setupAnim(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		rendererArmIn.xRot = 0.0F;
		VertexConsumer ivertexbuilder = bufferIn.getBuffer(playermodel.renderType(skinTexture));
		rendererArmIn.render(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY);
		rendererArmwearIn.xRot = 0.0F;
		rendererArmwearIn.render(matrixStackIn,
				bufferIn.getBuffer(RenderType.entityTranslucent(playerIn.getSkinTextureLocation())), combinedLightIn,
				OverlayTexture.NO_OVERLAY);
	}

	@SuppressWarnings("unused")
	private void spawnFirstPersonParticlesForStack(ItemStack stack, HumanoidArm hand) {
		if (Minecraft.getInstance().isPaused() || !(stack.getItem() instanceof ICellHand)) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		boolean playerIsRightHanded = player.getMainArm() == HumanoidArm.RIGHT;
		boolean itemIsInUse = player.getUseItemRemainingTicks() > 0;
		InteractionHand activeHand = player.getUsedItemHand();
		Level world = player.level;
		int globalPartCount = 20;

		if (itemIsInUse) {
			Random rand = new Random();
			Vec3 particlePos = player.position().add(0.0, player.getEyeHeight() - 0.2f, 0.0);
			Vec3 look = player.getLookAngle().normalize().scale(0.5);
			Vec3 perp = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize()
					.scale(hand == HumanoidArm.LEFT ? (double) -0.4f : (double) 0.4f);
			particlePos = particlePos.add(look).add(perp);

			Vec3 origin = new Vec3(particlePos.x, particlePos.y + 0.1, particlePos.z);

			if (player.getItemInHand(activeHand).getItem() instanceof BloodAbsorptionItem) {

				List<Entity> targets = player.level.getEntities(player, player.getBoundingBox().inflate(5.0));
				if (targets.size() > 0) {
					for (Entity target : targets) {
						if (target instanceof LivingEntity) {
							LivingEntity livingTarget = (LivingEntity) target;
							Vector3 targetVec = Vector3.fromEntityCenter(livingTarget);
							Vec3 finalPos = particlePos.subtract(targetVec.x, targetVec.y, targetVec.z).reverse();
							Predicate<Entity> targetPred = EntityParticleUtils.getEntityPredicate(target);
							ParticleColor targetColor = EntityParticleUtils.getColorFromPredicate(targetPred);
							world.addParticle(AbsrobedBloodCellParticleFactory.createData(targetColor), particlePos.x,
									particlePos.y + 1.05D, particlePos.z, (float) finalPos.x + rand.nextFloat() - 0.5D,
									(float) finalPos.y - rand.nextFloat() - 0F,
									(float) finalPos.z + rand.nextFloat() - 0.5D);
						}
					}
				}

			} else {
				HitResult trace = player.pick(5, HLClientUtils.getPartialTicks(), true);
				if (trace.getType() == Type.BLOCK) {
					Vec3 hitVec = trace.getLocation();
					Vec3 finalPos = hitVec.subtract(particlePos.x, particlePos.y, particlePos.z).reverse();

					world.addParticle(AbsrobedBloodCellParticleFactory.createData(ParticleColor.BLOOD), hitVec.x,
							hitVec.y + 1.05D, hitVec.z, ((float) finalPos.x) - 0.5D,
							(float) finalPos.y - rand.nextFloat() - 0.5F, ((float) finalPos.z) - 0.5D);
				}
			}

			Vec3[] fibboSphere = HLParticleUtils.fibboSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vec3[] corona = HLParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.15);
			Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -world.getGameTime() * 0.016, 0.15,
					false);
			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(BloodCellParticleFactory.createData(new ParticleColor(255, 0, 0)),
						origin.x() + inversedSphere[i].x, origin.y() + inversedSphere[i].y,
						origin.z() + inversedSphere[i].z, 0, 0.00, 0);

			}

		}
	}
}