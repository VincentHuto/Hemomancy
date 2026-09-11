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
        if(LivingStaffWeaponFormHelper.applySelection(player, this) && !world.isClientSide) {
            var form=getTend()==EnumBloodTendency.ANIMUS?com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.ANIMUS_CONJURE:
                    getTend()==EnumBloodTendency.MORTEM?com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.MORTEM_CONJURE:
                    getName().equals("conjure_torch")?com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.FLAME_CONJURE:
                    getName().equals("conjure_flail")?com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.FROST_CONJURE:
                    getSecondaryTend()==EnumBloodTendency.FERRIC?com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.FERRIC_CONJURE:null;
            if(form!=null)com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.attached(player,form,.5,20,1);
        }
	}

	public DeferredHolder<Item, Item> getFormItem() {
		return formItem;
	}
}
