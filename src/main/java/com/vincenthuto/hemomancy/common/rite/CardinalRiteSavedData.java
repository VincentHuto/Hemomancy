package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

/**
 * World-level SavedData that tracks all active cardinal rites.
 * Persists across server restarts so in-progress rites are not lost.
 */
public class CardinalRiteSavedData extends SavedData {

	private static final String DATA_NAME = Hemomancy.MOD_ID + "_cardinal_rites";
	private static final SavedData.Factory<CardinalRiteSavedData> FACTORY =
			new SavedData.Factory<>(CardinalRiteSavedData::new, CardinalRiteSavedData::load, null);
	private final Map<UUID, ActiveCardinalRite> activeRites = new HashMap<>();
	private final Map<UUID, Map<UUID, List<ItemStack>>> recovery = new HashMap<>();
	private final Set<UUID> settledRecovery = new HashSet<>();

	public CardinalRiteSavedData() {
	}

	public static CardinalRiteSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static CardinalRiteSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		CardinalRiteSavedData data = new CardinalRiteSavedData();
		ListTag list = tag.getList("ActiveRites", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			ActiveCardinalRite rite = ActiveCardinalRite.deserialize(list.getCompound(i), provider);
			data.activeRites.put(rite.getPlayerUUID(), rite);
		}
		ListTag claims = tag.getList("Recovery", Tag.TAG_COMPOUND);
		ListTag settled = tag.getList("SettledRecovery", Tag.TAG_STRING);
		for (int i = 0; i < settled.size(); i++) {
			try { data.settledRecovery.add(UUID.fromString(settled.getString(i))); }
			catch (IllegalArgumentException ignored) { }
		}
		for (int i = 0; i < claims.size(); i++) {
			CompoundTag claim = claims.getCompound(i);
			if (!claim.hasUUID("Player")) continue;
			List<ItemStack> items = new ArrayList<>();
			ListTag stacks = claim.getList("Items", Tag.TAG_COMPOUND);
			for (int j = 0; j < stacks.size(); j++) {
				ItemStack stack = ItemStack.parseOptional(provider, stacks.getCompound(j));
				if (!stack.isEmpty()) items.add(stack);
			}
			if (!items.isEmpty()) data.recovery.computeIfAbsent(claim.getUUID("Player"), ignored -> new HashMap<>())
					.put(claim.hasUUID("Rite") ? claim.getUUID("Rite") : UUID.randomUUID(), items);
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (ActiveCardinalRite rite : activeRites.values()) {
			list.add(rite.serialize(provider));
		}
		tag.put("ActiveRites", list);
		ListTag claims = new ListTag();
		for (var playerClaims : recovery.entrySet()) {
			for (var claim : playerClaims.getValue().entrySet()) {
				CompoundTag entry = new CompoundTag();
				entry.putUUID("Player", playerClaims.getKey());
				entry.putUUID("Rite", claim.getKey());
				ListTag items = new ListTag();
				for (ItemStack stack : claim.getValue()) if (!stack.isEmpty()) items.add(stack.save(provider));
				entry.put("Items", items);
				claims.add(entry);
			}
		}
		tag.put("Recovery", claims);
		ListTag settled = new ListTag();
		for (UUID rite : settledRecovery) settled.add(StringTag.valueOf(rite.toString()));
		tag.put("SettledRecovery", settled);
		return tag;
	}

	public void startRite(ActiveCardinalRite rite) {
		activeRites.put(rite.getPlayerUUID(), rite);
		setDirty();
	}

	public void removeRite(UUID playerUUID) {
		activeRites.remove(playerUUID);
		setDirty();
	}

	public ActiveCardinalRite getRite(UUID playerUUID) {
		return activeRites.get(playerUUID);
	}

	public boolean hasActiveRite(UUID playerUUID) {
		return activeRites.containsKey(playerUUID);
	}

	public boolean hasRiteAt(net.minecraft.core.BlockPos centerPos) {
		return activeRites.values().stream().anyMatch(rite -> rite.getCenterPos().equals(centerPos));
	}

	public Map<UUID, ActiveCardinalRite> getActiveRites() {
		return activeRites;
	}

	public void addRecovery(UUID player, UUID rite, List<ItemStack> items) {
		if (!settledRecovery.add(rite)) return;
		Map<UUID, List<ItemStack>> claims = recovery.computeIfAbsent(player, ignored -> new HashMap<>());
		List<ItemStack> pending = new ArrayList<>();
		for (ItemStack item : items) if (!item.isEmpty()) pending.add(item.copy());
		if (!pending.isEmpty()) claims.put(rite, pending);
		setDirty();
	}

	public void deliverRecovery(ServerPlayer player) {
		Map<UUID, List<ItemStack>> claims = recovery.get(player.getUUID());
		if (claims == null) return;
		for (List<ItemStack> pending : claims.values())
			pending.removeIf(stack -> {
				player.getInventory().add(stack);
				return stack.isEmpty();
			});
		claims.values().removeIf(List::isEmpty);
		if (claims.isEmpty()) recovery.remove(player.getUUID());
		setDirty();
	}
}
