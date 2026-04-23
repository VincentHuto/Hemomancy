package com.vincenthuto.hemomancy.common.item.tool;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.util.LazyLoadedValue;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum EnumModToolTiers implements Tier {

	HEMATIC_IRON(5, 1024, 3.0F, 7.0F, 10, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> {
		return Ingredient.of(ItemInit.hematic_iron_scrap.get());
	}), CHITINITE(5, 1024, 10.0F, 5.0F, 25, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> {
		return Ingredient.of(ItemInit.chitinous_husk.get());
	}), LIVING(5, 1024, 10.0F, 5.0F, 25, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, () -> {
		return Ingredient.of(Items.ROTTEN_FLESH);
	}), UNSTAINED(4, 1024, 6.0F, 8.0F, 15, BlockTags.INCORRECT_FOR_DIAMOND_TOOL, () -> {
		return Ingredient.of(ItemInit.cleansed_blood_crystal_shard.get());
	});

	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final TagKey<Block> incorrectBlocksForDrops;
	private final LazyLoadedValue<Ingredient> repairMaterial;

	private EnumModToolTiers(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn,
			int enchantabilityIn, TagKey<Block> incorrectBlocksForDropsIn, Supplier<Ingredient> repairMaterialIn) {
		this.harvestLevel = harvestLevelIn;
		this.maxUses = maxUsesIn;
		this.efficiency = efficiencyIn;
		this.attackDamage = attackDamageIn;
		this.enchantability = enchantabilityIn;
		this.incorrectBlocksForDrops = incorrectBlocksForDropsIn;
		this.repairMaterial = new LazyLoadedValue<>(repairMaterialIn);
	}

	@Override
	public float getAttackDamageBonus() {
		return this.attackDamage;
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantability;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairMaterial.get();
	}

	@Override
	public TagKey<Block> getIncorrectBlocksForDrops() {
		return this.incorrectBlocksForDrops;
	}

	@Override
	public float getSpeed() {
		return this.efficiency;
	}

	@Override
	public int getUses() {
		return this.maxUses;
	}
}