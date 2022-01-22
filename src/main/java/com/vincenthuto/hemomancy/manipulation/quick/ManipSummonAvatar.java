package com.vincenthuto.hemomancy.manipulation.quick;

import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ManipSummonAvatar extends BloodManipulation {

	public ManipSummonAvatar(String name, double cost, double alignLevel, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		System.out.println("Summoned Avatar");

		if (player.isAddedToWorld()) {
			player.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent((manip) -> {
				manip.setAvatarActive(!manip.isAvatarActive());
				System.out.println(manip.getKnownManips());
				System.out.println(manip.getSelectedManip());
				System.out.println(manip.isAvatarActive());

			});

		}
	}

}
