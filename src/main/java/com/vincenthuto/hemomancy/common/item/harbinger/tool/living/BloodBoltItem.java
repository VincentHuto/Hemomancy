package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class BloodBoltItem extends ArrowItem {

	public BloodBoltItem(Properties builder) {
		super(builder);
	}

	@Override
	public BloodBoltEntity createArrow(Level worldIn, ItemStack stack, LivingEntity shooter, @Nullable ItemStack weaponStack) {
		BloodBoltEntity arrowentity = new BloodBoltEntity(worldIn, shooter);
		arrowentity.setPotionEffect(stack);
		return arrowentity;
	}
}
