package com.vincenthuto.hemomancy.common.capability.player.harbinger.will;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class WillAmbushState implements INBTSerializable<CompoundTag> {
	private static final String LAST_AMBUSH_GAME_TIME_KEY = "LastAmbushGameTime";
	private static final String HERALD_UNTIL_GAME_TIME_KEY = "HeraldUntilGameTime";
	private static final String HIVE_ATTENTION_KEY = "HiveAttention";

	private long lastAmbushGameTime;
	private long heraldUntilGameTime;
	private int hiveAttention;

	public long getLastAmbushGameTime() {
		return lastAmbushGameTime;
	}

	public void setLastAmbushGameTime(long lastAmbushGameTime) {
		this.lastAmbushGameTime = Math.max(0L, lastAmbushGameTime);
	}

	public long getHeraldUntilGameTime() {
		return heraldUntilGameTime;
	}

	public void setHeraldUntilGameTime(long heraldUntilGameTime) {
		this.heraldUntilGameTime = Math.max(0L, heraldUntilGameTime);
	}

	public int getHiveAttention() {
		return hiveAttention;
	}

	public void setHiveAttention(int hiveAttention) {
		this.hiveAttention = Math.max(0, hiveAttention);
	}

	public void addHiveAttention(int amount) {
		setHiveAttention(hiveAttention + Math.max(0, amount));
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putLong(LAST_AMBUSH_GAME_TIME_KEY, lastAmbushGameTime);
		tag.putLong(HERALD_UNTIL_GAME_TIME_KEY, heraldUntilGameTime);
		tag.putInt(HIVE_ATTENTION_KEY, hiveAttention);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		if (nbt == null) return;
		setLastAmbushGameTime(nbt.getLong(LAST_AMBUSH_GAME_TIME_KEY));
		setHeraldUntilGameTime(nbt.getLong(HERALD_UNTIL_GAME_TIME_KEY));
		setHiveAttention(nbt.getInt(HIVE_ATTENTION_KEY));
	}
}
