package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.model.armor.BarbedArmorModel;
import com.vincenthuto.hemomancy.model.armor.BloodAvatarModel;
import com.vincenthuto.hemomancy.model.armor.BloodGourdModel;
import com.vincenthuto.hemomancy.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.model.armor.BloodLustArmorModel.EnumBloodLustMaskTypes;
import com.vincenthuto.hemomancy.model.armor.ChitiniteArmorModel;
import com.vincenthuto.hemomancy.model.armor.CurvedHornModel;
import com.vincenthuto.hemomancy.model.armor.UnstainedArmorModel;
import com.vincenthuto.hemomancy.model.block.CentrifugeArmsModel;
import com.vincenthuto.hemomancy.model.block.EarthenVeinModel;
import com.vincenthuto.hemomancy.model.block.FloatingHeartModel;
import com.vincenthuto.hemomancy.model.entity.BloodBulletModel;
import com.vincenthuto.hemomancy.model.entity.IronPillarModel;
import com.vincenthuto.hemomancy.model.entity.IronSpikeModel;
import com.vincenthuto.hemomancy.model.entity.IronWallModel;
import com.vincenthuto.hemomancy.model.entity.WretchedWillModel;
import com.vincenthuto.hemomancy.model.entity.mob.AbhorentThoughtModel;
import com.vincenthuto.hemomancy.model.entity.mob.BloodDrunkPuppeteerModel;
import com.vincenthuto.hemomancy.model.entity.mob.ChitiniteModel;
import com.vincenthuto.hemomancy.model.entity.mob.ChthonianModel;
import com.vincenthuto.hemomancy.model.entity.mob.ChthonianQueenModel;
import com.vincenthuto.hemomancy.model.entity.mob.DrudgeModel;
import com.vincenthuto.hemomancy.model.entity.mob.EnthralledDollModel;
import com.vincenthuto.hemomancy.model.entity.mob.FargoneModel;
import com.vincenthuto.hemomancy.model.entity.mob.FunglingModel;
import com.vincenthuto.hemomancy.model.entity.mob.LeechModel;
import com.vincenthuto.hemomancy.model.entity.mob.LumpOfThoughtModel;
import com.vincenthuto.hemomancy.model.entity.mob.MorphlingPolypModel;
import com.vincenthuto.hemomancy.model.entity.mob.ThirsterModel;
import com.vincenthuto.hemomancy.model.item.BarbedShieldModel;
import com.vincenthuto.hemomancy.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.model.item.ChitiniteShieldModel;
import com.vincenthuto.hemomancy.model.item.LivingAxeModel;
import com.vincenthuto.hemomancy.model.item.LivingBladeHandTameModel;
import com.vincenthuto.hemomancy.model.item.LivingBladeUnleashedModel;
import com.vincenthuto.hemomancy.model.item.LivingSpearModel;
import com.vincenthuto.hemomancy.render.entity.blood.BloodBoltRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.BloodBulletRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.BloodCloudCarrierRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.BloodCloudRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.BloodNeedleRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.BloodOrbDirectedRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.BloodOrbTrackingRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.BloodShotRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.WretchedWillRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.iron.IronPillarRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.iron.IronSpikeRenderer;
import com.vincenthuto.hemomancy.render.entity.blood.iron.IronWallRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.AbhorentThoughtRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.BloodDrunkPuppeteerRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.ChitiniteRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.ChthonianQueenRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.ChthonianRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.EnthralledDollRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.FargoneRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.FunglingRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.LeechRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.LumpOfThoughtRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.MorphlingPolypRenderer;
import com.vincenthuto.hemomancy.render.entity.mob.ThirsterRenderer;
import com.vincenthuto.hemomancy.render.entity.projectile.TrackingPestsRenderer;
import com.vincenthuto.hemomancy.render.entity.projectile.TrackingSerpentRenderer;
import com.vincenthuto.hemomancy.render.item.MorphlingPolypItemRenderer;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class EntityEvents {

	@SubscribeEvent
	public static void registerModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(AbhorentThoughtModel.abhorent_thought, AbhorentThoughtModel::createBodyLayer);
		event.registerLayerDefinition(LivingBladeHandTameModel.living_blade_tame,
				LivingBladeHandTameModel::createLayers);
		event.registerLayerDefinition(LivingBladeUnleashedModel.living_blade_unleashed,
				LivingBladeUnleashedModel::createLayers);
		event.registerLayerDefinition(LivingAxeModel.living_axe, LivingAxeModel::createLayers);
		event.registerLayerDefinition(LivingSpearModel.living_spear, LivingSpearModel::createLayers);
		event.registerLayerDefinition(FloatingHeartModel.mortal_display, FloatingHeartModel::createBodyLayer);
		event.registerLayerDefinition(CentrifugeArmsModel.LAYER_LOCATION, CentrifugeArmsModel::createBodyLayer);

		event.registerLayerDefinition(BarbedShieldModel.barbed_shield, BarbedShieldModel::createLayers);
		event.registerLayerDefinition(ChitiniteShieldModel.chitinite_shield, ChitiniteShieldModel::createBodyLayer);
		event.registerLayerDefinition(IronWallModel.iron_wall, IronWallModel::createBodyLayer);
		event.registerLayerDefinition(IronSpikeModel.iron_spike, IronSpikeModel::createBodyLayer);
		event.registerLayerDefinition(IronPillarModel.iron_pillar, IronPillarModel::createBodyLayer);
		event.registerLayerDefinition(BloodBulletModel.blood_bullet, BloodBulletModel::createBodyLayer);
		event.registerLayerDefinition(EarthenVeinModel.earth_vein, EarthenVeinModel::createBodyLayer);
		event.registerLayerDefinition(WretchedWillModel.wretched_will, WretchedWillModel::createBodyLayer);
		event.registerLayerDefinition(BloodGourdModel.blood_gourd, BloodGourdModel::createBodyLayer);
		event.registerLayerDefinition(CurvedHornModel.curved_horn, CurvedHornModel::createBodyLayer);
		event.registerLayerDefinition(ChthonianModel.LAYER_LOCATION, ChthonianModel::createBodyLayer);
		event.registerLayerDefinition(ChthonianQueenModel.LAYER_LOCATION, ChthonianQueenModel::createBodyLayer);
		event.registerLayerDefinition(ChitiniteModel.LAYER_LOCATION, ChitiniteModel::createBodyLayer);
		event.registerLayerDefinition(DrudgeModel.LAYER_LOCATION, DrudgeModel::createBodyLayer);
		event.registerLayerDefinition(FargoneModel.LAYER_LOCATION, FargoneModel::createBodyLayer);
		event.registerLayerDefinition(FunglingModel.LAYER_LOCATION, FunglingModel::createBodyLayer);
		event.registerLayerDefinition(LeechModel.LAYER_LOCATION, LeechModel::createBodyLayer);
		event.registerLayerDefinition(LumpOfThoughtModel.LAYER_LOCATION, LumpOfThoughtModel::createBodyLayer);
		event.registerLayerDefinition(BloodDrunkPuppeteerModel.LAYER_LOCATION,
				BloodDrunkPuppeteerModel::createbodyLayer);
		event.registerLayerDefinition(EnthralledDollModel.LAYER_LOCATION, EnthralledDollModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingPolypModel.LAYER_LOCATION, MorphlingPolypModel::createBodyLayer);
		event.registerLayerDefinition(ThirsterModel.LAYER_LOCATION, ThirsterModel::createBodyLayer);
		event.registerLayerDefinition(BloodArmModel.blood_arm, BloodArmModel::createHeadLayer);

		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_HEAD_LAYER,
				() -> BloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.NONE));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_HEAD_TENGU_LAYER,
				() -> BloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.TENGU));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_HEAD_HORNED_LAYER,
				() -> BloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.HORNED));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_CHEST_LAYER,
				() -> BloodLustArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_LEGS_LAYER,
				() -> BloodLustArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_BOOTS_LAYER,
				() -> BloodLustArmorModel.createBodyLayer(EquipmentSlot.FEET));
		event.registerLayerDefinition(BloodAvatarModel.layer, BloodAvatarModel::createLayer);

		event.registerLayerDefinition(ChitiniteArmorModel.CHITINITE_HELMET_LAYER,
				() -> ChitiniteArmorModel.createHeadLayer(EquipmentSlot.HEAD));
		event.registerLayerDefinition(ChitiniteArmorModel.CHITINITE_CHEST_LAYER,
				() -> ChitiniteArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(ChitiniteArmorModel.CHITINITE_LEGS_LAYER,
				() -> ChitiniteArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(ChitiniteArmorModel.CHITINITE_FEET_LAYER,
				() -> ChitiniteArmorModel.createBodyLayer(EquipmentSlot.FEET));

		event.registerLayerDefinition(BarbedArmorModel.BARBED_HELMET_LAYER,
				() -> BarbedArmorModel.createHeadLayer(EquipmentSlot.HEAD));
		event.registerLayerDefinition(BarbedArmorModel.BARBED_CHEST_LAYER,
				() -> BarbedArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(BarbedArmorModel.BARBED_LEGS_LAYER,
				() -> BarbedArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(BarbedArmorModel.BARBED_FEET_LAYER,
				() -> BarbedArmorModel.createBodyLayer(EquipmentSlot.FEET));

		event.registerLayerDefinition(UnstainedArmorModel.UNSTAINED_HELMET_LAYER,
				() -> UnstainedArmorModel.createHeadLayer(EquipmentSlot.HEAD));
		event.registerLayerDefinition(UnstainedArmorModel.UNSTAINED_CHEST_LAYER,
				() -> UnstainedArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(UnstainedArmorModel.UNSTAINED_LEGS_LAYER,
				() -> UnstainedArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(UnstainedArmorModel.UNSTAINED_FEET_LAYER,
				() -> UnstainedArmorModel.createBodyLayer(EquipmentSlot.FEET));

	}

	@SubscribeEvent
	public static void renderEntities(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EntityInit.directed_blood_orb.get(), BloodOrbDirectedRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_cloud_carrier.get(), BloodCloudCarrierRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_cloud.get(), BloodCloudRenderer::new);
		event.registerEntityRenderer(EntityInit.tracking_blood_orb.get(), BloodOrbTrackingRenderer::new);
		event.registerEntityRenderer(EntityInit.tracking_snake.get(), TrackingSerpentRenderer::new);
		event.registerEntityRenderer(EntityInit.tracking_pests.get(), TrackingPestsRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_bolt.get(), BloodBoltRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_needle.get(), BloodNeedleRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_shot.get(), BloodShotRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_bullet.get(), BloodBulletRenderer::new);
		event.registerEntityRenderer(EntityInit.morphling_polyp_item.get(), MorphlingPolypItemRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
		event.registerEntityRenderer(EntityInit.wretched_will.get(), WretchedWillRenderer::new);
		event.registerEntityRenderer(EntityInit.leech.get(), LeechRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_pillar.get(), IronPillarRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_spike.get(), IronSpikeRenderer::new);
		event.registerEntityRenderer(EntityInit.iron_wall.get(), IronWallRenderer::new);
		event.registerEntityRenderer(EntityInit.fargone.get(), FargoneRenderer::new);
		event.registerEntityRenderer(EntityInit.thirster.get(), ThirsterRenderer::new);
		event.registerEntityRenderer(EntityInit.fungling.get(), FunglingRenderer::new);
		event.registerEntityRenderer(EntityInit.chitinite.get(), ChitiniteRenderer::new);
		event.registerEntityRenderer(EntityInit.chthonian.get(), ChthonianRenderer::new);
		event.registerEntityRenderer(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerRenderer::new);
		event.registerEntityRenderer(EntityInit.enthralled_doll.get(), EnthralledDollRenderer::new);
		event.registerEntityRenderer(EntityInit.lump_of_thought.get(), LumpOfThoughtRenderer::new);
		event.registerEntityRenderer(EntityInit.chthonian_queen.get(), ChthonianQueenRenderer::new);
		event.registerEntityRenderer(EntityInit.abhorent_thought.get(), AbhorentThoughtRenderer::new);
		event.registerEntityRenderer(EntityInit.morphling_polyp.get(), MorphlingPolypRenderer::new);
		event.registerEntityRenderer(EntityInit.flying_charm.get(), ThrownItemRenderer::new);

	}

}
