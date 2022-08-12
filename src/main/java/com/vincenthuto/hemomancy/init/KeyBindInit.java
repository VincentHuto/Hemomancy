package com.vincenthuto.hemomancy.init;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class KeyBindInit {
	public static final KeyMapping bloodFormation = new KeyMapping("key.hemomancy.bloodformation.desc", GLFW.GLFW_KEY_F,
			"key.hemomancy.category");
	public static final KeyMapping bloodCrafting = new KeyMapping("key.hemomancy.bloodcrafting.desc", GLFW.GLFW_KEY_C,
			"key.hemomancy.category");
	public static final KeyMapping bloodDraw = new KeyMapping("key.hemomancy.drawtest.desc", GLFW.GLFW_KEY_LEFT_CONTROL,
			"key.hemomancy.category");
	public static final KeyMapping toggleMorphlingJarPickup = new KeyMapping("key.hemomancy.morphjarpickup.desc",
			GLFW.GLFW_KEY_LEFT_CONTROL, "key.hemomancy.category");
	public static final KeyMapping toggleMorphlingOpenJar = new KeyMapping("key.hemomancy.openjar.desc",
			GLFW.GLFW_KEY_F, "key.hemomancy.category");
	public static final KeyMapping cycleSelectedManip = new KeyMapping("key.hemomancy.cyclemanip.desc", GLFW.GLFW_KEY_C,
			"key.hemomancy.category");
	public static final KeyMapping useQuickManip = new KeyMapping("key.hemomancy.quickusemanip.desc", GLFW.GLFW_KEY_R,
			"key.hemomancy.category");
	public static final KeyMapping useContManip = new KeyMapping("key.hemomancy.contusemanip.desc", GLFW.GLFW_KEY_V,
			"key.hemomancy.category");
	public static final KeyMapping OPEN_TOOL_MENU_KEYBIND = new KeyMapping("key.toolbelt.open", GLFW.GLFW_KEY_R,
			"key.toolbelt.category");
	public static final KeyMapping OPEN_CHARM_SLOT_KEYBIND = new KeyMapping("key.charm_slot.slot", GLFW.GLFW_KEY_B,
			"key.hemomancy.category");
	public static final KeyMapping openVascCharmMenu = new KeyMapping("key.spellbookopen", 90, "key.categories.mna");

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		ClientRegistry.registerKeyBinding(bloodFormation);
		ClientRegistry.registerKeyBinding(bloodCrafting);
		ClientRegistry.registerKeyBinding(bloodDraw);
		ClientRegistry.registerKeyBinding(toggleMorphlingJarPickup);
		ClientRegistry.registerKeyBinding(toggleMorphlingOpenJar);
		ClientRegistry.registerKeyBinding(cycleSelectedManip);
		ClientRegistry.registerKeyBinding(useQuickManip);
		ClientRegistry.registerKeyBinding(useContManip);
		ClientRegistry.registerKeyBinding(useContManip);
		ClientRegistry.registerKeyBinding(OPEN_TOOL_MENU_KEYBIND);
		ClientRegistry.registerKeyBinding(OPEN_CHARM_SLOT_KEYBIND);
		ClientRegistry.registerKeyBinding(openVascCharmMenu);

	}
}
