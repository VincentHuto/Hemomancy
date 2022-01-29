package com.vincenthuto.hemomancy.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.model.armor.ModelBloodAvatar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
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

	@SubscribeEvent
	public static void renderPlayerFirstPerson(RenderHandEvent event) {
		PoseStack ms = event.getPoseStack();
		Minecraft mc = Minecraft.getInstance();
		AbstractClientPlayer player = mc.player;
		player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(manip -> {

			if (manip.isAvatarActive()) {
				mc.getTextureManager().bindForSetup(mc.player.getSkinTextureLocation());
				PlayerRenderer playerrenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
				PlayerModel<AbstractClientPlayer> model = playerrenderer.getModel();
				final ModelBloodAvatar<AbstractClientPlayer> modelBloodAvatar = new ModelBloodAvatar<>(
						Minecraft.getInstance().getEntityModels().bakeLayer(ModelBloodAvatar.layer));
				playerrenderer.getModel().copyPropertiesTo(modelBloodAvatar);
				ms.pushPose();
				ms.scale(2.75f, 2.75f, 2.75f);
				ResourceLocation glowTexture = new ResourceLocation(Hemomancy.MOD_ID,
						"textures/models/armor/avatar_glow.png");
				VertexConsumer swirlConsumer = event.getMultiBufferSource()
						.getBuffer(RenderType.energySwirl(glowTexture, 1 % 4.0F, 1 * .01F % 2.0F));
				modelBloodAvatar.setupAnim(mc.player, 1, 1, 1, 1, 1);
				modelBloodAvatar.renderToBuffer(ms, swirlConsumer, event.getPackedLight(), OverlayTexture.NO_OVERLAY,
						0.5F, 0.5F, 0.5F, 0.3F);
			}
		});

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
