package com.vincenthuto.hemomancy.item.armor;

import com.google.common.base.Supplier;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.ItemInit;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum EnumModArmorTiers implements ArmorMaterial {

	HEMATIC_IRON(Hemomancy.MOD_ID + ":hematic_iron", 37, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ARMOR_EQUIP_GENERIC,
			3.0F, 0.1F, () -> {
				return Ingredient.of(ItemInit.hematic_iron_scrap.get());
			}),
	CHITINITE(Hemomancy.MOD_ID + ":chitinite", 37, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F,
			0.1F, () -> {
				return Ingredient.of(ItemInit.chitinous_husk.get());
			}),
	CHITINITECHEST(Hemomancy.MOD_ID + ":chitinite_chest", 37, new int[] { 3, 6, 8, 3 }, 15,
			SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F, () -> {
				return Ingredient.of(ItemInit.chitinous_husk.get());
			}),
	CHITINITEHELMET(Hemomancy.MOD_ID + ":chitinite_helmet", 37, new int[] { 3, 6, 8, 3 }, 15,
			SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F, () -> {
				return Ingredient.of(ItemInit.chitinous_husk.get());
			}),
	BARBEDCHEST(Hemomancy.MOD_ID + ":barbed_chestplate", 37, new int[] { 3, 6, 8, 3 }, 15,
			SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F, () -> {
				return Ingredient.of(ItemInit.chitinous_husk.get());
			}),
	BLOODLUST(Hemomancy.MOD_ID + ":blood_lust", 37, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ARMOR_EQUIP_GENERIC,
			3.0F, 0.1F, () -> {
				return Ingredient.of(ItemInit.hematic_iron_scrap.get());
			}),

	;

	private static final int[] MAX_DAMAGE_ARRAY = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int maxDamageFactor;
	private final int[] damageReductionAmountArray;
	private final int enchantability;
	private final SoundEvent soundEvent;
	private final float toughness;
	private final float knockbackResistance;
	private final LazyLoadedValue<Ingredient> repairMaterial;

	private EnumModArmorTiers(String name, int damgaeFactor, int[] armorVals, int ench, SoundEvent soundevent,
			float tough, float p_i231593_9_, Supplier<Ingredient> repairIng) {
		this.name = name;
		this.maxDamageFactor = damgaeFactor;
		this.damageReductionAmountArray = armorVals;
		this.enchantability = ench;
		this.soundEvent = soundevent;
		this.toughness = tough;
		this.knockbackResistance = p_i231593_9_;
		this.repairMaterial = new LazyLoadedValue<>(repairIng);
	}

	@Override
	public int getDurabilityForSlot(EquipmentSlot slotIn) {
		return MAX_DAMAGE_ARRAY[slotIn.getIndex()] * this.maxDamageFactor;
	}

	@Override
	public int getDefenseForSlot(EquipmentSlot slotIn) {
		return this.damageReductionAmountArray[slotIn.getIndex()];
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantability;
	}

	@Override
	public SoundEvent getEquipSound() {
		return this.soundEvent;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairMaterial.get();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getName() {
		return this.name;
	}

	@Override
	public float getToughness() {
		return this.toughness;
	}

	@Override
	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}

}
