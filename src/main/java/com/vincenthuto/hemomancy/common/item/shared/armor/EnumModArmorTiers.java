package com.vincenthuto.hemomancy.common.item.shared.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public enum EnumModArmorTiers {

	HEMATIC_IRON("hematic_iron", 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.hematic_iron_scrap.get())),
	MARROW_CROWN("marrow_crown", 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.hematic_iron_scrap.get())),
	CHITINITE("chitinite", 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.chitinous_husk.get())),
	BARBED("barbed", 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.calcified_blood_spine.get())),
	PRISMATIC("prismatic", 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,
			() -> Ingredient.of(Items.GLOW_INK_SAC)),
	UNSTAINED("unstained", 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.chitinous_husk.get())),
	BLOODLUST("blood_lust", 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.hematic_iron_scrap.get())),
	SILENT_ARCHON("silent_archon", 18, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.monolith_imbued_cloth.get())),
	CHALYBEATE_SCLERITE("chalybeate_sclerite", 12, SoundEvents.ARMOR_EQUIP_GENERIC, 4.0F, 0.1F,
			() -> Ingredient.of(ItemInit.chalybeate_sclerite.get())),
	COVENANT_MANTLE("covenant_mantle", 18, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F,
			() -> Ingredient.of(ItemInit.crimson_lacquer.get()));

	private final Holder<ArmorMaterial> holder;

	EnumModArmorTiers(String layerName, int enchantability, Holder<SoundEvent> equipSound,
			float toughness, float knockbackResistance, Supplier<Ingredient> repairIngredient) {
		ArmorMaterial material = new ArmorMaterial(
				createDefenseValues(),
				enchantability,
				equipSound,
				repairIngredient,
				List.of(new ArmorMaterial.Layer(Hemomancy.rloc(layerName))),
				toughness,
				knockbackResistance);
		this.holder = Holder.direct(material);
	}

	public Holder<ArmorMaterial> holder() {
		return this.holder;
	}

	private static Map<Type, Integer> createDefenseValues() {
		return new EnumMap<>(Map.of(
				Type.BOOTS, 3,
				Type.LEGGINGS, 8,
				Type.CHESTPLATE, 6,
				Type.HELMET, 3,
				Type.BODY, 0));
	}

}
