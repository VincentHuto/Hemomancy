package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every surrogate a possession has stilled, and what becomes of it. The surrogate's chunk is usually unloaded when the
 * will comes home, so the verdict (release, or kill after a broken visit) is kept here and carried out the next time
 * the surrogate loads. A held surrogate is never offered to a second caster.
 */
public final class SurrogateLedger extends SavedData {
	private static final String NAME = "hemomancy_possession_surrogates";

	public enum Verdict { HELD, RELEASE, KILL }

	private record Entry(UUID owner, Verdict verdict) {}

	private final Map<UUID, Entry> entries = new HashMap<>();

	public static SurrogateLedger get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(SurrogateLedger::new, SurrogateLedger::load, null), NAME);
	}

	public void hold(UUID body, UUID owner) {
		entries.put(body, new Entry(owner, Verdict.HELD));
		setDirty();
	}

	public boolean isHeld(UUID body) {
		Entry entry = entries.get(body);
		return entry != null && entry.verdict() == Verdict.HELD;
	}

	@Nullable
	public Verdict verdict(UUID body) {
		Entry entry = entries.get(body);
		return entry == null ? null : entry.verdict();
	}

	@Nullable
	public UUID owner(UUID body) {
		Entry entry = entries.get(body);
		return entry == null ? null : entry.owner();
	}

	/** Records how the visit ended; carried out when the surrogate is next seen. */
	public void settle(UUID body, boolean kill) {
		Entry entry = entries.get(body);
		if (entry == null) return;
		entries.put(body, new Entry(entry.owner(), kill ? Verdict.KILL : Verdict.RELEASE));
		setDirty();
	}

	public void forget(UUID body) {
		if (entries.remove(body) != null) setDirty();
	}

	private static SurrogateLedger load(CompoundTag tag, HolderLookup.Provider registries) {
		SurrogateLedger ledger = new SurrogateLedger();
		for (Tag element : tag.getList("Surrogates", Tag.TAG_COMPOUND)) {
			CompoundTag entry = (CompoundTag) element;
			Verdict verdict;
			try {
				verdict = Verdict.valueOf(entry.getString("Verdict"));
			} catch (IllegalArgumentException unknown) {
				verdict = Verdict.RELEASE;
			}
			ledger.entries.put(entry.getUUID("Body"), new Entry(entry.getUUID("Owner"), verdict));
		}
		return ledger;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		entries.forEach((body, entry) -> {
			CompoundTag element = new CompoundTag();
			element.putUUID("Body", body);
			element.putUUID("Owner", entry.owner());
			element.putString("Verdict", entry.verdict().name());
			list.add(element);
		});
		tag.put("Surrogates", list);
		return tag;
	}
}
