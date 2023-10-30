package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BarbedArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.BloodAvatarModel;
import com.vincenthuto.hemomancy.client.model.armor.BloodGourdModel;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel.EnumBloodLustMaskTypes;
import com.vincenthuto.hemomancy.client.model.armor.ChitiniteArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.CurvedHornModel;
import com.vincenthuto.hemomancy.client.model.armor.UnstainedArmorModel;
import com.vincenthuto.hemomancy.client.model.block.CentrifugeArmsModel;
import com.vincenthuto.hemomancy.client.model.block.EarthenVeinModel;
import com.vincenthuto.hemomancy.client.model.block.FloatingEyeModel;
import com.vincenthuto.hemomancy.client.model.block.FloatingHeartModel;
import com.vincenthuto.hemomancy.client.model.entity.BloodBulletModel;
import com.vincenthuto.hemomancy.client.model.entity.IronPillarModel;
import com.vincenthuto.hemomancy.client.model.entity.IronSpikeModel;
import com.vincenthuto.hemomancy.client.model.entity.IronWallModel;
import com.vincenthuto.hemomancy.client.model.entity.WretchedWillModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.AbhorentThoughtModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.BarbedUrchinBigModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.BarbedUrchinMidModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.BarbedUrchinModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.BloodDrunkPuppeteerModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.ChitiniteModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.ChthonianModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.ChthonianQueenModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.DrudgeModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.EnthralledDollModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.ErythromyceliumEruptusModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.FargoneModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.FunglingModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.LeechModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.LumpOfThoughtModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.MorphlingPolypModel;
import com.vincenthuto.hemomancy.client.model.entity.mob.ThirsterModel;
import com.vincenthuto.hemomancy.client.model.item.BarbedShieldModel;
import com.vincenthuto.hemomancy.client.model.item.BloodArmModel;
import com.vincenthuto.hemomancy.client.model.item.ChitiniteShieldModel;
import com.vincenthuto.hemomancy.client.model.item.LivingAxeModel;
import com.vincenthuto.hemomancy.client.model.item.LivingBladeHandTameModel;
import com.vincenthuto.hemomancy.client.model.item.LivingBladeUnleashedModel;
import com.vincenthuto.hemomancy.client.model.item.LivingSpearModel;
import com.vincenthuto.hemomancy.client.model.item.SanguisLanceaModel;
import com.vincenthuto.hemomancy.client.render.layer.player.BloodAvatarLayer;
import com.vincenthuto.hemomancy.client.render.layer.player.BloodGourdLayer;
import com.vincenthuto.hemomancy.client.render.layer.player.CellHandLayer;
import com.vincenthuto.hemomancy.client.render.layer.player.VascCharmLayer;
import com.vincenthuto.hemomancy.common.registry.ItemInit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
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
		event.registerLayerDefinition(SanguisLanceaModel.LAYER_LOCATION, SanguisLanceaModel::createBodyLayer);

		event.registerLayerDefinition(BarbedShieldModel.barbed_shield, BarbedShieldModel::createLayers);
		event.registerLayerDefinition(ChitiniteShieldModel.chitinite_shield, ChitiniteShieldModel::createBodyLayer);
		event.registerLayerDefinition(IronWallModel.iron_wall, IronWallModel::createBodyLayer);
		event.registerLayerDefinition(IronSpikeModel.iron_spike, IronSpikeModel::createBodyLayer);
		event.registerLayerDefinition(IronPillarModel.iron_pillar, IronPillarModel::createBodyLayer);
		event.registerLayerDefinition(BloodBulletModel.blood_bullet, BloodBulletModel::createBodyLayer);
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
		event.registerLayerDefinition(ErythromyceliumEruptusModel.LAYER_LOCATION,
				ErythromyceliumEruptusModel::createBodyLayer);
		event.registerLayerDefinition(EnthralledDollModel.LAYER_LOCATION, EnthralledDollModel::createBodyLayer);
		event.registerLayerDefinition(MorphlingPolypModel.LAYER_LOCATION, MorphlingPolypModel::createBodyLayer);
		event.registerLayerDefinition(ThirsterModel.LAYER_LOCATION, ThirsterModel::createBodyLayer);
		event.registerLayerDefinition(BloodArmModel.blood_arm, BloodArmModel::createHeadLayer);
		event.registerLayerDefinition(EarthenVeinModel.LAYER_LOCATION, EarthenVeinModel::createBodyLayer);
		event.registerLayerDefinition(FloatingEyeModel.LAYER_LOCATION, FloatingEyeModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinModel.LAYER_LOCATION, BarbedUrchinModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinBigModel.LAYER_LOCATION, BarbedUrchinBigModel::createBodyLayer);
		event.registerLayerDefinition(BarbedUrchinMidModel.LAYER_LOCATION, BarbedUrchinMidModel::createBodyLayer);
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
			renderer.addLayer(new BloodGourdLayer(renderer));
			renderer.addLayer(new BloodAvatarLayer(renderer));
			renderer.addLayer(new CellHandLayer(renderer));
			renderer.addLayer(new VascCharmLayer<>(renderer));

		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
		EntityRenderer<? extends Player> render = event.getSkin(skinName);
		if (render instanceof LivingEntityRenderer livingRenderer) {
			livingRenderer.addLayer(new BloodGourdLayer<>(livingRenderer));
			livingRenderer.addLayer(new BloodAvatarLayer(livingRenderer));
			livingRenderer.addLayer(new CellHandLayer(livingRenderer));
			livingRenderer.addLayer(new VascCharmLayer(livingRenderer));

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
		addLayerToPlayerSkin(event, "default");
		addLayerToPlayerSkin(event, "slim");

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
