package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * The one opening a Saint has open at a time: its age, ticks left, hits counted and extensions, and how long since the
 * last one closed. Changes are reported to the listener (the Saint's {@code onOpeningChanged}). Pure, so it is
 * JVM-tested.
 */
public final class SaintOpenings {
	@Nullable
	private SaintOpening.Spec current;
	private int age;
	private int remaining;
	private int length;
	private int hits;
	private int extended;
	private int sinceClosed;
	private final BiConsumer<SaintOpening.Spec, SaintOpening.Spec> listener;

	public SaintOpenings() {
		this((from, to) -> {
		});
	}

	public SaintOpenings(BiConsumer<SaintOpening.Spec, SaintOpening.Spec> listener) {
		this.listener = listener;
	}

	/** Opens {@code spec} for {@code ticks} (its own length when ticks <= 0); false while another is open. */
	public boolean open(SaintOpening.Spec spec, int ticks) {
		if (current != null) return false;
		start(spec, ticks);
		listener.accept(null, spec);
		return true;
	}

	/** Closes the current opening straight into this one (Velorum's Bow under a wound). */
	public void chainTo(SaintOpening.Spec spec, int ticks) {
		SaintOpening.Spec from = current;
		start(spec, ticks);
		listener.accept(from, spec);
	}

	/** Adds up to {@code ticks}, at most {@code capPerWindow} over the window's life; returns the ticks added. */
	public int extend(int ticks, int capPerWindow) {
		if (current == null || ticks <= 0) return 0;
		int added = Math.max(0, Math.min(ticks, capPerWindow - extended));
		remaining += added;
		length += added;
		extended += added;
		return added;
	}

	public void close() {
		if (current == null) return;
		SaintOpening.Spec from = current;
		current = null;
		age = 0;
		remaining = 0;
		length = 0;
		hits = 0;
		extended = 0;
		sinceClosed = 0;
		listener.accept(from, null);
	}

	public boolean isOpen() {
		return current != null;
	}

	public boolean isOpen(SaintOpening.Spec spec) {
		return current != null && current.id() == spec.id();
	}

	@Nullable
	public SaintOpening.Spec current() {
		return current;
	}

	public int age() {
		return age;
	}

	public int remaining() {
		return remaining;
	}

	/** The opening's whole length, its extensions included. */
	public int length() {
		return length;
	}

	public int hits() {
		return hits;
	}

	/** Ticks since the last opening closed; 0 while one is open. */
	public int ticksSinceClosed() {
		return current != null ? 0 : sinceClosed;
	}

	/** The damage-taken factor: the open spec's, 1 when closed. */
	public float damageTaken() {
		return current == null ? 1.0F : current.damageTaken();
	}

	/** What the open window makes of {@code hit}; a COUNTED hit is counted against the cap. */
	public SaintOpening.Admit admit(SaintHit hit, int challengers) {
		return admit(hit.delivery(), challengers);
	}

	public SaintOpening.Admit admit(SaintOpening.Delivery delivery, int challengers) {
		if (current == null) return SaintOpening.Admit.CLOSED;
		if (!current.counts().contains(delivery)) return SaintOpening.Admit.NOT_COUNTED;
		int cap = current.cap(challengers);
		if (cap > 0 && hits >= cap) return SaintOpening.Admit.CAPPED;
		hits++;
		return SaintOpening.Admit.COUNTED;
	}

	public void tick() {
		if (current == null) {
			if (sinceClosed < Integer.MAX_VALUE) sinceClosed++;
			return;
		}
		age++;
		if (--remaining <= 0) close();
	}

	public void save(CompoundTag tag) {
		CompoundTag openings = new CompoundTag();
		if (current != null) {
			openings.put("spec", current.save());
			openings.putInt("age", age);
			openings.putInt("remaining", remaining);
			openings.putInt("length", length);
			openings.putInt("hits", hits);
			openings.putInt("extended", extended);
		}
		openings.putInt("since_closed", sinceClosed);
		tag.put("SaintOpenings", openings);
	}

	public void load(CompoundTag tag) {
		CompoundTag openings = tag.getCompound("SaintOpenings");
		current = openings.contains("spec") ? SaintOpening.Spec.load(openings.getCompound("spec")) : null;
		age = openings.getInt("age");
		remaining = openings.getInt("remaining");
		length = openings.getInt("length");
		hits = openings.getInt("hits");
		extended = openings.getInt("extended");
		sinceClosed = openings.getInt("since_closed");
	}

	private void start(SaintOpening.Spec spec, int ticks) {
		current = spec;
		age = 0;
		remaining = Math.max(1, ticks > 0 ? ticks : spec.ticks());
		length = remaining;
		hits = 0;
		extended = 0;
		sinceClosed = 0;
	}
}
