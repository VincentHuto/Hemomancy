package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.entity.projectile.DirectedBloodOrbEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class VespersLivingStaffItem extends LivingStaffItem {
	public VespersLivingStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.translatable("item.hemomancy.vespers_living_staff.tooltip"));
	}

	@Override
	public void summonDirectedOrb(Level worldIn, Player playerIn) {
		fireOrb(worldIn, playerIn, -8.0F, 1.05F, 0.75F);
		fireOrb(worldIn, playerIn, 0.0F, 1.25F, 0.55F);
		fireOrb(worldIn, playerIn, 8.0F, 1.05F, 0.75F);
	}

	private static void fireOrb(Level worldIn, Player playerIn, float yawOffset, float velocity, float inaccuracy) {
		DirectedBloodOrbEntity orb = new DirectedBloodOrbEntity(playerIn, false);
		orb.setPos(playerIn.getX() - 0.5D, playerIn.getY() + 0.6D, playerIn.getZ() - 0.5D);
		orb.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot() + yawOffset, 0.0F, velocity,
				inaccuracy);
		worldIn.addFreshEntity(orb);
	}
}
