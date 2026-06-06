package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.CellHandFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingArsenalInventoryGuard;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ConjurationManip extends BloodManipulation {

	DeferredHolder<Item, Item> item;

	public ConjurationManip(String name,DeferredHolder<Item, Item> item, double cost, double alignLevel, double xpCost,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, EnumManipulationType.QUICK, rank, tendency, section);
		this.item = item;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (CellHandFormHelper.applySelection(player, this)
				&& CellHandFormHelper.isCellHandManip(getName())
				&& CellHandFormHelper.isCellHandForm(player.getMainHandItem())) {
			return;
		}
		if (heldItemMainhand.isEmpty()) {
			if (item.get() == ItemInit.living_staff.get()
					&& LivingArsenalInventoryGuard.summonOrRecoverStaff(player, heldItemMainhand)) {
				return;
			}
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item.get()));
		}
	}

	public 	DeferredHolder<Item, Item> getItem() {
		return item;
	}
}
