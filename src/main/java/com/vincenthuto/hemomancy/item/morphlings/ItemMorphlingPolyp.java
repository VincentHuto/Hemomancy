package com.vincenthuto.hemomancy.item.morphlings;

import com.vincenthuto.hemomancy.entity.item.EntityMorphlingPolypItem;
import com.vincenthuto.hemomancy.init.EntityInit;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemMorphlingPolyp extends Item {

	public ItemMorphlingPolyp(Properties prop) {
		super(prop);
		prop.stacksTo(1);
	}

	@Override
	public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
		EntityMorphlingPolypItem itementity = new EntityMorphlingPolypItem(EntityInit.morphling_polyp_item.get(), world,
				location.getX(), location.getY(), location.getZ(), itemstack);
		itementity.setPickUpDelay(40);
		itementity.setDeltaMovement(location.getDeltaMovement());
		return itementity;
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}
}
