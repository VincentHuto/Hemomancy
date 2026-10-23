package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstDrawsAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.encounter.MycophantEncounterManager;
import com.vincenthuto.hemomancy.common.encounter.VesperOrdealManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

/**
 * Earned items that wait until they can really be handed over (the AGENTS.md documentation contract). A claim is
 * delivered only to a living player, never during an observational Chamber visit, and only what actually lands in the
 * inventory counts: a Creative discard is not a delivery. Whatever did not fit stays owed for the next try, on the
 * 20-tick driver, login or respawn. Each claim keeps its own NBT and its own rules through the functions it is built
 * from; this class owns the order of the checks and the driver.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PendingDelivery {
	/**
	 * One earned claim.
	 *
	 * @param owed   the stacks still owed, read from the claim's own storage; empty when nothing is owed
	 * @param gate   the claim's own conditions, checked after the shared alive and observational ones
	 * @param held   true when what the player already carries settles the claim without inserting anything
	 * @param settle stores what is still owed after a delivery that moved something; an empty list settles the claim
	 */
	public record Claim(String name, Function<ServerPlayer, List<ItemStack>> owed, Predicate<ServerPlayer> gate,
			Predicate<ServerPlayer> held, BiConsumer<ServerPlayer, List<ItemStack>> settle) {
		public Claim(String name, Function<ServerPlayer, List<ItemStack>> owed, Predicate<ServerPlayer> gate,
				BiConsumer<ServerPlayer, List<ItemStack>> settle) {
			this(name, owed, gate, player -> false, settle);
		}
	}

	/** What one attempt left owed, entry by entry, and whether it delivered anything. */
	record Settlement(int[] remaining, boolean moved) {
	}

	/** Every claim the driver retries, in delivery order. */
	private static final class Claims {
		static final List<Claim> ALL = List.of(FirstDrawsAssignment.VIALS, FirstSeparationAssignment.TOOLS,
				FirstSeparationAssignment.BRIEFING_VIALS, MycophantEncounterManager.TENDRIL, VesperOrdealManager.MEMORY,
				QliphothBloomEvents.FUNGAL_SPINE);
	}

	private PendingDelivery() {
	}

	public static List<Claim> claims() {
		return Claims.ALL;
	}

	/** Tries to deliver one claim now. Returns true when this attempt settled it. */
	public static boolean deliver(ServerPlayer player, Claim claim) {
		List<ItemStack> owed = claim.owed().apply(player);
		if (owed.isEmpty()) return false;
		int[] counts = owed.stream().mapToInt(ItemStack::getCount).toArray();
		boolean deliverable = player.isAlive() && !ChamberVisitService.isObservational(player) && claim.gate().test(player);
		Settlement settlement = settle(deliverable, () -> claim.held().test(player), counts,
				index -> insert(player, owed.get(index)));
		if (!settlement.moved()) return false;
		List<ItemStack> left = new ArrayList<>();
		for (int i = 0; i < owed.size(); i++) {
			if (settlement.remaining()[i] > 0) left.add(owed.get(i).copyWithCount(settlement.remaining()[i]));
		}
		claim.settle().accept(player, left);
		return left.isEmpty();
	}

	public static void deliverAll(ServerPlayer player) {
		for (Claim claim : claims()) deliver(player, claim);
	}

	/**
	 * Inserts a copy of {@code stack} and returns how many of its item actually landed. A Creative player's full
	 * inventory swallows the stack and reports success; the count does not move, so nothing landed.
	 */
	public static int insert(Player player, ItemStack stack) {
		Inventory inventory = player.getInventory();
		return landed(() -> inventory.countItem(stack.getItem()), () -> inventory.add(stack.copy()));
	}

	/** The rule itself, free of game state. {@code landed} answers how many of entry {@code i} reached the inventory. */
	static Settlement settle(boolean deliverable, BooleanSupplier held, int[] owed, IntUnaryOperator landed) {
		if (!deliverable) return new Settlement(owed.clone(), false);
		if (held.getAsBoolean()) return new Settlement(new int[owed.length], true);
		int[] remaining = new int[owed.length];
		boolean moved = false;
		for (int i = 0; i < owed.length; i++) {
			int got = Math.max(0, Math.min(owed[i], landed.applyAsInt(i)));
			remaining[i] = owed[i] - got;
			moved |= got > 0;
		}
		return new Settlement(remaining, moved);
	}

	/** How far {@code count} rose across {@code add}; never negative. */
	static int landed(IntSupplier count, Runnable add) {
		int before = count.getAsInt();
		add.run();
		return Math.max(0, count.getAsInt() - before);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) deliverAll(player);
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) deliverAll(player);
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) deliverAll(player);
	}
}
