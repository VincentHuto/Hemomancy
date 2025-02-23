package com.vincenthuto.hemomancy.compat.mna.item;

import com.mna.items.armor.DyeableMageArmor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class DyeableLivingThreadArmor extends DyeableMageArmor {

	public DyeableLivingThreadArmor(ArmorMaterial armorMaterial, Type slot, Properties properties,
			float manaPerRepairTick, float repairPerTick) {
		super(armorMaterial, slot, properties, manaPerRepairTick, repairPerTick);
	}

	@Override
	public int getColor(ItemStack stack) {
		CompoundTag compoundnbt = stack.getTagElement("display");
		return compoundnbt != null && compoundnbt.contains("color", 99) ? compoundnbt.getInt("color") :5505029;
	}

}
