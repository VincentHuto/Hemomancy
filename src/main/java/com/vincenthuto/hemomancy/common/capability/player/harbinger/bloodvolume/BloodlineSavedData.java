package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
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

	private final Map<UUID, Bloodline> bloodlines = new HashMap<>();

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
	 * Remove an entire bloodline from world data and return the removed entry.
	 */
	public Bloodline disbandBloodline(UUID bloodlineUUID) {
		Bloodline removed = bloodlines.remove(bloodlineUUID);
		if (removed != null) {
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

}
