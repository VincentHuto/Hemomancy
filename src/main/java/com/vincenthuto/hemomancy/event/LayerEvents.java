package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.model.armor.ModelBloodLustArmor;
import com.vincenthuto.hemomancy.model.armor.ModelBloodLustArmor.EnumBloodLustMaskTypes;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LayerEvents {

	public static final ModelLayerLocation BLOOD_LUST_HEAD_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_lust_helmet"), "main");
	public static final ModelLayerLocation BLOOD_LUST_HEAD_TENGU_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_lust_helmet_tengu"), "main");
	public static final ModelLayerLocation BLOOD_LUST_HEAD_HORNED_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_lust_helmet_horned"), "main");
	public static final ModelLayerLocation BLOOD_LUST_CHEST_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_lust_chest"), "main");
	public static final ModelLayerLocation BLOOD_LUST_LEGS_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_lust_legs"), "main");
	public static final ModelLayerLocation BLOOD_LUST_BOOTS_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_lust_boots"), "main");
	public static ModelBloodLustArmor<LivingEntity> helmet, tengu, horned, chest, legs, boots;

	@SubscribeEvent
	public static void initLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(BLOOD_LUST_HEAD_LAYER,
				() -> ModelBloodLustArmor.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.NONE));
		event.registerLayerDefinition(BLOOD_LUST_HEAD_TENGU_LAYER,
				() -> ModelBloodLustArmor.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.TENGU));
		event.registerLayerDefinition(BLOOD_LUST_HEAD_HORNED_LAYER,
				() -> ModelBloodLustArmor.createHeadLayer(EquipmentSlot.HEAD, EnumBloodLustMaskTypes.HORNED));
		event.registerLayerDefinition(BLOOD_LUST_CHEST_LAYER,
				() -> ModelBloodLustArmor.createBodyLayer(EquipmentSlot.CHEST));
		event.registerLayerDefinition(BLOOD_LUST_LEGS_LAYER,
				() -> ModelBloodLustArmor.createBodyLayer(EquipmentSlot.LEGS));
		event.registerLayerDefinition(BLOOD_LUST_BOOTS_LAYER,
				() -> ModelBloodLustArmor.createBodyLayer(EquipmentSlot.FEET));

	}

	@SubscribeEvent
	public static void initModels(EntityRenderersEvent.AddLayers event) {
		helmet = new ModelBloodLustArmor<>(event.getEntityModels().bakeLayer(BLOOD_LUST_HEAD_LAYER));
		tengu = new ModelBloodLustArmor<>(event.getEntityModels().bakeLayer(BLOOD_LUST_HEAD_TENGU_LAYER));
		horned = new ModelBloodLustArmor<>(event.getEntityModels().bakeLayer(BLOOD_LUST_HEAD_HORNED_LAYER));
		chest = new ModelBloodLustArmor<>(event.getEntityModels().bakeLayer(BLOOD_LUST_CHEST_LAYER));
		legs = new ModelBloodLustArmor<>(event.getEntityModels().bakeLayer(BLOOD_LUST_LEGS_LAYER));
		boots = new ModelBloodLustArmor<>(event.getEntityModels().bakeLayer(BLOOD_LUST_BOOTS_LAYER));

	}

	public static ModelBloodLustArmor<LivingEntity> getArmor(EquipmentSlot slot) {
		return switch (slot) {
		case CHEST -> chest;
		case LEGS -> legs;
		case FEET -> boots;
		default -> null;
		};
	}

	public static ModelBloodLustArmor<LivingEntity> getHelmArmor(EquipmentSlot armorSlot,
			EnumBloodLustMaskTypes maskType) {
		return switch (maskType) {
		case NONE -> helmet;
		case TENGU -> tengu;
		case HORNED -> horned;
		default -> null;
		};
	}

}
