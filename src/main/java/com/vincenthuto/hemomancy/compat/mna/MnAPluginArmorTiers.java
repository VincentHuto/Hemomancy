package com.vincenthuto.hemomancy.compat.mna;

import com.google.common.base.Supplier;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.item.MnAPluginItemInit;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public enum MnAPluginArmorTiers implements ArmorMaterial {

	LIVING_THREAD(Hemomancy.MOD_ID + ":living_thread", 37, new int[] { 3, 6, 8, 3 }, 15,
			SoundEvents.ARMOR_EQUIP_GENERIC, 3.0F, 0.1F, () -> {
				return Ingredient.of(MnAPluginItemInit.living_infused_thread.get());
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

	private MnAPluginArmorTiers(String name, int damageFactor, int[] armorVals, int ench, SoundEvent soundevent,
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
