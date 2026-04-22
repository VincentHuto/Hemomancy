package com.vincenthuto.hemomancy.common.item.scar;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScarsItemHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemMindSpike extends Item {

	/** scar slots 1–4 hold regular (non-fungal) scars. */
	private static final int SCAR_SLOT_MIN = 1;
	private static final int SCAR_SLOT_MAX = 4;

	public ItemMindSpike(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide) {
			HemoCapabilityAccess.getScars(player).ifPresent((IScarsItemHandler scars) -> {
				boolean wasUnlocked = scars.isScarsUnlocked();
				scars.setScarsUnlocked(!wasUnlocked);

				if (!wasUnlocked) {
					player.displayClientMessage(
							Component.literal("The spike pierces your mind... Scar slots are now ")
									.withStyle(ChatFormatting.DARK_PURPLE)
									.append(Component.literal("open").withStyle(ChatFormatting.GREEN,
											ChatFormatting.BOLD)),
							false);
				} else {
					// Drop any equipped scars from SCAR-type slots before locking
					for (int i = SCAR_SLOT_MIN; i <= SCAR_SLOT_MAX; i++) {
						ItemStack scarStack = scars.getStackInSlot(i);
						if (!scarStack.isEmpty()) {
							player.drop(scarStack.copy(), false);
							scars.setStackInSlot(i, ItemStack.EMPTY);
						}
					}

					player.displayClientMessage(
							Component.literal("Your mind closes... Scar slots are now ")
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
