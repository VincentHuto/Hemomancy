package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bloodline {

	public static final UUID INVALID_UUID = new UUID(0, 0);
	public static final float BLOOD_VOLUME_PER_MEMBER = 5000f;
	public static final float NPC_BLOOD_VOLUME_PER_MEMBER = 1000f;

	public static Bloodline NOBLOODLINE = new Bloodline();

	public static Bloodline deserialize(CompoundTag nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("name") && nbt.contains("leader") && nbt.contains("bloodlineUUID")
					&& nbt.contains("players")) {
				if (nbt.get("players") instanceof ListTag listtag) {
					List<UUID> playerUUIDS = new ArrayList<>();
					for (int i = 0; i < listtag.size(); i++) {
						if (listtag.get(i) instanceof CompoundTag comp) {
							playerUUIDS.add(comp.getUUID("player" + i));
						}
					}
					Bloodline line = new Bloodline(nbt.getString("name"), nbt.getUUID("leader"),
							nbt.getUUID("bloodlineUUID"), playerUUIDS);
					if (nbt.contains("sharedBloodVolume")) {
						line.setBloodVolume(nbt.getFloat("sharedBloodVolume"));
					}
					// Deserialize recruited NPC members
					if (nbt.contains("npcMembers")) {
						ListTag npcTag = nbt.getList("npcMembers", 10);
						for (int i = 0; i < npcTag.size(); i++) {
							if (npcTag.get(i) instanceof CompoundTag comp) {
								line.npcMemberUUIDs.add(comp.getUUID("npc" + i));
								if (comp.contains("type" + i)) {
									ResourceLocation typeId = ResourceLocation.tryParse(comp.getString("type" + i));
									if (typeId != null && !line.npcMemberTypes.contains(typeId)) {
										line.npcMemberTypes.add(typeId);
									}
								}
							}
						}
						line.recalculateMaxVolume();
					}
					return line;
				}
			}
		}
		return Bloodline.NOBLOODLINE;
	}

	String name;
	float bloodVolume, maxBloodVolume;
	UUID leaderUUID, bloodlineUUID;

	List<UUID> playerUUIDS = new ArrayList<>();

	/**
	 * UUIDs of recruited NPC Harbingers that have pledged their blood to this
	 * bloodline. NPC members count toward the shared blood pool capacity
	 * ({@link #BLOOD_VOLUME_PER_MEMBER} per NPC) but never appear as online
	 * players. This allows single-player users to grow their pool without
	 * needing real multiplayer partners.
	 */
	List<UUID> npcMemberUUIDs = new ArrayList<>();
	List<ResourceLocation> npcMemberTypes = new ArrayList<>();

	public Bloodline() {
		this.name = "No Bloodline";
		this.bloodVolume = 0;
		this.maxBloodVolume = 0;
		this.leaderUUID = INVALID_UUID;
		this.bloodlineUUID = INVALID_UUID;
		this.playerUUIDS = new ArrayList<>();
	}

	public Bloodline(String name, UUID leaderUUID, UUID bloodlineUUID, List<UUID> playerUUIDS) {
		this.name = name;
		this.bloodVolume = 0;
		this.leaderUUID = leaderUUID;
		this.bloodlineUUID = bloodlineUUID;
		if (!playerUUIDS.contains(leaderUUID)) {
			playerUUIDS.add(leaderUUID);
		}
		this.playerUUIDS = playerUUIDS;
		this.maxBloodVolume = this.playerUUIDS.size() * BLOOD_VOLUME_PER_MEMBER
				+ npcMemberUUIDs.size() * NPC_BLOOD_VOLUME_PER_MEMBER;
	}

	public boolean isValid() {
		return !bloodlineUUID.equals(INVALID_UUID);
	}

	public boolean hasMember(UUID playerUUID) {
		return playerUUIDS.contains(playerUUID);
	}

	public boolean addMember(UUID playerUUID) {
		if (!playerUUIDS.contains(playerUUID)) {
			playerUUIDS.add(playerUUID);
			recalculateMaxVolume();
			return true;
		}
		return false;
	}

	public boolean removeMember(UUID playerUUID) {
		if (playerUUIDS.remove(playerUUID)) {
			recalculateMaxVolume();
			return true;
		}
		return false;
	}

	// ── NPC Harbinger Recruitment ──

	/**
	 * Adds a recruited NPC Harbinger to this bloodline. The NPC's entity UUID
	 * is stored so that the same NPC cannot be recruited twice. Each NPC
	 * increases the shared pool capacity by {@link #NPC_BLOOD_VOLUME_PER_MEMBER}.
	 *
	 * @return {@code true} if the NPC was added, {@code false} if already a member.
	 */
	public boolean addNpcMember(UUID npcUUID) {
		if (!npcMemberUUIDs.contains(npcUUID)) {
			npcMemberUUIDs.add(npcUUID);
			recalculateMaxVolume();
			return true;
		}
		return false;
	}

	public boolean addNpcMember(UUID npcUUID, ResourceLocation npcType) {
		if (npcType != null && npcMemberTypes.contains(npcType)) {
			return false;
		}
		if (!addNpcMember(npcUUID)) {
			return false;
		}
		if (npcType != null) {
			npcMemberTypes.add(npcType);
		}
		return true;
	}

	/**
	 * Removes a recruited NPC Harbinger from this bloodline.
	 *
	 * @return {@code true} if the NPC was removed.
	 */
	public boolean removeNpcMember(UUID npcUUID) {
		if (npcMemberUUIDs.remove(npcUUID)) {
			recalculateMaxVolume();
			return true;
		}
		return false;
	}

	public boolean removeNpcMember(UUID npcUUID, ResourceLocation npcType) {
		boolean removed = removeNpcMember(npcUUID);
		if (removed && npcType != null) {
			npcMemberTypes.remove(npcType);
		}
		return removed;
	}

	/** Returns {@code true} if the given NPC entity UUID is already recruited. */
	public boolean hasNpcMember(UUID npcUUID) {
		return npcMemberUUIDs.contains(npcUUID);
	}

	public boolean hasNpcMemberType(ResourceLocation npcType) {
		return npcType != null && npcMemberTypes.contains(npcType);
	}

	/** Returns the number of recruited NPC Harbingers. */
	public int getNpcMemberCount() {
		return npcMemberUUIDs.size();
	}

	/** Returns the list of recruited NPC UUIDs. */
	public List<UUID> getNpcMemberUUIDs() {
		return npcMemberUUIDs;
	}

	public List<ResourceLocation> getNpcMemberTypes() {
		return npcMemberTypes;
	}

	/**
	 * Total member count including both real players and recruited NPC
	 * Harbingers. Used to calculate the shared blood pool capacity.
	 */
	public int getTotalMemberCount() {
		return playerUUIDS.size() + npcMemberUUIDs.size();
	}

	/** Recalculates max blood volume based on player and NPC member counts. */
	private void recalculateMaxVolume() {
		this.maxBloodVolume = playerUUIDS.size() * BLOOD_VOLUME_PER_MEMBER
				+ npcMemberUUIDs.size() * NPC_BLOOD_VOLUME_PER_MEMBER;
	}

	public boolean contributeBlood(float amount) {
		if (bloodVolume + amount <= maxBloodVolume) {
			bloodVolume += amount;
			return true;
		} else if (bloodVolume < maxBloodVolume) {
			bloodVolume = maxBloodVolume;
			return true;
		}
		return false;
	}

	public float drawBlood(float amount) {
		if (bloodVolume >= amount) {
			bloodVolume -= amount;
			return amount;
		} else {
			float drawn = bloodVolume;
			bloodVolume = 0;
			return drawn;
		}
	}

	public UUID getBloodlineUUID() {
		return bloodlineUUID;
	}

	public float getBloodVolume() {
		return bloodVolume;
	}

	public Player getLeader(Level level) {
		return level.getPlayerByUUID(leaderUUID);
	}

	public UUID getLeaderUUID() {
		return leaderUUID;
	}

	public float getMaxBloodVolume() {
		return maxBloodVolume;
	}

	public String getName() {
		return name;
	}

	public List<Player> getPlayers(Level level) {
		List<Player> players = new ArrayList<>();
		playerUUIDS.forEach(id -> {
			Player p = level.getPlayerByUUID(id);
			if (p != null) {
				players.add(p);
			}
		});
		return players;
	}

	public List<UUID> getPlayerUUIDS() {
		return playerUUIDS;
	}

	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putString("name", name);
		tag.putUUID("leader", getLeaderUUID());
		tag.putUUID("bloodlineUUID", getBloodlineUUID());
		tag.putFloat("sharedBloodVolume", bloodVolume);
		ListTag playerList = new ListTag();
		if (!playerUUIDS.isEmpty()) {
			for (int i = 0; i < playerUUIDS.size(); i++) {
				CompoundTag ply = new CompoundTag();
				ply.putUUID("player" + i, playerUUIDS.get(i));
				playerList.add(ply);
			}
		}
		tag.put("players", playerList);
		// Serialize recruited NPC members
		if (!npcMemberUUIDs.isEmpty()) {
			ListTag npcList = new ListTag();
			for (int i = 0; i < npcMemberUUIDs.size(); i++) {
				CompoundTag npc = new CompoundTag();
				npc.putUUID("npc" + i, npcMemberUUIDs.get(i));
				if (i < npcMemberTypes.size()) {
					npc.putString("type" + i, npcMemberTypes.get(i).toString());
				}
				npcList.add(npc);
			}
			tag.put("npcMembers", npcList);
		}
		return tag;
	}

	public void setBloodlineUUID(UUID bloodlineUUID) {
		this.bloodlineUUID = bloodlineUUID;
	}

	public void setBloodVolume(float bloodVolume) {
		this.bloodVolume = bloodVolume;
	}

	public void setLeaderUUID(UUID leaderUUID) {
		this.leaderUUID = leaderUUID;
	}

	public void setMaxBloodVolume(float maxBloodVolume) {
		this.maxBloodVolume = maxBloodVolume;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPlayerUUIDS(List<UUID> playerUUIDS) {
		this.playerUUIDS = playerUUIDS;
	}

}
