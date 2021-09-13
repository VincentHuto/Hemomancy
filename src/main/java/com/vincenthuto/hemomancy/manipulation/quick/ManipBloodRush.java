package com.vincenthuto.hemomancy.manipulation.quick;

import com.vincenthuto.hemomancy.capa.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ManipBloodRush extends BloodManipulation {

	public ManipBloodRush(String name, double cost, double alignLevel,double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		player.addEffect(new MobEffectInstance(PotionInit.blood_rush.get(), 250, 1));
	}

}
