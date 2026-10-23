package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Holds a Working rite's brazier offerings from activation until it settles:
 * consumed on completion, or returned with the planted staff through the
 * Cardinal recovery queue. Shared by station upgrades and Working crafts.
 */
public final class RiteOfferingEscrow {
	public static final String ESCROWED = "ESCROWED";
	public static final String CONSUMED = "CONSUMED";
	public static final String RETURNED = "RETURNED";

	private RiteOfferingEscrow() {
	}

	/** Starts an escrow ledger and returns the rite identity it is keyed by. */
	public static UUID open(CompoundTag state) {
		UUID identity = UUID.randomUUID();
		state.putUUID("RiteIdentity", identity);
		state.put("Offerings", new ListTag());
		state.putString("EscrowState", ESCROWED);
		return identity;
	}

	/** Whether each itinerary brazier still holds exactly the offering captured for it. */
	public static boolean itineraryPresent(ServerLevel level, List<ActiveCardinalRite.RiteOffering> offerings) {
		for (var offering : offerings) {
			if (!(level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier)
					|| offering.stack() == null
					|| !ItemStack.isSameItemSameComponents(offering.stack(), brazier.getOfferingForMatching())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Takes each itinerary offering out of its brazier into the ledger. A brazier
	 * that yields nothing stops the take; the caller then returns the escrow.
	 */
	public static boolean take(ServerLevel level, CompoundTag state, List<ActiveCardinalRite.RiteOffering> offerings) {
		ListTag escrow = state.getList("Offerings", Tag.TAG_COMPOUND);
		state.put("Offerings", escrow);
		UUID riteId = state.hasUUID("RiteIdentity") ? state.getUUID("RiteIdentity") : null;
		for (var offering : offerings) {
			if (!(level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier)) return false;
			ItemStack consumed = brazier.consumeForRite(false);
			if (consumed.isEmpty()) return false;
			escrow.add(consumed.save(level.registryAccess()));
			// The offering stays in view, untouchable, until the daemon takes it.
			brazier.showEscrowed(consumed, riteId);
		}
		return true;
	}

	/** The daemon has taken this brazier's escrowed offering: it no longer shows in the bowl. */
	public static void releaseShown(ServerLevel level, net.minecraft.core.BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) brazier.clearEscrowed();
	}

	/** Clears every bowl still showing this rite's escrowed offerings, however it ended. */
	public static void clearShown(ServerLevel level, ActiveCardinalRite rite) {
		for (var offering : rite.getOfferingItinerary()) {
			if (level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier
					&& brazier.escrowRite() != null && brazier.escrowRite().equals(identity(rite))) {
				brazier.clearEscrowed();
			}
		}
	}

	/** Whether a running rite still holds an escrow under this identity. */
	public static boolean isLive(ServerLevel level, UUID riteId) {
		if (riteId == null) return false;
		for (ActiveCardinalRite rite : CardinalRiteSavedData.get(level).getActiveRites().values()) {
			if (!rite.isEnded() && riteId.equals(identity(rite))) return true;
		}
		return false;
	}

	private static UUID identity(ActiveCardinalRite rite) {
		if (rite.craft().tag().hasUUID("RiteIdentity")) return rite.craft().tag().getUUID("RiteIdentity");
		return rite.upgrade().tag().hasUUID("RiteIdentity") ? rite.upgrade().tag().getUUID("RiteIdentity") : null;
	}

	public static boolean isEscrowed(CompoundTag state) {
		return ESCROWED.equals(state.getString("EscrowState"));
	}

	public static boolean isConsumed(CompoundTag state) {
		return CONSUMED.equals(state.getString("EscrowState"));
	}

	/** The offerings were spent on the rite's result; nothing remains to return. */
	public static void markConsumed(CompoundTag state) {
		state.putString("EscrowState", CONSUMED);
		state.remove("Offerings");
	}

	/**
	 * Returns every escrowed offering and the planted staff to the caster through
	 * the recovery queue, delivering at once when they are present and alive.
	 */
	public static void returnEscrow(ServerLevel level, ActiveCardinalRite rite, CompoundTag state) {
		UUID riteId = state.getUUID("RiteIdentity");
		state.putString("EscrowState", RETURNED);
		clearShown(level, rite);
		List<ItemStack> items = new ArrayList<>();
		ListTag escrow = state.getList("Offerings", Tag.TAG_COMPOUND);
		for (int i = 0; i < escrow.size(); i++) {
			items.add(ItemStack.parseOptional(level.registryAccess(), escrow.getCompound(i)));
		}
		ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
		if (!staff.isEmpty()) items.add(staff);
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level.getServer().overworld());
		saved.addRecovery(rite.getPlayerUUID(), riteId, items);
		ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
		if (owner != null && owner.level() == level && owner.isAlive()) saved.deliverRecovery(owner);
		CardinalRiteSavedData.get(level).setDirty();
	}

	/** Sends a settled rite's planted staff to the recovery queue. */
	public static void releaseStaffToRecovery(ServerLevel level, ActiveCardinalRite rite, UUID riteId) {
		ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
		if (!staff.isEmpty()) CardinalRiteSavedData.get(level.getServer().overworld())
				.addRecovery(rite.getPlayerUUID(), UUID.nameUUIDFromBytes(
						(riteId + ":staff").getBytes(StandardCharsets.UTF_8)), List.of(staff));
	}
}
