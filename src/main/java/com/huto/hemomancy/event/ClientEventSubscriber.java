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
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.render.entity.construct.RenderIronPillar;
import com.huto.hemomancy.render.entity.construct.RenderIronSpike;
import com.huto.hemomancy.render.entity.construct.RenderIronWall;
import com.huto.hemomancy.render.entity.mob.RenderAbhorentThought;
import com.huto.hemomancy.render.entity.mob.RenderChitinite;
import com.huto.hemomancy.render.entity.mob.RenderChthonian;
import com.huto.hemomancy.render.entity.mob.RenderChthonianQueen;
import com.huto.hemomancy.render.entity.mob.RenderDrudge;
import com.huto.hemomancy.render.entity.mob.RenderFargone;
import com.huto.hemomancy.render.entity.mob.RenderFungling;
import com.huto.hemomancy.render.entity.mob.RenderLeech;
import com.huto.hemomancy.render.entity.mob.RenderLumpOfThought;
import com.huto.hemomancy.render.entity.mob.RenderMorphlingPolyp;
import com.huto.hemomancy.render.entity.mob.RenderThirster;
import com.huto.hemomancy.render.entity.projectile.RenderBloodBolt;
import com.huto.hemomancy.render.entity.projectile.RenderBloodCloud;
import com.huto.hemomancy.render.entity.projectile.RenderBloodCloudCarrier;
import com.huto.hemomancy.render.entity.projectile.RenderBloodNeedle;
import com.huto.hemomancy.render.entity.projectile.RenderBloodOrbDirected;
import com.huto.hemomancy.render.entity.projectile.RenderBloodOrbTracking;
import com.huto.hemomancy.render.entity.projectile.RenderBloodShot;
import com.huto.hemomancy.render.entity.projectile.RenderDarkArrow;
import com.huto.hemomancy.render.entity.projectile.RenderTrackingPests;
import com.huto.hemomancy.render.entity.projectile.RenderTrackingSerpent;
import com.huto.hemomancy.render.item.RenderMorphlingPolypItem;
import com.huto.hemomancy.render.layer.RenderBloodLayer;
import com.huto.hemomancy.render.tile.RenderChiselStation;
import com.huto.hemomancy.render.tile.RenderDendriticDistributor;
import com.huto.hemomancy.render.tile.RenderMorphlingIncubator;
import com.huto.hemomancy.render.tile.RenderMortalDisplay;
import com.huto.hemomancy.render.tile.RenderRuneModStation;
import com.huto.hemomancy.render.tile.RenderUnstainedPodium;
import com.huto.hemomancy.render.tile.RenderVisceralRecaller;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {
	public static NonNullList<KeyBinding> keyBinds = NonNullList.create();
	public static final KeyBinding toggleRuneBinderPickup = new KeyBinding("key.hemomancy.runebinderpickup.desc",
			GLFW.GLFW_KEY_B, "key.hemomancy.category");
	public static final KeyBinding bloodFormation = new KeyBinding("key.hemomancy.bloodformation.desc", GLFW.GLFW_KEY_F,
			"key.hemomancy.category");
	public static final KeyBinding bloodCrafting = new KeyBinding("key.hemomancy.bloodcrafting.desc", GLFW.GLFW_KEY_C,
			"key.hemomancy.category");
	public static final KeyBinding bloodDraw = new KeyBinding("key.hemomancy.drawtest.desc", GLFW.GLFW_KEY_LEFT_CONTROL,
			"key.hemomancy.category");
	public static final KeyBinding toggleMorphlingJarPickup = new KeyBinding("key.hemomancy.morphjarpickup.desc",
			GLFW.GLFW_KEY_LEFT_CONTROL, "key.hemomancy.category");
	public static final KeyBinding toggleMorphlingOpenJar = new KeyBinding("key.hemomancy.openjar.desc",
			GLFW.GLFW_KEY_F, "key.hemomancy.category");
	public static final KeyBinding cycleSelectedManip = new KeyBinding("key.hemomancy.cyclemanip.desc", GLFW.GLFW_KEY_C,
			"key.hemomancy.category");
	public static final KeyBinding useQuickManip = new KeyBinding("key.hemomancy.quickusemanip.desc", GLFW.GLFW_KEY_R,
			"key.hemomancy.category");
	public static final KeyBinding useContManip = new KeyBinding("key.hemomancy.contusemanip.desc", GLFW.GLFW_KEY_V,
			"key.hemomancy.category");

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {

		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addListener(RenderBloodLayer::renderWorld);
		forgeBus.addListener(RenderBloodLayer::renderEntities);

		// Tiles
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.runic_chisel_station.get(), RenderChiselStation::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.rune_mod_station.get(), RenderRuneModStation::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.morphling_incubator.get(), RenderMorphlingIncubator::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.unstained_podium.get(), RenderUnstainedPodium::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.dendritic_distributor.get(),
				RenderDendriticDistributor::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.mortal_display.get(), RenderMortalDisplay::new);
		ClientRegistry.bindTileEntityRenderer(TileEntityInit.visceral_artificial_recaller.get(),
				RenderVisceralRecaller::new);

		// Screen
		ScreenManager.registerFactory(ContainerInit.runic_chisel_station.get(), GuiChiselStation::new);
		ScreenManager.registerFactory(ContainerInit.visceral_recaller.get(), GuiVisceralRecaller::new);
		ScreenManager.registerFactory(ContainerInit.playerrunes, PlayerExpandedScreen::new);
		ScreenManager.registerFactory(ContainerRuneBinder.type, GuiRuneBinder::new);
		ScreenManager.registerFactory(ContainerMorphlingJar.type, GuiMorphlingJar::new);
		ScreenManager.registerFactory(ContainerLivingStaff.type, GuiLivingStaff::new);

		// Entity
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.leech.get(), RenderLeech::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_pillar.get(), RenderIronPillar::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_spike.get(), RenderIronSpike::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.iron_wall.get(), RenderIronWall::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.fargone.get(), RenderFargone::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.thirster.get(), RenderThirster::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.drudge.get(), RenderDrudge::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.fungling.get(), RenderFungling::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.chitinite.get(), RenderChitinite::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.chthonian.get(), RenderChthonian::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.lump_of_thought.get(), RenderLumpOfThought::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.chthonian_queen.get(), RenderChthonianQueen::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.abhorent_thought.get(), RenderAbhorentThought::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.morphling_polyp.get(), RenderMorphlingPolyp::new);

		RenderingRegistry.registerEntityRenderingHandler(EntityInit.directed_blood_orb.get(),
				RenderBloodOrbDirected::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.blood_cloud_carrier.get(),
				RenderBloodCloudCarrier::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.blood_cloud.get(), RenderBloodCloud::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.tracking_blood_orb.get(),
				RenderBloodOrbTracking::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.tracking_snake.get(), RenderTrackingSerpent::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.tracking_pests.get(), RenderTrackingPests::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.blood_bolt.get(), RenderBloodBolt::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.blood_needle.get(), RenderBloodNeedle::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.blood_shot.get(), RenderBloodShot::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.dark_arrow.get(), RenderDarkArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityInit.morphling_polyp_item.get(),
				RenderMorphlingPolypItem::new);

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
		for (KeyBinding bind : keyBinds) {
			ClientRegistry.registerKeyBinding(bind);

		}
	}
}
