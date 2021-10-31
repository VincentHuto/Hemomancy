package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.FORGE)
public class CameraEventSubscriber {
	@SubscribeEvent
	public static void cameraVIew(EntityEvent.Size event) {
		if (event.getEntity()instanceof Player player) {
			player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
			//	System.out.println(manip.getKnownManips());

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
