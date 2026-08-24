package com.vincenthuto.hemomancy.common.mission.unstained;

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
import net.minecraft.world.level.block.Blocks;

/** Server-authoritative NPC-directed assignments for the Unstained path. */
public final class UnstainedObservances {
	public enum Issuer {
		ACOLYTE("acolyte"),
		ZEALOT("zealot"),
		GUARDIAN("guardian");

		private final String id;
		Issuer(String id) { this.id = id; }
		public String id() { return id; }
	}

	public enum Observance {
		GATHER_GHOST_PIPE(0, "acolyte_task_gather_ghost_pipe", Issuer.ACOLYTE),
		WEAVE_WREATH(1, "acolyte_task_wreath", Issuer.ACOLYTE),
		PREPARE_HEMOLYTIC(2, "acolyte_task_hemolytic", Issuer.ACOLYTE),
		CONSECRATE_COPPER(3, "acolyte_task_consecrate", Issuer.ACOLYTE),
		OFFER_CHALICE(4, "acolyte_task_chalice", Issuer.ACOLYTE),
		CONDENSE_STILL_WATERS(5, "zealot_task_still_waters", Issuer.ZEALOT),
		BEAR_PALLID_ICON(6, "zealot_task_pallid_icon", Issuer.ZEALOT),
		PLATE_THE_WARD(7, "guardian_task_plating", Issuer.GUARDIAN),
		RING_THE_PALE_WATCH(8, "guardian_task_bell", Issuer.GUARDIAN),
		NOVITIATE_GATHER_REMEDIES(9, "novitiate_gather_remedies", Issuer.ACOLYTE),
		NOVITIATE_GENTLE_SEPARATION(10, "novitiate_gentle_separation", Issuer.ACOLYTE),
		NOVITIATE_STILLWATER_LABOR(11, "novitiate_stillwater_labor", Issuer.ZEALOT),
		NOVITIATE_CLEAN_LABOR(12, "novitiate_clean_labor", Issuer.ZEALOT),
		NOVITIATE_SHELTER_AFFLICTED(13, "novitiate_shelter_afflicted", Issuer.GUARDIAN);

		private final int bit;
		private final String eventId;
		private final Issuer issuer;
		Observance(int bit, String eventId, Issuer issuer) {
			this.bit = bit;
			this.eventId = eventId;
			this.issuer = issuer;
		}
		public int mask() { return 1 << bit; }
		public String eventId() { return eventId; }
		public Issuer issuer() { return issuer; }
		public String translation(String suffix) {
			return "screen.hemomancy.book_of_observances.observance."
					+ name().toLowerCase(java.util.Locale.ROOT) + "." + suffix;
		}
		public static Observance fromEventId(String id) {
			for (Observance value : values()) if (value.eventId.equals(id)) return value;
			return null;
		}
	}

	private UnstainedObservances() {}

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
		if (isNovitiate(observance)) {
			if (progress.hasBegunPurification() || progress.isBaselineRestored() || progress.hasClarityUnlocked()) return false;
			return switch (observance) {
				case NOVITIATE_GATHER_REMEDIES -> true;
				case NOVITIATE_GENTLE_SEPARATION -> claimed(progress, Observance.NOVITIATE_GATHER_REMEDIES);
				case NOVITIATE_STILLWATER_LABOR -> claimed(progress, Observance.NOVITIATE_GENTLE_SEPARATION);
				case NOVITIATE_CLEAN_LABOR -> claimed(progress, Observance.NOVITIATE_STILLWATER_LABOR);
				case NOVITIATE_SHELTER_AFFLICTED -> claimed(progress, Observance.NOVITIATE_CLEAN_LABOR);
				default -> false;
			};
		}
		if (!progress.hasBegunPurification()) return false;
		return switch (observance) {
			case GATHER_GHOST_PIPE -> true;
			case WEAVE_WREATH, PREPARE_HEMOLYTIC -> progress.getPurity() >= 25f;
			case CONSECRATE_COPPER -> progress.isPurified();
			case OFFER_CHALICE -> progress.hasClarityUnlocked();
			case CONDENSE_STILL_WATERS, PLATE_THE_WARD -> progress.getPurity() >= 50f;
			case BEAR_PALLID_ICON -> progress.getPurity() >= 75f;
			case RING_THE_PALE_WATCH -> progress.hasClarityUnlocked() && progress.getClarity() >= 50f;
			default -> false;
		};
	}

	public static boolean isReady(ServerPlayer player, Observance observance) {
		return switch (observance) {
			case GATHER_GHOST_PIPE -> has(player, BlockInit.ghost_pipe.get().asItem(), 4);
			case WEAVE_WREATH -> has(player, BlockInit.lethean_poppy_wreath.get().asItem(), 1);
			case PREPARE_HEMOLYTIC -> has(player, ItemInit.hemolytic_solution.get(), 2);
			case CONSECRATE_COPPER -> has(player, ItemInit.consecrated_copper_ingot.get(), 4);
			case OFFER_CHALICE -> has(player, ItemInit.lethean_chalice.get(), 1);
			case CONDENSE_STILL_WATERS -> has(player, ItemInit.lethean_dew.get(), 4);
			case BEAR_PALLID_ICON -> has(player, ItemInit.pallid_icon.get(), 1);
			case PLATE_THE_WARD -> has(player, ItemInit.hemolytic_plating.get(), 4);
			case RING_THE_PALE_WATCH -> has(player, ItemInit.pale_silver_bell.get(), 1);
			case NOVITIATE_GATHER_REMEDIES -> has(player, BlockInit.ghost_pipe.get().asItem(), 4)
					&& has(player, BlockInit.lethean_poppy.get().asItem(), 4);
			case NOVITIATE_GENTLE_SEPARATION -> HemoCapabilityAccess.requireUnstainedProgress(player).isNovitiateRetortComplete();
			case NOVITIATE_STILLWATER_LABOR -> HemoCapabilityAccess.requireUnstainedProgress(player).getNovitiateDewProduced() >= 4;
			case NOVITIATE_CLEAN_LABOR -> HemoCapabilityAccess.requireUnstainedProgress(player).getNovitiateBlocksConsecrated() >= 8;
			case NOVITIATE_SHELTER_AFFLICTED -> HemoCapabilityAccess.requireUnstainedProgress(player).isNovitiateProtectionComplete();
		};
	}

	private static boolean consumeOffering(ServerPlayer player, Observance observance) {
		return switch (observance) {
			case GATHER_GHOST_PIPE -> consume(player, BlockInit.ghost_pipe.get().asItem(), 4);
			case WEAVE_WREATH -> consume(player, BlockInit.lethean_poppy_wreath.get().asItem(), 1);
			case PREPARE_HEMOLYTIC -> consume(player, ItemInit.hemolytic_solution.get(), 2);
			case CONSECRATE_COPPER -> consume(player, ItemInit.consecrated_copper_ingot.get(), 4);
			case OFFER_CHALICE -> consume(player, ItemInit.lethean_chalice.get(), 1);
			case CONDENSE_STILL_WATERS -> consume(player, ItemInit.lethean_dew.get(), 4);
			case BEAR_PALLID_ICON -> consume(player, ItemInit.pallid_icon.get(), 1);
			case PLATE_THE_WARD -> consume(player, ItemInit.hemolytic_plating.get(), 4);
			case RING_THE_PALE_WATCH -> consume(player, ItemInit.pale_silver_bell.get(), 1);
			case NOVITIATE_GATHER_REMEDIES -> {
				if (!has(player, BlockInit.ghost_pipe.get().asItem(), 4)
						|| !has(player, BlockInit.lethean_poppy.get().asItem(), 4)) yield false;
				consume(player, BlockInit.ghost_pipe.get().asItem(), 4);
				consume(player, BlockInit.lethean_poppy.get().asItem(), 4);
				yield true;
			}
			case NOVITIATE_GENTLE_SEPARATION, NOVITIATE_STILLWATER_LABOR,
					NOVITIATE_CLEAN_LABOR, NOVITIATE_SHELTER_AFFLICTED -> true;
		};
	}

	private static void applyReward(ServerPlayer player, IUnstainedProgress progress, Observance observance) {
		switch (observance) {
			case GATHER_GHOST_PIPE -> { progress.addPurity(4f); give(player, new ItemStack(ItemInit.lethean_dew.get(), 2)); }
			case WEAVE_WREATH -> { progress.addPurity(6f); give(player, new ItemStack(ItemInit.pale_distillate.get(), 2)); }
			case PREPARE_HEMOLYTIC -> { progress.addPurity(8f); give(player, new ItemStack(ItemInit.hemolytic_plating.get(), 2)); }
			case CONSECRATE_COPPER -> { progress.addClarity(5f); give(player, new ItemStack(ItemInit.pale_silver_ingot.get(), 2)); }
			case OFFER_CHALICE -> { progress.addClarity(8f); give(player, new ItemStack(ItemInit.pale_silver_ingot.get(), 4)); }
			case CONDENSE_STILL_WATERS -> { progress.addPurity(10f); give(player, new ItemStack(ItemInit.tears_of_silthmere.get(), 2)); }
			case BEAR_PALLID_ICON -> { progress.addPurity(12f); give(player, new ItemStack(ItemInit.pale_silver_ingot.get(), 3)); }
			case PLATE_THE_WARD -> { progress.addPurity(8f); give(player, new ItemStack(ItemInit.consecrated_copper_ingot.get(), 4)); }
			case RING_THE_PALE_WATCH -> { progress.addClarity(12f); give(player, new ItemStack(ItemInit.tears_of_silthmere.get(), 3)); }
			case NOVITIATE_GATHER_REMEDIES -> {
				give(player, new ItemStack(ItemInit.hemolytic_solution.get()));
				give(player, new ItemStack(ItemInit.pale_distillate.get()));
			}
			case NOVITIATE_GENTLE_SEPARATION -> {
				give(player, new ItemStack(ItemInit.consecrated_copper_ingot.get(), 6));
				give(player, new ItemStack(ItemInit.pale_silver_ingot.get()));
				give(player, new ItemStack(ItemInit.pale_distillate.get()));
			}
			case NOVITIATE_STILLWATER_LABOR -> {
				give(player, new ItemStack(ItemInit.consecrated_copper_ingot.get(), 8));
				give(player, new ItemStack(ItemInit.pale_silver_ingot.get(), 2));
			}
			case NOVITIATE_CLEAN_LABOR -> {
				give(player, new ItemStack(BlockInit.cleansed_stone.get(), 8));
				give(player, new ItemStack(Blocks.CALCITE, 16));
			}
			case NOVITIATE_SHELTER_AFFLICTED -> give(player, new ItemStack(ItemInit.consecrated_copper_ingot.get()));
		}
	}

	private static String requirement(Observance observance) {
		return switch (observance) {
			case GATHER_GHOST_PIPE -> "Bring four Ghost Pipe blooms.";
			case WEAVE_WREATH -> "Bring one Lethean Poppy Wreath.";
			case PREPARE_HEMOLYTIC -> "Bring two Hemolytic Solutions.";
			case CONSECRATE_COPPER -> "Bring four Consecrated Copper Ingots.";
			case OFFER_CHALICE -> "Bring one Lethean Chalice.";
			case CONDENSE_STILL_WATERS -> "Bring four measures of Lethean Dew.";
			case BEAR_PALLID_ICON -> "Bring one Pallid Icon.";
			case PLATE_THE_WARD -> "Bring four Hemolytic Platings.";
			case RING_THE_PALE_WATCH -> "Bring one Pale Silver Bell.";
			case NOVITIATE_GATHER_REMEDIES -> "Bring four Ghost Pipe blooms and four Lethean Poppies.";
			case NOVITIATE_GENTLE_SEPARATION -> "Distill Hemolytic Solution in a Pallid Retort.";
			case NOVITIATE_STILLWATER_LABOR -> "Produce four Lethean Dew in a Stillwater Condenser.";
			case NOVITIATE_CLEAN_LABOR -> "Consecrate eight blood-infested blocks.";
			case NOVITIATE_SHELTER_AFFLICTED -> "Complete a protective rite while sheltering another soul.";
		};
	}

	public static void beginNovitiate(ServerPlayer player) {
		handle(player, Observance.NOVITIATE_GATHER_REMEDIES);
		if (player.getInventory().items.stream().noneMatch(stack -> stack.is(ItemInit.liber_immaculatus.get()))) {
			give(player, new ItemStack(ItemInit.liber_immaculatus.get()));
		}
		giveJournal(player);
	}

	public static void recordRetortSolution(ServerPlayer player) {
		record(player, Observance.NOVITIATE_GENTLE_SEPARATION, progress -> progress.setNovitiateRetortComplete(true));
	}

	public static void recordDewProduced(ServerPlayer player, int amount) {
		record(player, Observance.NOVITIATE_STILLWATER_LABOR,
				progress -> progress.setNovitiateDewProduced(progress.getNovitiateDewProduced() + amount));
	}

	public static void recordConsecratedBlock(ServerPlayer player) {
		record(player, Observance.NOVITIATE_CLEAN_LABOR,
				progress -> progress.setNovitiateBlocksConsecrated(progress.getNovitiateBlocksConsecrated() + 1));
	}

	public static void recordProtection(ServerPlayer player) {
		record(player, Observance.NOVITIATE_SHELTER_AFFLICTED,
				progress -> progress.setNovitiateProtectionComplete(true));
	}

	private static void record(ServerPlayer player, Observance observance,
			java.util.function.Consumer<IUnstainedProgress> update) {
		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
			if ((progress.getAcceptedObservances() & observance.mask()) == 0
					|| (progress.getClaimedObservances() & observance.mask()) != 0) return;
			update.accept(progress);
			UnstainedProgressEvents.syncProgress(player, progress);
		});
	}

	private static boolean isNovitiate(Observance observance) {
		return observance.ordinal() >= Observance.NOVITIATE_GATHER_REMEDIES.ordinal();
	}

	private static boolean claimed(IUnstainedProgress progress, Observance observance) {
		return (progress.getClaimedObservances() & observance.mask()) != 0;
	}

	private static boolean consume(ServerPlayer player, Item item, int count) {
		if (!has(player, item, count)) return false;
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

	private static boolean has(ServerPlayer player, Item item, int count) {
		int found = 0;
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(item)) found += stack.getCount();
		}
		return found >= count;
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
