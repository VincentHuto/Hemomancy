package com.vincenthuto.hemomancy.common.capability.player.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Bloodline {

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

	public Bloodline() {
		this.name = "No Bloodline";
		this.bloodVolume = 0;
		this.maxBloodVolume = 0;
		this.leaderUUID = new UUID(0, 0);
		this.bloodlineUUID = new UUID(0, 0);
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
		this.maxBloodVolume = this.playerUUIDS.size() * 1000f;
	}

	public boolean isValid() {
		return !bloodlineUUID.equals(new UUID(0, 0));
	}

	public boolean hasMember(UUID playerUUID) {
		return playerUUIDS.contains(playerUUID);
	}

	public boolean addMember(UUID playerUUID) {
		if (!playerUUIDS.contains(playerUUID)) {
			playerUUIDS.add(playerUUID);
			this.maxBloodVolume = playerUUIDS.size() * 1000f;
			return true;
		}
		return false;
	}

	public boolean removeMember(UUID playerUUID) {
		if (playerUUIDS.remove(playerUUID)) {
			this.maxBloodVolume = playerUUIDS.size() * 1000f;
			return true;
		}
		return false;
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
