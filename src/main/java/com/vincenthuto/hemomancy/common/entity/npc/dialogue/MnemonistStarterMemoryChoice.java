package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public enum MnemonistStarterMemoryChoice {
	BLOOD_SHOT("mnemonist_grant_crude_blood_shot", "blood_shot", "crude_memory_blood_shot"),
	BLOOD_RUSH("mnemonist_grant_crude_blood_rush", "blood_rush", "crude_memory_blood_rush"),
	DEADLY_GAZE("mnemonist_grant_crude_deadly_gaze", "deadly_gaze", "crude_memory_deadly_gaze");

	private final String eventId;
	private final String manipulationName;
	private final String itemName;

	public static final String CLAIM_KEY = "hemomancy.mnemonist_starter_memory_claimed";

	MnemonistStarterMemoryChoice(String eventId, String manipulationName, String itemName) {
		this.eventId = eventId;
		this.manipulationName = manipulationName;
		this.itemName = itemName;
	}

	public String eventId() {
		return eventId;
	}

	public String manipulationName() {
		return manipulationName;
	}

	public String itemName() {
		return itemName;
	}

	public static Optional<MnemonistStarterMemoryChoice> fromEventId(String eventId) {
		for (MnemonistStarterMemoryChoice choice : values()) {
			if (choice.eventId.equals(eventId)) {
				return Optional.of(choice);
			}
		}
		return Optional.empty();
	}

	public static boolean canClaim(int degree, boolean purifying, boolean clarity, boolean alreadyClaimed) {
		return degree >= 2 && !purifying && !clarity && !alreadyClaimed;
	}

	/** The one starter claim, kept under PlayerPersisted so a death or an End return does not offer it again. */
	public static boolean isClaimed(Player player) {
		return root(player).getCompound(Player.PERSISTED_NBT_TAG).getBoolean(CLAIM_KEY);
	}

	public static void markClaimed(Player player) {
		CompoundTag root = root(player);
		CompoundTag durable = root.getCompound(Player.PERSISTED_NBT_TAG);
		durable.putBoolean(CLAIM_KEY, true);
		root.put(Player.PERSISTED_NBT_TAG, durable);
	}

	/** The mnemonist_starter_claim migration: the claim was once a root key, which a death clone drops. */
	public static void migrateLegacyRootKey(ServerPlayer player) {
		CompoundTag root = root(player);
		if (!root.contains(CLAIM_KEY)) return;
		if (root.getBoolean(CLAIM_KEY)) markClaimed(player);
		root.remove(CLAIM_KEY);
	}

	private static CompoundTag root(Player player) {
		return player.getPersistentData();
	}
}
