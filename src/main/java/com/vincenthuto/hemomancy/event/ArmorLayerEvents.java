package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.model.armor.ModelBloodLustArmor;
import com.vincenthuto.hemomancy.model.armor.ModelBloodLustArmor.EnumBloodLustMaskTypes;
import com.vincenthuto.hemomancy.render.layer.LayerBloodGourd;
import com.vincenthuto.hutoslib.common.container.BannerSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class ArmorLayerEvents {

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

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			event.addSprite(new ResourceLocation(Hemomancy.MOD_ID, "entity/royal_guard_shield_base"));
			event.addSprite(new ResourceLocation(Hemomancy.MOD_ID, "entity/spiked_shield/model_spiked_shield"));

		}
		if (event.getAtlas().location() == InventoryMenu.BLOCK_ATLAS) {
			event.addSprite(BannerSlot.SLOT_BACKGROUND);
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
		EntityRenderer<? extends Player> render = event.getSkin(skinName);
		if (render instanceof LivingEntityRenderer livingRenderer) {
			livingRenderer.addLayer(new LayerBloodGourd<>(livingRenderer));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends LivingEntity, M extends HumanoidModel<T>, R extends LivingEntityRenderer<T, M>> void addLayerToEntity(
			EntityRenderersEvent.AddLayers event, EntityType<? extends T> entityType) {
		R renderer = event.getRenderer(entityType);
		if (renderer != null)
			renderer.addLayer(new LayerBloodGourd(renderer));
	}

	// For when the horn breaks it shows the custom animation
	public static void playHornAnimation() {
		LocalPlayer ent = Minecraft.getInstance().player;
		Minecraft.getInstance().particleEngine.createTrackingEmitter(ent, ParticleTypes.SOUL, 30);
		Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ItemInit.curved_horn.get()));
		Minecraft.getInstance().level.playLocalSound(ent.getX(), ent.getY(), ent.getZ(), SoundEvents.TOTEM_USE,
				ent.getSoundSource(), 1F, 0.6F, false);
	}

}
