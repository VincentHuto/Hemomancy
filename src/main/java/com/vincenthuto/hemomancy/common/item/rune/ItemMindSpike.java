package com.vincenthuto.hemomancy.common.item.rune;

import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemMindSpike extends Item {

	/** Rune slots 1–4 hold regular (non-fungal) runes. */
	private static final int RUNE_SLOT_MIN = 1;
	private static final int RUNE_SLOT_MAX = 4;

	public ItemMindSpike(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide) {
			player.getCapability(RunesCapabilities.RUNES).ifPresent((IRunesItemHandler runes) -> {
				boolean wasUnlocked = runes.isRunesUnlocked();
				runes.setRunesUnlocked(!wasUnlocked);

				if (!wasUnlocked) {
					player.displayClientMessage(
							Component.literal("The spike pierces your mind... Rune slots are now ")
									.withStyle(ChatFormatting.DARK_PURPLE)
									.append(Component.literal("open").withStyle(ChatFormatting.GREEN,
											ChatFormatting.BOLD)),
							false);
				} else {
					// Drop any equipped runes from RUNE-type slots before locking
					for (int i = RUNE_SLOT_MIN; i <= RUNE_SLOT_MAX; i++) {
						ItemStack runeStack = runes.getStackInSlot(i);
						if (!runeStack.isEmpty()) {
							player.drop(runeStack.copy(), false);
							runes.setStackInSlot(i, ItemStack.EMPTY);
						}
					}

					player.displayClientMessage(
							Component.literal("Your mind closes... Rune slots are now ")
									.withStyle(ChatFormatting.DARK_PURPLE)
									.append(Component.literal("sealed").withStyle(ChatFormatting.RED,
											ChatFormatting.BOLD)),
							false);
				}
			});
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
