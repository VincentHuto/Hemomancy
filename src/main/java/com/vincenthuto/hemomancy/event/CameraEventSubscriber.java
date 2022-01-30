package com.vincenthuto.hemomancy.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.init.RenderTypeInit;
import com.vincenthuto.hemomancy.model.armor.ModelBloodAvatar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.FORGE)
public class CameraEventSubscriber {

//	@SubscribeEvent
//	public static void skybox(RenderLevelLastEvent event) {
//		ClientLevel level = ClientUtils.getWorld();
//		LevelRenderer levelRenderer = event.getLevelRenderer();
//		LocalPlayer player = (LocalPlayer) ClientUtils.getClientPlayer();
//		if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == ItemInit.sanguine_formation.get()) {
//			level.effects().setSkyRenderHandler(new BloodMoonSkyRenderHandler());
//			level.effects().setWeatherRenderHandler(new BloodMoonWeatherRenderHandler());
//		} else {
//			level.effects().setSkyRenderHandler(null);
//			level.effects().setWeatherRenderHandler(null);
//		}

//	}

	@SubscribeEvent
	public static void cameraVIew(EntityEvent.Size event) {
		if (event.getEntity()instanceof Player player) {
			if (player.isAddedToWorld()) {
				player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
					if (manip.isAvatarActive()) {
						event.setNewEyeHeight(3.5f);
						event.setNewSize(player.getDimensions(Pose.STANDING).scale(2));
					} else {
						event.setNewEyeHeight(Player.DEFAULT_EYE_HEIGHT);
						event.setNewSize(player.getDimensions(Pose.STANDING));

					}
				});
			}
		}
	}

	private static Font fontRenderer;

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(receiveCanceled = true)
	public static void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {

		if (fontRenderer == null) {
			fontRenderer = Minecraft.getInstance().font;
		}
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			if (player.isAlive()) {
				// Redraws Icons so they dont get overwrote
				Minecraft.getInstance().textureManager
						.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
				// Coven color Overlay

				player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manip -> {
					if (manip.isAvatarActive()) {
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
						float time = (float) Math.sin(player.level.getGameTime() * 0.05f);
						RenderSystem.setShaderColor(1, 1, 1, 0.5f + time);
						RenderSystem._setShaderTexture(0,
								new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_shot_overlay.png"));
						event.getMatrixStack().pushPose();
						float ratio = (float) event.getWindow().getGuiScale();
						event.getMatrixStack().scale(1 / ratio, 1 / ratio, 1);
						GuiComponent.blit(event.getMatrixStack(), 0, 0, 0, 0, event.getWindow().getScreenWidth(),
								event.getWindow().getScreenHeight(), event.getWindow().getScreenWidth(),
								event.getWindow().getScreenHeight());
						event.getMatrixStack().popPose();
						Minecraft.getInstance().textureManager
								.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
					}
				});

//				IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
//				if (volume.isActive()) {
//					if (volume.getBloodVolume() < 250) {
//						RenderSystem.setShader(GameRenderer::getPositionTexShader);
//						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//						float time = (float) Math.sin(player.level.getGameTime() * 0.05f);
//						RenderSystem.setShaderColor(1, 1, 1, 0.5f + time);
//						RenderSystem._setShaderTexture(0,
//								new ResourceLocation(Hemomancy.MOD_ID, "textures/gui/blood_shot_overlay.png"));
//						event.getMatrixStack().pushPose();
//						float ratio = (float) event.getWindow().getGuiScale();
//						event.getMatrixStack().scale(1 / ratio, 1 / ratio, 1);
//						GuiComponent.blit(event.getMatrixStack(), 0, 0, 0, 0, event.getWindow().getScreenWidth(),
//								event.getWindow().getScreenHeight(), event.getWindow().getScreenWidth(),
//								event.getWindow().getScreenHeight());
//						event.getMatrixStack().popPose();
//						Minecraft.getInstance().textureManager
//								.bindForSetup(new ResourceLocation("minecraft", "textures/gui/icons.png"));
//					}
//				}
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void renderPlayerFirstPerson(RenderHandEvent event) {
//		PoseStack ms = event.getPoseStack();
//		Minecraft mc = Minecraft.getInstance();
//		AbstractClientPlayer player = mc.player;
//		ms.pushPose();
//		player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manip -> {
//			if (manip.isAvatarActive()) {
//				mc.getTextureManager().bindForSetup(mc.player.getSkinTextureLocation());
//				PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
//				PlayerModel<AbstractClientPlayer> model = playerrenderer.getModel();
//				final ModelBloodAvatar<AbstractClientPlayer> bloodAvatar = new ModelBloodAvatar<>(
//						Minecraft.getInstance().getEntityModels().bakeLayer(ModelBloodAvatar.layer));
//				ms.scale(2.75f, 2.75f, 2.75f);
//				ResourceLocation glowTexture = new ResourceLocation(Hemomancy.MOD_ID,
//						"textures/models/armor/avatar_glow.png");
//				VertexConsumer swirlConsumer = event.getMultiBufferSource()
//						.getBuffer(RenderTypeInit.energySwirl(glowTexture, 1 % 4.0F, 1 * .01F % 2.0F));
//				bloodAvatar.setupAnim(mc.player, 1, 1, 1, 1, 1);
//				bloodAvatar.renderToBuffer(ms, swirlConsumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY, 0.5F,
//						0.5F, 0.5F, 0.3F);
//				renderPlayerArm(ms, event.getMultiBufferSource(), event.getPackedLight(), event.getEquipProgress(),
//						event.getSwingProgress(),
//						event.getHand().equals(InteractionHand.MAIN_HAND) ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
//			}
//		});
//		ms.popPose();

	}

	public static void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight,
			float pEquippedProgress, float pSwingProgress, HumanoidArm pSide) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean flag = pSide != HumanoidArm.LEFT;
		float f = flag ? 1.0F : -1.0F;
		float f1 = Mth.sqrt(pSwingProgress);
		float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
		float f3 = 0.4F * Mth.sin(f1 * ((float) Math.PI * 2F));
		float f4 = -0.4F * Mth.sin(pSwingProgress * (float) Math.PI);
		pMatrixStack.translate((double) (f * (f2 + 0.64000005F)), (double) (f3 + -0.6F + pEquippedProgress * -0.6F),
				(double) (f4 + -0.71999997F));
		pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
		float f5 = Mth.sin(pSwingProgress * pSwingProgress * (float) Math.PI);
		float f6 = Mth.sin(f1 * (float) Math.PI);
		pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
		pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));
		AbstractClientPlayer abstractclientplayer = minecraft.player;
		RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
		pMatrixStack.translate((double) (f * -1.0F), (double) 3.6F, 3.5D);
		pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
		pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
		pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
		pMatrixStack.translate((double) (f * 5.6F), 0.0D, 0.0D);
		pMatrixStack.scale(2, 2, 2);
		PlayerRenderer playerrenderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher()
				.<AbstractClientPlayer>getRenderer(abstractclientplayer);
		if (flag) {
			playerrenderer.renderRightHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
		} else {
			playerrenderer.renderLeftHand(pMatrixStack, pBuffer, pCombinedLight, abstractclientplayer);
		}

	}

	@SubscribeEvent
	public static void renderPlayerSize(RenderPlayerEvent event) {
		if (event.getEntity()instanceof Player player) {
			if (player.isAddedToWorld()) {
				player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
					// System.out.println(manip.isAvatarActive());
					if (manip.isAvatarActive()) {
						// event.getPoseStack().scale(2, 2, 2);
						event.getPoseStack().translate(0, 2, 0);
					} else {
						// event.getPoseStack().scale(1, 1, 1);
					}
				});

			}
		}
	}

}
