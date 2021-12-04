package com.vincenthuto.hemomancy.capa.player.volume;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Bloodline {

	String name;
	float bloodVolume, maxBloodVolume;
	UUID leaderUUID, bloodlineUUID;
	List<UUID> playerUUIDS = new ArrayList<UUID>();
	public static Bloodline NOBLOODLINE = new Bloodline();

	public Bloodline() {
		this.name = "No Bloodline";
		this.bloodVolume = 0;
		this.maxBloodVolume = 0;
		this.leaderUUID = new UUID(0, 0);
		this.bloodlineUUID = UUID.randomUUID();
		this.playerUUIDS = new ArrayList<UUID>();
	}

	public Bloodline(String name, UUID leaderUUID, UUID bloodlineUUID, List<UUID> playerUUIDS) {
		this.name = name;
		this.bloodVolume = 0;
		this.maxBloodVolume = playerUUIDS.size() * 1000f;
		this.leaderUUID = leaderUUID;
		this.bloodlineUUID = bloodlineUUID;
		playerUUIDS.add(leaderUUID);
		this.playerUUIDS = playerUUIDS;
	}

	public CompoundTag serialize() {
		CompoundTag tag = new CompoundTag();
		tag.putString("name", name);
		tag.putUUID("leader", getLeaderUUID());
		tag.putUUID("bloodlineUUID", getBloodlineUUID());
		ListTag playerList = new ListTag();
		if (!playerUUIDS.isEmpty()) {
			for (int i = 0; i < playerUUIDS.size(); i++) {
				CompoundTag ply = new CompoundTag();
				ply.putUUID("player" + i, playerUUIDS.get(i));
				playerList.add(ply);
			}
		}
		tag.put("players", playerList);
		System.out.println(tag);
		return tag;
	}

	public static Bloodline deserialize(CompoundTag nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("name") && nbt.contains("leader") && nbt.contains("bloodlineUUID")
					&& nbt.contains("players")) {
				if (nbt.get("players")instanceof ListTag listtag) {
					List<UUID> playerUUIDS = new ArrayList<UUID>();
					if (!listtag.isEmpty()) {
						for (int i = 0; i < listtag.size(); i++) {
							if (listtag.get(i)instanceof CompoundTag comp) {
								comp.getUUID("player" + i);
								Bloodline line = new Bloodline(nbt.getString("name"), nbt.getUUID("leader"),
										nbt.getUUID("bloodlineUUID"), playerUUIDS);
								return line;
							}
						}
					} else {
						Bloodline line = new Bloodline(nbt.getString("name"), nbt.getUUID("leader"),
								nbt.getUUID("bloodlineUUID"), playerUUIDS);
						return line;
					}

				}
			}
		}
		return Bloodline.NOBLOODLINE;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getBloodVolume() {
		return bloodVolume;
	}

	public void setBloodVolume(float bloodVolume) {
		this.bloodVolume = bloodVolume;
	}

	public float getMaxBloodVolume() {
		return maxBloodVolume;
	}

	public void setMaxBloodVolume(float maxBloodVolume) {
		this.maxBloodVolume = maxBloodVolume;
	}

	public UUID getLeaderUUID() {
		return leaderUUID;
	}

	public void setLeaderUUID(UUID leaderUUID) {
		this.leaderUUID = leaderUUID;
	}

	public UUID getBloodlineUUID() {
		return bloodlineUUID;
	}

	public void setBloodlineUUID(UUID bloodlineUUID) {
		this.bloodlineUUID = bloodlineUUID;
	}

	public List<UUID> getPlayerUUIDS() {
		return playerUUIDS;
	}

	public void setPlayerUUIDS(List<UUID> playerUUIDS) {
		this.playerUUIDS = playerUUIDS;
	}

	public Player getLeader(Level level) {
		return level.getPlayerByUUID(leaderUUID);
	}

	public List<Player> getPlayers(Level level) {
		List<Player> players = new ArrayList<Player>();
		playerUUIDS.forEach(id -> {
			players.add(level.getPlayerByUUID(id));
		});
		return players;
	}

}
