package com.vincenthuto.hemomancy.common.item.shared;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class CurorLensItem extends Item {
	public CurorLensItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.SPYGLASS;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return CurorLensRules.USE_DURATION_TICKS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.startUsingItem(hand);
		return InteractionResultHolder.consume(stack);
	}
}
