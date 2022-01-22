package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.model.armor.ModelBloodGourd;
import com.vincenthuto.hemomancy.model.armor.ModelCurvedHorn;
import com.vincenthuto.hemomancy.model.block.ModelEarthenVein;
import com.vincenthuto.hemomancy.model.block.ModelFloatingHeart;
import com.vincenthuto.hemomancy.model.entity.ModelBloodBullet;
import com.vincenthuto.hemomancy.model.entity.ModelIronPillar;
import com.vincenthuto.hemomancy.model.entity.ModelIronSpike;
import com.vincenthuto.hemomancy.model.entity.ModelIronWall;
import com.vincenthuto.hemomancy.model.entity.ModelWretchedWill;
import com.vincenthuto.hemomancy.model.entity.mob.ModelAbhorentThought;
import com.vincenthuto.hemomancy.model.entity.mob.ModelChitinite;
import com.vincenthuto.hemomancy.model.entity.mob.ModelChthonian;
import com.vincenthuto.hemomancy.model.entity.mob.ModelChthonianQueen;
import com.vincenthuto.hemomancy.model.entity.mob.ModelDrudge;
import com.vincenthuto.hemomancy.model.entity.mob.ModelFargone;
import com.vincenthuto.hemomancy.model.entity.mob.ModelFungling;
import com.vincenthuto.hemomancy.model.entity.mob.ModelLeech;
import com.vincenthuto.hemomancy.model.entity.mob.ModelLumpOfThought;
import com.vincenthuto.hemomancy.model.entity.mob.ModelMorphlingPolyp;
import com.vincenthuto.hemomancy.model.entity.mob.ModelThirster;
import com.vincenthuto.hemomancy.model.item.ModelBloodArm;
import com.vincenthuto.hemomancy.model.item.ModelLivingAxe;
import com.vincenthuto.hemomancy.model.item.ModelLivingBladeHandTame;
import com.vincenthuto.hemomancy.model.item.ModelLivingBladeUnleashed;
import com.vincenthuto.hemomancy.model.item.ModelLivingSpear;
import com.vincenthuto.hemomancy.model.item.ModelSpikedShield;
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
import com.vincenthuto.hemomancy.render.entity.mob.RenderAbhorentThought;
import com.vincenthuto.hemomancy.render.entity.mob.RenderChitinite;
import com.vincenthuto.hemomancy.render.entity.mob.RenderChthonian;
import com.vincenthuto.hemomancy.render.entity.mob.RenderChthonianQueen;
import com.vincenthuto.hemomancy.render.entity.mob.RenderDrudge;
import com.vincenthuto.hemomancy.render.entity.mob.RenderFargone;
import com.vincenthuto.hemomancy.render.entity.mob.RenderFungling;
import com.vincenthuto.hemomancy.render.entity.mob.RenderLeech;
import com.vincenthuto.hemomancy.render.entity.mob.RenderLumpOfThought;
import com.vincenthuto.hemomancy.render.entity.mob.RenderMorphlingPolyp;
import com.vincenthuto.hemomancy.render.entity.mob.RenderThirster;
import com.vincenthuto.hemomancy.render.entity.projectile.RenderTrackingPests;
import com.vincenthuto.hemomancy.render.entity.projectile.RenderTrackingSerpent;
import com.vincenthuto.hemomancy.render.item.RenderItemMorphlingPolyp;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class EntityEventSubscriber {

	
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
		event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), RenderItemMorphlingPolyp::new);
		event.registerEntityRenderer(EntityInit.iron_pillar.get(), RenderIronPillar::new);
		event.registerEntityRenderer(EntityInit.iron_spike.get(), RenderIronSpike::new);
		event.registerEntityRenderer(EntityInit.iron_wall.get(), RenderIronWall::new);
		event.registerEntityRenderer(EntityInit.wretched_will.get(), RenderWretchedWill::new);
		event.registerEntityRenderer(EntityInit.leech.get(), RenderLeech::new);
		event.registerEntityRenderer(EntityInit.iron_pillar.get(), RenderIronPillar::new);
		event.registerEntityRenderer(EntityInit.iron_spike.get(), RenderIronSpike::new);
		event.registerEntityRenderer(EntityInit.iron_wall.get(), RenderIronWall::new);
		event.registerEntityRenderer(EntityInit.fargone.get(), RenderFargone::new);
		event.registerEntityRenderer(EntityInit.thirster.get(), RenderThirster::new);
		event.registerEntityRenderer(EntityInit.drudge.get(), RenderDrudge::new);
		event.registerEntityRenderer(EntityInit.fungling.get(), RenderFungling::new);
		event.registerEntityRenderer(EntityInit.chitinite.get(), RenderChitinite::new);
		event.registerEntityRenderer(EntityInit.chthonian.get(), RenderChthonian::new);
		event.registerEntityRenderer(EntityInit.lump_of_thought.get(), RenderLumpOfThought::new);
		event.registerEntityRenderer(EntityInit.chthonian_queen.get(), RenderChthonianQueen::new);
		event.registerEntityRenderer(EntityInit.abhorent_thought.get(), RenderAbhorentThought::new);
		event.registerEntityRenderer(EntityInit.morphling_polyp.get(), RenderMorphlingPolyp::new);
	}
	

	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ModelAbhorentThought.abhorent_thought, ModelAbhorentThought::createLayers);
		event.registerLayerDefinition(ModelLivingBladeHandTame.living_blade_tame, ModelLivingBladeHandTame::createLayers);
		event.registerLayerDefinition(ModelLivingBladeUnleashed.living_blade_unleashed, ModelLivingBladeUnleashed::createLayers);
		event.registerLayerDefinition(ModelLivingAxe.living_axe, ModelLivingAxe::createLayers);
		event.registerLayerDefinition(ModelLivingSpear.living_spear, ModelLivingSpear::createLayers);
		event.registerLayerDefinition(ModelBloodArm.blood_arm, ModelBloodArm::createLayers);
		event.registerLayerDefinition(ModelFloatingHeart.mortal_display, ModelFloatingHeart::createBodyLayer);
		event.registerLayerDefinition(ModelSpikedShield.spiked_shield, ModelSpikedShield::createLayers);
		event.registerLayerDefinition(ModelIronWall.iron_wall, ModelIronWall::createBodyLayer);
		event.registerLayerDefinition(ModelIronSpike.iron_spike, ModelIronSpike::createBodyLayer);
		event.registerLayerDefinition(ModelIronPillar.iron_pillar, ModelIronPillar::createBodyLayer);
		event.registerLayerDefinition(ModelBloodBullet.blood_bullet, ModelBloodBullet::createBodyLayer);
		event.registerLayerDefinition(ModelEarthenVein.earth_vein, ModelEarthenVein::createBodyLayer);
		event.registerLayerDefinition(ModelWretchedWill.wretched_will, ModelWretchedWill::createBodyLayer);
		event.registerLayerDefinition(ModelBloodGourd.blood_gourd, ModelBloodGourd::createBodyLayer);
		event.registerLayerDefinition(ModelCurvedHorn.curved_horn, ModelCurvedHorn::createBodyLayer);
		event.registerLayerDefinition(ModelChthonian.LAYER_LOCATION, ModelChthonian::createBodyLayer);
		event.registerLayerDefinition(ModelChthonianQueen.LAYER_LOCATION, ModelChthonianQueen::createBodyLayer);
		event.registerLayerDefinition(ModelChitinite.LAYER_LOCATION, ModelChitinite::createBodyLayer);
		event.registerLayerDefinition(ModelDrudge.LAYER_LOCATION, ModelDrudge::createBodyLayer);
		event.registerLayerDefinition(ModelFargone.LAYER_LOCATION, ModelFargone::createBodyLayer);
		event.registerLayerDefinition(ModelFungling.LAYER_LOCATION, ModelFungling::createBodyLayer);
		event.registerLayerDefinition(ModelLeech.LAYER_LOCATION, ModelLeech::createBodyLayer);
		event.registerLayerDefinition(ModelLumpOfThought.LAYER_LOCATION, ModelLumpOfThought::createBodyLayer);
		event.registerLayerDefinition(ModelMorphlingPolyp.LAYER_LOCATION, ModelMorphlingPolyp::createBodyLayer);
		event.registerLayerDefinition(ModelThirster.LAYER_LOCATION, ModelThirster::createBodyLayer);

	}
	
}
