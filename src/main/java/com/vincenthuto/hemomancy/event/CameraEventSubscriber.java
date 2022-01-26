package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
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
	public static void renderPlayerSize(RenderPlayerEvent event) {
		if (event.getEntity()instanceof Player player) {
			if (player.isAddedToWorld()) {
				player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
				//	System.out.println(manip.isAvatarActive());
					if (manip.isAvatarActive()) {
				//		event.getPoseStack().scale(2, 2, 2);
						event.getPoseStack().translate(0, 2, 0);
					} else {
				//		event.getPoseStack().scale(1, 1, 1);
					}
				});

			}
		}
	}

}
