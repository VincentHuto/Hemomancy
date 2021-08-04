package com.huto.hemomancy.event;

import org.lwjgl.glfw.GLFW;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerLivingStaff;
import com.huto.hemomancy.container.ContainerMorphlingJar;
import com.huto.hemomancy.container.ContainerRuneBinder;
import com.huto.hemomancy.gui.mindrunes.GuiChiselStation;
import com.huto.hemomancy.gui.mindrunes.GuiRuneBinder;
import com.huto.hemomancy.gui.mindrunes.PlayerExpandedScreen;
import com.huto.hemomancy.gui.morphlingjar.GuiLivingStaff;
import com.huto.hemomancy.gui.morphlingjar.GuiMorphlingJar;
import com.huto.hemomancy.gui.recaller.GuiVisceralRecaller;
import com.huto.hemomancy.init.BlockEntityInit;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.render.entity.projectile.RenderBloodBolt;
import com.huto.hemomancy.render.entity.projectile.RenderBloodCloud;
import com.huto.hemomancy.render.entity.projectile.RenderBloodCloudCarrier;
import com.huto.hemomancy.render.entity.projectile.RenderBloodNeedle;
import com.huto.hemomancy.render.entity.projectile.RenderBloodOrbDirected;
import com.huto.hemomancy.render.entity.projectile.RenderBloodOrbTracking;
import com.huto.hemomancy.render.entity.projectile.RenderBloodShot;
import com.huto.hemomancy.render.entity.projectile.RenderTrackingPests;
import com.huto.hemomancy.render.entity.projectile.RenderTrackingSerpent;
import com.huto.hemomancy.render.item.RenderMorphlingPolypItem;
import com.huto.hemomancy.render.tile.RenderChiselStation;
import com.huto.hemomancy.render.tile.RenderDendriticDistributor;
import com.huto.hemomancy.render.tile.RenderMorphlingIncubator;
import com.huto.hemomancy.render.tile.RenderMortalDisplay;
import com.huto.hemomancy.render.tile.RenderRuneModStation;
import com.huto.hemomancy.render.tile.RenderUnstainedPodium;
import com.huto.hemomancy.render.tile.RenderVisceralRecaller;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

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
	public static void renderEntities(EntityRenderersEvent.RegisterRenderers event) {

		event.registerEntityRenderer(EntityInit.directed_blood_orb.get(), RenderBloodOrbDirected::new);
		event.registerEntityRenderer(EntityInit.blood_cloud_carrier.get(), RenderBloodCloudCarrier::new);
		event.registerEntityRenderer(EntityInit.blood_cloud.get(), RenderBloodCloud::new);
		event.registerEntityRenderer(EntityInit.tracking_blood_orb.get(), RenderBloodOrbTracking::new);
		event.registerEntityRenderer(EntityInit.tracking_snake.get(), RenderTrackingSerpent::new);
		event.registerEntityRenderer(EntityInit.tracking_pests.get(), RenderTrackingPests::new);
		event.registerEntityRenderer(EntityInit.blood_bolt.get(), RenderBloodBolt::new);
		event.registerEntityRenderer(EntityInit.blood_needle.get(), RenderBloodNeedle::new);
		event.registerEntityRenderer(EntityInit.blood_shot.get(), RenderBloodShot::new);
		// RenderingRegistry.registerEntityRenderingHandler(EntityInit.dark_arrow.get(),
		// RenderDarkArrow::new);
		event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), RenderMorphlingPolypItem::new);
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {

//		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
//		forgeBus.addListener(RenderBloodLayer::renderLevel);
//		forgeBus.addListener(RenderBloodLayer::renderEntities);

		// Tiles
		BlockEntityRenderers.register(BlockEntityInit.runic_chisel_station.get(), RenderChiselStation::new);
		BlockEntityRenderers.register(BlockEntityInit.rune_mod_station.get(), RenderRuneModStation::new);
		BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), RenderMorphlingIncubator::new);
		BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), RenderUnstainedPodium::new);
		BlockEntityRenderers.register(BlockEntityInit.dendritic_distributor.get(), RenderDendriticDistributor::new);
		BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), RenderMortalDisplay::new);
		BlockEntityRenderers.register(BlockEntityInit.visceral_artificial_recaller.get(), RenderVisceralRecaller::new);

		// Screen
		MenuScreens.register(ContainerInit.runic_chisel_station.get(), GuiChiselStation::new);
		MenuScreens.register(ContainerInit.visceral_recaller.get(), GuiVisceralRecaller::new);
		MenuScreens.register(ContainerInit.playerrunes, PlayerExpandedScreen::new);
		MenuScreens.register(ContainerRuneBinder.type, GuiRuneBinder::new);
		MenuScreens.register(ContainerMorphlingJar.type, GuiMorphlingJar::new);
		MenuScreens.register(ContainerLivingStaff.type, GuiLivingStaff::new);

		// Entity
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.leech.get(), RenderLeech::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_pillar.get(), RenderIronPillar::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_spike.get(), RenderIronSpike::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_wall.get(), RenderIronWall::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.fargone.get(), RenderFargone::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.thirster.get(), RenderThirster::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.drudge.get(), RenderDrudge::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.fungling.get(), RenderFungling::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.chitinite.get(), RenderChitinite::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.chthonian.get(), RenderChthonian::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.lump_of_thought.get(), RenderLumpOfThought::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.chthonian_queen.get(), RenderChthonianQueen::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.abhorent_thought.get(), RenderAbhorentThought::new);
//		RenderingRegistry.registerEntityRenderingHandler(EntityInit.morphling_polyp.get(), RenderMorphlingPolyp::new);

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
