package com.vincenthuto.hemomancy.radial;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.hemomancy.ConfigData;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.radial.finder.CharmFinder;
import com.vincenthuto.hemomancy.radial.finder.PacketOpenCharm;

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
	public static KeyMapping OPEN_TOOL_MENU_KEYBIND;
	public static KeyMapping CYCLE_TOOL_MENU_LEFT_KEYBIND;
	public static KeyMapping CYCLE_TOOL_MENU_RIGHT_KEYBIND;

	public static KeyMapping OPEN_BELT_SLOT_KEYBIND;
	public static KeyMapping OPEN_CHARM_SLOT_KEYBIND;

	public static void wipeOpen() {
		Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
		while (OPEN_TOOL_MENU_KEYBIND.consumeClick()) {
		}
	}

	public static void initKeybinds() {
		ClientRegistry.registerKeyBinding(
				OPEN_TOOL_MENU_KEYBIND = new KeyMapping("key.toolbelt.open", GLFW.GLFW_KEY_R, "key.toolbelt.category"));

		ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_LEFT_KEYBIND = new KeyMapping("key.toolbelt.cycle.left",
				InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

		ClientRegistry.registerKeyBinding(CYCLE_TOOL_MENU_RIGHT_KEYBIND = new KeyMapping("key.toolbelt.cycle.right",
				InputConstants.UNKNOWN.getValue(), "key.toolbelt.category"));

		ClientRegistry.registerKeyBinding(
				OPEN_BELT_SLOT_KEYBIND = new KeyMapping("key.toolbelt.slot", GLFW.GLFW_KEY_V, "key.toolbelt.category"));
		ClientRegistry.registerKeyBinding(OPEN_CHARM_SLOT_KEYBIND = new KeyMapping("key.charm_slot.slot",
				GLFW.GLFW_KEY_B, "key.hemomancy.category"));

	}

	private static boolean toolMenuKeyWasDown = false;

	@SubscribeEvent
	public static void handleKeys(TickEvent.ClientTickEvent ev) {
		if (ev.phase != TickEvent.Phase.START)
			return;

		Minecraft mc = Minecraft.getInstance();
		while (OPEN_CHARM_SLOT_KEYBIND.consumeClick()) {
			if (mc.screen == null) {
				PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketOpenCharm());
			}
		}
		if (mc.screen == null) {
			boolean toolMenuKeyIsDown = OPEN_TOOL_MENU_KEYBIND.isDown();
			if (toolMenuKeyIsDown && !toolMenuKeyWasDown) {
				while (OPEN_TOOL_MENU_KEYBIND.consumeClick()) {
					if (mc.screen == null) {
						ItemStack inHand = mc.player.getMainHandItem();
						System.out.println("hit that shit");
						if (ConfigData.isItemStackAllowed(inHand)) {
							CharmFinder.findCharm(mc.player)
								.ifPresent((getter) -> mc.setScreen(new RadialMenuScreen(getter)));
						}
					}
				}
			}
			toolMenuKeyWasDown = toolMenuKeyIsDown;
		} else {
			toolMenuKeyWasDown = true;
		}

//		if (ConfigData.customBeltSlotEnabled) {
//			while (OPEN_BELT_SLOT_KEYBIND.consumeClick()) {
//				if (mc.screen == null) {
//					Hemomancy.channel.sendToServer(new OpenBeltSlotInventory());
//				}
//			}
//		}
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
