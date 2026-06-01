package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

public class StaffWeaponFormManip extends BloodManipulation {
	private final DeferredHolder<Item, Item> formItem;

	public StaffWeaponFormManip(String name, DeferredHolder<Item, Item> formItem, double alignLevel,
			double xpCost, EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, 0, alignLevel, xpCost, EnumManipulationType.QUICK, rank, tendency, section);
		this.formItem = formItem;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		LivingStaffWeaponFormHelper.applySelection(player, this);
	}

	public DeferredHolder<Item, Item> getFormItem() {
		return formItem;
	}
}
