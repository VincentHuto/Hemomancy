package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.KeyBindInit;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UseContManipKeyPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UseQuickManipKeyPacket;
import com.vincenthuto.hemomancy.network.keybind.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.network.keybind.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.network.morphling.ChangeMorphKeyPacket;
import com.vincenthuto.hemomancy.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hemomancy.recipe.BloodStructureRecipe;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class KeyBindEvents {
	public static void onClientTick(ClientTickEvent event) {

		if (KeyBindInit.bloodFormation.consumeClick()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new BloodFormationKeyPressPacket());
		}

		if (KeyBindInit.bloodCrafting.consumeClick()) {

			PacketHandler.CHANNELBLOODVOLUME
					.sendToServer(new BloodCraftingKeyPressPacket(HLClientUtils.getClientPlayer().getMainHandItem()));
		}

		if (KeyBindInit.bloodDraw.isDown()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new GroundBloodDrawPacket(HLClientUtils.getPartialTicks()));
		}
		if (KeyBindInit.toggleMorphlingOpenJar.consumeClick()) {
			PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new ChangeMorphKeyPacket());

		}

		if (KeyBindInit.cycleSelectedManip.consumeClick()) {
			PacketHandler.CHANNELKNOWNMANIPS
					.sendToServer(new ChangeSelectedManipPacket(HLClientUtils.getPartialTicks()));
		}
		if (KeyBindInit.useQuickManip.consumeClick()) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new UseQuickManipKeyPacket(HLClientUtils.getPartialTicks()));
		}
		if (KeyBindInit.useContManip.isDown()) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new UseContManipKeyPacket(HLClientUtils.getPartialTicks()));
		}
	}

}
