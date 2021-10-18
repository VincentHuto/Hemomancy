package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.model.block.ModelEarthenVein;
import com.vincenthuto.hemomancy.model.block.ModelFloatingHeart;
import com.vincenthuto.hemomancy.model.entity.ModelBloodBullet;
import com.vincenthuto.hemomancy.model.entity.ModelIronPillar;
import com.vincenthuto.hemomancy.model.entity.ModelIronSpike;
import com.vincenthuto.hemomancy.model.entity.ModelIronWall;
import com.vincenthuto.hemomancy.model.entity.ModelWretchedWill;
import com.vincenthuto.hemomancy.model.item.ModelArmBanner;
import com.vincenthuto.hemomancy.model.item.ModelBloodArm;
import com.vincenthuto.hemomancy.model.item.ModelLivingAxe;
import com.vincenthuto.hemomancy.model.item.ModelLivingBladeHandTame;
import com.vincenthuto.hemomancy.model.item.ModelLivingBladeUnleashed;
import com.vincenthuto.hemomancy.model.item.ModelLivingSpear;
import com.vincenthuto.hemomancy.model.item.ModelSpikedShield;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelLayersInit {

	public static final ModelLayerLocation living_blade_tame = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "living_blade_tame"), "main");
	public static final ModelLayerLocation living_blade_unleashed = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "living_blade_unleashed"), "main");
	public static final ModelLayerLocation living_axe = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "living_axe"), "main");
	public static final ModelLayerLocation living_spear = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "living_spear"), "main");
	public static final ModelLayerLocation blood_arm = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_arm"), "main");
	public static final ModelLayerLocation mortal_display = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "mortal_display"), "main");
	public static final ModelLayerLocation spiked_shield = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "spiked_shield"), "main");
	public static final ModelLayerLocation arm_banner = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "arm_banner"), "main");

	// Entity
	public static final ModelLayerLocation abhorent_thought = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "abhorent_thought"), "main");

	public static final ModelLayerLocation iron_pillar = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "iron_pillar"), "main");
	public static final ModelLayerLocation iron_wall = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "iron_wall"), "main");
	public static final ModelLayerLocation iron_spike = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "iron_spike"), "main");
	public static final ModelLayerLocation blood_bullet = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_bullet"), "main");
	public static final ModelLayerLocation earth_vein = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "modelearthenvein"), "main");

	public static final ModelLayerLocation wretched_will = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "modelwretchedwill"), "main");

	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(living_blade_tame, ModelLivingBladeHandTame::createLayers);
		event.registerLayerDefinition(living_blade_unleashed, ModelLivingBladeUnleashed::createLayers);
		event.registerLayerDefinition(living_axe, ModelLivingAxe::createLayers);
		event.registerLayerDefinition(living_spear, ModelLivingSpear::createLayers);
		event.registerLayerDefinition(blood_arm, ModelBloodArm::createLayers);
		event.registerLayerDefinition(mortal_display, ModelFloatingHeart::createBodyLayer);
		event.registerLayerDefinition(spiked_shield, ModelSpikedShield::createLayers);
		event.registerLayerDefinition(arm_banner, ModelArmBanner::createLayers);
		event.registerLayerDefinition(iron_wall, ModelIronWall::createBodyLayer);
		event.registerLayerDefinition(iron_spike, ModelIronSpike::createBodyLayer);
		event.registerLayerDefinition(iron_pillar, ModelIronPillar::createBodyLayer);
		event.registerLayerDefinition(blood_bullet, ModelBloodBullet::createBodyLayer);
		event.registerLayerDefinition(earth_vein, ModelEarthenVein::createBodyLayer);
		event.registerLayerDefinition(wretched_will, ModelWretchedWill::createBodyLayer);

	}

}
