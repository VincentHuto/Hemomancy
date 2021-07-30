package com.huto.hemomancy.item.tool;

import com.google.common.base.Supplier;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum EnumModArmorTiers implements ArmorMaterial {

	HEMATIC_IRON(Hemomancy.MOD_ID + ":hematic_iron", 37, new int[] { 3, 6, 8, 3 }, 15,
			SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 3.0F, 0.1F, () -> {
				return Ingredient.fromItems(ItemInit.hematic_iron_scrap.get());
			}),
	CHITINITE(Hemomancy.MOD_ID + ":chitinite", 37, new int[] { 3, 6, 8, 3 }, 15, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
			3.0F, 0.1F, () -> {
				return Ingredient.fromItems(ItemInit.chitinous_husk.get());
			}),
	CHITINITECHEST(Hemomancy.MOD_ID + ":chitinite_chest", 37, new int[] { 3, 6, 8, 3 }, 15,
			SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 3.0F, 0.1F, () -> {
				return Ingredient.fromItems(ItemInit.chitinous_husk.get());
			}),
	CHITINITEHELMET(Hemomancy.MOD_ID + ":chitinite_helmet", 37, new int[] { 3, 6, 8, 3 }, 15,
			SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 3.0F, 0.1F, () -> {
				return Ingredient.fromItems(ItemInit.chitinous_husk.get());
			});
	;

	private static final int[] MAX_DAMAGE_ARRAY = new int[] { 13, 15, 16, 11 };
	private final String name;
	private final int maxDamageFactor;
	private final int[] damageReductionAmountArray;
	private final int enchantability;
	private final SoundEvent soundEvent;
	private final float toughness;
	private final float field_234660_o_;
	private final LazyValue<Ingredient> repairMaterial;

	private EnumModArmorTiers(String name, int damgaeFactor, int[] armorVals, int ench, SoundEvent soundevent,
			float tough, float p_i231593_9_, Supplier<Ingredient> repairIng) {
		this.name = name;
		this.maxDamageFactor = damgaeFactor;
		this.damageReductionAmountArray = armorVals;
		this.enchantability = ench;
		this.soundEvent = soundevent;
		this.toughness = tough;
		this.field_234660_o_ = p_i231593_9_;
		this.repairMaterial = new LazyValue<>(repairIng);
	}

	public int getDurability(EquipmentSlotType slotIn) {
		return MAX_DAMAGE_ARRAY[slotIn.getIndex()] * this.maxDamageFactor;
	}

	public int getDamageReductionAmount(EquipmentSlotType slotIn) {
		return this.damageReductionAmountArray[slotIn.getIndex()];
	}

	public int getEnchantability() {
		return this.enchantability;
	}

	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}

	public Ingredient getRepairMaterial() {
		return this.repairMaterial.getValue();
	}

	@OnlyIn(Dist.CLIENT)
	public String getName() {
		return this.name;
	}

	public float getToughness() {
		return this.toughness;
	}

	public float getKnockbackResistance() {
		return this.field_234660_o_;
	}

}
