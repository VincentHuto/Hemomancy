package com.vincenthuto.hemomancy.common.item.armor;

import com.google.common.base.Supplier;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem.Type;
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
	BARBED(Hemomancy.MOD_ID + ":barbed", 37, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F,
			() -> {
				return Ingredient.of(ItemInit.chitinous_husk.get());
			}),
	UNSTAINED(Hemomancy.MOD_ID + ":unstained", 37, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F,
			0.1F, () -> {
				return Ingredient.of(ItemInit.chitinous_husk.get());
			}),
	BLOODLUST(Hemomancy.MOD_ID + ":blood_lust", 37, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F,
			0.1F, () -> {
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

	private EnumModArmorTiers(String name, int damageFactor, int[] armorVals, int ench, SoundEvent soundevent,
			float tough, float p_i231593_9_, Supplier<Ingredient> repairIng) {
		this.name = name;
		this.maxDamageFactor = damageFactor;
		this.damageReductionAmountArray = armorVals;
		this.enchantability = ench;
		this.soundEvent = soundevent;
		this.toughness = tough;
		this.knockbackResistance = p_i231593_9_;
		this.repairMaterial = new LazyLoadedValue<>(repairIng);
	}

	@Override
	public int getDefenseForType(Type p_267168_) {
		return this.damageReductionAmountArray[p_267168_.getSlot().getIndex()];
	}

	@Override
	public int getDurabilityForType(Type p_266807_) {
		return MAX_DAMAGE_ARRAY[p_266807_.getSlot().getIndex()] * this.maxDamageFactor;
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
	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getName() {
		return this.name;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairMaterial.get();
	}

	@Override
	public float getToughness() {
		return this.toughness;
	}

}
