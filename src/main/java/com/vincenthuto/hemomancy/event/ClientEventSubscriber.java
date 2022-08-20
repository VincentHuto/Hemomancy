package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.gui.CharmGourdScreen;
import com.vincenthuto.hemomancy.gui.JuiceinatorScreen;
import com.vincenthuto.hemomancy.gui.VisceralRecallerScreen;
import com.vincenthuto.hemomancy.gui.morphlingjar.LivingStaffScreen;
import com.vincenthuto.hemomancy.gui.morphlingjar.LivingSyringeScreen;
import com.vincenthuto.hemomancy.gui.morphlingjar.MorphlingJarScreen;
import com.vincenthuto.hemomancy.gui.overlay.BloodVolumeOverlay;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.render.tile.DendriticDistributorRenderer;
import com.vincenthuto.hemomancy.render.tile.EarthenVeinRenderer;
import com.vincenthuto.hemomancy.render.tile.MorphlingIncubatorRenderer;
import com.vincenthuto.hemomancy.render.tile.MortalDisplayRenderer;
import com.vincenthuto.hemomancy.render.tile.UnstainedPodiumRenderer;
import com.vincenthuto.hemomancy.render.tile.VialCentrifugeRenderer;
import com.vincenthuto.hemomancy.render.tile.VisceralRecallerRenderer;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		// Overlay
		OverlayRegistry.registerOverlayAbove(ForgeIngameGui.HOTBAR_ELEMENT, "blood_volume",
				BloodVolumeOverlay.HUD_BLOODVOLUME);

		// Tiles
		BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), MorphlingIncubatorRenderer::new);
		BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), UnstainedPodiumRenderer::new);
		BlockEntityRenderers.register(BlockEntityInit.dendritic_distributor.get(), DendriticDistributorRenderer::new);
		BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), MortalDisplayRenderer::new);
		BlockEntityRenderers.register(BlockEntityInit.vial_centrifuge.get(), VialCentrifugeRenderer::new);
		BlockEntityRenderers.register(BlockEntityInit.visceral_artificial_recaller.get(),
				VisceralRecallerRenderer::new);
		BlockEntityRenderers.register(BlockEntityInit.earthen_vein.get(), EarthenVeinRenderer::new);
		MenuScreens.register(ContainerInit.gourd_charm_inventory.get(), CharmGourdScreen::new);
		MenuScreens.register(ContainerInit.visceral_recaller.get(), VisceralRecallerScreen::new);
		MenuScreens.register(ContainerInit.morphling_jar.get(), MorphlingJarScreen::new);
		MenuScreens.register(ContainerInit.living_syringe.get(), LivingSyringeScreen::new);
		MenuScreens.register(ContainerInit.living_staff.get(), LivingStaffScreen::new);
		MenuScreens.register(ContainerInit.juiceinator.get(), JuiceinatorScreen::new);
	}
}
