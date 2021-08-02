package com.huto.hemomancy.item.tool;

import java.util.function.Supplier;

import com.huto.hemomancy.init.ItemInit;

import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum EnumModToolTiers implements Tier {

	HEMATIC_IRON(5, 1024, 3.0F, 7.0F, 10, () -> {
		return Ingredient.of(ItemInit.hematic_iron_scrap.get());
	}), CHITINITE(5, 1024, 10.0F, 5.0F, 25, () -> {
		return Ingredient.of(ItemInit.chitinous_husk.get());
	}), LIVING(5, 1024, 10.0F, 5.0F, 25, () -> {
		return Ingredient.of(ItemInit.shred_of_animus.get());
	});

	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final LazyLoadedValue<Ingredient> repairMaterial;

	private EnumModToolTiers(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn,
			int enchantabilityIn, Supplier<Ingredient> repairMaterialIn) {
		this.harvestLevel = harvestLevelIn;
		this.maxUses = maxUsesIn;
		this.efficiency = efficiencyIn;
		this.attackDamage = attackDamageIn;
		this.enchantability = enchantabilityIn;
		this.repairMaterial = new LazyLoadedValue<>(repairMaterialIn);
	}

	public int getUses() {
		return this.maxUses;
	}

	public float getSpeed() {
		return this.efficiency;
	}

	public float getAttackDamageBonus() {
		return this.attackDamage;
	}

	public int getLevel() {
		return this.harvestLevel;
	}

	public int getEnchantmentValue() {
		return this.enchantability;
	}

	public Ingredient getRepairIngredient() {
		return this.repairMaterial.get();
	}
}