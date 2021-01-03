package com.huto.hemomancy.event;

import org.lwjgl.glfw.GLFW;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.containers.ContainerRuneBinder;
import com.huto.hemomancy.gui.mindrunes.GuiChiselStation;
import com.huto.hemomancy.gui.mindrunes.GuiRuneBinder;
import com.huto.hemomancy.gui.mindrunes.PlayerExpandedScreen;
import com.huto.hemomancy.registries.ContainerInit;
import com.huto.hemomancy.registries.TileEntityInit;
import com.huto.hemomancy.render.tile.RenderChiselStation;
import com.huto.hemomancy.render.tile.RenderRuneModStation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {
	public static NonNullList<KeyBinding> keyBinds = NonNullList.create();

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
	
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.runic_chisel_station.get(), RenderChiselStation::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.rune_mod_station.get(), RenderRuneModStation::new);
	
		ScreenManager.registerFactory(ContainerInit.runic_chisel_station.get(), GuiChiselStation::new);
		ScreenManager.registerFactory(ContainerInit.playerrunes, PlayerExpandedScreen::new);
		ScreenManager.registerFactory(ContainerRuneBinder.type, GuiRuneBinder::new);
		keyBinds.add(0, new KeyBinding("key.Hemomancy.runebinderpickup.desc", GLFW.GLFW_KEY_B,
				"key.Hemomancy.category"));
		ClientRegistry.registerKeyBinding(keyBinds.get(0));

	}

	public static PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

}
