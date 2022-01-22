package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.render.handler.BloodMoonSkyRenderHandler;
import com.vincenthuto.hemomancy.render.handler.BloodMoonWeatherRenderHandler;
import com.vincenthuto.hutoslib.client.ClientUtils;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.FORGE)
public class CameraEventSubscriber {

	@SubscribeEvent
	public static void skybox(RenderLevelLastEvent event) {
		ClientLevel level = ClientUtils.getWorld();
		LevelRenderer levelRenderer = event.getLevelRenderer();
		LocalPlayer player = (LocalPlayer) ClientUtils.getClientPlayer();
		if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == ItemInit.sanguine_formation.get()) {
			level.effects().setSkyRenderHandler(new BloodMoonSkyRenderHandler());
			level.effects().setWeatherRenderHandler(new BloodMoonWeatherRenderHandler());
		} else {
			level.effects().setSkyRenderHandler(null);
			level.effects().setWeatherRenderHandler(null);
		}

	}

	@SubscribeEvent
	public static void cameraVIew(EntityEvent.Size event) {
		if (event.getEntity()instanceof Player player) {
			if (player.isAddedToWorld()) {
				player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
					System.out.println(manip.getKnownManips());
					System.out.println(manip.getSelectedManip());
					System.out.println(manip.isAvatarActive());

					if (player.isAddedToWorld()) {
//					float oldHeight = event.getOldEyeHeight();
//					float defaultHeight = Player.DEFAULT_EYE_HEIGHT;
//					float crouchHeight = Player.CROUCH_BB_HEIGHT;
//			if(player.isCrouching()) {
//				event.setNewEyeHeight(crouchHeight);
//			}
						// float oldHeight = event.getOldEyeHeight();
						// event.setNewEyeHeight(defaultHeight);
					}
				});
			}
		}
	}

}
