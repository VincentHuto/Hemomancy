package com.vincenthuto.hemomancy.common.mission.shared;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PendingDelivery;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

/**
 * One-time NPC rewards and the initiation kit (PT-3, PT-16). The exact stacks are owed before the reward's one-time
 * flag is recorded, and the {@link PendingDelivery} claim hands them over: to a living player, outside an observational
 * Chamber visit, counting only what really lands. Nothing is dropped at the NPC or at the player's feet.
 */
public final class OwedRewards {
	public static final PendingDelivery.Claim CLAIM = new PendingDelivery.Claim("owed_rewards",
			player -> HemoCapabilityAccess.hasOwedRewards(player) ? HemoCapabilityAccess.owedRewards(player).owed()
					: List.of(),
			player -> true, (player, left) -> HemoCapabilityAccess.owedRewards(player).settle(left));

	private OwedRewards() {
	}

	/** Records {@code stacks} as owed. Call it before the reward's one-time flag, then {@link #deliver}. */
	public static void owe(ServerPlayer player, List<ItemStack> stacks) {
		HemoCapabilityAccess.owedRewards(player).park(stacks);
	}

	/** Hands over what fits now. What is left stays owed for the next try, and a living player is told to make room. */
	public static void deliver(ServerPlayer player) {
		PendingDelivery.deliver(player, CLAIM);
		if (player.isAlive() && !ChamberVisitService.isObservational(player) && !CLAIM.owed().apply(player).isEmpty()) {
			player.displayClientMessage(Component.translatable("message.hemomancy.owed_rewards.make_room")
					.withStyle(ChatFormatting.GRAY), false);
		}
	}

	/** True while the player carries {@code item} anywhere in their inventory or is still owed one. */
	public static boolean carriesOrOwed(ServerPlayer player, Item item) {
		return carriesOrOwed(player, stack -> stack.is(item));
	}

	/** True while the player carries a matching stack anywhere in their inventory or is still owed one. */
	public static boolean carriesOrOwed(ServerPlayer player, Predicate<ItemStack> match) {
		return player.getInventory().contains(match) || CLAIM.owed().apply(player).stream().anyMatch(match);
	}

	/** Owes {@code stacks} and hands over what fits now. For a reward with no one-time flag of its own. */
	public static void give(ServerPlayer player, List<ItemStack> stacks) {
		owe(player, stacks);
		deliver(player);
	}
}
