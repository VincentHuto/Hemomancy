package com.huto.hemomancy.item.tool.living;

import com.huto.hemomancy.entity.projectile.EntityBloodBolt;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemBloodBolt extends ArrowItem {

	public ItemBloodBolt(Properties builder) {
		super(builder);
	}

	@Override
	public EntityBloodBolt createArrow(Level worldIn, ItemStack stack, LivingEntity shooter) {
		EntityBloodBolt arrowentity = new EntityBloodBolt(worldIn, shooter);
		arrowentity.setPotionEffect(stack);
		return arrowentity;
	}
}
