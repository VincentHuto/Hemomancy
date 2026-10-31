package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kinbud's Cultivated Mending: a share of each non-lethal hit is queued and regrown evenly over
 * {@link KinbudRules#REGROWTH_TICKS}. Each queued wound is a fixed amount spread across its window, so a burst can
 * still kill: nothing regrows early. A Kindled window adds a cap on the total it may regrow.
 *
 * <p>The queue itself is plain arithmetic for unit tests. Hosts' queues live in a UUID-keyed session map that a relog
 * does not clear, so a wound queued before logging out still regrows if the host returns inside its window; the map
 * empties when the server stops.</p>
 */
public final class MorphlingRegrowthQueue {
	private static final Map<UUID, MorphlingRegrowthQueue> QUEUES = new ConcurrentHashMap<>();

	private final List<Wound> wounds = new ArrayList<>();
	private long kindledUntil = Long.MIN_VALUE;
	private float kindledBudget;

	private static final class Wound {
		final long start;
		final long end;
		final float total;
		float regrown;

		Wound(long start, long end, float total) {
			this.start = start;
			this.end = end;
			this.total = total;
		}
	}

	public static MorphlingRegrowthQueue of(UUID host) {
		return QUEUES.computeIfAbsent(host, id -> new MorphlingRegrowthQueue());
	}

	public static MorphlingRegrowthQueue peek(UUID host) {
		return QUEUES.get(host);
	}

	public static void clearSessionState() {
		QUEUES.clear();
	}

	/** Queues {@code amount} to regrow over the window starting {@code now}. */
	public void queue(float amount, long now, int windowTicks) {
		if (amount <= 0.0F) return;
		wounds.add(new Wound(now, now + Math.max(1, windowTicks), amount));
	}

	/** Opens a Kindled window: until {@code until}, queued regrowth may total at most {@code cap}. */
	public void kindle(long until, float cap) {
		kindledUntil = until;
		kindledBudget = cap;
	}

	public boolean isKindled(long now) {
		return now < kindledUntil;
	}

	public float kindledBudget() {
		return kindledBudget;
	}

	/**
	 * What a hit queues: the strain's share, or all of it inside a Kindled window up to what the cap has left.
	 * Lethal hits queue nothing.
	 */
	public float queueHit(float damage, double share, boolean lethal, long now, int windowTicks) {
		if (lethal || damage <= 0.0F) return 0.0F;
		float amount;
		if (isKindled(now)) {
			amount = Math.min(damage, kindledBudget);
			kindledBudget -= amount;
		} else {
			amount = (float) (damage * share);
		}
		queue(amount, now, windowTicks);
		return amount;
	}

	/** Advances to {@code now} and returns how much health has become due since the last call. */
	public float regrow(long now) {
		float due = 0.0F;
		Iterator<Wound> it = wounds.iterator();
		while (it.hasNext()) {
			Wound wound = it.next();
			float progress = (float) Math.min(1.0D, Math.max(0.0D, (double) (now - wound.start) / (wound.end - wound.start)));
			float target = wound.total * progress;
			due += Math.max(0.0F, target - wound.regrown);
			wound.regrown = Math.max(wound.regrown, target);
			if (now >= wound.end) it.remove();
		}
		return due;
	}

	public float pending() {
		float pending = 0.0F;
		for (Wound wound : wounds) pending += wound.total - wound.regrown;
		return pending;
	}

	public boolean isEmpty() {
		return wounds.isEmpty() && kindledBudget <= 0.0F;
	}

	public void clear() {
		wounds.clear();
		kindledBudget = 0.0F;
		kindledUntil = Long.MIN_VALUE;
	}

	public static void remove(UUID host) {
		QUEUES.remove(host);
	}
}
