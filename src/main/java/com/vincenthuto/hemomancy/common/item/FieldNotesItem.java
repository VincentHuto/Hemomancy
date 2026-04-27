package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class FieldNotesItem extends Item {
	public FieldNotesItem(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && MemoHelper.getRemainingMemos(stack) < MemoHelper.MAX_FIELD_NOTES_MEMOS) {
			ItemStack ink = findInk(player.getInventory());
			if (!ink.isEmpty()) {
				MemoHelper.refillFieldNotes(stack);
				if (!player.getAbilities().instabuild) {
					ink.shrink(1);
				}
				player.displayClientMessage(Component.translatable("message.hemomancy.memo.field_notes_refilled")
						.withStyle(ChatFormatting.DARK_RED), true);
				return InteractionResultHolder.success(stack);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.field_notes.tooltip.memos",
				MemoHelper.getMemoCount(stack), MemoHelper.getRemainingMemos(stack))
				.withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.hemomancy.field_notes.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return MemoHelper.getRemainingMemos(stack) < MemoHelper.MAX_FIELD_NOTES_MEMOS;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * MemoHelper.getRemainingMemos(stack) / MemoHelper.MAX_FIELD_NOTES_MEMOS);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return 0x8B1A1A;
	}

	private static ItemStack findInk(Inventory inventory) {
		for (ItemStack stack : inventory.items) {
			if (stack.is(ItemInit.hematic_field_ink.get())) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
}
