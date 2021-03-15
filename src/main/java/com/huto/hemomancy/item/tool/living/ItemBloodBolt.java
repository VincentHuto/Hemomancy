package com.huto.hemomancy.item.tool.living;

import com.huto.hemomancy.entity.projectile.EntityBloodBolt;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBloodBolt extends ArrowItem {

	public ItemBloodBolt(Properties builder) {
		super(builder);
	}

	@Override
	public EntityBloodBolt createArrow(World worldIn, ItemStack stack, LivingEntity shooter) {
		EntityBloodBolt arrowentity = new EntityBloodBolt(worldIn, shooter);
		arrowentity.setPotionEffect(stack);
		return arrowentity;
	}
}
