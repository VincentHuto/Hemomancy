package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.*;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel.EnumBloodLustMaskTypes;
import com.vincenthuto.hemomancy.client.model.entity.boss.Hemorath.HemorathModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.AnnettaKnowlesModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.LatentAnnettaInfectionModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.annetta.StainedPriestessModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.MycophantModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.putriciel.PutricielModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.ContainmentAnchorModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeFragmentModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.client.model.entity.boss.velorum.VelorumModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.*;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.*;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.*;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.*;
import com.vincenthuto.hemomancy.client.model.entity.mob.will.WillModel;
import com.vincenthuto.hemomancy.client.model.entity.npc.*;
import com.vincenthuto.hemomancy.client.model.entity.summon.*;
import com.vincenthuto.hemomancy.client.model.item.*;
import com.vincenthuto.hemomancy.client.model.item.unstained.*;
import com.vincenthuto.hemomancy.client.model.tile.SuspendedBloodCrystalModel;
import com.vincenthuto.hemomancy.client.model.tile.SuspendedCleansedBloodCrystalModel;
import com.vincenthuto.hemomancy.client.model.tile.SuspendedVivianiteModel;
import com.vincenthuto.hemomancy.client.model.tile.crafting.*;
import com.vincenthuto.hemomancy.client.model.tile.functional.*;
import com.vincenthuto.hemomancy.client.render.layer.MonolithicDislocationShellLayer;
import com.vincenthuto.hemomancy.client.render.layer.player.*;
import com.vincenthuto.hemomancy.client.render.layer.mob.HuskEffigyControlGlowLayer;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID,  value = Dist.CLIENT)
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
		event.registerLayerDefinition(LivingTorchModel.LAYER_LOCATION, LivingTorchModel::createBodyLayer);
		event.registerLayerDefinition(LivingFlailModel.LAYER_LOCATION, LivingFlailModel::createBodyLayer);
		event.registerLayerDefinition(LivingSickleModel.LAYER_LOCATION, LivingSickleModel::createBodyLayer);
		event.registerLayerDefinition(FloatingHeartModel.mortal_display, FloatingHeartModel::createBodyLayer);
		event.registerLayerDefinition(CentrifugeArmsModel.LAYER_LOCATION, CentrifugeArmsModel::createBodyLayer);
		event.registerLayerDefinition(CentrifugeStandModel.LAYER_LOCATION, CentrifugeStandModel::createBodyLayer);
		event.registerLayerDefinition(VisceralMirrorModel.LAYER_LOCATION, VisceralMirrorModel::createBodyLayer);
		event.registerLayerDefinition(SanguisLanceaModel.LAYER_LOCATION, SanguisLanceaModel::createBodyLayer);
		event.registerLayerDefinition(AnnettasSanguisLanceaModel.LAYER_LOCATION, AnnettasSanguisLanceaModel::createBodyLayer);
		event.registerLayerDefinition(SporiticThuribleModel.LAYER_LOCATION, SporiticThuribleModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedWarhammerModel.LAYER_LOCATION, UnstainedWarhammerModel::createBodyLayer);
		event.registerLayerDefinition(SilthmereGlaiveModel.LAYER_LOCATION, SilthmereGlaiveModel::createBodyLayer);
		event.registerLayerDefinition(AbsolutionDaggerModel.LAYER_LOCATION, AbsolutionDaggerModel::createBodyLayer);
		event.registerLayerDefinition(SuspendedVivianiteModel.LAYER_LOCATION, SuspendedVivianiteModel::createBodyLayer);
		event.registerLayerDefinition(SuspendedBloodCrystalModel.LAYER_LOCATION, SuspendedBloodCrystalModel::createBodyLayer);
		event.registerLayerDefinition(SuspendedCleansedBloodCrystalModel.LAYER_LOCATION, SuspendedCleansedBloodCrystalModel::createBodyLayer);
		event.registerLayerDefinition(MnemonicReliquaryModel.LAYER_LOCATION, MnemonicReliquaryModel::createBodyLayer);
		event.registerLayerDefinition(HematicArmatureModel.LAYER_LOCATION, HematicArmatureModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingIncubatorModel.LAYER_LOCATION, MorphlingIncubatorModel::createBodyLayer);
		event.registerLayerDefinition(PuppeteersSpindleModel.LAYER_LOCATION, PuppeteersSpindleModel::createBodyLayer);
		event.registerLayerDefinition(MasonsEffigyModel.LAYER_LOCATION, MasonsEffigyModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerSaintSarcophagusModel.LAYER_LOCATION, HarbingerSaintSarcophagusModel::createBodyLayer);
		event.registerLayerDefinition(SanguineMonolithModel.LAYER_LOCATION, SanguineMonolithModel::createBodyLayer);
		event.registerLayerDefinition(CovenantThroneModel.LAYER_LOCATION, CovenantThroneModel::createBodyLayer);
		event.registerLayerDefinition(BarbedShieldModel.barbed_shield, BarbedShieldModel::createLayers);
		event.registerLayerDefinition(ChitiniteShieldModel.chitinite_shield, ChitiniteShieldModel::createBodyLayer);
		event.registerLayerDefinition(IronWallModel.iron_wall, IronWallModel::createBodyLayer);
		event.registerLayerDefinition(IronSpikeModel.iron_spike, IronSpikeModel::createBodyLayer);
		event.registerLayerDefinition(IronPillarModel.iron_pillar, IronPillarModel::createBodyLayer);
		event.registerLayerDefinition(BloodBulletModel.blood_bullet, BloodBulletModel::createBodyLayer);
		event.registerLayerDefinition(WretchedWillModel.wretched_will, WretchedWillModel::createBodyLayer);
		event.registerLayerDefinition(WillModel.LAYER_LOCATION, WillModel::createBodyLayer);
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
		event.registerLayerDefinition(com.vincenthuto.hemomancy.client.model.entity.mob.monster.RougeDrudgeModel.LAYER_LOCATION,
				com.vincenthuto.hemomancy.client.model.entity.mob.monster.RougeDrudgeModel::createBodyLayer);
		event.registerLayerDefinition(ModelBogRevenant.LAYER_LOCATION, ModelBogRevenant::createBodyLayer);
		event.registerLayerDefinition(FargoneModel.LAYER_LOCATION, FargoneModel::createBodyLayer);
		event.registerLayerDefinition(FunglingModel.LAYER_LOCATION, FunglingModel::createBodyLayer);
		event.registerLayerDefinition(LeechModel.LAYER_LOCATION, LeechModel::createBodyLayer);
		event.registerLayerDefinition(BloodlickerModel.LAYER_LOCATION, BloodlickerModel::createBodyLayer);
		event.registerLayerDefinition(ToothPecksModel.LAYER_LOCATION, ToothPecksModel::createBodyLayer);
		event.registerLayerDefinition(LumpOfThoughtModel.LAYER_LOCATION, LumpOfThoughtModel::createBodyLayer);
		event.registerLayerDefinition(BloodDrunkPuppeteerModel.LAYER_LOCATION,BloodDrunkPuppeteerModel::createBodyLayer);
		event.registerLayerDefinition(ErythromyceliumEruptusModel.LAYER_LOCATION,
				ErythromyceliumEruptusModel::createBodyLayer);
		event.registerLayerDefinition(EnthralledDollModel.LAYER_LOCATION, EnthralledDollModel::createBodyLayer);

		event.registerLayerDefinition(BloodThrallModel.LAYER_LOCATION, BloodThrallModel::createBodyLayer);
		event.registerLayerDefinition(VeinwingVultureModel.LAYER_LOCATION, VeinwingVultureModel::createBodyLayer);
		event.registerLayerDefinition(MarrowSpitterModel.LAYER_LOCATION, MarrowSpitterModel::createBodyLayer);
		event.registerLayerDefinition(GoreboundHulkModel.LAYER_LOCATION, GoreboundHulkModel::createBodyLayer);
		event.registerLayerDefinition(MnemonistPuppetModel.LAYER_LOCATION, MnemonistPuppetModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingPolypModel.LAYER_LOCATION, MorphlingPolypModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingPolypLayerModel.LAYER_LOCATION, MorphlingPolypLayerModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.HEAD_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.BODY_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.ARMS_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingAttachmentExampleModel.LEGS_LAYER, MorphlingAttachmentExampleModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingBatHeadAttachmentModel.LAYER_LOCATION, MorphlingBatHeadAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingSpiderBodyAttachmentModel.LAYER_LOCATION, MorphlingSpiderBodyAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingFungalHeadModel.LAYER_LOCATION, MorphlingFungalHeadModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingLeechArmAttachmentModel.LAYER_LOCATION, MorphlingLeechArmAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingChitiniteLegAttachmentModel.LAYER_LOCATION, MorphlingChitiniteLegAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingSerpentLegAttachmentModel.LAYER_LOCATION, MorphlingSerpentLegAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingUrchinBodyAttachmentModel.LAYER_LOCATION, MorphlingUrchinBodyAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingCuttlefishHeadAttachmentModel.LAYER_LOCATION, MorphlingCuttlefishHeadAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingCentipedeBodyAttachmentModel.LAYER_LOCATION, MorphlingCentipedeBodyAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingPestsBodyAttachmentModel.LAYER_LOCATION, MorphlingPestsBodyAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingTickBodyAttachmentModel.LAYER_LOCATION, MorphlingTickBodyAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingMoleArmAttachmentModel.LAYER_LOCATION, MorphlingMoleArmAttachmentModel::createBodyLayer);
		event.registerLayerDefinition(ThirsterModel.LAYER_LOCATION, ThirsterModel::createBodyLayer);
		event.registerLayerDefinition(BloodArmModel.blood_arm, BloodArmModel::createHeadLayer);
		event.registerLayerDefinition(EarthenVeinModel.LAYER_LOCATION, EarthenVeinModel::createBodyLayer);
		event.registerLayerDefinition(FungalImplantationPylonModel.LAYER_LOCATION,
				FungalImplantationPylonModel::createBodyLayer);
		event.registerLayerDefinition(FloatingEyeModel.LAYER_LOCATION, FloatingEyeModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinModel.LAYER_LOCATION, BarbedUrchinModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinBigModel.LAYER_LOCATION, BarbedUrchinBigModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinMidModel.LAYER_LOCATION, BarbedUrchinMidModel::createBodyLayer);
		event.registerLayerDefinition(ChalybeateSnailModel.LAYER_LOCATION, ChalybeateSnailModel::createBodyLayer);
		event.registerLayerDefinition(BloodLanternJellyModel.LAYER_LOCATION, BloodLanternJellyModel::createBodyLayer);
		event.registerLayerDefinition(MnemonicWhaleModel.LAYER_LOCATION, MnemonicWhaleModel::createBodyLayer);
		event.registerLayerDefinition(BrinedVotaryModel.LAYER_LOCATION, BrinedVotaryModel::createBodyLayer);
		event.registerLayerDefinition(HemolymphopodaModel.LAYER_LOCATION, HemolymphopodaModel::createBodyLayer);
		event.registerLayerDefinition(LanternTickModel.LAYER_LOCATION, LanternTickModel::createBodyLayer);
		event.registerLayerDefinition(PrismCuttleModel.LAYER_LOCATION, PrismCuttleModel::createBodyLayer);
		event.registerLayerDefinition(HematicBurrowerModel.LAYER_LOCATION, HematicBurrowerModel::createBodyLayer);
		event.registerLayerDefinition(VenomRibCentipedeModel.LAYER_LOCATION, VenomRibCentipedeModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedZealotModel.LAYER_LOCATION, UnstainedZealotModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedGuardianModel.LAYER_LOCATION, UnstainedGuardianModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedAcolyteModel.LAYER_LOCATION, UnstainedAcolyteModel::createBodyLayer);
		event.registerLayerDefinition(UnstainedScoutModel.LAYER_LOCATION, UnstainedScoutModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerHermitModel.LAYER_LOCATION, HarbingerHermitModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerAlchemistModel.LAYER_LOCATION, HarbingerAlchemistModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerArtificerModel.LAYER_LOCATION, HarbingerArtificerModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerCicatrixAnchoriteModel.LAYER_LOCATION,
				HarbingerCicatrixAnchoriteModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerMnemonistModel.LAYER_LOCATION, HarbingerMnemonistModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerVicarModel.LAYER_LOCATION, HarbingerVicarModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerVoyagerModel.LAYER_LOCATION, HarbingerVoyagerModel::createBodyLayer);
		event.registerLayerDefinition(HarbingerVotaryWayfarerModel.LAYER_LOCATION,
				HarbingerVotaryWayfarerModel::createBodyLayer);
		event.registerLayerDefinition(HemorathModel.LAYER_LOCATION, HemorathModel::createBodyLayer);
		event.registerLayerDefinition(AnnettaKnowlesModel.LAYER_LOCATION, AnnettaKnowlesModel::createBodyLayer);
		event.registerLayerDefinition(StainedPriestessModel.LAYER_LOCATION, StainedPriestessModel::createBodyLayer);
		event.registerLayerDefinition(LatentAnnettaInfectionModel.LAYER_LOCATION, LatentAnnettaInfectionModel::createBodyLayer);
		event.registerLayerDefinition(PutricielModel.LAYER_LOCATION, PutricielModel::createBodyLayer);
		event.registerLayerDefinition(VelorumModel.LAYER_LOCATION, VelorumModel::createBodyLayer);
		event.registerLayerDefinition(VesperTheCrownedRefusalModel.LAYER_LOCATION, VesperTheCrownedRefusalModel::createBodyLayer);
		event.registerLayerDefinition(VesperTheEveningStarModel.LAYER_LOCATION, VesperTheEveningStarModel::createBodyLayer);
		event.registerLayerDefinition(MycophantModel.LAYER_LOCATION, MycophantModel::createBodyLayer);
		event.registerLayerDefinition(SeraphaeModel.LAYER_LOCATION, SeraphaeModel::createBodyLayer);
		event.registerLayerDefinition(SeraphaeFragmentModel.LAYER_LOCATION, SeraphaeFragmentModel::createBodyLayer);
		event.registerLayerDefinition(ContainmentAnchorModel.LAYER_LOCATION, ContainmentAnchorModel::createBodyLayer);
		event.registerLayerDefinition(DesiccantModel.LAYER_LOCATION, DesiccantModel::createBodyLayer);
//		event.registerLayerDefinition(CruorFiendModel.LAYER_LOCATION, CruorFiendModel::createBodyLayer);
//		event.registerLayerDefinition(VoidDrinkerModel.LAYER_LOCATION, VoidDrinkerModel::createBodyLayer);
//		event.registerLayerDefinition(FrozenClotModel.LAYER_LOCATION, FrozenClotModel::createBodyLayer);
//		event.registerLayerDefinition(AbyssalSiphonModel.LAYER_LOCATION, AbyssalSiphonModel::createBodyLayer);
//		event.registerLayerDefinition(SynapseHoundModel.LAYER_LOCATION, SynapseHoundModel::createBodyLayer);
//		event.registerLayerDefinition(MyelinBorerModel.LAYER_LOCATION, MyelinBorerModel::createBodyLayer);
		event.registerLayerDefinition(CrimsonDoeModel.LAYER_LOCATION, CrimsonDoeModel::createBodyLayer);
		event.registerLayerDefinition(VerdigrisMothModel.LAYER_LOCATION, VerdigrisMothModel::createBodyLayer);
		event.registerLayerDefinition(ScarletSerpentModel.LAYER_LOCATION, ScarletSerpentModel::createBodyLayer);
		event.registerLayerDefinition(HemojellyModel.LAYER_LOCATION, HemojellyModel::createBodyLayer);
		event.registerLayerDefinition(VenousStriderModel.LAYER_LOCATION, VenousStriderModel::createBodyLayer);

		event.registerLayerDefinition(MarrowCrownModel.LAYER_LOCATION,
				() -> MarrowCrownModel.createHeadLayer(EquipmentSlot.HEAD));

		event.registerLayerDefinition(HemolymphopodaHeadArmorModel.LAYER_LOCATION,
				HemolymphopodaHeadArmorModel::createHeadLayer);
		event.registerLayerDefinition(LanternTickHeadArmorModel.LAYER_LOCATION,
				LanternTickHeadArmorModel::createHeadLayer);

		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_HEAD_LAYER,
				() -> BloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.NONE));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_HEAD_TENGU_LAYER,
				() -> BloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.TENGU));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_HEAD_GRINNING_LAYER,
				() -> BloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.GRINNING));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_CHEST_LAYER,
				() -> BloodLustArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_LEGS_LAYER,
				() -> BloodLustArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(BloodLustArmorModel.BLOOD_LUST_BOOTS_LAYER,
				() -> BloodLustArmorModel.createBodyLayer(EquipmentSlot.FEET));
		event.registerLayerDefinition(BloodAvatarModel.layer, BloodAvatarModel::createLayer);

		event.registerLayerDefinition(SilentArchonArmorModel.SILENT_ARCHON_HELMET_LAYER,
				SilentArchonArmorModel::createBodyLayer);
		event.registerLayerDefinition(SilentArchonArmorModel.SILENT_ARCHON_CHEST_LAYER,
				SilentArchonArmorModel::createBodyLayer);
		event.registerLayerDefinition(SilentArchonArmorModel.SILENT_ARCHON_LEGS_LAYER,
				SilentArchonArmorModel::createBodyLayer);
		event.registerLayerDefinition(SilentArchonArmorModel.SILENT_ARCHON_BOOTS_LAYER,
				SilentArchonArmorModel::createBodyLayer);
		event.registerLayerDefinition(ChalybeateFortressArmorModel.CHALYBEATE_FORTRESS_BOOTS_LAYER,
				ChalybeateFortressArmorModel::createBodyLayer);
		event.registerLayerDefinition(CovenantLeaderArmorModel.COVENANT_LEADER_CHEST_LAYER,
				CovenantLeaderArmorModel::createBodyLayer);
		event.registerLayerDefinition(VestmentOfTheFinalMoltArmorModel.VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER,
				VestmentOfTheFinalMoltArmorModel::createBodyLayer);

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

		event.registerLayerDefinition(PrismaticArmorModel.PRISMATIC_HELMET_LAYER,
				() -> PrismaticArmorModel.createHeadLayer(EquipmentSlot.HEAD));
		event.registerLayerDefinition(PrismaticArmorModel.PRISMATIC_CHEST_LAYER,
				() -> PrismaticArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(PrismaticArmorModel.PRISMATIC_LEGS_LAYER,
				() -> PrismaticArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(PrismaticArmorModel.PRISMATIC_FEET_LAYER,
				() -> PrismaticArmorModel.createBodyLayer(EquipmentSlot.FEET));

		event.registerLayerDefinition(EdaciousBloodLustArmorModel.EDACIOUS_BLOOD_LUST_HELMET_LAYER,
				() -> EdaciousBloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD));
		event.registerLayerDefinition(EdaciousBloodLustArmorModel.EDACIOUS_BLOOD_LUST_CHEST_LAYER,
				() -> EdaciousBloodLustArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(EdaciousBloodLustArmorModel.EDACIOUS_BLOOD_LUST_LEGS_LAYER,
				() -> EdaciousBloodLustArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(EdaciousBloodLustArmorModel.EDACIOUS_BLOOD_LUST_FEET_LAYER,
				() -> EdaciousBloodLustArmorModel.createBodyLayer(EquipmentSlot.FEET));

		event.registerLayerDefinition(SheolicBloodLustArmorModel.SHEOLIC_BLOOD_LUST_HELMET_LAYER,
				() -> SheolicBloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD));
		event.registerLayerDefinition(SheolicBloodLustArmorModel.SHEOLIC_BLOOD_LUST_CHEST_LAYER,
				() -> SheolicBloodLustArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(SheolicBloodLustArmorModel.SHEOLIC_BLOOD_LUST_LEGS_LAYER,
				() -> SheolicBloodLustArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(SheolicBloodLustArmorModel.SHEOLIC_BLOOD_LUST_FEET_LAYER,
				() -> SheolicBloodLustArmorModel.createBodyLayer(EquipmentSlot.FEET));

		event.registerLayerDefinition(PhantasmalBloodLustArmorModel.PHANTASMAL_BLOOD_LUST_HELMET_LAYER,
				() -> PhantasmalBloodLustArmorModel.createHeadLayer(EquipmentSlot.HEAD));
		event.registerLayerDefinition(PhantasmalBloodLustArmorModel.PHANTASMAL_BLOOD_LUST_CHEST_LAYER,
				() -> PhantasmalBloodLustArmorModel.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(PhantasmalBloodLustArmorModel.PHANTASMAL_BLOOD_LUST_LEGS_LAYER,
				() -> PhantasmalBloodLustArmorModel.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(PhantasmalBloodLustArmorModel.PHANTASMAL_BLOOD_LUST_FEET_LAYER,
				() -> PhantasmalBloodLustArmorModel.createBodyLayer(EquipmentSlot.FEET));

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
			renderer.addLayer(new LanternTickHelmetLayer(renderer));
			renderer.addLayer(new SilentArchonArmorOverlayLayer(renderer));
			renderer.addLayer(new EdaciousBloodLustWingLayer(renderer));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends LivingEntity> void addEffigyGlowLayer(EntityRenderersEvent.AddLayers event,
			EntityType<? extends T> entityType) {
		EntityRenderer<? extends T> renderer = event.getRenderer(entityType);
		if (renderer instanceof LivingEntityRenderer livingRenderer) {
			livingRenderer.addLayer(new HuskEffigyControlGlowLayer(livingRenderer));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addMonolithicDislocationShellLayers(EntityRenderersEvent.AddLayers event) {
		for (EntityType<?> entityType : event.getEntityTypes()) {
			EntityRenderer<?> renderer = event.getRenderer(entityType);
			if (renderer instanceof LivingEntityRenderer livingRenderer) {
				livingRenderer.addLayer(new MonolithicDislocationShellLayer(livingRenderer));
			}
		}
		for (PlayerSkin.Model skinModel : event.getSkins()) {
			EntityRenderer<? extends Player> renderer = event.getSkin(skinModel);
			if (renderer instanceof LivingEntityRenderer livingRenderer) {
				livingRenderer.addLayer(new MonolithicDislocationShellLayer(livingRenderer));
			}
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
			livingRenderer.addLayer(new MycophantTendrilFungalizationLayer(livingRenderer));
			livingRenderer.addLayer(new MorphlingMutationLayer<>(livingRenderer));
			livingRenderer.addLayer(new EquippedMorphlingLayer(livingRenderer));
			livingRenderer.addLayer(new HemolymphopodaHeadpieceLayer(livingRenderer));
			livingRenderer.addLayer(new LanternTickHelmetLayer(livingRenderer));
			livingRenderer.addLayer(new MorphlingJarLayer<>(livingRenderer));
			livingRenderer.addLayer(new SporiticThuribleLayer<>(livingRenderer));
			livingRenderer.addLayer(new LivingFlailLayer<>(livingRenderer));
			livingRenderer.addLayer(new CardinalRiteStaffPlantingLayer(livingRenderer));
			livingRenderer.addLayer(new SilentArchonArmorOverlayLayer(livingRenderer));
			livingRenderer.addLayer(new EdaciousBloodLustWingLayer(livingRenderer));
		}
	}

	@SubscribeEvent
	public static void constructLayers(EntityRenderersEvent.AddLayers event) {
		addMonolithicDislocationShellLayers(event);

		addLayerToEntity(event, EntityType.ARMOR_STAND);
		addLayerToEntity(event, EntityType.ZOMBIE);
		addLayerToEntity(event, EntityType.SKELETON);
		addLayerToEntity(event, EntityType.HUSK);
		addLayerToEntity(event, EntityType.DROWNED);
		addLayerToEntity(event, EntityType.STRAY);
		addEffigyGlowLayer(event, EntityType.ZOMBIE);
		addEffigyGlowLayer(event, EntityType.HUSK);
		addEffigyGlowLayer(event, EntityType.SPIDER);
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
