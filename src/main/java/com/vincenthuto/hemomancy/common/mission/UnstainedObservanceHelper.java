package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative Acolyte assignments for the Unstained path. */
public final class UnstainedObservanceHelper {
	public enum Observance {
		GATHER_GHOST_PIPE(0, "acolyte_task_gather_ghost_pipe"),
		WEAVE_WREATH(1, "acolyte_task_wreath"),
		PREPARE_HEMOLYTIC(2, "acolyte_task_hemolytic"),
		CONSECRATE_COPPER(3, "acolyte_task_consecrate"),
		OFFER_CHALICE(4, "acolyte_task_chalice");

		private final int bit;
		private final String eventId;
		Observance(int bit, String eventId) { this.bit = bit; this.eventId = eventId; }
		public int mask() { return 1 << bit; }
		public String eventId() { return eventId; }
		public static Observance fromEventId(String id) {
			for (Observance value : values()) if (value.eventId.equals(id)) return value;
			return null;
		}
	}

	private UnstainedObservanceHelper() {}

	public static void handle(ServerPlayer player, Observance observance) {
		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
			if (!isAvailable(progress, observance)) {
				notify(player, "This observance belongs to a later step of the Unstained path.", ChatFormatting.GRAY);
				return;
			}
			if ((progress.getClaimedObservances() & observance.mask()) != 0) {
				notify(player, "You have already fulfilled this observance.", ChatFormatting.AQUA);
				return;
			}
			if ((progress.getAcceptedObservances() & observance.mask()) == 0) {
				progress.setAcceptedObservances(progress.getAcceptedObservances() | observance.mask());
				giveJournal(player);
				UnstainedProgressEvents.syncProgress(player, progress);
				notify(player, "Observance accepted. Consult the Book of Observances for its requirements.", ChatFormatting.AQUA);
				return;
			}
			if (!consumeOffering(player, observance)) {
				notify(player, requirement(observance), ChatFormatting.GRAY);
				return;
			}
			applyReward(player, progress, observance);
			progress.setClaimedObservances(progress.getClaimedObservances() | observance.mask());
			UnstainedProgressEvents.syncProgress(player, progress);
			notify(player, "Observance fulfilled. Your patient work leaves the blood quieter.", ChatFormatting.GREEN);
		});
	}

	public static boolean isAvailable(IUnstainedProgress progress, Observance observance) {
		if (!progress.hasBegunPurification()) return false;
		return switch (observance) {
			case GATHER_GHOST_PIPE -> true;
			case WEAVE_WREATH, PREPARE_HEMOLYTIC -> progress.getPurity() >= 25f;
			case CONSECRATE_COPPER -> progress.isPurified();
			case OFFER_CHALICE -> progress.hasClarityUnlocked();
		};
	}

	private static boolean consumeOffering(ServerPlayer player, Observance observance) {
		return switch (observance) {
			case GATHER_GHOST_PIPE -> consume(player, BlockInit.ghost_pipe.get().asItem(), 4);
			case WEAVE_WREATH -> consume(player, BlockInit.lethean_poppy_wreath.get().asItem(), 1);
			case PREPARE_HEMOLYTIC -> consume(player, ItemInit.hemolytic_solution.get(), 2);
			case CONSECRATE_COPPER -> consume(player, ItemInit.consecrated_copper_ingot.get(), 4);
			case OFFER_CHALICE -> consume(player, ItemInit.lethean_chalice.get(), 1);
		};
	}

	private static void applyReward(ServerPlayer player, IUnstainedProgress progress, Observance observance) {
		switch (observance) {
			case GATHER_GHOST_PIPE -> { progress.addPurity(4f); give(player, new ItemStack(ItemInit.lethean_dew.get(), 2)); }
			case WEAVE_WREATH -> { progress.addPurity(6f); give(player, new ItemStack(ItemInit.pale_distillate.get(), 2)); }
			case PREPARE_HEMOLYTIC -> { progress.addPurity(8f); give(player, new ItemStack(ItemInit.hemolytic_plating.get(), 2)); }
			case CONSECRATE_COPPER -> { progress.addClarity(5f); give(player, new ItemStack(ItemInit.pale_silver_ingot.get(), 2)); }
			case OFFER_CHALICE -> { progress.addClarity(8f); give(player, new ItemStack(ItemInit.pale_silver_ingot.get(), 4)); }
		}
	}

	private static String requirement(Observance observance) {
		return switch (observance) {
			case GATHER_GHOST_PIPE -> "Bring four Ghost Pipe blooms.";
			case WEAVE_WREATH -> "Bring one Lethean Poppy Wreath.";
			case PREPARE_HEMOLYTIC -> "Bring two Hemolytic Solutions.";
			case CONSECRATE_COPPER -> "Bring four Consecrated Copper Ingots.";
			case OFFER_CHALICE -> "Bring one Lethean Chalice.";
		};
	}

	private static boolean consume(ServerPlayer player, Item item, int count) {
		int found = 0;
		for (ItemStack stack : player.getInventory().items) if (stack.is(item)) found += stack.getCount();
		if (found < count) return false;
		int remaining = count;
		for (ItemStack stack : player.getInventory().items) {
			if (!stack.is(item)) continue;
			int taken = Math.min(remaining, stack.getCount());
			stack.shrink(taken);
			remaining -= taken;
			if (remaining == 0) break;
		}
		player.getInventory().setChanged();
		return true;
	}

	private static void giveJournal(ServerPlayer player) {
		boolean owns = player.getInventory().items.stream().anyMatch(stack -> stack.is(ItemInit.book_of_observances.get()));
		if (!owns) give(player, new ItemStack(ItemInit.book_of_observances.get()));
	}

	private static void give(ServerPlayer player, ItemStack stack) {
		if (!player.addItem(stack)) player.drop(stack, false);
	}

	private static void notify(ServerPlayer player, String message, ChatFormatting color) {
		player.displayClientMessage(Component.literal(message).withStyle(color), false);
	}
}
