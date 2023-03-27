package com.vincenthuto.hemomancy.event;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.gui.radial.RadialCharmScreen;
import com.vincenthuto.hemomancy.init.KeyBindInit;
import com.vincenthuto.hemomancy.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.ChangeSelectedManipPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UseContManipKeyPacket;
import com.vincenthuto.hemomancy.network.capa.manips.UseQuickManipKeyPacket;
import com.vincenthuto.hemomancy.network.keybind.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.network.keybind.BloodFormationKeyPressPacket;
import com.vincenthuto.hemomancy.network.morphling.ChangeMorphKeyPacket;
import com.vincenthuto.hemomancy.network.morphling.JarTogglePickupPacket;
import com.vincenthuto.hemomancy.network.particle.GroundBloodDrawPacket;
import com.vincenthuto.hutoslib.client.HLClientUtils;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class KeyBindEvents {
	private static boolean menuKey = false;

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
		if (KeyBindInit.toggleMorphlingJarPickup.consumeClick()) {
			PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new JarTogglePickupPacket());
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

		// Radial
		if (event.phase != TickEvent.Phase.START)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null) {
			boolean toolMenuKeyIsDown = KeyBindInit.openVascCharmMenu.isDown();
			if (toolMenuKeyIsDown && !menuKey) {
				while (KeyBindInit.openVascCharmMenu.consumeClick()) {
					if (mc.screen == null) {
						mc.player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
							if (inv.getStackInSlot(4).getItem() instanceof VasculariumCharmItem charm) {
								mc.setScreen(new RadialCharmScreen(inv));
							}
						});
					}
				}
			}
			menuKey = toolMenuKeyIsDown;
		} else {
			menuKey = true;
		}

	}

	public static boolean isKeyDown(KeyMapping keybind) {
		if (keybind.isUnbound())
			return false;

		boolean isDown = switch (keybind.getKey().getType()) {
		case KEYSYM ->
			InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
		case MOUSE -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(),
				keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
		default -> false;
		};
		return isDown && keybind.getKeyConflictContext().isActive()
				&& keybind.getKeyModifier().isActive(keybind.getKeyConflictContext());
	}

//	public static void wipeOpen() {
//		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
//
//		while (KeyBindInit.openVascCharmMenu.consumeClick()) {
//		}
//	}
}
