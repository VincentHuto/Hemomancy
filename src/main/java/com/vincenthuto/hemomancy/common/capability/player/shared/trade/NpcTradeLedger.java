package com.vincenthuto.hemomancy.common.capability.player.shared.trade;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What a player's trading with NPCs leaves behind: rapport per speaker (never shared between NPCs), per offer the
 * restock window of a limited card, the day the player last saw each daily (rotation) card, and the acts they have
 * watched through and not yet traded (a Circus spectacle). A window is counted in in-game days; one older than the
 * offer's restock days reads as empty and is replaced on the next use, so nothing ticks. All of it survives death and
 * the End return.
 */
public class NpcTradeLedger implements INBTSerializable<CompoundTag> {
	public static final int RAPPORT_MAX = 100;
	private static final String TAG_RAPPORT = "Rapport";
	private static final String TAG_USES = "Uses";
	private static final String TAG_START = "Start";
	private static final String TAG_USED = "Used";
	private static final String TAG_ROTATION_SEEN = "RotationSeen";
	private static final String TAG_ACTS = "Acts";
	/** The most unspent acts of one kind a player can bank. */
	public static final int ACTS_MAX = 8;

	/** One offer's restock window: the day it opened and how many trades it has seen. */
	public record Window(long startDay, int used) {
		/** Whether this window still holds on {@code day}, for an offer that restocks every {@code restockDays}. */
		public boolean current(long day, int restockDays) {
			return day >= startDay && day - startDay < Math.max(1, restockDays);
		}
	}

	private final Map<String, Integer> rapport = new LinkedHashMap<>();
	private final Map<ResourceLocation, Window> uses = new LinkedHashMap<>();
	private final Map<ResourceLocation, Long> rotationSeen = new LinkedHashMap<>();
	private final Map<String, Integer> acts = new LinkedHashMap<>();

	public int rapport(String speaker) {
		return rapport.getOrDefault(speaker, 0);
	}

	/** Adds {@code delta} to this speaker's rapport, clamped to 0..{@link #RAPPORT_MAX}; returns the new value. */
	public int addRapport(String speaker, int delta) {
		int value = Math.clamp((long) rapport(speaker) + delta, 0, RAPPORT_MAX);
		if (value == 0) rapport.remove(speaker);
		else rapport.put(speaker, value);
		return value;
	}

	/** Trades made with {@code offer} in its current window on {@code day}; a lapsed window counts as none. */
	public int used(ResourceLocation offer, long day, int restockDays) {
		Window window = uses.get(offer);
		return window != null && window.current(day, restockDays) ? window.used() : 0;
	}

	/** Trades left of {@code uses} in the current window. */
	public int usesLeft(ResourceLocation offer, int uses, long day, int restockDays) {
		return Math.max(0, uses - used(offer, day, restockDays));
	}

	/** Whole days until the window on {@code offer} lapses; 0 when there is no current window. */
	public long daysToRestock(ResourceLocation offer, long day, int restockDays) {
		Window window = uses.get(offer);
		if (window == null || !window.current(day, restockDays)) return 0L;
		return window.startDay() + Math.max(1, restockDays) - day;
	}

	/** Counts one trade with {@code offer}, opening a new window first if the old one has lapsed. */
	public void recordUse(ResourceLocation offer, long day, int restockDays) {
		Window window = uses.get(offer);
		if (window == null || !window.current(day, restockDays)) window = new Window(day, 0);
		uses.put(offer, new Window(window.startDay(), window.used() + 1));
	}

	/** Whether the player has already seen daily card {@code offer} on {@code day}. */
	public boolean rotationSeen(ResourceLocation offer, long day) {
		Long seen = rotationSeen.get(offer);
		return seen != null && seen == day;
	}

	/** Records that the player saw daily card {@code offer} on {@code day}; returns false when they already had. */
	public boolean markRotationSeen(ResourceLocation offer, long day) {
		Long previous = rotationSeen.put(offer, day);
		return previous == null || previous != day;
	}

	/** Unspent acts of this kind the player has watched through. */
	public int acts(String act) {
		return acts.getOrDefault(act, 0);
	}

	/** Banks one watched act, up to {@link #ACTS_MAX}; returns the new count. */
	public int recordAct(String act) {
		int value = Math.min(ACTS_MAX, acts(act) + 1);
		acts.put(act, value);
		return value;
	}

	/** Spends one banked act; false when there was none. */
	public boolean spendAct(String act) {
		int value = acts(act);
		if (value <= 0) return false;
		if (value == 1) acts.remove(act);
		else acts.put(act, value - 1);
		return true;
	}

	public Map<String, Integer> rapportView() {
		return Collections.unmodifiableMap(rapport);
	}

	public Map<ResourceLocation, Window> usesView() {
		return Collections.unmodifiableMap(uses);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		CompoundTag rapportTag = new CompoundTag();
		rapport.forEach(rapportTag::putInt);
		tag.put(TAG_RAPPORT, rapportTag);
		CompoundTag usesTag = new CompoundTag();
		uses.forEach((offer, window) -> {
			CompoundTag windowTag = new CompoundTag();
			windowTag.putLong(TAG_START, window.startDay());
			windowTag.putInt(TAG_USED, window.used());
			usesTag.put(offer.toString(), windowTag);
		});
		tag.put(TAG_USES, usesTag);
		CompoundTag seenTag = new CompoundTag();
		rotationSeen.forEach((offer, day) -> seenTag.putLong(offer.toString(), day));
		tag.put(TAG_ROTATION_SEEN, seenTag);
		CompoundTag actsTag = new CompoundTag();
		acts.forEach(actsTag::putInt);
		tag.put(TAG_ACTS, actsTag);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		rapport.clear();
		uses.clear();
		rotationSeen.clear();
		acts.clear();
		CompoundTag rapportTag = tag.getCompound(TAG_RAPPORT);
		for (String speaker : rapportTag.getAllKeys()) {
			int value = Math.clamp(rapportTag.getInt(speaker), 0, RAPPORT_MAX);
			if (value > 0) rapport.put(speaker, value);
		}
		CompoundTag usesTag = tag.getCompound(TAG_USES);
		for (String key : usesTag.getAllKeys()) {
			ResourceLocation offer = ResourceLocation.tryParse(key);
			if (offer == null) continue;
			CompoundTag windowTag = usesTag.getCompound(key);
			uses.put(offer, new Window(windowTag.getLong(TAG_START), Math.max(0, windowTag.getInt(TAG_USED))));
		}
		CompoundTag seenTag = tag.getCompound(TAG_ROTATION_SEEN);
		for (String key : seenTag.getAllKeys()) {
			ResourceLocation offer = ResourceLocation.tryParse(key);
			if (offer != null) rotationSeen.put(offer, seenTag.getLong(key));
		}
		CompoundTag actsTag = tag.getCompound(TAG_ACTS);
		for (String act : actsTag.getAllKeys()) {
			int value = Math.clamp(actsTag.getInt(act), 0, ACTS_MAX);
			if (value > 0) acts.put(act, value);
		}
	}
}
