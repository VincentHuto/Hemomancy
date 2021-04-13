package com.huto.hemomancy.item.morphlings;

import com.huto.hemomancy.entity.EntityMorphlingPolypItem;
import com.huto.hemomancy.init.EntityInit;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMorphlingPolyp extends Item {

	public ItemMorphlingPolyp(Properties prop) {
		super(prop);
		prop.maxStackSize(1);
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack) {
		EntityMorphlingPolypItem itementity = new EntityMorphlingPolypItem(EntityInit.morphling_polyp_item.get(), world,
				location.getPosX(), location.getPosY(), location.getPosZ(), itemstack);
		itementity.setPickupDelay(40);
		itementity.setMotion(location.getMotion());
		return itementity;
	}
}
