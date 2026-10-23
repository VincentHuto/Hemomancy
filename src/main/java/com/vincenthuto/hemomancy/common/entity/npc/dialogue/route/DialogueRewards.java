package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Rewards shared by the dialogue routes: give or drop a stack, and count or take items.
 */
final class DialogueRewards {
	private DialogueRewards() {
	}

	static boolean hasItemCount(ServerPlayer player, Item item, int needed) {
		int count = 0;
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(item)) {
				count += stack.getCount();
				if (count >= needed) {
					return true;
				}
			}
		}
		return false;
	}

	static void consumeItemCount(ServerPlayer player, Item item, int amount) {
		int remaining = amount;
		for (ItemStack stack : player.getInventory().items) {
			if (remaining <= 0) {
				break;
			}
			if (stack.is(item)) {
				int consumed = Math.min(remaining, stack.getCount());
				stack.shrink(consumed);
				remaining -= consumed;
			}
		}
		player.getInventory().setChanged();
	}

	static void giveOrDropAtEntity(ServerPlayer player, int entityId, ItemStack stack) {
		Entity entity = player.level().getEntity(entityId);
		if (entity != null) {
			Vec3 pos = entity.position();
			ItemEntity drop = new ItemEntity(entity.level(), pos.x, pos.y + 0.5, pos.z, stack);
			if (entity.level().addFreshEntity(drop)) return;
		}
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
	}

	static void giveOrDrop(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack)) player.drop(stack, false);
	}
}
