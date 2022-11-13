package com.vincenthuto.hemomancy.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.volume.RenderBloodLaserEvent;
import com.vincenthuto.hemomancy.gui.CharmGourdScreen;
import com.vincenthuto.hemomancy.gui.JuiceinatorScreen;
import com.vincenthuto.hemomancy.gui.VialCentrifugeScreen;
import com.vincenthuto.hemomancy.gui.VisceralRecallerScreen;
import com.vincenthuto.hemomancy.gui.guide.HemoLib;
import com.vincenthuto.hemomancy.gui.living.LivingStaffScreen;
import com.vincenthuto.hemomancy.gui.living.LivingSyringeScreen;
import com.vincenthuto.hemomancy.gui.living.MorphlingJarScreen;
import com.vincenthuto.hemomancy.gui.overlay.BloodVolumeOverlay;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.render.tile.DendriticDistributorRenderer;
import com.vincenthuto.hemomancy.render.tile.EarthenVeinRenderer;
import com.vincenthuto.hemomancy.render.tile.MorphlingIncubatorRenderer;
import com.vincenthuto.hemomancy.render.tile.MortalDisplayRenderer;
import com.vincenthuto.hemomancy.render.tile.UnstainedPodiumRenderer;
import com.vincenthuto.hemomancy.render.tile.VialCentrifugeRenderer;
import com.vincenthuto.hemomancy.render.tile.VisceralRecallerRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {

	@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
	public static class ClientForgeEvents {

		@SubscribeEvent
		public static void cameraView(EntityEvent.Size event) {
			if (event.getEntity() instanceof Player player) {
				if (player.isAddedToWorld()) {
					player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
						if (manip.isAvatarActive()) {
							event.setNewEyeHeight(3.5f);
							event.setNewSize(player.getDimensions(Pose.STANDING).scale(2));
						} else {
							if (player.isCrouching()) {
								event.setNewSize(player.getDimensions(Pose.CROUCHING));

							} else {
								event.setNewEyeHeight(Player.DEFAULT_EYE_HEIGHT);
								event.setNewSize(player.getDimensions(Pose.STANDING));

							}

						}
					});
				}
			}
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
			pMatrixStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + pEquippedProgress * -0.6F, f4 + -0.71999997F);
			pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0F));
			float f5 = Mth.sin(pSwingProgress * pSwingProgress * (float) Math.PI);
			float f6 = Mth.sin(f1 * (float) Math.PI);
			pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * f6 * 70.0F));
			pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * f5 * -20.0F));
			AbstractClientPlayer abstractclientplayer = minecraft.player;
			RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
			pMatrixStack.translate(f * -1.0F, 3.6F, 3.5D);
			pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0F));
			pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(200.0F));
			pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -135.0F));
			pMatrixStack.translate(f * 5.6F, 0.0D, 0.0D);
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
			if (event.getEntity().isAddedToWorld()) {
				event.getEntity().getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
					if (manip.isAvatarActive()) {
						event.getPoseStack().translate(0, 2, 0);
					}
				});

			}
		}

	}

	@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
	public static class ClientModBusEvents {
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			MinecraftForge.EVENT_BUS.register(RenderBloodLaserEvent.class);
			HemoLib hemo = new HemoLib();
			hemo.registerTome();

			// Tiles
			BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), MorphlingIncubatorRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), UnstainedPodiumRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.dendritic_distributor.get(),
					DendriticDistributorRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), MortalDisplayRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.vial_centrifuge.get(), VialCentrifugeRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.visceral_artificial_recaller.get(),
					VisceralRecallerRenderer::new);
			BlockEntityRenderers.register(BlockEntityInit.earthen_vein.get(), EarthenVeinRenderer::new);
			MenuScreens.register(ContainerInit.gourd_charm_inventory.get(), CharmGourdScreen::new);
			MenuScreens.register(ContainerInit.vial_centrifuge.get(), VialCentrifugeScreen::new);
			MenuScreens.register(ContainerInit.visceral_recaller.get(), VisceralRecallerScreen::new);
			MenuScreens.register(ContainerInit.morphling_jar.get(), MorphlingJarScreen::new);
			MenuScreens.register(ContainerInit.living_syringe.get(), LivingSyringeScreen::new);
			MenuScreens.register(ContainerInit.living_staff.get(), LivingStaffScreen::new);
			MenuScreens.register(ContainerInit.juiceinator.get(), JuiceinatorScreen::new);

		}

		// Overlay
		@SubscribeEvent
		public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
			event.registerAboveAll("bloodvolume", BloodVolumeOverlay.HUD_BLOODVOLUME);
		}
	}
}
