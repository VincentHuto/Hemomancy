package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Each challenger's Tithe debt in this fight (design section 4): what they spent near him, owed back. Saved with the
 * Saint and gone with him; never player data. JVM-tested.
 */
public final class HemorathTitheLedger {
	private final Map<UUID, Float> debts = new LinkedHashMap<>();

	public float debt(UUID player) {
		return debts.getOrDefault(player, 0.0F);
	}

	public void accrue(UUID player, float cost, boolean litany) {
		float next = HemorathTitheRules.accrue(debt(player), cost, litany);
		if (next > 0.0F) debts.put(player, next);
	}

	/** One tick of payment; returns the debt it took off. */
	public float pay(UUID player, float paid) {
		float debt = debt(player);
		float taken = HemorathTitheRules.pay(debt, paid);
		set(player, debt - taken);
		return taken;
	}

	/** Collected or cleared: the debt is gone. */
	public void clear(UUID player) {
		debts.remove(player);
	}

	public void clearAll() {
		debts.clear();
	}

	/** The challenger among {@code among} who owes most, or null when nobody owes anything. */
	@Nullable
	public UUID mostIndebted(Collection<UUID> among) {
		UUID best = null;
		float most = 0.0F;
		for (UUID id : among) {
			float debt = debt(id);
			if (debt > most) {
				most = debt;
				best = id;
			}
		}
		return best;
	}

	public float max(Collection<UUID> among) {
		float most = 0.0F;
		for (UUID id : among) most = Math.max(most, debt(id));
		return most;
	}

	public void save(CompoundTag tag) {
		ListTag list = new ListTag();
		debts.forEach((id, debt) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("id", id);
			entry.putFloat("debt", debt);
			list.add(entry);
		});
		tag.put("HemorathTithe", list);
	}

	public void load(CompoundTag tag) {
		debts.clear();
		ListTag list = tag.getList("HemorathTithe", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			if (entry.hasUUID("id")) set(entry.getUUID("id"), entry.getFloat("debt"));
		}
	}

	private void set(UUID player, float debt) {
		if (debt <= 1.0E-4F) debts.remove(player);
		else debts.put(player, debt);
	}
}
