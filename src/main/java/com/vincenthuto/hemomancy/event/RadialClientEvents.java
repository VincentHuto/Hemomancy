package com.vincenthuto.hemomancy.event;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.hemomancy.ConfigData;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.player.charm.CharmFinder;
import com.vincenthuto.hemomancy.capa.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.gui.radial.RadialCharmScreen;
import com.vincenthuto.hemomancy.init.KeyBindInit;
import com.vincenthuto.hemomancy.item.VasculariumCharmItem;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.charm.OpenCharmPacket;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RadialClientEvents {

	public static void wipeOpen() {
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		while (KeyBindInit.openVascCharmMenu.consumeClick()) {
		}
	}

	public static void initKeybinds() {
		ClientRegistry.registerKeyBinding(KeyBindInit.openVascCharmMenu);

	}

	private static boolean toolMenuKeyWasDown = false;

	@SubscribeEvent
	public static void handleKeys(TickEvent.ClientTickEvent ev) {
		if (ev.phase != TickEvent.Phase.START)
			return;

		Minecraft mc = Minecraft.getInstance();
		while (KeyBindInit.OPEN_CHARM_SLOT_KEYBIND.consumeClick()) {
			if (mc.screen == null) {
				PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new OpenCharmPacket());
			}
		}
		if (mc.screen == null) {
			boolean toolMenuKeyIsDown = KeyBindInit.openVascCharmMenu.isDown();
			if (toolMenuKeyIsDown && !toolMenuKeyWasDown) {
				while (KeyBindInit.openVascCharmMenu.consumeClick()) {
					if (mc.screen == null) {
						mc.player.getCapability(RunesCapabilities.RUNES).ifPresent(inv -> {
							if (inv.getStackInSlot(4).getItem() instanceof VasculariumCharmItem charm) {
								
								mc.setScreen(new RadialCharmScreen(inv.getStackInSlot(4)));
							}
						});
						
//						CharmFinder.findCharm(mc.player)
//								.ifPresent((getter) -> mc.setScreen(new RadialCharmScreen(getter)));
					}
				}
			}
			toolMenuKeyWasDown = toolMenuKeyIsDown;
		} else {
			toolMenuKeyWasDown = true;
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

}
