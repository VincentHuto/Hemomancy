package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * World-level persistence for all bloodlines. Stored in the overworld's data
 * folder so that bloodlines are a global constant across dimensions and server
 * restarts.
 */
public class BloodlineSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_bloodlines";
	private static final SavedData.Factory<BloodlineSavedData> FACTORY =
			new SavedData.Factory<>(BloodlineSavedData::new, BloodlineSavedData::load, null);

	/** Petitions a line keeps waiting for its leader's answer (NQ-11-7). */
	public static final int MAX_PETITIONS = 16;

	private final Map<UUID, Bloodline> bloodlines = new HashMap<>();
	/** Per player: what happened to them while they were away, told and settled at login. Outlives the line. */
	private final Map<UUID, List<PendingNotice>> pendingNotices = new HashMap<>();
	/** Per line: each removed player and the overworld game time of the removal (PT-37). */
	private final Map<UUID, Map<UUID, Long>> removedMembers = new HashMap<>();
	/** Per line: waiting petitions, applicant to name, in the order they came (PT-35). */
	private final Map<UUID, LinkedHashMap<UUID, String>> petitions = new HashMap<>();

	public enum NoticeKind { EXPELLED, DISSOLVED, ADMITTED, REFUSED }

	public enum PetitionResult { ADDED, ALREADY_WAITING, FULL }

	/**
	 * A settlement owed to a player who was away: the line's name pieces (so it reads in their language), who did it,
	 * the blood share owed and whether their Throne bond goes. Kept out of {@link Bloodline#serialize()}, which every
	 * ledger also carries.
	 */
	public record PendingNotice(NoticeKind kind, UUID lineId, String founderName, String customName, String actor,
			float bloodOwed, boolean clearThrone) {

		public static PendingNotice of(NoticeKind kind, Bloodline line, String actor, float bloodOwed,
				boolean clearThrone) {
			return new PendingNotice(kind, line.getBloodlineUUID(), line.getFounderName(), line.getCustomName(), actor,
					bloodOwed, clearThrone);
		}

		public Component lineName() {
			return Bloodline.displayName(true, founderName, customName);
		}

		public CompoundTag save() {
			CompoundTag tag = new CompoundTag();
			tag.putString("kind", kind.name());
			tag.putUUID("line", lineId);
			tag.putString("founder", founderName);
			if (!customName.isEmpty()) tag.putString("customName", customName);
			tag.putString("actor", actor);
			if (bloodOwed > 0) tag.putFloat("owed", bloodOwed);
			if (clearThrone) tag.putBoolean("clearThrone", true);
			return tag;
		}

		@Nullable
		public static PendingNotice load(CompoundTag tag) {
			if (!tag.hasUUID("line")) return null;
			NoticeKind kind;
			try {
				kind = NoticeKind.valueOf(tag.getString("kind"));
			} catch (IllegalArgumentException unknown) {
				return null;
			}
			return new PendingNotice(kind, tag.getUUID("line"), tag.getString("founder"), tag.getString("customName"),
					tag.getString("actor"), tag.getFloat("owed"), tag.getBoolean("clearThrone"));
		}
	}

	public BloodlineSavedData() {
	}

	public static BloodlineSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static BloodlineSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		BloodlineSavedData data = new BloodlineSavedData();
		if (tag.contains("bloodlines", Tag.TAG_LIST)) {
			ListTag list = tag.getList("bloodlines", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				Bloodline line = Bloodline.deserialize(entry);
				if (line.isValid()) {
					data.bloodlines.put(line.getBloodlineUUID(), line);
				}
			}
		}
		ListTag notices = tag.getList("pendingNotices", Tag.TAG_COMPOUND);
		for (int i = 0; i < notices.size(); i++) {
			CompoundTag entry = notices.getCompound(i);
			if (!entry.hasUUID("player")) continue;
			ListTag owed = entry.getList("notices", Tag.TAG_COMPOUND);
			for (int j = 0; j < owed.size(); j++) {
				PendingNotice notice = PendingNotice.load(owed.getCompound(j));
				if (notice != null) {
					data.pendingNotices.computeIfAbsent(entry.getUUID("player"), id -> new ArrayList<>()).add(notice);
				}
			}
		}
		ListTag removed = tag.getList("removedMembers", Tag.TAG_COMPOUND);
		for (int i = 0; i < removed.size(); i++) {
			CompoundTag entry = removed.getCompound(i);
			if (!entry.hasUUID("line") || !entry.hasUUID("player")) continue;
			data.removedMembers.computeIfAbsent(entry.getUUID("line"), id -> new HashMap<>())
					.put(entry.getUUID("player"), entry.getLong("time"));
		}
		ListTag waiting = tag.getList("petitions", Tag.TAG_COMPOUND);
		for (int i = 0; i < waiting.size(); i++) {
			CompoundTag entry = waiting.getCompound(i);
			if (!entry.hasUUID("line") || !entry.hasUUID("player")) continue;
			data.petitions.computeIfAbsent(entry.getUUID("line"), id -> new LinkedHashMap<>())
					.put(entry.getUUID("player"), entry.getString("name"));
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Bloodline line : bloodlines.values()) {
			list.add(line.serialize());
		}
		tag.put("bloodlines", list);
		ListTag notices = new ListTag();
		pendingNotices.forEach((player, owed) -> {
			if (owed.isEmpty()) return;
			CompoundTag entry = new CompoundTag();
			entry.putUUID("player", player);
			ListTag each = new ListTag();
			owed.forEach(notice -> each.add(notice.save()));
			entry.put("notices", each);
			notices.add(entry);
		});
		tag.put("pendingNotices", notices);
		ListTag removed = new ListTag();
		removedMembers.forEach((line, players) -> players.forEach((player, time) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("line", line);
			entry.putUUID("player", player);
			entry.putLong("time", time);
			removed.add(entry);
		}));
		tag.put("removedMembers", removed);
		ListTag waiting = new ListTag();
		petitions.forEach((line, applicants) -> applicants.forEach((player, name) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("line", line);
			entry.putUUID("player", player);
			entry.putString("name", name);
			waiting.add(entry);
		}));
		tag.put("petitions", waiting);
		return tag;
	}

	/**
	 * Register a newly created bloodline. Called when the leader first signs the
	 * ledger.
	 */
	public void registerBloodline(Bloodline bloodline) {
		bloodlines.put(bloodline.getBloodlineUUID(), bloodline);
		setDirty();
	}

	/**
	 * Add a player to an existing bloodline by the bloodline's UUID. Returns the
	 * updated Bloodline, or null if not found.
	 */
	public Bloodline addMember(UUID bloodlineUUID, UUID playerUUID) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null) {
			line.addMember(playerUUID);
			setDirty();
		}
		return line;
	}

	/**
	 * Remove a player from their bloodline.
	 */
	public void removeMember(UUID bloodlineUUID, UUID playerUUID) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null) {
			line.removeMember(playerUUID);
			setDirty();
		}
	}

	/**
	 * Remove an entire bloodline from world data and return the removed entry. Its removal records and petitions go
	 * with it; settlements already owed to its members stay.
	 */
	public Bloodline disbandBloodline(UUID bloodlineUUID) {
		Bloodline removed = bloodlines.remove(bloodlineUUID);
		boolean records = removedMembers.remove(bloodlineUUID) != null | petitions.remove(bloodlineUUID) != null;
		if (removed != null || records) {
			setDirty();
		}
		return removed;
	}

	/**
	 * Get a bloodline by its UUID.
	 */
	public Bloodline getBloodline(UUID bloodlineUUID) {
		return bloodlines.get(bloodlineUUID);
	}

	/**
	 * Find which bloodline a player belongs to. Returns null if the player is not
	 * in any bloodline.
	 */
	public Bloodline getBloodlineForPlayer(UUID playerUUID) {
		for (Bloodline line : bloodlines.values()) {
			if (line.hasMember(playerUUID)) {
				return line;
			}
		}
		return null;
	}

	/** The line {@code playerUUID} leads, or null. */
	@Nullable
	public Bloodline getBloodlineLedBy(UUID playerUUID) {
		Bloodline line = getBloodlineForPlayer(playerUUID);
		return line != null && line.canManage(playerUUID) ? line : null;
	}

	/**
	 * Contribute blood to a bloodline's shared pool.
	 */
	public boolean contributeBlood(UUID bloodlineUUID, float amount) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null) {
			boolean result = line.contributeBlood(amount);
			if (result) {
				setDirty();
			}
			return result;
		}
		return false;
	}

	/**
	 * Draw blood from a bloodline's shared pool.
	 */
	public float drawBlood(UUID bloodlineUUID, float amount) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null) {
			float drawn = line.drawBlood(amount);
			if (drawn > 0) {
				setDirty();
			}
			return drawn;
		}
		return 0;
	}

	public Map<UUID, Bloodline> getAllBloodlines() {
		return bloodlines;
	}

	// ── Settlements owed to players who were away ──

	public void addNotice(UUID player, PendingNotice notice) {
		pendingNotices.computeIfAbsent(player, id -> new ArrayList<>()).add(notice);
		setDirty();
	}

	/** Takes every settlement owed to {@code player}, leaving none. */
	public List<PendingNotice> takeNotices(UUID player) {
		List<PendingNotice> owed = pendingNotices.remove(player);
		if (owed == null) return List.of();
		setDirty();
		return owed;
	}

	public List<PendingNotice> noticesFor(UUID player) {
		return List.copyOf(pendingNotices.getOrDefault(player, List.of()));
	}

	// ── Removed players and the ledgers that crumble for them ──

	public void recordRemoval(UUID lineId, UUID player, long gameTime) {
		removedMembers.computeIfAbsent(lineId, id -> new HashMap<>()).put(player, gameTime);
		setDirty();
	}

	@Nullable
	public Long removedAt(UUID lineId, UUID player) {
		Map<UUID, Long> removed = removedMembers.get(lineId);
		return removed == null ? null : removed.get(player);
	}

	/** A ledger of {@code lineId} held by {@code holder} crumbles when it was minted at or before their removal. */
	public boolean ledgerCrumbles(UUID lineId, UUID holder, long minted) {
		return crumbles(removedAt(lineId, holder), minted);
	}

	/** The stale-ledger rule (NQ-11-1 B): only a ledger written before the removal crumbles. */
	public static boolean crumbles(@Nullable Long removedAt, long minted) {
		return removedAt != null && minted <= removedAt;
	}

	// ── Petitions ──

	public PetitionResult addPetition(UUID lineId, UUID applicant, String name) {
		LinkedHashMap<UUID, String> waiting = petitions.computeIfAbsent(lineId, id -> new LinkedHashMap<>());
		if (waiting.containsKey(applicant)) return PetitionResult.ALREADY_WAITING;
		if (waiting.size() >= MAX_PETITIONS) return PetitionResult.FULL;
		waiting.put(applicant, name);
		setDirty();
		return PetitionResult.ADDED;
	}

	/** The line's waiting petitions, applicant to name, oldest first. */
	public Map<UUID, String> petitions(UUID lineId) {
		LinkedHashMap<UUID, String> waiting = petitions.get(lineId);
		return waiting == null ? Map.of() : Collections.unmodifiableMap(waiting);
	}

	public boolean removePetition(UUID lineId, UUID applicant) {
		LinkedHashMap<UUID, String> waiting = petitions.get(lineId);
		if (waiting == null || waiting.remove(applicant) == null) return false;
		if (waiting.isEmpty()) petitions.remove(lineId);
		setDirty();
		return true;
	}

	/** Drops {@code applicant}'s petition on every line, as when they join a line or leave the Harbinger path. */
	public List<UUID> dropPetitionsOf(UUID applicant) {
		List<UUID> lines = new ArrayList<>();
		for (UUID lineId : List.copyOf(petitions.keySet())) {
			if (removePetition(lineId, applicant)) lines.add(lineId);
		}
		return lines;
	}

	/**
	 * Recruit an NPC Harbinger into a bloodline. The NPC's entity UUID is added
	 * to the bloodline's NPC member list, increasing shared pool capacity
	 * without requiring a real player.
	 *
	 * @return the updated Bloodline, or null if the bloodline was not found.
	 */
	public Bloodline addNpcMember(UUID bloodlineUUID, UUID npcUUID) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null) {
			line.addNpcMember(npcUUID);
			setDirty();
		}
		return line;
	}

	public int drawNpcRiteReserve(UUID bloodlineUUID, UUID npcUUID, int amount, long gameTime) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line == null) return 0;
		int drawn = line.drawNpcRiteReserve(npcUUID, amount, gameTime);
		if (drawn > 0) setDirty();
		return drawn;
	}

	public Bloodline addNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType) {
		return addNpcMember(bloodlineUUID, npcUUID, npcType, null);
	}

	public Bloodline addNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType, String npcOutpost) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null && line.addNpcMember(npcUUID, npcType, npcOutpost)) {
			setDirty();
		}
		return line;
	}

	/**
	 * Remove a recruited NPC Harbinger from a bloodline.
	 */
	public void removeNpcMember(UUID bloodlineUUID, UUID npcUUID) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null) {
			line.removeNpcMember(npcUUID);
			setDirty();
		}
	}

	public void removeNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType) {
		removeNpcMember(bloodlineUUID, npcUUID, npcType, null);
	}

	public void removeNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType, String npcOutpost) {
		Bloodline line = bloodlines.get(bloodlineUUID);
		if (line != null && line.removeNpcMember(npcUUID, npcType, npcOutpost)) {
			setDirty();
		}
	}

}
