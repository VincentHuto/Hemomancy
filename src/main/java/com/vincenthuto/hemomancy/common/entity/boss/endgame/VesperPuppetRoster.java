package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vesper's puppets for the whole fight. Every shape starts available; up to three are out at once. A shape the player
 * kills is struck for good. Survivors (still out, recalled, or never called) carry into the Evening Star, each
 * granting him one borrowed attack. The Ringmaster Pattern is not his: Vesper is the conductor here.
 */
public final class VesperPuppetRoster {
	public static final List<String> SHAPES = List.of(
			PuppeteerSummonDefinitions.VEINWING_VULTURE,
			PuppeteerSummonDefinitions.MARROW_SPITTER,
			PuppeteerSummonDefinitions.GOREBOUND_HULK,
			PuppeteerSummonDefinitions.SCARLET_MUMMER,
			PuppeteerSummonDefinitions.SANGUINE_HOUND,
			PuppeteerSummonDefinitions.CINDER_BELLOWS,
			PuppeteerSummonDefinitions.MNEMONIST_PUPPET);
	public static final int MAX_OUT = 3;
	public static final int RECALL_AFTER_TICKS = 400;

	public enum Slot { AVAILABLE, OUT, STRUCK }

	private final Map<String, Slot> slots = new LinkedHashMap<>();
	private final Map<String, Integer> fieldedOrder = new LinkedHashMap<>();
	private int sequence;

	public VesperPuppetRoster() {
		for (String shape : SHAPES) slots.put(shape, Slot.AVAILABLE);
	}

	public Slot slot(String shape) {
		return slots.getOrDefault(shape, Slot.STRUCK);
	}

	public boolean hasFielded(String shape) {
		return fieldedOrder.containsKey(shape);
	}

	public int out() {
		return (int) slots.values().stream().filter(slot -> slot == Slot.OUT).count();
	}

	/**
	 * The next shapes to call, up to the free slots: shapes never fielded first (shuffled for variety), then the
	 * least recently fielded, so every shape gets a turn before any repeats.
	 */
	public List<String> nextMuster(RandomSource random) {
		int free = MAX_OUT - out();
		if (free <= 0) return List.of();
		List<String> unseen = new ArrayList<>();
		List<String> seen = new ArrayList<>();
		for (String shape : SHAPES) {
			if (slot(shape) != Slot.AVAILABLE) continue;
			(hasFielded(shape) ? seen : unseen).add(shape);
		}
		for (int i = unseen.size() - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			String swap = unseen.get(i);
			unseen.set(i, unseen.get(j));
			unseen.set(j, swap);
		}
		seen.sort(Comparator.comparingInt(fieldedOrder::get));
		List<String> muster = new ArrayList<>(unseen);
		muster.addAll(seen);
		return List.copyOf(muster.subList(0, Math.min(free, muster.size())));
	}

	public void markOut(String shape) {
		if (slot(shape) == Slot.STRUCK) return;
		slots.put(shape, Slot.OUT);
		fieldedOrder.put(shape, sequence++);
	}

	public void recall(String shape) {
		if (slot(shape) == Slot.OUT) slots.put(shape, Slot.AVAILABLE);
	}

	public void strike(String shape) {
		if (slots.containsKey(shape)) slots.put(shape, Slot.STRUCK);
	}

	/** Every shape the player has not killed, in roster order. */
	public List<String> survivors() {
		return SHAPES.stream().filter(shape -> slot(shape) != Slot.STRUCK).toList();
	}

	/** Shapes on the field when the encounter resets (owner left, retry) come back; kills stay struck. */
	public void onEncounterReset() {
		slots.replaceAll((shape, slot) -> slot == Slot.OUT ? Slot.AVAILABLE : slot);
	}

	public static boolean mayRecall(int ticksOut) {
		return ticksOut >= RECALL_AFTER_TICKS;
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		for (String shape : SHAPES) {
			CompoundTag entry = new CompoundTag();
			entry.putInt("Slot", slot(shape).ordinal());
			if (hasFielded(shape)) entry.putInt("Fielded", fieldedOrder.get(shape));
			tag.put(shape, entry);
		}
		tag.putInt("Sequence", sequence);
		return tag;
	}

	public static VesperPuppetRoster load(CompoundTag tag) {
		VesperPuppetRoster roster = new VesperPuppetRoster();
		Slot[] values = Slot.values();
		for (String shape : SHAPES) {
			if (!tag.contains(shape)) continue;
			CompoundTag entry = tag.getCompound(shape);
			roster.slots.put(shape, values[Math.max(0, Math.min(values.length - 1, entry.getInt("Slot")))]);
			if (entry.contains("Fielded")) roster.fieldedOrder.put(shape, entry.getInt("Fielded"));
		}
		roster.sequence = Math.max(tag.getInt("Sequence"), roster.fieldedOrder.size());
		return roster;
	}
}
