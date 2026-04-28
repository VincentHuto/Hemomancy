package com.vincenthuto.hemomancy.common.item.tool;

import java.util.List;

import com.vincenthuto.hemomancy.common.util.FungalGardenTravelHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class FungalSpineItem extends Item {

	public FungalSpineItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
			FungalGardenTravelHelper.handleTravelUse(serverPlayer, this);
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("A calcified tendril from somewhere deep inside. It hums faintly, as if listening.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}
}

