package com.vincenthuto.hemomancy.event;

import org.lwjgl.glfw.GLFW;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.gui.mindrunes.GuiChiselStation;
import com.vincenthuto.hemomancy.gui.mindrunes.GuiRuneBinder;
import com.vincenthuto.hemomancy.gui.mindrunes.PlayerExpandedScreen;
import com.vincenthuto.hemomancy.gui.morphlingjar.GuiLivingStaff;
import com.vincenthuto.hemomancy.gui.morphlingjar.GuiLivingSyringe;
import com.vincenthuto.hemomancy.gui.morphlingjar.GuiMorphlingJar;
import com.vincenthuto.hemomancy.gui.recaller.GuiVisceralRecaller;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodBolt;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodBullet;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodCloud;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodCloudCarrier;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodNeedle;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodOrbDirected;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodOrbTracking;
import com.vincenthuto.hemomancy.render.entity.blood.RenderBloodShot;
import com.vincenthuto.hemomancy.render.entity.blood.RenderWretchedWill;
import com.vincenthuto.hemomancy.render.entity.blood.iron.RenderIronPillar;
import com.vincenthuto.hemomancy.render.entity.blood.iron.RenderIronSpike;
import com.vincenthuto.hemomancy.render.entity.blood.iron.RenderIronWall;
import com.vincenthuto.hemomancy.render.entity.projectile.RenderTrackingPests;
import com.vincenthuto.hemomancy.render.entity.projectile.RenderTrackingSerpent;
import com.vincenthuto.hemomancy.render.item.RenderMorphlingPolypItem;
import com.vincenthuto.hemomancy.render.tile.RenderChiselStation;
import com.vincenthuto.hemomancy.render.tile.RenderDendriticDistributor;
import com.vincenthuto.hemomancy.render.tile.RenderEarthenVein;
import com.vincenthuto.hemomancy.render.tile.RenderMorphlingIncubator;
import com.vincenthuto.hemomancy.render.tile.RenderMortalDisplay;
import com.vincenthuto.hemomancy.render.tile.RenderRuneModStation;
import com.vincenthuto.hemomancy.render.tile.RenderUnstainedPodium;
import com.vincenthuto.hemomancy.render.tile.RenderVisceralRecaller;

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
		event.registerEntityRenderer(EntityInit.blood_bullet.get(), RenderBloodBullet::new);

		// event.registerEntityRenderer(EntityInit.dark_arrow.get(),
		// RenderDarkArrow::new);
		event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), RenderMorphlingPolypItem::new);

		event.registerEntityRenderer(EntityInit.iron_pillar.get(), RenderIronPillar::new);
		event.registerEntityRenderer(EntityInit.iron_spike.get(), RenderIronSpike::new);
		event.registerEntityRenderer(EntityInit.iron_wall.get(), RenderIronWall::new);
		
		event.registerEntityRenderer(EntityInit.wretched_will.get(), RenderWretchedWill::new);

	}
	
	

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {

		// Tiles
		BlockEntityRenderers.register(BlockEntityInit.runic_chisel_station.get(), RenderChiselStation::new);
		BlockEntityRenderers.register(BlockEntityInit.rune_mod_station.get(), RenderRuneModStation::new);
		BlockEntityRenderers.register(BlockEntityInit.morphling_incubator.get(), RenderMorphlingIncubator::new);
		BlockEntityRenderers.register(BlockEntityInit.unstained_podium.get(), RenderUnstainedPodium::new);
		BlockEntityRenderers.register(BlockEntityInit.dendritic_distributor.get(), RenderDendriticDistributor::new);
		BlockEntityRenderers.register(BlockEntityInit.mortal_display.get(), RenderMortalDisplay::new);
		BlockEntityRenderers.register(BlockEntityInit.visceral_artificial_recaller.get(), RenderVisceralRecaller::new);
		BlockEntityRenderers.register(BlockEntityInit.earthen_vein.get(), RenderEarthenVein::new);

		// Screen
		MenuScreens.register(ContainerInit.runic_chisel_station.get(), GuiChiselStation::new);
		MenuScreens.register(ContainerInit.visceral_recaller.get(), GuiVisceralRecaller::new);
		MenuScreens.register(ContainerInit.rune_binder.get(), GuiRuneBinder::new);
		MenuScreens.register(ContainerInit.morphling_jar.get(), GuiMorphlingJar::new);
		MenuScreens.register(ContainerInit.living_syringe.get(), GuiLivingSyringe::new);
		MenuScreens.register(ContainerInit.living_staff.get(), GuiLivingStaff::new);
		MenuScreens.register(ContainerInit.playerrunes, PlayerExpandedScreen::new);

		// Entity
//		event.registerEntityRenderer(EntityInit.leech.get(), RenderLeech::new);
//		event.registerEntityRenderer(EntityInit.iron_pillar.get(), RenderIronPillar::new);
//		event.registerEntityRenderer(EntityInit.iron_spike.get(), RenderIronSpike::new);
//		event.registerEntityRenderer(EntityInit.iron_wall.get(), RenderIronWall::new);
//		event.registerEntityRenderer(EntityInit.fargone.get(), RenderFargone::new);
//		event.registerEntityRenderer(EntityInit.thirster.get(), RenderThirster::new);
//		event.registerEntityRenderer(EntityInit.drudge.get(), RenderDrudge::new);
//		event.registerEntityRenderer(EntityInit.fungling.get(), RenderFungling::new);
//		event.registerEntityRenderer(EntityInit.chitinite.get(), RenderChitinite::new);
//		event.registerEntityRenderer(EntityInit.chthonian.get(), RenderChthonian::new);
//		event.registerEntityRenderer(EntityInit.lump_of_thought.get(), RenderLumpOfThought::new);
//		event.registerEntityRenderer(EntityInit.chthonian_queen.get(), RenderChthonianQueen::new);
//		event.registerEntityRenderer(EntityInit.abhorent_thought.get(), RenderAbhorentThought::new);
//		event.registerEntityRenderer(EntityInit.morphling_polyp.get(), RenderMorphlingPolyp::new);

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
