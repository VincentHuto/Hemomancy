package com.vincenthuto.hemomancy.event;

import org.lwjgl.glfw.GLFW;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.gui.ScreenJuiceinator;
import com.vincenthuto.hemomancy.gui.ScreenVisceralRecaller;
import com.vincenthuto.hemomancy.gui.morphlingjar.ScreenLivingStaff;
import com.vincenthuto.hemomancy.gui.morphlingjar.ScreenLivingSyringe;
import com.vincenthuto.hemomancy.gui.morphlingjar.ScreenMorphlingJar;
import com.vincenthuto.hemomancy.gui.overlay.BloodVolumeOverlay;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.render.tile.RenderDendriticDistributor;
import com.vincenthuto.hemomancy.render.tile.RenderEarthenVein;
import com.vincenthuto.hemomancy.render.tile.RenderMorphlingIncubator;
import com.vincenthuto.hemomancy.render.tile.RenderMortalDisplay;
import com.vincenthuto.hemomancy.render.tile.RenderUnstainedPodium;
import com.vincenthuto.hemomancy.render.tile.RenderVisceralRecaller;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {
	public static NonNullList<KeyMapping> keyBinds = NonNullList.create();
	public static final KeyMapping toggleRuneBinderPickup = new KeyMapping("key.hemomancy.runebinderpickup.desc",
			GLFW.GLFW_KEY_B, "key.hemomancy.category");
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

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		// Overlay
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "blood_volume",
				BloodVolumeOverlay.HUD_BLOODVOLUME);

		// Tiles
		BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), RenderMorphlingIncubator::new);
		BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), RenderUnstainedPodium::new);
		BlockEntityRenderers.register(BlockEntityInit.dendritic_distributor.get(), RenderDendriticDistributor::new);
		BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), RenderMortalDisplay::new);
		BlockEntityRenderers.register(BlockEntityInit.visceral_artificial_recaller.get(), RenderVisceralRecaller::new);
		BlockEntityRenderers.register(BlockEntityInit.earthen_vein.get(), RenderEarthenVein::new);

		MenuScreens.register(ContainerInit.visceral_recaller.get(), ScreenVisceralRecaller::new);
		MenuScreens.register(ContainerInit.morphling_jar.get(), ScreenMorphlingJar::new);
		MenuScreens.register(ContainerInit.living_syringe.get(), ScreenLivingSyringe::new);
		MenuScreens.register(ContainerInit.living_staff.get(), ScreenLivingStaff::new);
		MenuScreens.register(ContainerInit.juiceinator.get(), ScreenJuiceinator::new);

		// Keybinds
		keyBinds.add(0, toggleRuneBinderPickup);
		keyBinds.add(1, bloodFormation);
		keyBinds.add(2, bloodCrafting);
		keyBinds.add(3, bloodDraw);
		keyBinds.add(4, toggleMorphlingJarPickup);
		keyBinds.add(5, toggleMorphlingOpenJar);
		keyBinds.add(6, cycleSelectedManip);
		keyBinds.add(7, useQuickManip);
		keyBinds.add(8, useContManip);
		for (KeyMapping bind : keyBinds) {
			ClientRegistry.registerKeyBinding(bind);

		}
	}

}
