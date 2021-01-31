package com.huto.hemomancy.event;

import org.lwjgl.glfw.GLFW;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.containers.ContainerMorphlingJar;
import com.huto.hemomancy.containers.ContainerRuneBinder;
import com.huto.hemomancy.gui.mindrunes.GuiChiselStation;
import com.huto.hemomancy.gui.mindrunes.GuiRuneBinder;
import com.huto.hemomancy.gui.mindrunes.PlayerExpandedScreen;
import com.huto.hemomancy.gui.morphlingjar.GuiMorphlingJar;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.render.entity.RenderIronPillar;
import com.huto.hemomancy.render.entity.RenderIronSpike;
import com.huto.hemomancy.render.entity.RenderLeech;
import com.huto.hemomancy.render.entity.projectile.RenderBloodOrbDirected;
import com.huto.hemomancy.render.entity.projectile.RenderBloodOrbTracking;
import com.huto.hemomancy.render.tile.RenderChiselStation;
import com.huto.hemomancy.render.tile.RenderRuneModStation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {
	public static NonNullList<KeyBinding> keyBinds = NonNullList.create();
	public static KeyBinding toggleRuneBinderPickup = new KeyBinding("key.Hemomancy.runebinderpickup.desc",
			GLFW.GLFW_KEY_B, "key.Hemomancy.category");
	public static KeyBinding bloodFormation = new KeyBinding("key.Hemomancy.bloodformation.desc", GLFW.GLFW_KEY_F,
			"key.Hemomancy.category");
	public static KeyBinding bloodCrafting = new KeyBinding("key.Hemomancy.bloodcrafting.desc", GLFW.GLFW_KEY_C,
			"key.Hemomancy.category");
	public static KeyBinding bloodDraw = new KeyBinding("key.Hemomancy.drawtest.desc", GLFW.GLFW_KEY_LEFT_CONTROL,
			"key.Hemomancy.category");
	public static KeyBinding toggleMorphlingJarPickup = new KeyBinding("key.Hemomancy.morphjarpickup.desc", GLFW.GLFW_KEY_LEFT_CONTROL,
			"key.Hemomancy.category");

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {

		// Tiles
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.runic_chisel_station.get(), RenderChiselStation::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.rune_mod_station.get(), RenderRuneModStation::new);
		// Screen
		ScreenManager.registerFactory(ContainerInit.runic_chisel_station.get(), GuiChiselStation::new);
		ScreenManager.registerFactory(ContainerInit.playerrunes, PlayerExpandedScreen::new);
		ScreenManager.registerFactory(ContainerRuneBinder.type, GuiRuneBinder::new);
		ScreenManager.registerFactory(ContainerMorphlingJar.type, GuiMorphlingJar::new);

		// Entity
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.leech.get(), RenderLeech::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_pillar.get(), RenderIronPillar::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_spike.get(), RenderIronSpike::new);

		RenderingRegistry.registerEntityRenderingHandler(EntityInit.directed_blood_orb.get(),
				RenderBloodOrbDirected::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.tracking_blood_orb.get(),
				RenderBloodOrbTracking::new);
		// Keybinds
		keyBinds.add(0, toggleRuneBinderPickup);
		keyBinds.add(1, bloodFormation);
		keyBinds.add(2, bloodCrafting);
		keyBinds.add(3, bloodDraw);
		keyBinds.add(4, toggleMorphlingJarPickup);

		ClientRegistry.registerKeyBinding(keyBinds.get(0));
		ClientRegistry.registerKeyBinding(keyBinds.get(1));
		ClientRegistry.registerKeyBinding(keyBinds.get(2));
		ClientRegistry.registerKeyBinding(keyBinds.get(3));
		ClientRegistry.registerKeyBinding(keyBinds.get(4));

	}

	// for class loading issues
	public static Minecraft getClient() {
		return Minecraft.getInstance();
	}

	public static PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	public static ClientWorld getWorld() {
		return getClient().world;
	}

	public static float getPartialTicks() {
		return getClient().getRenderPartialTicks();
	}

}
