package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.*;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel.EnumBloodLustMaskTypes;
import com.vincenthuto.hemomancy.client.model.entity.summon.BloodBulletModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.IronPillarModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.IronSpikeModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.IronWallModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.WretchedWillModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.AbhorentThoughtModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.BarbedUrchinBigModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.BarbedUrchinMidModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.BarbedUrchinModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.BloodDrunkPuppeteerModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.BloodThrallModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.ChitiniteModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.ChthonianModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.ChthonianQueenModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.DrudgeModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.EnthralledDollModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.ErythromyceliumEruptusModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.FargoneModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.FerventChitiniteModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.FunglingModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.ToothPecksModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemolymphopodaModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.LeechModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.LumpOfThoughtModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingAttachmentExampleModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingPolypModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.ThirsterModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerAlchemistModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerHermitModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerVicarModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.UnstainedAcolyteModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.UnstainedGuardianModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.UnstainedZealotModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.HollowVesselModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.ContainmentAnchorModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeFragmentModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.DessicantModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.CruorFiendModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.VoidDrinkerModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.FrozenClotModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.AbyssalSiphonModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.SynapseHoundModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.MyelinBorerModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.CrimsonDoeModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemojellyModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.VenousStriderModel;
import com.vincenthuto.hemomancy.client.model.item.BarbedShieldModel;
import com.vincenthuto.hemomancy.client.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.client.model.item.ChitiniteShieldModel;
import com.vincenthuto.hemomancy.client.model.item.LivingAxeModel;
import com.vincenthuto.hemomancy.client.model.item.LivingBladeHandTameModel;
import com.vincenthuto.hemomancy.client.model.item.LivingBladeUnleashedModel;
import com.vincenthuto.hemomancy.client.model.item.LivingSpearModel;
import com.vincenthuto.hemomancy.client.model.item.SanguisLanceaModel;
import com.vincenthuto.hemomancy.client.model.tile.*;
import com.vincenthuto.hemomancy.client.model.tile.crafting.*;
import com.vincenthuto.hemomancy.client.model.tile.functional.*;
import com.vincenthuto.hemomancy.client.render.layer.player.*;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class LayerEvents {

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
		event.registerLayerDefinition(CentrifugeStandModel.LAYER_LOCATION, CentrifugeStandModel::createBodyLayer);
		event.registerLayerDefinition(VisceralMirrorModel.LAYER_LOCATION, VisceralMirrorModel::createBodyLayer);
		event.registerLayerDefinition(SanguisLanceaModel.LAYER_LOCATION, SanguisLanceaModel::createBodyLayer);
		event.registerLayerDefinition(SuspendedVivianiteModel.LAYER_LOCATION, SuspendedVivianiteModel::createBodyLayer);
		event.registerLayerDefinition(SuspendedBloodCrystalModel.LAYER_LOCATION, SuspendedBloodCrystalModel::createBodyLayer);
		event.registerLayerDefinition(SuspendedCleansedBloodCrystalModel.LAYER_LOCATION, SuspendedCleansedBloodCrystalModel::createBodyLayer);
		event.registerLayerDefinition(MnemonicReliquaryModel.LAYER_LOCATION, MnemonicReliquaryModel::createBodyLayer);
		event.registerLayerDefinition(ScarStationModel.LAYER_LOCATION, ScarStationModel::createBodyLayer);
		event.registerLayerDefinition(SomaticLoomModel.LAYER_LOCATION, SomaticLoomModel::createBodyLayer);
		event.registerLayerDefinition(GhastlyAlembicModel.LAYER_LOCATION, GhastlyAlembicModel::createBodyLayer);
		event.registerLayerDefinition(PallidRetortModel.LAYER_LOCATION, PallidRetortModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingIncubatorModel.LAYER_LOCATION, MorphlingIncubatorModel::createBodyLayer);
		event.registerLayerDefinition(DictationTableModel.LAYER_LOCATION, DictationTableModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingCradleModel.LAYER_LOCATION, MorphlingCradleModel::createBodyLayer);
		event.registerLayerDefinition(CleansingAltarModel.LAYER_LOCATION, CleansingAltarModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerSaintSarcophagusModel.LAYER_LOCATION, HarbingerSaintSarcophagusModel::createBodyLayer);
		event.registerLayerDefinition(SanguineMonolithModel.LAYER_LOCATION, SanguineMonolithModel::createBodyLayer);
		event.registerLayerDefinition(BarbedShieldModel.barbed_shield, BarbedShieldModel::createLayers);
		event.registerLayerDefinition(ChitiniteShieldModel.chitinite_shield, ChitiniteShieldModel::createBodyLayer);
		event.registerLayerDefinition(IronWallModel.iron_wall, IronWallModel::createBodyLayer);
		event.registerLayerDefinition(IronSpikeModel.iron_spike, IronSpikeModel::createBodyLayer);
		event.registerLayerDefinition(IronPillarModel.iron_pillar, IronPillarModel::createBodyLayer);
		event.registerLayerDefinition(BloodBulletModel.blood_bullet, BloodBulletModel::createBodyLayer);
		event.registerLayerDefinition(WretchedWillModel.wretched_will, WretchedWillModel::createBodyLayer);
		event.registerLayerDefinition(BloodGourdModel.blood_gourd, BloodGourdModel::createBodyLayer);
		event.registerLayerDefinition(CurvedHornModel.curved_horn, CurvedHornModel::createBodyLayer);
		event.registerLayerDefinition(HemorathRibModel.hemorath_rib, HemorathRibModel::createBodyLayer);
		event.registerLayerDefinition(OpenBloodGourdModel.open_blood_gourd, OpenBloodGourdModel::createBodyLayer);
		event.registerLayerDefinition(OpenCurvedHornModel.open_curved_horn, OpenCurvedHornModel::createBodyLayer);
		event.registerLayerDefinition(ChthonianModel.LAYER_LOCATION, ChthonianModel::createBodyLayer);
		event.registerLayerDefinition(ChthonianQueenModel.LAYER_LOCATION, ChthonianQueenModel::createBodyLayer);
		event.registerLayerDefinition(ChitiniteModel.LAYER_LOCATION, ChitiniteModel::createBodyLayer);
		event.registerLayerDefinition(FerventChitiniteModel.LAYER_LOCATION, FerventChitiniteModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingJarModel.morphling_jar, MorphlingJarModel::createBodyLayer);
		event.registerLayerDefinition(FerventChitiniteModel.CRYSTAL_LAYER_LOCATION,
				FerventChitiniteModel::createCrystalLayer);

		event.registerLayerDefinition(DrudgeModel.LAYER_LOCATION, DrudgeModel::createBodyLayer);
		event.registerLayerDefinition(FargoneModel.LAYER_LOCATION, FargoneModel::createBodyLayer);
		event.registerLayerDefinition(FunglingModel.LAYER_LOCATION, FunglingModel::createBodyLayer);
		event.registerLayerDefinition(LeechModel.LAYER_LOCATION, LeechModel::createBodyLayer);
		event.registerLayerDefinition(ToothPecksModel.LAYER_LOCATION, ToothPecksModel::createBodyLayer);
		event.registerLayerDefinition(LumpOfThoughtModel.LAYER_LOCATION, LumpOfThoughtModel::createBodyLayer);
		event.registerLayerDefinition(BloodDrunkPuppeteerModel.LAYER_LOCATION,
				BloodDrunkPuppeteerModel::createbodyLayer);
		event.registerLayerDefinition(ErythromyceliumEruptusModel.LAYER_LOCATION,
				ErythromyceliumEruptusModel::createBodyLayer);
		event.registerLayerDefinition(EnthralledDollModel.LAYER_LOCATION, EnthralledDollModel::createBodyLayer);

		event.registerLayerDefinition(BloodThrallModel.LAYER_LOCATION, BloodThrallModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingPolypModel.LAYER_LOCATION, MorphlingPolypModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.HEAD_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.BODY_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.RIGHT_ARM_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.LEFT_ARM_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.RIGHT_LEG_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.LEFT_LEG_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(ThirsterModel.LAYER_LOCATION, ThirsterModel::createBodyLayer);
		event.registerLayerDefinition(BloodArmModel.blood_arm, BloodArmModel::createHeadLayer);
		event.registerLayerDefinition(EarthenVeinModel.LAYER_LOCATION, EarthenVeinModel::createBodyLayer);
		event.registerLayerDefinition(FungalImplantationPylonModel.LAYER_LOCATION,
				FungalImplantationPylonModel::createBodyLayer);
		event.registerLayerDefinition(FloatingEyeModel.LAYER_LOCATION, FloatingEyeModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinModel.LAYER_LOCATION, BarbedUrchinModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinBigModel.LAYER_LOCATION, BarbedUrchinBigModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinMidModel.LAYER_LOCATION, BarbedUrchinMidModel::createBodyLayer);
		event.registerLayerDefinition(HemolymphopodaModel.LAYER_LOCATION, HemolymphopodaModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedZealotModel.LAYER_LOCATION, UnstainedZealotModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedGuardianModel.LAYER_LOCATION, UnstainedGuardianModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedAcolyteModel.LAYER_LOCATION, UnstainedAcolyteModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerHermitModel.LAYER_LOCATION, HarbingerHermitModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerAlchemistModel.LAYER_LOCATION, HarbingerAlchemistModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerVicarModel.LAYER_LOCATION, HarbingerVicarModel::createBodyLayer);
		event.registerLayerDefinition(HollowVesselModel.LAYER_LOCATION, HollowVesselModel::createBodyLayer);
		event.registerLayerDefinition(SeraphaeModel.LAYER_LOCATION, SeraphaeModel::createBodyLayer);
		event.registerLayerDefinition(SeraphaeFragmentModel.LAYER_LOCATION, SeraphaeFragmentModel::createBodyLayer);
		event.registerLayerDefinition(ContainmentAnchorModel.LAYER_LOCATION, ContainmentAnchorModel::createBodyLayer);
		event.registerLayerDefinition(DessicantModel.LAYER_LOCATION, DessicantModel::createBodyLayer);
		event.registerLayerDefinition(CruorFiendModel.LAYER_LOCATION, CruorFiendModel::createBodyLayer);
		event.registerLayerDefinition(VoidDrinkerModel.LAYER_LOCATION, VoidDrinkerModel::createBodyLayer);
		event.registerLayerDefinition(FrozenClotModel.LAYER_LOCATION, FrozenClotModel::createBodyLayer);
		event.registerLayerDefinition(AbyssalSiphonModel.LAYER_LOCATION, AbyssalSiphonModel::createBodyLayer);
		event.registerLayerDefinition(SynapseHoundModel.LAYER_LOCATION, SynapseHoundModel::createBodyLayer);
		event.registerLayerDefinition(MyelinBorerModel.LAYER_LOCATION, MyelinBorerModel::createBodyLayer);
		event.registerLayerDefinition(CrimsonDoeModel.LAYER_LOCATION, CrimsonDoeModel::createBodyLayer);
		event.registerLayerDefinition(HemojellyModel.LAYER_LOCATION, HemojellyModel::createBodyLayer);
		event.registerLayerDefinition(VenousStriderModel.LAYER_LOCATION, VenousStriderModel::createBodyLayer);

		event.registerLayerDefinition(MarrowCrownModel.LAYER_LOCATION,
				() -> MarrowCrownModel.createHeadLayer(EquipmentSlot.HEAD));

		event.registerLayerDefinition(HemolymphopodaHeadArmorModel.LAYER_LOCATION,
				HemolymphopodaHeadArmorModel::createHeadLayer);

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends LivingEntity, M extends HumanoidModel<T>, R extends LivingEntityRenderer<T, M>> void addLayerToEntity(
			EntityRenderersEvent.AddLayers event, EntityType<? extends T> entityType) {
		R renderer = event.getRenderer(entityType);
		if (renderer != null) {
//			renderer.addLayer(new BloodGourdLayer(renderer));
//			renderer.addLayer(new BloodAvatarLayer(renderer));
//			renderer.addLayer(new CellHandLayer(renderer));
//			renderer.addLayer(new RenderScarsLayer(renderer));
//			renderer.addLayer(new VascCharmLayer<>(renderer));
			renderer.addLayer(new HemolymphopodaHeadpieceLayer(renderer));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, PlayerSkin.Model skinModel) {
		EntityRenderer<? extends Player> render = event.getSkin(skinModel);
		if (render instanceof LivingEntityRenderer livingRenderer) {
			livingRenderer.addLayer(new BloodGourdLayer<>(livingRenderer));
			livingRenderer.addLayer(new BloodAvatarLayer(livingRenderer));
			livingRenderer.addLayer(new CellHandLayer(livingRenderer));
			livingRenderer.addLayer(new RenderScarsLayer(livingRenderer));
			livingRenderer.addLayer(new VascCharmLayer(livingRenderer));
			livingRenderer.addLayer(new FungalElytraLayer(livingRenderer));
			livingRenderer.addLayer(new MorphlingMutationLayer<>(livingRenderer));
			livingRenderer.addLayer(new EquippedMorphlingLayer(livingRenderer));
			livingRenderer.addLayer(new HemolymphopodaHeadpieceLayer(livingRenderer));
			livingRenderer.addLayer(new MorphlingJarLayer<>(livingRenderer));
		}
	}

	@SubscribeEvent
	public static void constructLayers(EntityRenderersEvent.AddLayers event) {

		addLayerToEntity(event, EntityType.ARMOR_STAND);
		addLayerToEntity(event, EntityType.ZOMBIE);
		addLayerToEntity(event, EntityType.SKELETON);
		addLayerToEntity(event, EntityType.HUSK);
		addLayerToEntity(event, EntityType.DROWNED);
		addLayerToEntity(event, EntityType.STRAY);
		addLayerToPlayerSkin(event, PlayerSkin.Model.WIDE);
		addLayerToPlayerSkin(event, PlayerSkin.Model.SLIM);

	}

//	@SuppressWarnings("deprecation")
//	@SubscribeEvent
//	public static void onStitch(TextureStitchEvent.Pre event) {
//		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
//			event.addSprite(Hemomancy.rloc("entity/royal_guard_shield_base"));
//			event.addSprite(Hemomancy.rloc("entity/barbed_shield/model_barbed_shield"));
//			event.addSprite(Hemomancy.rloc("entity/chitinite_shield/model_chitinite_shield"));
//
//		}
//		if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
//			event.addSprite(BannerSlot.SLOT_BACKGROUND);
//		}
//	}

	// For when the horn breaks it shows the custom animation
	public static void playHornAnimation() {
		LocalPlayer ent = Minecraft.getInstance().player;
		Minecraft.getInstance().particleEngine.createTrackingEmitter(ent, ParticleTypes.SOUL, 30);
		Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ItemInit.curved_horn.get()));
		Minecraft.getInstance().level.playLocalSound(ent.getX(), ent.getY(), ent.getZ(), SoundEvents.TOTEM_USE,
				ent.getSoundSource(), 1F, 0.6F, false);
	}

}

